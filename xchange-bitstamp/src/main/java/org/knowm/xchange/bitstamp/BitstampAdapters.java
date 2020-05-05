package org.knowm.xchange.bitstamp;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.knowm.xchange.bitstamp.dto.account.BitstampBalance;
import org.knowm.xchange.bitstamp.dto.marketdata.BitstampOrderBook;
import org.knowm.xchange.bitstamp.dto.marketdata.BitstampPairInfo;
import org.knowm.xchange.bitstamp.dto.marketdata.BitstampTicker;
import org.knowm.xchange.bitstamp.dto.marketdata.BitstampTransaction;
import org.knowm.xchange.bitstamp.dto.trade.*;
import org.knowm.xchange.bitstamp.order.dto.BitstampGenericOrder;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.account.AccountInfo;
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
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;
import org.knowm.xchange.utils.DateUtils;

/** Various adapters for converting from Bitstamp DTOs to XChange DTOs */
public final class BitstampAdapters {

  /** private Constructor */
  private BitstampAdapters() {}

  /**
   * Adapts a BitstampBalance to an AccountInfo
   *
   * @param bitstampBalance The Bitstamp balance
   * @param userName The user name
   * @return The account info
   */
  public static AccountInfo adaptAccountInfo(BitstampBalance bitstampBalance, String userName) {

    // Adapt to XChange DTOs
    List<Balance> balances = new ArrayList<>();
    for (org.knowm.xchange.bitstamp.dto.account.BitstampBalance.Balance b :
        bitstampBalance.getBalances()) {
      Balance xchangeBalance =
          new Balance(
              Currency.getInstance(b.getCurrency().toUpperCase()),
              b.getBalance(),
              b.getAvailable(),
              b.getReserved(),
              ZERO,
              ZERO,
              b.getBalance().subtract(b.getAvailable()).subtract(b.getReserved()),
              ZERO);
      balances.add(xchangeBalance);
    }
    return new AccountInfo(
        userName, bitstampBalance.getFee(), Wallet.Builder.from(balances).build());
  }

  /**
   * Adapts a org.knowm.xchange.bitstamp.api.model.OrderBook to a OrderBook Object
   *
   * @param bitstampOrderBook orderbook
   * @param currencyPair (e.g. BTC/USD)
   * @return The XChange OrderBook
   */
  public static OrderBook adaptOrderBook(
      BitstampOrderBook bitstampOrderBook, CurrencyPair currencyPair) {
    List<LimitOrder> asks =
        createOrders(currencyPair, Order.OrderType.ASK, bitstampOrderBook.getAsks());
    List<LimitOrder> bids =
        createOrders(currencyPair, Order.OrderType.BID, bitstampOrderBook.getBids());
    return new OrderBook(bitstampOrderBook.getTimestamp(), asks, bids);
  }

  public static List<LimitOrder> createOrders(
      CurrencyPair currencyPair, Order.OrderType orderType, List<List<BigDecimal>> orders) {

    List<LimitOrder> limitOrders = new ArrayList<>();
    for (List<BigDecimal> ask : orders) {
      checkArgument(
          ask.size() == 2, "Expected a pair (price, amount) but got {0} elements.", ask.size());
      limitOrders.add(createOrder(currencyPair, ask, orderType));
    }
    return limitOrders;
  }

  public static LimitOrder createOrder(
      CurrencyPair currencyPair, List<BigDecimal> priceAndAmount, Order.OrderType orderType) {

    return new LimitOrder(
        orderType, priceAndAmount.get(1), currencyPair, "", null, priceAndAmount.get(0));
  }

  public static void checkArgument(boolean argument, String msgPattern, Object... msgArgs) {

    if (!argument) {
      throw new IllegalArgumentException(MessageFormat.format(msgPattern, msgArgs));
    }
  }

