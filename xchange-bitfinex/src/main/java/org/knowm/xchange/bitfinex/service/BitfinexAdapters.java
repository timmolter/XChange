package org.knowm.xchange.bitfinex.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.knowm.xchange.bitfinex.v1.BitfinexOrderType;
import org.knowm.xchange.bitfinex.v1.BitfinexUtils;
import org.knowm.xchange.bitfinex.v1.dto.account.BitfinexAccountFeesResponse;
import org.knowm.xchange.bitfinex.v1.dto.account.BitfinexBalancesResponse;
import org.knowm.xchange.bitfinex.v1.dto.account.BitfinexDepositWithdrawalHistoryResponse;
import org.knowm.xchange.bitfinex.v1.dto.account.BitfinexTradingFeeResponse;
import org.knowm.xchange.bitfinex.v1.dto.marketdata.*;
import org.knowm.xchange.bitfinex.v1.dto.trade.BitfinexAccountInfosResponse;
import org.knowm.xchange.bitfinex.v1.dto.trade.BitfinexOrderFlags;
import org.knowm.xchange.bitfinex.v1.dto.trade.BitfinexOrderStatusResponse;
import org.knowm.xchange.bitfinex.v1.dto.trade.BitfinexTradeResponse;
import org.knowm.xchange.bitfinex.v2.dto.account.Movement;
import org.knowm.xchange.bitfinex.v2.dto.marketdata.BitfinexPublicTrade;
import org.knowm.xchange.bitfinex.v2.dto.marketdata.BitfinexTickerFundingCurrency;
import org.knowm.xchange.bitfinex.v2.dto.marketdata.BitfinexTickerTraidingPair;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.Order.OrderStatus;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.Fee;
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
import org.knowm.xchange.dto.trade.*;
import org.knowm.xchange.utils.DateUtils;
import org.knowm.xchange.utils.jackson.CurrencyPairDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BitfinexAdapters {

  public static final Logger log = LoggerFactory.getLogger(BitfinexAdapters.class);
  private static final ObjectMapper mapper = new ObjectMapper();

  private static final AtomicBoolean warnedStopLimit = new AtomicBoolean();
  private static final String USDT_SYMBOL_BITFINEX = "UST";
  private static final String USDT_SYMBOL_XCHANGE = "USDT";
  private static final int PLATFORM_STATUS_ONLINE = 1;
  private static final int PLATFORM_STATUS_OFFLINE = 0;

  private BitfinexAdapters() {}

  /**
   * Each element in the response array contains a set of currencies that are at a given fee tier.
   * The API returns the fee per currency in each tier and does not make any promises that they are
   * all the same, so this adapter will use the fee per currency instead of the fee per tier.
   */
  public static Map<CurrencyPair, Fee> adaptDynamicTradingFees(
      BitfinexTradingFeeResponse[] responses, List<CurrencyPair> currencyPairs) {
    Map<CurrencyPair, Fee> result = new HashMap<>();
    for (BitfinexTradingFeeResponse response : responses) {
      BitfinexTradingFeeResponse.BitfinexTradingFeeResponseRow[] responseRows =
          response.getTradingFees();
      for (BitfinexTradingFeeResponse.BitfinexTradingFeeResponseRow responseRow : responseRows) {
        Currency currency = Currency.getInstance(responseRow.getCurrency());
        BigDecimal percentToFraction = BigDecimal.ONE.divide(BigDecimal.ONE.scaleByPowerOfTen(2));
        Fee fee =
            new Fee(
                responseRow.getMakerFee().multiply(percentToFraction),
                responseRow.getTakerFee().multiply(percentToFraction));
        for (CurrencyPair pair : currencyPairs) {
          // Fee to trade for a currency is the fee to trade currency pairs with this base.
          // Fee is typically assessed in units counter.
          if (pair.base.equals(currency)) {
            if (result.put(pair, fee) != null) {
              throw new IllegalStateException(
                  "Fee for currency pair " + pair + " is overspecified");
            }
          }
        }
      }
    }
    return result;
  }

  public static String adaptBitfinexCurrency(String bitfinexSymbol) {
    String result = bitfinexSymbol.toUpperCase();
    if (USDT_SYMBOL_BITFINEX.equals(result)) {
      result = USDT_SYMBOL_XCHANGE;
    }
    return result;
  }

  public static String adaptOrderType(OrderType type) {
    switch (type) {
      case BID:
      case EXIT_BID:
        return "buy";
      case ASK:
      case EXIT_ASK:
        return "sell";
    }

    throw new IllegalArgumentException(String.format("Unexpected type of order: %s", type));
  }

  public static BitfinexOrderType adaptOrderFlagsToType(Set<Order.IOrderFlags> flags) {
    if (flags.contains(BitfinexOrderFlags.MARGIN)) {
      if (flags.contains(BitfinexOrderFlags.FILL_OR_KILL)) {
        return BitfinexOrderType.MARGIN_FILL_OR_KILL;
      } else if (flags.contains(BitfinexOrderFlags.TRAILING_STOP)) {
        return BitfinexOrderType.MARGIN_TRAILING_STOP;
      } else if (flags.contains(BitfinexOrderFlags.STOP)) {
        return BitfinexOrderType.MARGIN_STOP;
      } else {
        return BitfinexOrderType.MARGIN_LIMIT;
      }
    } else {
      if (flags.contains(BitfinexOrderFlags.FILL_OR_KILL)) {
        return BitfinexOrderType.FILL_OR_KILL;
      } else if (flags.contains(BitfinexOrderFlags.TRAILING_STOP)) {
        return BitfinexOrderType.TRAILING_STOP;
      } else if (flags.contains(BitfinexOrderFlags.STOP)) {
        return BitfinexOrderType.STOP;
      } else {
        return BitfinexOrderType.LIMIT;
      }
    }
  }

  public static CurrencyPair adaptCurrencyPair(String bitfinexSymbol) {
    String tradableIdentifier;
    String transactionCurrency;
    int startIndex =
        bitfinexSymbol.startsWith("t") && Character.isUpperCase(bitfinexSymbol.charAt(1)) ? 1 : 0;
    if (bitfinexSymbol.contains(":")) {
      // ie 'dusk:usd' or 'btc:cnht'
      int idx = bitfinexSymbol.indexOf(":");
      tradableIdentifier = bitfinexSymbol.substring(startIndex, idx);
      transactionCurrency = bitfinexSymbol.substring(idx + 1);
    } else {
      tradableIdentifier = bitfinexSymbol.substring(startIndex, startIndex + 3);
      transactionCurrency = bitfinexSymbol.substring(startIndex + 3);
    }

    return new CurrencyPair(
        adaptBitfinexCurrency(tradableIdentifier), adaptBitfinexCurrency(transactionCurrency));
  }

  public static OrderStatus adaptOrderStatus(BitfinexOrderStatusResponse order) {

    if (order.isCancelled()) return OrderStatus.CANCELED;
    else if (order.getExecutedAmount().compareTo(BigDecimal.ZERO) == 0) return OrderStatus.NEW;
    else if (order.getExecutedAmount().compareTo(order.getOriginalAmount()) < 0)
      return OrderStatus.PARTIALLY_FILLED;
    else if (order.getExecutedAmount().compareTo(order.getOriginalAmount()) == 0)
      return OrderStatus.FILLED;
    else return null;
  }

  public static String adaptCurrencyPair(CurrencyPair pair) {
    return BitfinexUtils.toPairString(pair);
  }

  public static OrderBook adaptOrderBook(BitfinexDepth btceDepth, CurrencyPair currencyPair) {

    OrdersContainer asksOrdersContainer =
        adaptOrders(btceDepth.getAsks(), currencyPair, OrderType.ASK);
    OrdersContainer bidsOrdersContainer =
        adaptOrders(btceDepth.getBids(), currencyPair, OrderType.BID);

    return new OrderBook(
        new Date(Math.max(asksOrdersContainer.getTimestamp(), bidsOrdersContainer.getTimestamp())),
        asksOrdersContainer.getLimitOrders(),
        bidsOrdersContainer.getLimitOrders());
  }

  public static OrdersContainer adaptOrders(
      BitfinexLevel[] bitfinexLevels, CurrencyPair currencyPair, OrderType orderType) {

    BigDecimal maxTimestamp = new BigDecimal(Long.MIN_VALUE);
    List<LimitOrder> limitOrders = new ArrayList<>(bitfinexLevels.length);

    for (BitfinexLevel bitfinexLevel : bitfinexLevels) {
      if (bitfinexLevel.getTimestamp().compareTo(maxTimestamp) > 0) {
        maxTimestamp = bitfinexLevel.getTimestamp();
      }

      Date timestamp = convertBigDecimalTimestampToDate(bitfinexLevel.getTimestamp());
      limitOrders.add(
          adaptOrder(
              bitfinexLevel.getAmount(),
              bitfinexLevel.getPrice(),
              currencyPair,
              orderType,
              timestamp));
    }

    long maxTimestampInMillis = maxTimestamp.multiply(new BigDecimal(1000L)).longValue();
    return new OrdersContainer(maxTimestampInMillis, limitOrders);
  }

  public static LimitOrder adaptOrder(
      BigDecimal originalAmount,
      BigDecimal price,
      CurrencyPair currencyPair,
      OrderType orderType,
      Date timestamp) {

    return new LimitOrder(orderType, originalAmount, currencyPair, "", timestamp, price);
  }

  public static List<FixedRateLoanOrder> adaptFixedRateLoanOrders(
      BitfinexLendLevel[] orders, String currency, String orderType, String id) {

    List<FixedRateLoanOrder> loanOrders = new ArrayList<>(orders.length);

    for (BitfinexLendLevel order : orders) {
      if ("yes".equalsIgnoreCase(order.getFrr())) {
        continue;
      }

      // Bid orderbook is reversed order. Insert at reversed indices
      if (orderType.equalsIgnoreCase("loan")) {
        loanOrders.add(
            0,
            adaptFixedRateLoanOrder(
                currency, order.getAmount(), order.getPeriod(), orderType, id, order.getRate()));
      } else {
        loanOrders.add(
            adaptFixedRateLoanOrder(
                currency, order.getAmount(), order.getPeriod(), orderType, id, order.getRate()));
      }
    }

    return loanOrders;
  }

  public static FixedRateLoanOrder adaptFixedRateLoanOrder(
      String currency,
      BigDecimal amount,
      int dayPeriod,
      String direction,
      String id,
      BigDecimal rate) {

    OrderType orderType = direction.equalsIgnoreCase("loan") ? OrderType.BID : OrderType.ASK;

    return new FixedRateLoanOrder(orderType, currency, amount, dayPeriod, id, null, rate);
  }

  public static List<FloatingRateLoanOrder> adaptFloatingRateLoanOrders(
      BitfinexLendLevel[] orders, String currency, String orderType, String id) {

    List<FloatingRateLoanOrder> loanOrders = new ArrayList<>(orders.length);

    for (BitfinexLendLevel order : orders) {
      if ("no".equals(order.getFrr())) {
        continue;
      }

      // Bid orderbook is reversed order. Insert at reversed indices
      if (orderType.equalsIgnoreCase("loan")) {
        loanOrders.add(
            0,
            adaptFloatingRateLoanOrder(
                currency, order.getAmount(), order.getPeriod(), orderType, id, order.getRate()));
      } else {
        loanOrders.add(
            adaptFloatingRateLoanOrder(
                currency, order.getAmount(), order.getPeriod(), orderType, id, order.getRate()));
      }
    }

    return loanOrders;
  }

  public static FloatingRateLoanOrder adaptFloatingRateLoanOrder(
      String currency,
      BigDecimal amount,
      int dayPeriod,
      String direction,
      String id,
      BigDecimal rate) {

    OrderType orderType = direction.equalsIgnoreCase("loan") ? OrderType.BID : OrderType.ASK;

    return new FloatingRateLoanOrder(orderType, currency, amount, dayPeriod, id, null, rate);
  }

  public static Trade adaptTrade(BitfinexTrade trade, CurrencyPair currencyPair) {

    OrderType orderType = trade.getType().equals("buy") ? OrderType.BID : OrderType.ASK;
    BigDecimal amount = trade.getAmount();
    BigDecimal price = trade.getPrice();
    Date date =
        DateUtils.fromMillisUtc(trade.getTimestamp() * 1000L); // Bitfinex uses Unix timestamps
    final String tradeId = String.valueOf(trade.getTradeId());
    return new Trade.Builder()
        .type(orderType)
        .originalAmount(amount)
        .currencyPair(currencyPair)
        .price(price)
        .timestamp(date)
        .id(tradeId)
        .build();
  }

  public static Trades adaptTrades(BitfinexTrade[] trades, CurrencyPair currencyPair) {

    List<Trade> tradesList = new ArrayList<>(trades.length);
    long lastTradeId = 0;
    for (BitfinexTrade trade : trades) {
      long tradeId = trade.getTradeId();
      if (tradeId > lastTradeId) {
        lastTradeId = tradeId;
      }
      tradesList.add(adaptTrade(trade, currencyPair));
    }
    return new Trades(tradesList, lastTradeId, TradeSortType.SortByID);
  }

  public static Ticker adaptTicker(BitfinexTicker bitfinexTicker, CurrencyPair currencyPair) {

    BigDecimal last = bitfinexTicker.getLast_price();
    BigDecimal bid = bitfinexTicker.getBid();
    BigDecimal bidSize = bitfinexTicker.getBidSize();
    BigDecimal ask = bitfinexTicker.getAsk();
    BigDecimal askSize = bitfinexTicker.getAskSize();
    BigDecimal high = bitfinexTicker.getHigh();
    BigDecimal low = bitfinexTicker.getLow();
    BigDecimal volume = bitfinexTicker.getVolume();

    Date timestamp = DateUtils.fromMillisUtc((long) (bitfinexTicker.getTimestamp() * 1000L));

    return new Ticker.Builder()
        .currencyPair(currencyPair)
        .last(last)
        .bid(bid)
        .bidSize(bidSize)
        .ask(ask)
        .askSize(askSize)
        .high(high)
        .low(low)
        .volume(volume)
        .timestamp(timestamp)
        .build();
  }

  public static List<Wallet> adaptWallets(BitfinexBalancesResponse[] response) {

    Map<String, Map<String, BigDecimal[]>> walletsBalancesMap = new HashMap<>();

    // for each currency we have multiple balances types: exchange, trading, deposit.
    // each of those may be partially frozen/available
    for (BitfinexBalancesResponse balance : response) {
      String walletId = balance.getType();

      if (!walletsBalancesMap.containsKey(walletId)) {
        walletsBalancesMap.put(walletId, new HashMap<>());
      }
      Map<String, BigDecimal[]> balancesByCurrency =
          walletsBalancesMap.get(walletId); // {total, available}

      String currencyName = adaptBitfinexCurrency(balance.getCurrency());
      BigDecimal[] balanceDetail = balancesByCurrency.get(currencyName);
      if (balanceDetail == null) {
        balanceDetail = new BigDecimal[] {balance.getAmount(), balance.getAvailable()};
      } else {
        balanceDetail[0] = balanceDetail[0].add(balance.getAmount());
        balanceDetail[1] = balanceDetail[1].add(balance.getAvailable());
      }
      balancesByCurrency.put(currencyName, balanceDetail);
    }

    List<Wallet> wallets = new ArrayList<>();
    for (Entry<String, Map<String, BigDecimal[]>> walletData : walletsBalancesMap.entrySet()) {
      Map<String, BigDecimal[]> balancesByCurrency = walletData.getValue();

      List<Balance> balances = new ArrayList<>(balancesByCurrency.size());
      for (Entry<String, BigDecimal[]> entry : balancesByCurrency.entrySet()) {
        String currencyName = entry.getKey();
        BigDecimal[] balanceDetail = entry.getValue();
        BigDecimal balanceTotal = balanceDetail[0];
        BigDecimal balanceAvailable = balanceDetail[1];
        balances.add(
            new Balance(Currency.getInstance(currencyName), balanceTotal, balanceAvailable));
      }
      wallets.add(Wallet.Builder.from(balances).id(walletData.getKey()).build());
    }

    return wallets;
  }

  public static OpenOrders adaptOrders(BitfinexOrderStatusResponse[] activeOrders) {

    List<LimitOrder> limitOrders = new ArrayList<>();
    List<Order> hiddenOrders = new ArrayList<>();

    for (BitfinexOrderStatusResponse order : activeOrders) {

      OrderType orderType = order.getSide().equalsIgnoreCase("buy") ? OrderType.BID : OrderType.ASK;
      OrderStatus status = adaptOrderStatus(order);
      CurrencyPair currencyPair = adaptCurrencyPair(order.getSymbol());
      Date timestamp = convertBigDecimalTimestampToDate(order.getTimestamp());

      Supplier<MarketOrder> marketOrderCreator =
          () ->
              new MarketOrder(
                  orderType,
                  order.getOriginalAmount(),
                  currencyPair,
                  String.valueOf(order.getId()),
                  timestamp,
                  order.getAvgExecutionPrice(),
                  order.getExecutedAmount(),
                  null,
                  status);

      Supplier<LimitOrder> limitOrderCreator =
          () ->
              new LimitOrder(
                  orderType,
                  order.getOriginalAmount(),
                  currencyPair,
                  String.valueOf(order.getId()),
                  timestamp,
                  order.getPrice(),
                  order.getAvgExecutionPrice(),
                  order.getExecutedAmount(),
                  null,
                  status);

      Supplier<StopOrder> stopOrderCreator =
          () ->
              new StopOrder(
                  orderType,
                  order.getOriginalAmount(),
                  currencyPair,
                  String.valueOf(order.getId()),
                  timestamp,
                  order.getPrice(),
                  null,
                  order.getAvgExecutionPrice(),
                  order.getExecutedAmount(),
                  status);

      LimitOrder limitOrder = null;
      StopOrder stopOrder = null;
      MarketOrder marketOrder = null;

      Optional<BitfinexOrderType> bitfinexOrderType =
          Arrays.stream(BitfinexOrderType.values())
              .filter(v -> v.getValue().equals(order.getType()))
              .findFirst();

      if (bitfinexOrderType.isPresent()) {
        switch (bitfinexOrderType.get()) {
          case FILL_OR_KILL:
            limitOrder = limitOrderCreator.get();
            limitOrder.addOrderFlag(BitfinexOrderFlags.FILL_OR_KILL);
            break;
          case MARGIN_FILL_OR_KILL:
            limitOrder = limitOrderCreator.get();
            limitOrder.addOrderFlag(BitfinexOrderFlags.FILL_OR_KILL);
            limitOrder.addOrderFlag(BitfinexOrderFlags.MARGIN);
            break;
          case MARGIN_LIMIT:
            limitOrder = limitOrderCreator.get();
            limitOrder.addOrderFlag(BitfinexOrderFlags.MARGIN);
            break;
          case MARGIN_STOP:
            stopOrder = stopOrderCreator.get();
            stopOrder.addOrderFlag(BitfinexOrderFlags.STOP);
            stopOrder.addOrderFlag(BitfinexOrderFlags.MARGIN);
            break;
          case MARGIN_STOP_LIMIT:
            stopLimitWarning();
            stopOrder = stopOrderCreator.get();
            stopOrder.addOrderFlag(BitfinexOrderFlags.STOP);
            stopOrder.addOrderFlag(BitfinexOrderFlags.MARGIN);
            break;
          case MARGIN_TRAILING_STOP:
            limitOrder = limitOrderCreator.get();
            limitOrder.addOrderFlag(BitfinexOrderFlags.TRAILING_STOP);
            limitOrder.addOrderFlag(BitfinexOrderFlags.MARGIN);
            break;
          case STOP:
            stopOrder = stopOrderCreator.get();
            stopOrder.addOrderFlag(BitfinexOrderFlags.STOP);
            break;
          case STOP_LIMIT:
            stopLimitWarning();
            stopOrder = stopOrderCreator.get();
            stopOrder.addOrderFlag(BitfinexOrderFlags.STOP);
            break;
          case TRAILING_STOP:
            limitOrder = limitOrderCreator.get();
            limitOrder.addOrderFlag(BitfinexOrderFlags.TRAILING_STOP);
            break;
          case LIMIT:
            limitOrder = limitOrderCreator.get();
            break;
          case MARGIN_MARKET:
          case MARKET:
            marketOrder = marketOrderCreator.get();
            break;
          default:
            log.warn(
                "Unhandled Bitfinex order type [{}]. Defaulting to limit order", order.getType());
            limitOrder = limitOrderCreator.get();
            break;
        }
      } else {
        log.warn("Unknown Bitfinex order type [{}]. Defaulting to limit order", order.getType());
        limitOrder = limitOrderCreator.get();
      }

      if (limitOrder != null) {
        limitOrders.add(limitOrder);
      } else if (stopOrder != null) {
        hiddenOrders.add(stopOrder);
      } else if (marketOrder != null) {
        hiddenOrders.add(marketOrder);
      }
    }

    return new OpenOrders(limitOrders, hiddenOrders);
  }

  private static void stopLimitWarning() {
    if (warnedStopLimit.compareAndSet(false, true)) {
      log.warn(
          "Found a stop-limit order. Bitfinex v1 API does not return limit prices for stop-limit "
              + "orders so these are returned as stop-at-market orders. This warning will only appear "
              + "once.");
    }
  }

  public static UserTrades adaptTradeHistory(BitfinexTradeResponse[] trades, String symbol) {

    List<UserTrade> pastTrades = new ArrayList<>(trades.length);
    CurrencyPair currencyPair = adaptCurrencyPair(symbol);

    for (BitfinexTradeResponse trade : trades) {
      OrderType orderType = trade.getType().equalsIgnoreCase("buy") ? OrderType.BID : OrderType.ASK;
      Date timestamp = convertBigDecimalTimestampToDate(trade.getTimestamp());
      final BigDecimal fee = trade.getFeeAmount() == null ? null : trade.getFeeAmount().negate();
      pastTrades.add(
          new UserTrade.Builder()
              .type(orderType)
              .originalAmount(trade.getAmount())
              .currencyPair(currencyPair)
              .price(trade.getPrice())
              .timestamp(timestamp)
              .id(trade.getTradeId())
              .orderId(trade.getOrderId())
              .feeAmount(fee)
              .feeCurrency(Currency.getInstance(trade.getFeeCurrency()))
              .build());
    }

    return new UserTrades(pastTrades, TradeSortType.SortByTimestamp);
  }

  public static UserTrades adaptTradeHistoryV2(
      List<org.knowm.xchange.bitfinex.v2.dto.trade.Trade> trades) {

    List<UserTrade> pastTrades = new ArrayList<>(trades.size());

    for (org.knowm.xchange.bitfinex.v2.dto.trade.Trade trade : trades) {
      OrderType orderType = trade.getExecAmount().signum() >= 0 ? OrderType.BID : OrderType.ASK;
      BigDecimal amount =
          trade.getExecAmount().signum() == -1
              ? trade.getExecAmount().negate()
              : trade.getExecAmount();
      final BigDecimal fee = trade.getFee() != null ? trade.getFee().negate() : null;
      pastTrades.add(
          new UserTrade.Builder()
              .type(orderType)
              .originalAmount(amount)
              .currencyPair(adaptCurrencyPair(trade.getSymbol()))
              .price(trade.getExecPrice())
              .timestamp(trade.getTimestamp())
              .id(trade.getId())
              .orderId(trade.getOrderId())
              .feeAmount(fee)
              .feeCurrency(Currency.getInstance(trade.getFeeCurrency()))
              .build());
    }

    return new UserTrades(pastTrades, TradeSortType.SortByTimestamp);
  }

  private static Date convertBigDecimalTimestampToDate(BigDecimal timestamp) {

    BigDecimal timestampInMillis = timestamp.multiply(new BigDecimal("1000"));
    return new Date(timestampInMillis.longValue());
  }

  public static ExchangeMetaData adaptMetaData(
      List<CurrencyPair> currencyPairs, ExchangeMetaData metaData) {

    Map<CurrencyPair, CurrencyPairMetaData> pairsMap = metaData.getCurrencyPairs();
    Map<Currency, CurrencyMetaData> currenciesMap = metaData.getCurrencies();

    // Remove pairs that are no-longer in use
    pairsMap.keySet().retainAll(currencyPairs);

    // Remove currencies that are no-longer in use
    Set<Currency> currencies =
        currencyPairs.stream()
            .flatMap(pair -> Stream.of(pair.base, pair.counter))
            .collect(Collectors.toSet());
    currenciesMap.keySet().retainAll(currencies);

    // Add missing pairs and currencies
    for (CurrencyPair c : currencyPairs) {
      if (!pairsMap.containsKey(c)) {
        pairsMap.put(c, null);
      }

      if (!currenciesMap.containsKey(c.base)) {
        currenciesMap.put(
            c.base,
            new CurrencyMetaData(
                2,
                null)); // When missing, add default meta-data with scale of 2 (Bitfinex's minimal
        // scale)
      }
      if (!currenciesMap.containsKey(c.counter)) {
        currenciesMap.put(c.counter, new CurrencyMetaData(2, null));
      }
    }

    return metaData;
  }

  /**
   * Flipped order of arguments to avoid type-erasure clash with {@link #adaptMetaData(List,
   * ExchangeMetaData)}
   *
   * @param exchangeMetaData The exchange metadata provided from bitfinex.json.
   * @param symbolDetails The symbol data fetced from Bitfinex.
   * @return The combined result.
   */
  public static ExchangeMetaData adaptMetaData(
      ExchangeMetaData exchangeMetaData,
      List<BitfinexSymbolDetail> symbolDetails,
      Map<CurrencyPair, BigDecimal> lastPrices) {

    final Map<CurrencyPair, CurrencyPairMetaData> currencyPairs =
        exchangeMetaData.getCurrencyPairs();
    symbolDetails.parallelStream()
        .forEach(
            bitfinexSymbolDetail -> {
              final CurrencyPair currencyPair = adaptCurrencyPair(bitfinexSymbolDetail.getPair());

              // Infer price-scale from last and price-precision
              BigDecimal last = lastPrices.get(currencyPair);

              if (last != null) {
                int pricePercision = bitfinexSymbolDetail.getPrice_precision();
                int priceScale = last.scale() + (pricePercision - last.precision());

                CurrencyPairMetaData newMetaData =
                    new CurrencyPairMetaData(
                        currencyPairs.get(currencyPair) == null
                            ? null
                            : currencyPairs
                                .get(currencyPair)
                                .getTradingFee(), // Take tradingFee from static metaData if exists
                        bitfinexSymbolDetail.getMinimum_order_size(),
                        bitfinexSymbolDetail.getMaximum_order_size(),
                        priceScale,
                        null);
                currencyPairs.put(currencyPair, newMetaData);
              }
            });
    return exchangeMetaData;
  }

  public static ExchangeMetaData adaptMetaData(
          BitfinexAccountFeesResponse accountFeesResponse, Integer platformStatus, ExchangeMetaData metaData) {
    final WalletHealth health;
    if (platformStatus == PLATFORM_STATUS_ONLINE) {
      health = WalletHealth.ONLINE;
    } else if (platformStatus == PLATFORM_STATUS_OFFLINE) {
      health = WalletHealth.OFFLINE;
    } else {
      health = WalletHealth.UNKNOWN;
    }

    Map<Currency, CurrencyMetaData> currencies = metaData.getCurrencies();
    final Map<Currency, BigDecimal> withdrawFees = accountFeesResponse.getWithdraw();
    withdrawFees.forEach(
        (currency, withdrawalFee) -> {
          CurrencyMetaData newMetaData =
              new CurrencyMetaData(
                  // Currency should have at least the scale of the withdrawalFee
                  currencies.get(currency) == null
                      ? withdrawalFee.scale()
                      : Math.max(withdrawalFee.scale(), currencies.get(currency).getScale()),
                  withdrawalFee, null, health);
          currencies.put(currency, newMetaData);
        });
    return metaData;
  }

  public static ExchangeMetaData adaptMetaData(
      BitfinexAccountInfosResponse[] bitfinexAccountInfos, ExchangeMetaData exchangeMetaData) {
    final Map<CurrencyPair, CurrencyPairMetaData> currencyPairs =
        exchangeMetaData.getCurrencyPairs();

    // lets go with the assumption that the trading fees are common across all trading pairs for
    // now.
    // also setting the taker_fee as the trading_fee for now.
    final CurrencyPairMetaData metaData =
        new CurrencyPairMetaData(
            bitfinexAccountInfos[0].getTakerFees().movePointLeft(2), null, null, null, null);
    currencyPairs.keySet().parallelStream()
        .forEach(
            currencyPair ->
                currencyPairs.merge(
                    currencyPair,
                    metaData,
                    (oldMetaData, newMetaData) ->
                        new CurrencyPairMetaData(
                            newMetaData.getTradingFee(),
                            oldMetaData.getMinimumAmount(),
                            oldMetaData.getMaximumAmount(),
                            oldMetaData.getPriceScale(),
                            oldMetaData.getFeeTiers())));

    return exchangeMetaData;
  }

  public static List<FundingRecord> adaptFundingHistory(List<Movement> movementHistorys) {
    final List<FundingRecord> fundingRecords = new ArrayList<>();
    for (Movement movement : movementHistorys) {
      Currency currency = Currency.getInstance(movement.getCurency());

      FundingRecord.Type type =
          movement.getAmount().compareTo(BigDecimal.ZERO) < 0
              ? FundingRecord.Type.WITHDRAWAL
              : FundingRecord.Type.DEPOSIT;

      FundingRecord.Status status = FundingRecord.Status.resolveStatus(movement.getStatus());
      if (status == null
          && movement
              .getStatus()
              .equalsIgnoreCase("CANCELED")) // there's a spelling mistake in the protocol
      status = FundingRecord.Status.CANCELLED;

      BigDecimal amount = movement.getAmount().abs();
      BigDecimal fee = movement.getFees().abs();
      if (fee != null && type.isOutflowing()) {
        // The amount reported form Bitfinex on a withdrawal is without the fee, so it has to be
        // added to get the full amount withdrawn from the wallet
        // Deposits don't seem to have fees, but it seems reasonable to assume that the reported
        // value is the full amount added to the wallet
        amount = amount.add(fee);
      }

      FundingRecord fundingRecordEntry =
          new FundingRecord(
              movement.getDestinationAddress(),
              null,
              movement.getMtsUpdated(),
              currency,
              amount,
              movement.getId(),
              movement.getTransactionId(),
              type,
              status,
              null,
              fee,
              null);

      fundingRecords.add(fundingRecordEntry);
    }
    return fundingRecords;
  }

  public static List<FundingRecord> adaptFundingHistory(
      BitfinexDepositWithdrawalHistoryResponse[] bitfinexDepositWithdrawalHistoryResponses) {
    final List<FundingRecord> fundingRecords = new ArrayList<>();
    for (BitfinexDepositWithdrawalHistoryResponse responseEntry :
        bitfinexDepositWithdrawalHistoryResponses) {
      String address = responseEntry.getAddress();
      String description = responseEntry.getDescription();
      Currency currency = Currency.getInstance(responseEntry.getCurrency());

      FundingRecord.Status status = FundingRecord.Status.resolveStatus(responseEntry.getStatus());
      if (status == null
          && responseEntry
              .getStatus()
              .equalsIgnoreCase("CANCELED")) // there's a spelling mistake in the protocol
      status = FundingRecord.Status.CANCELLED;

      String txnId = null;
      if (status == null || !status.equals(FundingRecord.Status.CANCELLED)) {
        /*
        sometimes the description looks like this (with the txn hash in it):
        "description":"a9d387cf5d9df58ff2ac4a338e0f050fd3857cf78d1dbca4f33619dc4ccdac82","address":"1Enx...

        and sometimes like this (with the address in it as well as the txn hash):
        "description":"3AXVnDapuRiAn73pjKe7gukLSx5813oFyn, txid: aa4057486d5f73747167beb9949a0dfe17b5fc630499a66af075abdaf4986987","address":"3AX...

        and sometimes when cancelled
        "description":"3LFVTLFZoDDzLCcLGDDQ7MNkk4YPe26Yva, expired","address":"3LFV...
         */

        String cleanedDescription =
            description.replace(",", "").replace("txid:", "").trim().toLowerCase();

        // Address will only be present for crypto payments. It will be null for all fiat payments
        if (address != null) {
          cleanedDescription = cleanedDescription.replace(address.toLowerCase(), "").trim();
        }

        // check its just some hex characters, and if so lets assume its the txn hash
        if (cleanedDescription.matches("^(0x)?[0-9a-f]+$")) {
          txnId = cleanedDescription;
        }
      }

      FundingRecord fundingRecordEntry =
          new FundingRecord(
              address,
              responseEntry.getTimestamp(),
              currency,
              responseEntry.getAmount(),
              String.valueOf(responseEntry.getId()),
              txnId,
              responseEntry.getType(),
              status,
              null,
              null,
              description);

      fundingRecords.add(fundingRecordEntry);
    }
    return fundingRecords;
  }

  public static class OrdersContainer {

    private final long timestamp;
    private final List<LimitOrder> limitOrders;

    /**
     * Constructor
     *
     * @param timestamp The timestamp for the data fetched.
     * @param limitOrders The orders.
     */
    public OrdersContainer(long timestamp, List<LimitOrder> limitOrders) {

      this.timestamp = timestamp;
      this.limitOrders = limitOrders;
    }

    public long getTimestamp() {

      return timestamp;
    }

    public List<LimitOrder> getLimitOrders() {

      return limitOrders;
    }
  }

  ////// v2

  public static String adaptCurrencyPairsToTickersParam(Collection<CurrencyPair> currencyPairs) {
    return currencyPairs == null || currencyPairs.isEmpty()
        ? "ALL"
        : currencyPairs.stream()
            .map(BitfinexAdapters::adaptCurrencyPair)
            .collect(Collectors.joining(","));
  }

  public static Ticker adaptTicker(
      org.knowm.xchange.bitfinex.v2.dto.marketdata.BitfinexTicker bitfinexTicker) {

    BigDecimal last = bitfinexTicker.getLastPrice();
    BigDecimal bid = bitfinexTicker.getBid();
    BigDecimal bidSize = bitfinexTicker.getBidSize();
    BigDecimal ask = bitfinexTicker.getAsk();
    BigDecimal askSize = bitfinexTicker.getAskSize();
    BigDecimal high = bitfinexTicker.getHigh();
    BigDecimal low = bitfinexTicker.getLow();
    BigDecimal volume = bitfinexTicker.getVolume();
    BigDecimal percentageChange =
        bitfinexTicker.getDailyChangePerc().multiply(new BigDecimal("100"), new MathContext(8));

    CurrencyPair currencyPair =
        CurrencyPairDeserializer.getCurrencyPairFromString(bitfinexTicker.getSymbol().substring(1));

    return new Ticker.Builder()
        .currencyPair(currencyPair)
        .last(last)
        .bid(bid)
        .ask(ask)
        .high(high)
        .low(low)
        .volume(volume)
        .bidSize(bidSize)
        .askSize(askSize)
        .percentageChange(percentageChange)
        .build();
  }

  public static Trade adaptPublicTrade(BitfinexPublicTrade trade, CurrencyPair currencyPair) {

    OrderType orderType = trade.getType();
    BigDecimal amount = trade.getAmount();
    BigDecimal price = trade.getPrice();
    Date date = DateUtils.fromMillisUtc(trade.getTimestamp());
    final String tradeId = String.valueOf(trade.getTradeId());
    return new Trade.Builder()
        .type(orderType)
        .originalAmount(amount == null ? null : amount.abs())
        .currencyPair(currencyPair)
        .price(price)
        .timestamp(date)
        .id(tradeId)
        .build();
  }

  public static Trades adaptPublicTrades(BitfinexPublicTrade[] trades, CurrencyPair currencyPair) {

    List<Trade> tradesList = new ArrayList<>(trades.length);
    long lastTradeId = 0;
    for (BitfinexPublicTrade trade : trades) {
      long tradeId = trade.getTradeId();
      if (tradeId > lastTradeId) {
        lastTradeId = tradeId;
      }
      tradesList.add(adaptPublicTrade(trade, currencyPair));
    }
    return new Trades(tradesList, lastTradeId, TradeSortType.SortByID);
  }

  public static org.knowm.xchange.bitfinex.v2.dto.marketdata.BitfinexTicker[] adoptBitfinexTickers(
      List<ArrayNode> tickers) throws IOException {

    return tickers.stream()
        .map(
            array -> {
              // tBTCUSD -> traiding pair
              // fUSD -> funding currency
              try {
                String symbol = array.get(0).asText();
                switch (symbol.charAt(0)) {
                  case 't':
                    return mapper.treeToValue(array, BitfinexTickerTraidingPair.class);
                  case 'f':
                    return mapper.treeToValue(array, BitfinexTickerFundingCurrency.class);
                  default:
                    throw new RuntimeException(
                        "Invalid symbol <" + symbol + ">, it must start with 't' or 'f'.");
                }
              } catch (JsonProcessingException e) {
                throw new RuntimeException("Could not convert ticker.", e);
              }
            })
        .toArray(org.knowm.xchange.bitfinex.v2.dto.marketdata.BitfinexTicker[]::new);
  }
}
