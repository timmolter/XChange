package org.knowm.xchange.simulated;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.knowm.xchange.dto.Order.OrderType.ASK;
import static org.knowm.xchange.dto.Order.OrderType.BID;

import com.google.common.collect.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.exceptions.ExchangeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The "exchange" which backs {@link SimulatedExchange}.
 *
 * @author Graham Crockford
 */
final class MatchingEngine {

  private static final Logger LOGGER = LoggerFactory.getLogger(MatchingEngine.class);
  private static final BigDecimal FEE_RATE = new BigDecimal("0.001");
  private static final int TRADE_HISTORY_SIZE = 50;

  private final AccountFactory accountFactory;
  private final CurrencyPair currencyPair;
  private final int priceScale;
  private final BigDecimal minimumAmount;
  private final Consumer<Fill> onFill;

  private final List<BookLevel> asks = new LinkedList<>();
  private final List<BookLevel> bids = new LinkedList<>();
  private final Deque<Trade> publicTrades = new ConcurrentLinkedDeque<>();
  private final Multimap<String, UserTrade> userTrades = LinkedListMultimap.create();

  private volatile Ticker ticker = new Ticker.Builder().build();

  MatchingEngine(
      AccountFactory accountFactory,
      CurrencyPair currencyPair,
      int priceScale,
      BigDecimal minimumAmount) {
    this(accountFactory, currencyPair, priceScale, minimumAmount, f -> {});
  }

  MatchingEngine(
      AccountFactory accountFactory,
      CurrencyPair currencyPair,
      int priceScale,
      BigDecimal minimumAmount,
      Consumer<Fill> onFill) {
    this.accountFactory = accountFactory;
    this.currencyPair = currencyPair;
    this.priceScale = priceScale;
    this.minimumAmount = minimumAmount;
    this.onFill = onFill;
  }

  public synchronized LimitOrder postOrder(String apiKey, Order original) {
    LOGGER.debug("User {} posting order: {}", apiKey, original);
    validate(original);
    Account account = accountFactory.get(apiKey);
    checkBalance(original, account);
    BookOrder takerOrder = BookOrder.fromOrder(original, apiKey);
    switch (takerOrder.getType()) {
      case ASK:
        LOGGER.debug("Matching against bids");
        chewBook(bids, takerOrder);
        if (!takerOrder.isDone()) {
          if (original instanceof MarketOrder) {
            throw new ExchangeException("Cannot fulfil order. No buyers.");
          }
          insertIntoBook(asks, takerOrder, ASK, account);
        }
        break;
      case BID:
        LOGGER.debug("Matching against asks");
        chewBook(asks, takerOrder);
        if (!takerOrder.isDone()) {
          if (original instanceof MarketOrder) {
            throw new ExchangeException("Cannot fulfil order. No sellers.");
          }
          insertIntoBook(bids, takerOrder, BID, account);
        }
        break;
      default:
        throw new ExchangeException("Unsupported order type: " + takerOrder.getType());
    }
    return takerOrder.toOrder(currencyPair);
  }

  private void validate(Order order) {
    if (order.getOriginalAmount().compareTo(minimumAmount) < 0) {
      throw new ExchangeException(
          "Trade amount is " + order.getOriginalAmount() + ", minimum is " + minimumAmount);
    }
    if (order instanceof LimitOrder) {
      LimitOrder limitOrder = (LimitOrder) order;
      if (limitOrder.getLimitPrice() == null) {
        throw new ExchangeException("No price");
      }
      if (limitOrder.getLimitPrice().compareTo(ZERO) <= 0) {
        throw new ExchangeException(
            "Limit price is " + limitOrder.getLimitPrice() + ", must be positive");
      }
      int scale = limitOrder.getLimitPrice().stripTrailingZeros().scale();
      if (scale > priceScale) {
        throw new ExchangeException("Price scale is " + scale + ", maximum is " + priceScale);
      }
    }
  }

  private void checkBalance(Order order, Account account) {
    if (order instanceof LimitOrder) {
      account.checkBalance((LimitOrder) order);
    } else {
      BigDecimal marketCostOrProceeds =
          marketCostOrProceeds(order.getType(), order.getOriginalAmount());
      BigDecimal marketAmount =
          order.getType().equals(OrderType.BID) ? marketCostOrProceeds : order.getOriginalAmount();
      account.checkBalance(order, marketAmount);
    }
  }

  private void insertIntoBook(
      List<BookLevel> book, BookOrder order, OrderType type, Account account) {

    int i = 0;
    boolean insert = false;

    Iterator<BookLevel> iter = book.iterator();
    while (iter.hasNext()) {
      BookLevel level = iter.next();
      int signum = level.getPrice().compareTo(order.getLimitPrice());
      if (signum == 0) {
        level.getOrders().add(order);
        return;
      } else if (signum < 0 && type == BID || signum > 0 && type == ASK) {
        insert = true;
        break;
      }
      i++;
    }

    account.reserve(order.toOrder(currencyPair));

    BookLevel newLevel = new BookLevel(order.getLimitPrice());
    newLevel.getOrders().add(order);
    if (insert) {
      book.add(i, newLevel);
    } else {
      book.add(newLevel);
    }

    ticker = newTickerFromBook().last(ticker.getLast()).build();
  }