  /**
   * Adapts a Transaction[] to a Trades Object
   *
   * @param transactions The Bitstamp transactions
   * @param currencyPair (e.g. BTC/USD)
   * @return The XChange Trades
   */
  public static Trades adaptTrades(BitstampTransaction[] transactions, CurrencyPair currencyPair) {

    List<Trade> trades = new ArrayList<>();
    long lastTradeId = 0;
    for (BitstampTransaction tx : transactions) {
      final long tradeId = tx.getTid();
      if (tradeId > lastTradeId) {
        lastTradeId = tradeId;
      }
      trades.add(adaptTrade(tx, currencyPair, 1000));
    }

    return new Trades(trades, lastTradeId, TradeSortType.SortByID);
  }

  /**
   * Adapts a Transaction to a Trade Object
   *
   * @param tx The Bitstamp transaction
   * @param currencyPair (e.g. BTC/USD)
   * @param timeScale polled order books provide a timestamp in seconds, stream in ms
   * @return The XChange Trade
   */
  public static Trade adaptTrade(BitstampTransaction tx, CurrencyPair currencyPair, int timeScale) {

    OrderType orderType = tx.getType() == 0 ? OrderType.BID : OrderType.ASK;
    final String tradeId = String.valueOf(tx.getTid());
    Date date =
        DateUtils.fromMillisUtc(
            tx.getDate()
                * timeScale); // polled order books provide a timestamp in seconds, stream in ms
    return new Trade.Builder()
        .type(orderType)
        .originalAmount(tx.getAmount())
        .currencyPair(currencyPair)
        .price(tx.getPrice())
        .timestamp(date)
        .id(tradeId)
        .build();
  }

  /**
   * Adapts a BitstampTicker to a Ticker Object
   *
   * @param bitstampTicker The exchange specific ticker
   * @param currencyPair (e.g. BTC/USD)
   * @return The ticker
   */
  public static Ticker adaptTicker(BitstampTicker bitstampTicker, CurrencyPair currencyPair) {

    BigDecimal open = bitstampTicker.getOpen();
    BigDecimal last = bitstampTicker.getLast();
    BigDecimal bid = bitstampTicker.getBid();
    BigDecimal ask = bitstampTicker.getAsk();
    BigDecimal high = bitstampTicker.getHigh();
    BigDecimal low = bitstampTicker.getLow();
    BigDecimal vwap = bitstampTicker.getVwap();
    BigDecimal volume = bitstampTicker.getVolume();
    Date timestamp = new Date(bitstampTicker.getTimestamp() * 1000L);

    return new Ticker.Builder()
        .currencyPair(currencyPair)
        .open(open)
        .last(last)
        .bid(bid)
        .ask(ask)
        .high(high)
        .low(low)
        .vwap(vwap)
        .volume(volume)
        .timestamp(timestamp)
        .build();
  }

  /**
   * Adapt the user's trades
   *
   * @param bitstampUserTransactions
   * @return
   */
  public static UserTrades adaptTradeHistory(BitstampUserTransaction[] bitstampUserTransactions) {

    List<UserTrade> trades = new ArrayList<>();
    long lastTradeId = 0;
    for (BitstampUserTransaction t : bitstampUserTransactions) {
      if (!t.getType()
          .equals(
              BitstampUserTransaction.TransactionType
                  .trade)) { // skip account deposits and withdrawals.
        continue;
      }
      final OrderType orderType;
      if (t.getCounterAmount().doubleValue() == 0.0) {
        orderType = t.getBaseAmount().doubleValue() < 0.0 ? OrderType.ASK : OrderType.BID;
      } else {
        orderType = t.getCounterAmount().doubleValue() > 0.0 ? OrderType.ASK : OrderType.BID;
      }

      long tradeId = t.getId();
      if (tradeId > lastTradeId) {
        lastTradeId = tradeId;
      }
      final CurrencyPair pair =
          new CurrencyPair(t.getBaseCurrency().toUpperCase(), t.getCounterCurrency().toUpperCase());
      UserTrade trade =
          new BitstampUserTrade.Builder()
              .type(orderType)
              .originalAmount(t.getBaseAmount().abs())
              .originalVolume(t.getCounterAmount().abs())
              .currencyPair(pair)
              .price(t.getPrice().abs())
              .timestamp(t.getDatetime())
              .id(Long.toString(tradeId))
              .orderId(Long.toString(t.getOrderId()))
              .feeAmount(t.getFee())
              .feeCurrency(Currency.getInstance(t.getFeeCurrency().toUpperCase()))
              .build();
      trades.add(trade);
    }
    return new UserTrades(trades, lastTradeId, TradeSortType.SortByID);
  }

