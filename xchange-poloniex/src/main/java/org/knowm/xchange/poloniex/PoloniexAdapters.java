package org.knowm.xchange.poloniex;

import static org.knowm.xchange.dto.account.FundingRecord.Type.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.LoanOrder;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.FundingRecord;
import org.knowm.xchange.dto.account.FundingRecord.Type;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.marketdata.Trades.TradeSortType;
import org.knowm.xchange.dto.meta.CurrencyMetaData;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.dto.trade.FixedRateLoanOrder;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.poloniex.dto.LoanInfo;
import org.knowm.xchange.poloniex.dto.account.PoloniexBalance;
import org.knowm.xchange.poloniex.dto.account.PoloniexLoan;
import org.knowm.xchange.poloniex.dto.marketdata.PoloniexCurrencyInfo;
import org.knowm.xchange.poloniex.dto.marketdata.PoloniexDepth;
import org.knowm.xchange.poloniex.dto.marketdata.PoloniexMarketData;
import org.knowm.xchange.poloniex.dto.marketdata.PoloniexPublicTrade;
import org.knowm.xchange.poloniex.dto.marketdata.PoloniexTicker;
import org.knowm.xchange.poloniex.dto.trade.*;

/**
 * @author Zach Holmes
 * @author Dave Seyb
 * @version 2.0 *
 */
public class PoloniexAdapters {

  public static Ticker adaptPoloniexTicker(
      PoloniexTicker poloniexTicker, CurrencyPair currencyPair) {
    PoloniexMarketData marketData = poloniexTicker.getPoloniexMarketData();
    return adaptPoloniexTicker(marketData, currencyPair);
  }

  public static Ticker adaptPoloniexTicker(
      PoloniexMarketData marketData, CurrencyPair currencyPair) {
    BigDecimal last = marketData.getLast();
    BigDecimal bid = marketData.getHighestBid();
    BigDecimal ask = marketData.getLowestAsk();
    BigDecimal high = marketData.getHigh24hr();
    BigDecimal low = marketData.getLow24hr();
    BigDecimal volume = marketData.getQuoteVolume();

    return new Ticker.Builder()
        .currencyPair(currencyPair)
        .last(last)
        .bid(bid)
        .ask(ask)
        .high(high)
        .low(low)
        .volume(volume)
        .build();
  }

  public static OrderBook adaptPoloniexDepth(PoloniexDepth depth, CurrencyPair currencyPair) {

    List<LimitOrder> asks = adaptPoloniexPublicOrders(depth.getAsks(), OrderType.ASK, currencyPair);
    List<LimitOrder> bids = adaptPoloniexPublicOrders(depth.getBids(), OrderType.BID, currencyPair);

    return new OrderBook(null, asks, bids);
  }

  public static List<LimitOrder> adaptPoloniexPublicOrders(
      List<List<BigDecimal>> rawLevels, OrderType orderType, CurrencyPair currencyPair) {
    List<LimitOrder> orders = new ArrayList<>();

    for (List<BigDecimal> rawlevel : rawLevels) {
      LimitOrder limitOrder =
          new LimitOrder.Builder(orderType, currencyPair)
              .originalAmount(rawlevel.get(1))
              .limitPrice(rawlevel.get(0))
              .build();
      orders.add(limitOrder);
    }
    return orders;
  }

  public static Trades adaptPoloniexPublicTrades(
      PoloniexPublicTrade[] poloniexPublicTrades, CurrencyPair currencyPair) {

    List<Trade> trades = new ArrayList<>();

    for (PoloniexPublicTrade poloniexTrade : poloniexPublicTrades) {
      trades.add(adaptPoloniexPublicTrade(poloniexTrade, currencyPair));
    }

    return new Trades(trades, TradeSortType.SortByTimestamp);
  }

  public static Trade adaptPoloniexPublicTrade(
      PoloniexPublicTrade poloniexTrade, CurrencyPair currencyPair) {

    OrderType type =
        poloniexTrade.getType().equalsIgnoreCase("buy") ? OrderType.BID : OrderType.ASK;
    Date timestamp = PoloniexUtils.stringToDate(poloniexTrade.getDate());

    return new Trade.Builder()
        .type(type)
        .originalAmount(poloniexTrade.getAmount())
        .currencyPair(currencyPair)
        .price(poloniexTrade.getRate())
        .timestamp(timestamp)
        .id(poloniexTrade.getTradeID())
        .build();
  }

  public static List<Balance> adaptPoloniexBalances(
      HashMap<String, PoloniexBalance> poloniexBalances) {

    List<Balance> balances = new ArrayList<>();

    for (Map.Entry<String, PoloniexBalance> item : poloniexBalances.entrySet()) {

      Currency currency = Currency.getInstance(item.getKey());
      balances.add(
          new Balance(
              currency, null, item.getValue().getAvailable(), item.getValue().getOnOrders()));
    }

    return balances;
  }