  private Ticker.Builder newTickerFromBook() {
    return new Ticker.Builder()
        .instrument(currencyPair)
        .ask(asks.isEmpty() ? null : asks.get(0).getPrice())
        .bid(bids.isEmpty() ? null : bids.get(0).getPrice());
  }

  /**
   * Calculates the total cost or proceeds at market price of the specified bid/ask amount.
   *
   * @param orderType Ask or bid.
   * @param amount The amount.
   * @return The market cost/proceeds
   * @throws ExchangeException If there is insufficient liquidity.
   */
  public BigDecimal marketCostOrProceeds(OrderType orderType, BigDecimal amount) {
    BigDecimal remaining = amount;
    BigDecimal cost = ZERO;
    List<BookLevel> orderbookSide = orderType.equals(BID) ? asks : bids;
    for (BookOrder order :
        FluentIterable.from(orderbookSide).transformAndConcat(BookLevel::getOrders)) {
      BigDecimal available = order.getRemainingAmount();
      BigDecimal tradeAmount = remaining.compareTo(available) >= 0 ? available : remaining;
      BigDecimal tradeCost = tradeAmount.multiply(order.getLimitPrice());
      cost = cost.add(tradeCost);
      remaining = remaining.subtract(tradeAmount);
      if (remaining.compareTo(ZERO) == 0) return cost;
    }
    throw new ExchangeException("Insufficient liquidity in book");
  }

  public synchronized Level3OrderBook book() {
    return new Level3OrderBook(
        FluentIterable.from(asks)
            .transformAndConcat(BookLevel::getOrders)
            .transform(o -> o.toOrder(currencyPair))
            .toList(),
        FluentIterable.from(bids)
            .transformAndConcat(BookLevel::getOrders)
            .transform(o -> o.toOrder(currencyPair))
            .toList());
  }

  public Ticker ticker() {
    return ticker;
  }

  public List<Trade> publicTrades() {
    return FluentIterable.from(publicTrades).transform(t -> Trade.Builder.from(t).build()).toList();
  }

  public synchronized List<UserTrade> tradeHistory(String apiKey) {
    return ImmutableList.copyOf(userTrades.get(apiKey));
  }

  private void chewBook(Iterable<BookLevel> makerOrders, BookOrder takerOrder) {
    Iterator<BookLevel> levelIter = makerOrders.iterator();
    while (levelIter.hasNext()) {
      BookLevel level = levelIter.next();
      Iterator<BookOrder> orderIter = level.getOrders().iterator();
      while (orderIter.hasNext() && !takerOrder.isDone()) {
        BookOrder makerOrder = orderIter.next();

        LOGGER.debug("Matching against maker order {}", makerOrder);
        if (!makerOrder.matches(takerOrder)) {
          LOGGER.debug("Ran out of maker orders at this price");
          return;
        }

        BigDecimal tradeAmount =
            takerOrder.getRemainingAmount().compareTo(makerOrder.getRemainingAmount()) > 0
                ? makerOrder.getRemainingAmount()
                : takerOrder.getRemainingAmount();

        LOGGER.debug("Matches for {}", tradeAmount);
        matchOff(takerOrder, makerOrder, tradeAmount);

        if (makerOrder.isDone()) {
          LOGGER.debug("Maker order removed from book");
          orderIter.remove();
          if (level.getOrders().isEmpty()) {
            levelIter.remove();
          }
        }
      }
    }
  }

  private void matchOff(BookOrder takerOrder, BookOrder makerOrder, BigDecimal tradeAmount) {
    Date timestamp = new Date();

    UserTrade takerTrade =
        new UserTrade.Builder()
            .currencyPair(currencyPair)
            .id(randomUUID().toString())
            .originalAmount(tradeAmount)
            .price(makerOrder.getLimitPrice())
            .timestamp(timestamp)
            .type(takerOrder.getType())
            .orderId(takerOrder.getId())
            .feeAmount(
                takerOrder.getType() == ASK
                    ? tradeAmount.multiply(makerOrder.getLimitPrice()).multiply(FEE_RATE)
                    : tradeAmount.multiply(FEE_RATE))
            .feeCurrency(takerOrder.getType() == ASK ? currencyPair.counter : currencyPair.base)
            .build();

    LOGGER.debug("Created taker trade: {}", takerTrade);

    accumulate(takerOrder, takerTrade);

    OrderType makerType = takerOrder.getType() == OrderType.ASK ? OrderType.BID : OrderType.ASK;
    UserTrade makerTrade =
        new UserTrade.Builder()
            .currencyPair(currencyPair)
            .id(randomUUID().toString())
            .originalAmount(tradeAmount)
            .price(makerOrder.getLimitPrice())
            .timestamp(timestamp)
            .type(makerType)
            .orderId(makerOrder.getId())
            .feeAmount(
                makerType == ASK
                    ? tradeAmount.multiply(makerOrder.getLimitPrice()).multiply(FEE_RATE)
                    : tradeAmount.multiply(FEE_RATE))
            .feeCurrency(makerType == ASK ? currencyPair.counter : currencyPair.base)
            .build();

    LOGGER.debug("Created maker trade: {}", makerOrder);
    accumulate(makerOrder, makerTrade);

    recordFill(new Fill(takerOrder.getApiKey(), takerTrade, true));
    recordFill(new Fill(makerOrder.getApiKey(), makerTrade, false));

    ticker = newTickerFromBook().last(makerOrder.getLimitPrice()).build();
  }

