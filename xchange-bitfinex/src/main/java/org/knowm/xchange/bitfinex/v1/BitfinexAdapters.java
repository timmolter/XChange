package org.knowm.xchange.bitfinex.v1;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.knowm.xchange.bitfinex.v1.dto.account.BitfinexAccountFeesResponse;
import org.knowm.xchange.bitfinex.v1.dto.account.BitfinexBalancesResponse;
import org.knowm.xchange.bitfinex.v1.dto.account.BitfinexDepositWithdrawalHistoryResponse;
import org.knowm.xchange.bitfinex.v1.dto.marketdata.BitfinexDepth;
import org.knowm.xchange.bitfinex.v1.dto.marketdata.BitfinexLendLevel;
import org.knowm.xchange.bitfinex.v1.dto.marketdata.BitfinexLevel;
import org.knowm.xchange.bitfinex.v1.dto.marketdata.BitfinexSymbolDetail;
import org.knowm.xchange.bitfinex.v1.dto.marketdata.BitfinexTicker;
import org.knowm.xchange.bitfinex.v1.dto.marketdata.BitfinexTrade;
import org.knowm.xchange.bitfinex.v1.dto.trade.BitfinexAccountInfosResponse;
import org.knowm.xchange.bitfinex.v1.dto.trade.BitfinexOrderStatusResponse;
import org.knowm.xchange.bitfinex.v1.dto.trade.BitfinexTradeResponse;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
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
import org.knowm.xchange.dto.trade.FixedRateLoanOrder;
import org.knowm.xchange.dto.trade.FloatingRateLoanOrder;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BitfinexAdapters {

  public static final Logger log = LoggerFactory.getLogger(BitfinexAdapters.class);

  private BitfinexAdapters() {}

  public static String adaptBitfinexCurrency(String bitfinexSymbol) {
    String currency = bitfinexSymbol.toUpperCase();
    if (currency.equals("DSH")) {
      currency = "DASH";
    } else if (currency.equals("QTM")) {
      currency = "QTUM";
    } else if (currency.equals("QSH")) {
      currency = "QASH";
    } else if (currency.equals("DAT")) {
      currency = "DATA";
    }

    return currency;
  }

  public static List<CurrencyPair> adaptCurrencyPairs(Collection<String> bitfinexSymbol) {

    List<CurrencyPair> currencyPairs = new ArrayList<>();
    for (String symbol : bitfinexSymbol) {
      currencyPairs.add(adaptCurrencyPair(symbol));
    }
    return currencyPairs;
  }

  public static CurrencyPair adaptCurrencyPair(String bitfinexSymbol) {

    String tradableIdentifier = adaptBitfinexCurrency(bitfinexSymbol.substring(0, 3));
    String transactionCurrency = adaptBitfinexCurrency(bitfinexSymbol.substring(3));
    return new CurrencyPair(tradableIdentifier, transactionCurrency);
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

    return (pair.base.getCurrencyCode() + pair.counter.getCurrencyCode()).toLowerCase();
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
    return new Trade(orderType, amount, currencyPair, price, date, tradeId);
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
    BigDecimal ask = bitfinexTicker.getAsk();
    BigDecimal high = bitfinexTicker.getHigh();
    BigDecimal low = bitfinexTicker.getLow();
    BigDecimal volume = bitfinexTicker.getVolume();

    Date timestamp = DateUtils.fromMillisUtc((long) (bitfinexTicker.getTimestamp() * 1000L));

    return new Ticker.Builder()
        .currencyPair(currencyPair)
        .last(last)
        .bid(bid)
        .ask(ask)
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
      wallets.add(new Wallet(walletData.getKey(), balances));
    }

    return wallets;
  }

  public static OpenOrders adaptOrders(BitfinexOrderStatusResponse[] activeOrders) {

    List<LimitOrder> limitOrders = new ArrayList<>(activeOrders.length);

    for (BitfinexOrderStatusResponse order : activeOrders) {

      OrderType orderType = order.getSide().equalsIgnoreCase("buy") ? OrderType.BID : OrderType.ASK;
      OrderStatus status = adaptOrderStatus(order);
      CurrencyPair currencyPair = adaptCurrencyPair(order.getSymbol());
      Date timestamp = convertBigDecimalTimestampToDate(order.getTimestamp());

      limitOrders.add(
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
              status));
    }

    return new OpenOrders(limitOrders);
  }

  public static UserTrades adaptTradeHistory(BitfinexTradeResponse[] trades, String symbol) {

    List<UserTrade> pastTrades = new ArrayList<>(trades.length);
    CurrencyPair currencyPair = adaptCurrencyPair(symbol);

    for (BitfinexTradeResponse trade : trades) {
      OrderType orderType = trade.getType().equalsIgnoreCase("buy") ? OrderType.BID : OrderType.ASK;
      Date timestamp = convertBigDecimalTimestampToDate(trade.getTimestamp());
      final BigDecimal fee = trade.getFeeAmount() == null ? null : trade.getFeeAmount().negate();
      pastTrades.add(
          new UserTrade(
              orderType,
              trade.getAmount(),
              currencyPair,
              trade.getPrice(),
              timestamp,
              trade.getTradeId(),
              trade.getOrderId(),
              fee,
              Currency.getInstance(trade.getFeeCurrency())));
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
    for (CurrencyPair c : currencyPairs) {
      if (!pairsMap.containsKey(c)) {
        pairsMap.put(c, null);
      }
      if (!currenciesMap.containsKey(c.base)) {
        currenciesMap.put(c.base, null);
      }
      if (!currenciesMap.containsKey(c.counter)) {
        currenciesMap.put(c.counter, null);
      }
    }

    return metaData;
  }

  /**
   * Flipped order of arguments to avoid type-erasure clash with {@link #adaptMetaData(List,
   * ExchangeMetaData)}
   *
   * @param exchangeMetaData
   * @param symbolDetails
   * @return
   */
  public static ExchangeMetaData adaptMetaData(
      ExchangeMetaData exchangeMetaData, List<BitfinexSymbolDetail> symbolDetails) {
    final Map<CurrencyPair, CurrencyPairMetaData> currencyPairs =
        exchangeMetaData.getCurrencyPairs();
    symbolDetails
        .parallelStream()
        .forEach(
            bitfinexSymbolDetail -> {
              final CurrencyPair currencyPair = adaptCurrencyPair(bitfinexSymbolDetail.getPair());
              if (currencyPairs.get(currencyPair) == null) {
                CurrencyPairMetaData newMetaData =
                    new CurrencyPairMetaData(
                        null,
                        bitfinexSymbolDetail.getMinimum_order_size(),
                        bitfinexSymbolDetail.getMaximum_order_size(),
                        bitfinexSymbolDetail.getPrice_precision());
                currencyPairs.put(currencyPair, newMetaData);
              } else {
                CurrencyPairMetaData oldMetaData = currencyPairs.get(currencyPair);
                CurrencyPairMetaData newMetaData =
                    new CurrencyPairMetaData(
                        oldMetaData.getTradingFee(),
                        bitfinexSymbolDetail.getMinimum_order_size(),
                        bitfinexSymbolDetail.getMaximum_order_size(),
                        bitfinexSymbolDetail.getPrice_precision());
                currencyPairs.put(currencyPair, newMetaData);
              }
            });
    return exchangeMetaData;
  }

  public static ExchangeMetaData adaptMetaData(
      BitfinexAccountFeesResponse accountFeesResponse, ExchangeMetaData metaData) {
    Map<Currency, CurrencyMetaData> currencies = metaData.getCurrencies();
    final Map<Currency, BigDecimal> withdrawFees = accountFeesResponse.getWithdraw();
    withdrawFees.forEach(
        (currency, withdrawalFee) -> {
          if (currencies.get(currency) == null) {
            CurrencyMetaData currencyMetaData = new CurrencyMetaData(0, withdrawalFee);
            currencies.put(currency, currencyMetaData);
          } else {
            final CurrencyMetaData oldMetaData = currencies.get(currency);
            CurrencyMetaData newMetaData =
                new CurrencyMetaData(oldMetaData.getScale(), withdrawalFee);
            currencies.put(currency, newMetaData);
          }
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
        new CurrencyPairMetaData(bitfinexAccountInfos[0].getTakerFees(), null, null, null);
    currencyPairs
        .keySet()
        .parallelStream()
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
                            oldMetaData.getPriceScale())));

    return exchangeMetaData;
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
            description
                .replace(address, "")
                .replace(",", "")
                .replace("txid:", "")
                .trim()
                .toLowerCase();

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
     * @param timestamp
     * @param limitOrders
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
}