  public static LoanInfo adaptPoloniexLoans(HashMap<String, PoloniexLoan[]> poloniexLoans) {

    Map<String, List<LoanOrder>> loans = new HashMap<>();

    for (Map.Entry<String, PoloniexLoan[]> item : poloniexLoans.entrySet()) {
      List<LoanOrder> loanOrders = new ArrayList<>();
      for (PoloniexLoan poloniexLoan : item.getValue()) {
        Date date = PoloniexUtils.stringToDate(poloniexLoan.getDate());
        loanOrders.add(
            new FixedRateLoanOrder(
                OrderType.ASK,
                poloniexLoan.getCurrency(),
                poloniexLoan.getAmount(),
                poloniexLoan.getRange(),
                poloniexLoan.getId(),
                date,
                poloniexLoan.getRate())); // TODO
      }
      loans.put(item.getKey(), loanOrders);
    }

    return new LoanInfo(loans.get("provided"), loans.get("used"));
  }

  public static OpenOrders adaptPoloniexOpenOrders(
      Map<String, PoloniexOpenOrder[]> poloniexOpenOrders) {

    List<LimitOrder> openOrders = new ArrayList<>();
    for (String pairString : poloniexOpenOrders.keySet()) {
      CurrencyPair currencyPair = PoloniexUtils.toCurrencyPair(pairString);

      for (PoloniexOpenOrder openOrder : poloniexOpenOrders.get(pairString)) {

        openOrders.add(adaptPoloniexOpenOrder(openOrder, currencyPair));
      }
    }

    return new OpenOrders(openOrders);
  }

  public static LimitOrder adaptPoloniexOpenOrder(
      PoloniexOpenOrder openOrder, CurrencyPair currencyPair) {

    OrderType type = openOrder.getType().equals("buy") ? OrderType.BID : OrderType.ASK;
    Date timestamp = PoloniexUtils.stringToDate(openOrder.getDate());

    return new LimitOrder.Builder(type, currencyPair)
        .limitPrice(openOrder.getRate())
        .originalAmount(openOrder.getStartingAmount())
        .cumulativeAmount(openOrder.getStartingAmount().subtract(openOrder.getAmount()))
        .id(openOrder.getOrderNumber())
        .timestamp(timestamp)
        .build();
  }

  public static UserTrade adaptPoloniexUserTrade(
      PoloniexTrade poloniexTrade, CurrencyPair currencyPair) {

    OrderType orderType =
        poloniexTrade.getType().equalsIgnoreCase("buy") ? OrderType.BID : OrderType.ASK;
    BigDecimal amount = poloniexTrade.getAmount();
    BigDecimal price = poloniexTrade.getRate();
    Date date = PoloniexUtils.stringToDate(poloniexTrade.getDate());
    String tradeId = String.valueOf(poloniexTrade.getTradeID());
    String orderId = String.valueOf(poloniexTrade.getOrderNumber());

    // Poloniex returns fee as a multiplier, e.g. a 0.2% fee is 0.002
    // fee currency/size depends on trade direction (buy/sell). It appears to be rounded down
    final BigDecimal feeAmount;
    final String feeCurrencyCode;
    if (orderType == OrderType.ASK) {
      feeAmount =
          amount.multiply(price).multiply(poloniexTrade.getFee()).setScale(8, RoundingMode.DOWN);
      feeCurrencyCode = currencyPair.counter.getCurrencyCode();
    } else {
      feeAmount = amount.multiply(poloniexTrade.getFee()).setScale(8, RoundingMode.DOWN);
      feeCurrencyCode = currencyPair.base.getCurrencyCode();
    }

    return new PoloniexUserTrade.Builder()
        .type(orderType)
        .originalAmount(amount)
        .originalVolume(poloniexTrade.getTotal())
        .currencyPair(currencyPair)
        .price(price)
        .timestamp(date)
        .id(tradeId)
        .orderId(orderId)
        .feeAmount(feeAmount)
        .feeCurrency(Currency.getInstance(feeCurrencyCode))
        .build();
  }

