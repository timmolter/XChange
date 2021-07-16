package org.knowm.xchange.coinbasepro;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import org.knowm.xchange.coinbasepro.dto.CoinbaseProTransfer;
import org.knowm.xchange.coinbasepro.dto.account.CoinbaseProAccount;
import org.knowm.xchange.coinbasepro.dto.marketdata.*;
import org.knowm.xchange.coinbasepro.dto.trade.CoinbaseProFill;
import org.knowm.xchange.coinbasepro.dto.trade.CoinbaseProOrder;
import org.knowm.xchange.coinbasepro.dto.trade.CoinbaseProOrderFlags;
import org.knowm.xchange.coinbasepro.dto.trade.CoinbaseProPlaceLimitOrder;
import org.knowm.xchange.coinbasepro.dto.trade.CoinbaseProPlaceMarketOrder;
import org.knowm.xchange.coinbasepro.dto.trade.CoinbaseProPlaceOrder;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.Order.OrderStatus;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.FundingRecord;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.marketdata.Trades.TradeSortType;
import org.knowm.xchange.dto.meta.CurrencyMetaData;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.dto.meta.WalletHealth;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.StopOrder;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.dto.trade.UserTrades;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoinbaseProAdapters {

  private static final Logger logger = LoggerFactory.getLogger(CoinbaseProAdapters.class);

  private CoinbaseProAdapters() {}

  protected static Date parseDate(final String rawDate) {

    String modified;
    if (rawDate.length() > 23) {
      modified = rawDate.substring(0, 23);
    } else if (rawDate.endsWith("Z")) {
      switch (rawDate.length()) {
        case 20:
          modified = rawDate.substring(0, 19) + ".000";
          break;
        case 22:
          modified = rawDate.substring(0, 21) + "00";
          break;
        case 23:
          modified = rawDate.substring(0, 22) + "0";
          break;
        default:
          modified = rawDate;
          break;
      }
    } else {
      switch (rawDate.length()) {
        case 19:
          modified = rawDate + ".000";
          break;
        case 21:
          modified = rawDate + "00";
          break;
        case 22:
          modified = rawDate + "0";
          break;
        default:
          modified = rawDate;
          break;
      }
    }
    try {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
      dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      return dateFormat.parse(modified);
    } catch (ParseException e) {
      logger.warn("unable to parse rawDate={} modified={}", rawDate, modified, e);
      return null;
    }
  }

  public static CurrencyPair toCurrencyPair(final String productId) {
    final String[] parts = productId.split("-");
    return new CurrencyPair(parts[0], parts[1]);
  }

  public static Ticker adaptTicker(
      CoinbaseProProductTicker ticker, CoinbaseProProductStats stats, CurrencyPair currencyPair) {
    return new Ticker.Builder()
        .instrument(currencyPair)
        .last(ticker.getPrice())
        .open(stats.getOpen())
        .high(stats.getHigh())
        .low(stats.getLow())
        .bid(ticker.getBid())
        .ask(ticker.getAsk())
        .volume(ticker.getVolume())
        .timestamp(parseDate(ticker.getTime()))
        .build();
  }

  public static List<Ticker> adaptTickers(Map<String, CoinbaseProStats> stats) {
    List<Ticker> tickers = new LinkedList<>();

    for (String pair : stats.keySet()) {
      CoinbaseProStats pairStats = stats.get(pair);
      tickers.add(
          new Ticker.Builder()
              .instrument(new CurrencyPair(pair))
              .last(pairStats.getLast())
              .open(pairStats.getOpen())
              .high(pairStats.getHigh())
              .low(pairStats.getLow())
              .volume(pairStats.getVolume())
              .build());
    }
    return tickers;
  }

  public static OrderBook adaptOrderBook(
      CoinbaseProProductBook book, CurrencyPair currencyPair, Date date) {

    List<LimitOrder> asks = toLimitOrderList(book.getAsks(), OrderType.ASK, currencyPair);
    List<LimitOrder> bids = toLimitOrderList(book.getBids(), OrderType.BID, currencyPair);

    return new OrderBook(date, asks, bids);
  }

  public static OrderBook adaptOrderBook(CoinbaseProProductBook book, CurrencyPair currencyPair) {
    return adaptOrderBook(book, currencyPair, null);
  }

  private static List<LimitOrder> toLimitOrderList(
      CoinbaseProProductBookEntry[] levels, OrderType orderType, CurrencyPair currencyPair) {

    List<LimitOrder> allLevels = new ArrayList<>();

    if (levels != null) {
      for (CoinbaseProProductBookEntry ask : levels) {
        allLevels.add(
            new LimitOrder(orderType, ask.getVolume(), currencyPair, "0", null, ask.getPrice()));
      }
    }

    return allLevels;
  }

  public static Wallet adaptAccountInfo(CoinbaseProAccount[] coinbaseProAccounts) {
    List<Balance> balances = new ArrayList<>(coinbaseProAccounts.length);

    for (CoinbaseProAccount coinbaseProAccount : coinbaseProAccounts) {
      balances.add(
          new Balance(
              Currency.getInstance(coinbaseProAccount.getCurrency()),
              coinbaseProAccount.getBalance(),
              coinbaseProAccount.getAvailable(),
              coinbaseProAccount.getHold()));
    }

    return Wallet.Builder.from(balances).id(coinbaseProAccounts[0].getProfile_id()).build();
  }

  @SuppressWarnings("unchecked")
  public static OpenOrders adaptOpenOrders(CoinbaseProOrder[] coinbaseExOpenOrders) {
    final Map<Boolean, List<Order>> twoTypes =
        Arrays.stream(coinbaseExOpenOrders)
            .map(CoinbaseProAdapters::adaptOrder)
            .collect(Collectors.partitioningBy(t -> t instanceof LimitOrder));
    @SuppressWarnings("rawtypes")
    List limitOrders = twoTypes.get(true);
    return new OpenOrders(limitOrders, twoTypes.get(false));
  }

  public static Order adaptOrder(CoinbaseProOrder order) {
    OrderType type = "buy".equals(order.getSide()) ? OrderType.BID : OrderType.ASK;
    CurrencyPair currencyPair = new CurrencyPair(order.getProductId().replace('-', '/'));
    Order.Builder builder = null;
    if (order.getType() == null) {
      return null;
    }
    switch (order.getType()) {
      case "market":
        builder = new MarketOrder.Builder(type, currencyPair);
        break;
      case "limit":
        if (order.getStop() == null) {
          builder = new LimitOrder.Builder(type, currencyPair).limitPrice(order.getPrice());
        } else {
          builder = new StopOrder.Builder(type, currencyPair).stopPrice(order.getStopPrice());
        }
        break;
    }
    if (builder == null) {
      return null;
    }
    builder
        .orderStatus(adaptOrderStatus(order))
        .originalAmount(order.getSize())
        .id(order.getId())
        .timestamp(parseDate(order.getCreatedAt()))
        .cumulativeAmount(order.getFilledSize())
        .fee(order.getFillFees());
    BigDecimal averagePrice;
    if (order.getFilledSize().signum() != 0 && order.getExecutedvalue().signum() != 0) {
      averagePrice = order.getExecutedvalue().divide(order.getFilledSize(), MathContext.DECIMAL32);
    } else {
      averagePrice = BigDecimal.ZERO;
    }
    return builder.averagePrice(averagePrice).build();
  }

  public static OrderStatus[] adaptOrderStatuses(CoinbaseProOrder[] orders) {

    OrderStatus[] orderStatuses = new OrderStatus[orders.length];

    int i = 0;
    for (CoinbaseProOrder coinbaseProOrder : orders) {
      orderStatuses[i++] = adaptOrderStatus(coinbaseProOrder);
    }

    return orderStatuses;
  }

  /** The status from the CoinbaseProOrder object converted to xchange status */
  public static OrderStatus adaptOrderStatus(CoinbaseProOrder order) {
    if (order.getStatus() == null) {
      return OrderStatus.UNKNOWN;
    }

    switch (order.getStatus()) {
      case "pending":
        return OrderStatus.PENDING_NEW;
      case "done":
      case "settled":
        if (order.getDoneReason() == null) {
          return OrderStatus.UNKNOWN;
        }
        switch (order.getDoneReason()) {
          case "filled":
            return OrderStatus.FILLED;
          case "canceled":
            return OrderStatus.CANCELED;
        }
        return OrderStatus.UNKNOWN;
    }

    if (order.getFilledSize().signum() == 0) {

      if ("open".equals(order.getStatus()) && order.getStop() != null) {
        // This is a massive edge case of a stop triggering but not immediately
        // fulfilling.  STOPPED status is only currently used by the HitBTC and
        // YoBit implementations and in both cases it looks like a
        // misunderstanding and those should return CANCELLED.  Should we just
        // remove this status?
        return OrderStatus.STOPPED;
      }
      return OrderStatus.NEW;
    }

    if (order.getFilledSize().compareTo(BigDecimal.ZERO) > 0
        // if size >= filledSize order should be partially filled
        && order.getSize().compareTo(order.getFilledSize()) >= 0)
      return OrderStatus.PARTIALLY_FILLED;

    return OrderStatus.UNKNOWN;
  }

  public static Trades adaptTrades(
      List<CoinbaseProTrade> coinbaseProTradesList, CurrencyPair currencyPair) {
    CoinbaseProTrade[] tradeArray = new CoinbaseProTrade[coinbaseProTradesList.size()];
    coinbaseProTradesList.toArray(tradeArray);
    return CoinbaseProAdapters.adaptTrades(tradeArray, currencyPair);
  }

  public static UserTrades adaptTradeHistory(List<CoinbaseProFill> coinbaseExFills) {

    List<UserTrade> trades = new ArrayList<>(coinbaseExFills.size());

    for (CoinbaseProFill fill : coinbaseExFills) {
      CurrencyPair currencyPair = new CurrencyPair(fill.getProductId().replace('-', '/'));

      trades.add(
          new UserTrade.Builder()
              .type("buy".equals(fill.getSide()) ? OrderType.BID : OrderType.ASK)
              .originalAmount(fill.getSize())
              .currencyPair(currencyPair)
              .price(fill.getPrice())
              .timestamp(parseDate(fill.getCreatedAt()))
              .id(String.valueOf(fill.getTradeId()))
              .orderId(fill.getOrderId())
              .feeAmount(fill.getFee())
              .feeCurrency(currencyPair.counter)
              .build());
    }

    return new UserTrades(trades, TradeSortType.SortByID);
  }

  public static Trades adaptTrades(CoinbaseProTrade[] coinbaseExTrades, CurrencyPair currencyPair) {

    List<Trade> trades = new ArrayList<>(coinbaseExTrades.length);
    for (CoinbaseProTrade trade : coinbaseExTrades) {
      // yes, sell means buy for coinbasePro reported trades..
      OrderType type = "sell".equals(trade.getSide()) ? OrderType.BID : OrderType.ASK;
      trades.add(
          new Trade.Builder()
              .type(type)
              .originalAmount(trade.getSize())
              .price(trade.getPrice())
              .instrument(currencyPair)
              .timestamp(parseDate(trade.getTimestamp()))
              .id(String.valueOf(trade.getTradeId()))
              .makerOrderId(trade.getMakerOrderId())
              .takerOrderId(trade.getTakerOrderId())
              .build());
    }

    return new Trades(trades, coinbaseExTrades[0].getTradeId(), TradeSortType.SortByID);
  }

  public static CurrencyPair adaptCurrencyPair(CoinbaseProProduct product) {
    return new CurrencyPair(product.getBaseCurrency(), product.getTargetCurrency());
  }

  private static Currency adaptCurrency(CoinbaseProCurrency currency) {
    return new Currency(currency.getId());
  }

  private static int numberOfDecimals(BigDecimal value) {
    double d = value.doubleValue();
    return -(int) Math.round(Math.log10(d));
  }

  public static ExchangeMetaData adaptToExchangeMetaData(
      ExchangeMetaData exchangeMetaData,
      CoinbaseProProduct[] products,
      CoinbaseProCurrency[] cbCurrencies) {

    Map<CurrencyPair, CurrencyPairMetaData> currencyPairs =
        exchangeMetaData == null ? new HashMap<>() : exchangeMetaData.getCurrencyPairs();

    Map<Currency, CurrencyMetaData> currencies =
        exchangeMetaData == null ? new HashMap<>() : exchangeMetaData.getCurrencies();

    for (CoinbaseProProduct product : products) {
      if (!"online".equals(product.getStatus())) {
        continue;
      }
      CurrencyPair pair = adaptCurrencyPair(product);

      CurrencyPairMetaData staticMetaData = currencyPairs.get(pair);
      int baseScale = numberOfDecimals(product.getBaseIncrement());
      int priceScale = numberOfDecimals(product.getQuoteIncrement());
      boolean marketOrderAllowed = !product.isLimitOnly();

      currencyPairs.put(
          pair,
          new CurrencyPairMetaData(
              new BigDecimal("0.50"), // Trading fee at Coinbase is 0.5 %
              product.getBaseMinSize(),
              product.getBaseMaxSize(),
              product.getMinMarketFunds(),
              product.getMaxMarketFunds(),
              baseScale,
              priceScale,
              null,
              staticMetaData != null ? staticMetaData.getFeeTiers() : null,
              null,
              pair.counter,
              marketOrderAllowed));
    }

    Arrays.stream(cbCurrencies)
        .forEach(
            currency ->
                currencies.put(
                    adaptCurrency(currency),
                    new CurrencyMetaData(
                        numberOfDecimals(currency.getMaxPrecision()),
                        BigDecimal.ZERO,
                        currency.getDetails().getMinWithdrawalAmount(),
                        "online".equals(currency.getStatus())
                            ? WalletHealth.ONLINE
                            : WalletHealth.OFFLINE)));

    return new ExchangeMetaData(
        currencyPairs,
        currencies,
        exchangeMetaData == null ? null : exchangeMetaData.getPublicRateLimits(),
        exchangeMetaData == null ? null : exchangeMetaData.getPrivateRateLimits(),
        true);
  }

  public static String adaptProductID(CurrencyPair currencyPair) {
    return currencyPair == null ? null : currencyPair.base.getCurrencyCode() + "-" + currencyPair.counter.getCurrencyCode();
  }

  public static CoinbaseProPlaceOrder.Side adaptSide(OrderType orderType) {
    return orderType == OrderType.ASK
        ? CoinbaseProPlaceOrder.Side.sell
        : CoinbaseProPlaceOrder.Side.buy;
  }

  public static CoinbaseProPlaceOrder.Stop adaptStop(OrderType orderType) {
    return orderType == OrderType.ASK
        ? CoinbaseProPlaceOrder.Stop.loss
        : CoinbaseProPlaceOrder.Stop.entry;
  }

  public static CoinbaseProPlaceLimitOrder adaptCoinbaseProPlaceLimitOrder(LimitOrder limitOrder) {
    CoinbaseProPlaceLimitOrder.Builder builder =
        new CoinbaseProPlaceLimitOrder.Builder()
            .clientOid(limitOrder.getUserReference())
            .price(limitOrder.getLimitPrice())
            .type(CoinbaseProPlaceOrder.Type.limit)
            .productId(adaptProductID(limitOrder.getCurrencyPair()))
            .side(adaptSide(limitOrder.getType()))
            .size(limitOrder.getOriginalAmount());

    if (limitOrder.getOrderFlags().contains(CoinbaseProOrderFlags.POST_ONLY))
      builder.postOnly(true);
    if (limitOrder.getOrderFlags().contains(CoinbaseProOrderFlags.FILL_OR_KILL))
      builder.timeInForce(CoinbaseProPlaceLimitOrder.TimeInForce.FOK);
    if (limitOrder.getOrderFlags().contains(CoinbaseProOrderFlags.IMMEDIATE_OR_CANCEL))
      builder.timeInForce(CoinbaseProPlaceLimitOrder.TimeInForce.IOC);

    return builder.build();
  }

  public static CoinbaseProPlaceMarketOrder adaptCoinbaseProPlaceMarketOrder(
      MarketOrder marketOrder) {
    return new CoinbaseProPlaceMarketOrder.Builder()
        .productId(adaptProductID(marketOrder.getCurrencyPair()))
        .clientOid(marketOrder.getUserReference())
        .type(CoinbaseProPlaceOrder.Type.market)
        .side(adaptSide(marketOrder.getType()))
        .size(marketOrder.getOriginalAmount())
        .build();
  }

  /**
   * Creates a 'stop' order. Stop limit order converts to a limit order when the stop amount is
   * triggered. The limit order can have a different price than the stop price.
   *
   * <p>If the stop order has no limit price it will execute as a market order once the stop price
   * is broken
   *
   * @param stopOrder
   * @return
   */
  public static CoinbaseProPlaceOrder adaptCoinbaseProStopOrder(StopOrder stopOrder) {
    // stop orders can also execute as 'stop limit' orders, that is converting to
    // a limit order, but a traditional 'stop' order converts to a market order
    if (stopOrder.getLimitPrice() == null) {
      return new CoinbaseProPlaceMarketOrder.Builder()
          .productId(adaptProductID(stopOrder.getCurrencyPair()))
          .clientOid(stopOrder.getUserReference())
          .type(CoinbaseProPlaceOrder.Type.market)
          .side(adaptSide(stopOrder.getType()))
          .size(stopOrder.getOriginalAmount())
          .stop(adaptStop(stopOrder.getType()))
          .stopPrice(stopOrder.getStopPrice())
          .build();
    }
    return new CoinbaseProPlaceLimitOrder.Builder()
        .productId(adaptProductID(stopOrder.getCurrencyPair()))
        .clientOid(stopOrder.getUserReference())
        .type(CoinbaseProPlaceOrder.Type.limit)
        .side(adaptSide(stopOrder.getType()))
        .size(stopOrder.getOriginalAmount())
        .stop(adaptStop(stopOrder.getType()))
        .stopPrice(stopOrder.getStopPrice())
        .price(stopOrder.getLimitPrice())
        .build();
  }

  public static FundingRecord adaptFundingRecord(
      Currency currency, CoinbaseProTransfer coinbaseProTransfer) {
    FundingRecord.Status status = FundingRecord.Status.PROCESSING;

    Date processedAt = coinbaseProTransfer.processedAt();
    Date canceledAt = coinbaseProTransfer.canceledAt();

    if (canceledAt != null) status = FundingRecord.Status.CANCELLED;
    else if (processedAt != null) status = FundingRecord.Status.COMPLETE;

    String address = coinbaseProTransfer.getDetails().getCryptoAddress();
    if (address == null) address = coinbaseProTransfer.getDetails().getSentToAddress();

    String cryptoTransactionHash = coinbaseProTransfer.getDetails().getCryptoTransactionHash();
    String transactionHash = adaptTransactionHash(currency.getSymbol(), cryptoTransactionHash);

    return new FundingRecord(
        address,
        coinbaseProTransfer.getDetails().getDestinationTag(),
        coinbaseProTransfer.createdAt(),
        currency,
        coinbaseProTransfer.amount(),
        coinbaseProTransfer.getId(),
        transactionHash,
        coinbaseProTransfer.type(),
        status,
        null,
        null,
        null);
  }

  // crypto_transaction_link: "https://etherscan.io/tx/0x{{txId}}"
  private static String adaptTransactionHash(String currency, String transactionHash) {
    switch (currency) {
      case "ZRX":
      case "BAT":
      case "LOOM":
      case "CVC":
      case "DNT":
      case "MANA":
      case "GNT":
      case "REP":
      case "LINK":
      case "ETH":
      case "ETC":
      case "USDC":
      case "DAI":
      case "ZIL":
      case "MKR":
        transactionHash = transactionHash != null ? "0x" + transactionHash : null;
        break;
    }
    return transactionHash;
  }
}