  private void accumulate(BookOrder bookOrder, UserTrade trade) {
    BigDecimal amount = trade.getOriginalAmount();
    BigDecimal price = trade.getPrice();
    BigDecimal newTotal = bookOrder.getCumulativeAmount().add(amount);

    if (bookOrder.getCumulativeAmount().compareTo(ZERO) == 0) {
      bookOrder.setAveragePrice(price);
    } else {
      bookOrder.setAveragePrice(
          bookOrder
              .getAveragePrice()
              .multiply(bookOrder.getCumulativeAmount())
              .add(price.multiply(amount))
              .divide(newTotal, priceScale, HALF_UP));
    }

    bookOrder.setCumulativeAmount(newTotal);
    bookOrder.setFee(bookOrder.getFee().add(trade.getFeeAmount()));
  }

  public synchronized List<LimitOrder> openOrders(String apiKey) {
    return Stream.concat(asks.stream(), bids.stream())
        .flatMap(v -> v.getOrders().stream())
        .filter(o -> o.getApiKey().equals(apiKey))
        .sorted(Ordering.natural().onResultOf(BookOrder::getTimestamp).reversed())
        .map(o -> o.toOrder(currencyPair))
        .collect(toList());
  }

  public synchronized OrderBook getLevel2OrderBook() {
    return new OrderBook(new Date(), accumulateBookSide(ASK, asks), accumulateBookSide(BID, bids));
  }

  private List<LimitOrder> accumulateBookSide(OrderType orderType, List<BookLevel> book) {
    BigDecimal price = null;
    BigDecimal amount = ZERO;
    List<LimitOrder> result = new ArrayList<>();
    Iterator<BookOrder> iter = book.stream().flatMap(v -> v.getOrders().stream()).iterator();
    while (iter.hasNext()) {
      BookOrder bookOrder = iter.next();
      if (price != null && bookOrder.getLimitPrice().compareTo(price) != 0) {
        result.add(
            new LimitOrder.Builder(orderType, currencyPair)
                .originalAmount(amount)
                .limitPrice(price)
                .build());
        amount = ZERO;
      }
      amount = amount.add(bookOrder.getRemainingAmount());
      price = bookOrder.getLimitPrice();
    }
    if (price != null) {
      result.add(
          new LimitOrder.Builder(orderType, currencyPair)
              .originalAmount(amount)
              .limitPrice(price)
              .build());
    }
    return result;
  }

  private void recordFill(Fill fill) {
    // XChange is unusual in this respect (see https://github.com/knowm/XChange/issues/2468)
    if (!fill.isTaker()) {
      publicTrades.push(fill.getTrade());
      if (publicTrades.size() > TRADE_HISTORY_SIZE) {
        publicTrades.removeLast();
      }
    }
    userTrades.put(fill.getApiKey(), fill.getTrade());
    accountFactory.get(fill.getApiKey()).fill(fill.getTrade(), !fill.isTaker());
    onFill.accept(fill);
  }

  public void cancelOrder(String apiKey, String orderId) {
    cancelOrder(apiKey, orderId, BID);
    cancelOrder(apiKey, orderId, ASK);
  }

  public void cancelOrder(String apiKey, String orderId, OrderType type) {
    Account account = accountFactory.get(apiKey);
    Stream<BookLevel> bookLevelStream;
    switch (type) {
      case ASK:
        bookLevelStream = asks.stream();
        break;
      case BID:
        bookLevelStream = bids.stream();
        break;
      default:
        throw new ExchangeException("Unsupported order type: " + type);
    }
    bookLevelStream.forEach(
        bookLevel ->
            bookLevel
                .getOrders()
                .removeIf(
                    bookOrder -> {
                      final boolean remove = bookOrder.getId().equals(orderId);
                      if (remove) {
                        account.release(bookOrder.toOrder(currencyPair));
                      }
                      return remove;
                    }));
  }
}