  public static ExchangeMetaData adaptToExchangeMetaData(
      Map<String, PoloniexCurrencyInfo> poloniexCurrencyInfo,
      Map<String, PoloniexMarketData> poloniexMarketData,
      ExchangeMetaData exchangeMetaData) {

    Map<Currency, CurrencyMetaData> currencyMetaDataMap = exchangeMetaData.getCurrencies();
    CurrencyMetaData currencyArchetype = currencyMetaDataMap.values().iterator().next();

    for (Map.Entry<String, PoloniexCurrencyInfo> entry : poloniexCurrencyInfo.entrySet()) {

      Currency ccy = Currency.getInstance(entry.getKey());

      if (!currencyMetaDataMap.containsKey(ccy)) currencyMetaDataMap.put(ccy, currencyArchetype);
    }

    Map<CurrencyPair, CurrencyPairMetaData> marketMetaDataMap = exchangeMetaData.getCurrencyPairs();
    CurrencyPairMetaData marketArchetype = marketMetaDataMap.values().iterator().next();

    for (String market : poloniexMarketData.keySet()) {
      CurrencyPair currencyPair = PoloniexUtils.toCurrencyPair(market);

      if (!marketMetaDataMap.containsKey(currencyPair))
        marketMetaDataMap.put(currencyPair, marketArchetype);
    }

    return exchangeMetaData;
  }

  public static List<FundingRecord> adaptFundingRecords(
      PoloniexDepositsWithdrawalsResponse poloFundings) {

    final ArrayList<FundingRecord> fundingRecords = new ArrayList<>();
    for (PoloniexAdjustment a : poloFundings.getAdjustments()) {
      fundingRecords.add(adaptAdjustment(a));
    }
    for (PoloniexDeposit d : poloFundings.getDeposits()) {
      fundingRecords.add(adaptDeposit(d));
    }
    for (PoloniexWithdrawal w : poloFundings.getWithdrawals()) {
      fundingRecords.add(adaptWithdrawal(w));
    }
    return fundingRecords;
  }

  private static FundingRecord adaptAdjustment(PoloniexAdjustment a) {
    FundingRecord.Type type = OTHER_INFLOW;
    // There seems to be a spelling error in the returning reason. In case that ever gets
    // corrected, this will still pick it up.
    if (a.getReason().toLowerCase().contains("aidrop")
        || a.getReason().toLowerCase().contains("airdrop")) {
      type = Type.AIRDROP;
    }
    // There could be other forms of adjustements, but it seems to be some kind of deposit.

    return new FundingRecord(
        null,
        a.getTimestamp(),
        Currency.getInstance(a.getCurrency()),
        a.getAmount(),
        null,
        null,
        type,
        FundingRecord.Status.resolveStatus(a.getStatus()),
        null,
        null,
        a.getCategory()
            + ":"
            + a.getReason()
            + "\n"
            + a.getAdjustmentTitle()
            + "\n"
            + a.getAdjustmentDesc()
            + "\n"
            + a.getAdjustmentHelp());
  }

  private static FundingRecord adaptDeposit(final PoloniexDeposit d) {
    return new FundingRecord(
        d.getAddress(),
        d.getTimestamp(),
        Currency.getInstance(d.getCurrency()),
        d.getAmount(),
        String.valueOf(d.getDepositNumber()),
        d.getTxid(),
        DEPOSIT,
        FundingRecord.Status.resolveStatus(d.getStatus()),
        null,
        null,
        d.getStatus());
  }

  private static FundingRecord adaptWithdrawal(final PoloniexWithdrawal w) {
    final String[] statusParts = w.getStatus().split(": *");
    final String statusStr = statusParts[0];
    final FundingRecord.Status status = FundingRecord.Status.resolveStatus(statusStr);
    final String externalId = statusParts.length == 1 ? null : statusParts[1];

    // Poloniex returns the fee as an absolute value, that behaviour differs from UserTrades
    final BigDecimal feeAmount = w.getFee();

    return new FundingRecord(
        w.getAddress(),
        w.getTimestamp(),
        Currency.getInstance(w.getCurrency()),
        w.getAmount(),
        String.valueOf(w.getWithdrawalNumber()),
        externalId,
        WITHDRAWAL,
        status,
        null,
        feeAmount,
        w.getStatus());
  }

  public static LimitOrder adaptUserTradesToOrderStatus(
      String orderId, PoloniexTrade[] poloniexTrades) {

    if (poloniexTrades.length == 0) return null;

    OrderType orderType = null;
    CurrencyPair currencyPair = null;
    BigDecimal amount = new BigDecimal(0);

    List<BigDecimal> weightedPrices = new ArrayList<>();

    for (PoloniexTrade poloniexTrade : poloniexTrades) {
      orderType =
          poloniexTrade.getType().equals("buy")
              ? OrderType.BID
              : OrderType.ASK; // what about others?
      amount = amount.add(poloniexTrade.getAmount());
      weightedPrices.add(poloniexTrade.getRate().multiply(poloniexTrade.getAmount()));
    }

    BigDecimal weightedAveragePrice =
        weightedPrices.stream()
            .reduce(new BigDecimal(0), BigDecimal::add)
            .divide(amount, RoundingMode.HALF_UP);

    return new LimitOrder(
        orderType,
        null,
        currencyPair,
        orderId,
        null,
        null,
        weightedAveragePrice,
        amount,
        null,
        Order.OrderStatus.UNKNOWN);
  }
}