  public static Map.Entry<String, BigDecimal> findNonzeroAmount(BitstampUserTransaction transaction)
      throws ExchangeException {
    for (Map.Entry<String, BigDecimal> entry : transaction.getAmounts().entrySet()) {
      if (entry.getValue().abs().compareTo(new BigDecimal(1e-6)) == 1) {
        return entry;
      }
    }
    throw new ExchangeException(
        "Could not find non-zero amount in transaction (id: " + transaction.getId() + ")");
  }

  public static List<FundingRecord> adaptFundingHistory(
      List<BitstampUserTransaction> userTransactions) {
    List<FundingRecord> fundingRecords = new ArrayList<>();
    for (BitstampUserTransaction trans : userTransactions) {
      if (trans.isDeposit() || trans.isWithdrawal()) {
        FundingRecord.Type type =
            trans.isDeposit() ? FundingRecord.Type.DEPOSIT : FundingRecord.Type.WITHDRAWAL;
        Map.Entry<String, BigDecimal> amount = BitstampAdapters.findNonzeroAmount(trans);
        FundingRecord record =
            new FundingRecord(
                null,
                trans.getDatetime(),
                Currency.getInstance(amount.getKey()),
                amount.getValue().abs(),
                String.valueOf(trans.getId()),
                null,
                type,
                FundingRecord.Status.COMPLETE,
                null,
                trans.getFee(),
                null);
        fundingRecords.add(record);
      }
    }
    return fundingRecords;
  }

  private static CurrencyPair adaptCurrencyPair(BitstampOrderTransaction transaction) {

    // USD section
    if (transaction.getBtc() != null && transaction.getUsd() != null) return CurrencyPair.BTC_USD;

    if (transaction.getLtc() != null && transaction.getUsd() != null) return CurrencyPair.LTC_USD;

    if (transaction.getEth() != null && transaction.getUsd() != null) return CurrencyPair.ETH_USD;

    if (transaction.getXrp() != null && transaction.getUsd() != null) return CurrencyPair.XRP_USD;

    if (transaction.getBch() != null && transaction.getUsd() != null) return CurrencyPair.BCH_USD;

    // EUR section
    if (transaction.getBtc() != null && transaction.getEur() != null) return CurrencyPair.BTC_EUR;

    if (transaction.getLtc() != null && transaction.getEur() != null) return CurrencyPair.LTC_EUR;

    if (transaction.getEth() != null && transaction.getEur() != null) return CurrencyPair.ETH_EUR;

    if (transaction.getXrp() != null && transaction.getEur() != null) return CurrencyPair.XRP_EUR;

    if (transaction.getBch() != null && transaction.getEur() != null) return CurrencyPair.BCH_EUR;

    // BTC section
    if (transaction.getLtc() != null && transaction.getBtc() != null) return CurrencyPair.LTC_BTC;

    if (transaction.getEth() != null && transaction.getBtc() != null) return CurrencyPair.ETH_BTC;

    if (transaction.getXrp() != null && transaction.getBtc() != null) return CurrencyPair.XRP_BTC;

    if (transaction.getBch() != null && transaction.getBtc() != null) return CurrencyPair.BCH_BTC;

    if (transaction.getBch() != null && transaction.getBtc() != null) return CurrencyPair.BCH_BTC;

    throw new NotYetImplementedForExchangeException();
  }

  private static BigDecimal getBaseCurrencyAmountFromBitstampTransaction(
      BitstampOrderTransaction bitstampTransaction) {

    CurrencyPair currencyPair = adaptCurrencyPair(bitstampTransaction);

    if (currencyPair.base.equals(Currency.LTC)) return bitstampTransaction.getLtc();

    if (currencyPair.base.equals(Currency.BTC)) return bitstampTransaction.getBtc();

    if (currencyPair.base.equals(Currency.BCH)) return bitstampTransaction.getBch();

    if (currencyPair.base.equals(Currency.ETH)) return bitstampTransaction.getEth();

    if (currencyPair.base.equals(Currency.XRP)) return bitstampTransaction.getXrp();

    throw new NotYetImplementedForExchangeException();
  }

  public static Order.OrderStatus adaptOrderStatus(BitstampOrderStatus bitstampOrderStatus) {

    if (bitstampOrderStatus.equals(BitstampOrderStatus.Queue)) return Order.OrderStatus.PENDING_NEW;

    if (bitstampOrderStatus.equals(BitstampOrderStatus.Finished)) return Order.OrderStatus.FILLED;

    if (bitstampOrderStatus.equals(BitstampOrderStatus.Open)) return Order.OrderStatus.NEW;

    throw new NotYetImplementedForExchangeException();
  }

  /**
   * There is no method to discern market versus limit order type - so this returns a generic
   * BitstampGenericOrder as a status
   *
   * @param bitstampOrderStatusResponse
   * @return
   */
  public static BitstampGenericOrder adaptOrder(
      String orderId, BitstampOrderStatusResponse bitstampOrderStatusResponse) {

    BitstampOrderTransaction[] bitstampTransactions = bitstampOrderStatusResponse.getTransactions();

    // Use only the first transaction, because we assume that for a single order id all transactions
    // will
    // be of the same currency pair
    CurrencyPair currencyPair = adaptCurrencyPair(bitstampTransactions[0]);
    Date date = bitstampTransactions[0].getDatetime();

    BigDecimal averagePrice =
        Arrays.stream(bitstampTransactions)
            .map(t -> t.getPrice())
            .reduce((x, y) -> x.add(y))
            .get()
            .divide(BigDecimal.valueOf(bitstampTransactions.length), 2);

    BigDecimal cumulativeAmount =
        Arrays.stream(bitstampTransactions)
            .map(t -> getBaseCurrencyAmountFromBitstampTransaction(t))
            .reduce((x, y) -> x.add(y))
            .get();

    BigDecimal totalFee =
        Arrays.stream(bitstampTransactions).map(t -> t.getFee()).reduce((x, y) -> x.add(y)).get();

    Order.OrderStatus orderStatus = adaptOrderStatus(bitstampOrderStatusResponse.getStatus());

    BitstampGenericOrder bitstampGenericOrder =
        new BitstampGenericOrder(
            null, // not discernable from response data
            null, // not discernable from the data
            currencyPair,
            orderId,
            date,
            averagePrice,
            cumulativeAmount,
            totalFee,
            orderStatus);

    return bitstampGenericOrder;
  }

  public static List<CurrencyPair> adaptCurrencyPairs(
      Collection<BitstampPairInfo> bitstampPairInfo) {

    List<CurrencyPair> currencyPairs = new ArrayList<>();
    for (BitstampPairInfo pairInfo : bitstampPairInfo) {
      String[] pairInfos = pairInfo.getName().split("/");
      currencyPairs.add(new CurrencyPair(pairInfos[0], pairInfos[1]));
    }
    return currencyPairs;
  }

  public static ExchangeMetaData adaptMetaData(
      List<BitstampPairInfo> rawSymbols, ExchangeMetaData metaData) {

    List<CurrencyPair> currencyPairs = adaptCurrencyPairs(rawSymbols);

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
}
