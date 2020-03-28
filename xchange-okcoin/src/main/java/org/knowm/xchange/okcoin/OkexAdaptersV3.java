package org.knowm.xchange.okcoin;

import java.util.ArrayList;
import java.util.List;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderStatus;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.FundingRecord;
import org.knowm.xchange.dto.account.FundingRecord.Status;
import org.knowm.xchange.dto.account.FundingRecord.Type;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.marketdata.Trades.TradeSortType;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.instrument.FuturesContract;
import org.knowm.xchange.okcoin.v3.dto.account.OkexDepositRecord;
import org.knowm.xchange.okcoin.v3.dto.account.OkexFundingAccountRecord;
import org.knowm.xchange.okcoin.v3.dto.account.OkexSpotAccountRecord;
import org.knowm.xchange.okcoin.v3.dto.account.OkexWithdrawalRecord;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexFuturesTrade;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexSpotTicker;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexTrade;
import org.knowm.xchange.okcoin.v3.dto.trade.FuturesAccountsResponse.FuturesAccount;
import org.knowm.xchange.okcoin.v3.dto.trade.FuturesSwapType;
import org.knowm.xchange.okcoin.v3.dto.trade.OkexFuturesOpenOrder;
import org.knowm.xchange.okcoin.v3.dto.trade.OkexOpenOrder;
import org.knowm.xchange.okcoin.v3.dto.trade.Side;
import org.knowm.xchange.okcoin.v3.dto.trade.SwapAccountsResponse.SwapAccountInfo;

public class OkexAdaptersV3 {

  public static Balance convert(OkexFundingAccountRecord rec) {
    return new Balance.Builder()
        .currency(Currency.getInstance(rec.getCurrency()))
        .available(rec.getAvailable())
        .frozen(rec.getBalance().subtract(rec.getAvailable()))
        .total(rec.getBalance())
        .build();
  }

  public static Balance convert(OkexSpotAccountRecord rec) {
    return new Balance.Builder()
        .currency(Currency.getInstance(rec.getCurrency()))
        .available(rec.getAvailable())
        .frozen(rec.getBalance().subtract(rec.getAvailable()))
        .total(rec.getBalance())
        .build();
  }

  public static Balance convert(String currency, FuturesAccount acc) {
    return new Balance.Builder()
        .currency(Currency.getInstance(currency.toUpperCase()))
        .total(acc.getEquity())
        .build();
  }

  public static Balance convert(SwapAccountInfo rec) {
    return new Balance.Builder()
        .currency(toPair(rec.getInstrumentId()).base)
        .total(rec.getEquity())
        .build();
  }

  public static String toSpotInstrument(CurrencyPair pair) {
    return pair == null ? null : pair.base.getCurrencyCode() + "-" + pair.counter.getCurrencyCode();
  }

  public static String toFuturesInstrument(FuturesContract futuresContract) {
    return futuresContract == null
        ? null
        : futuresContract.underlier.base.getCurrencyCode()
            + "-"
            + futuresContract.underlier.counter.getCurrencyCode()
            + "-"
            + futuresContract.prompt;
  }

  /**
   * there are different types of instruments: spot (ie 'ETH-BTC'), future (ie 'BCH-USD-190927'),
   * swap (ie 'ETH-USD-SWAP')
   *
   * @param instrument
   * @return
   */
  public static CurrencyPair toPair(String instrument) {
    String[] split = instrument.split("-");
    if (split == null || split.length < 2) {
      throw new ExchangeException("Not supported instrument: " + instrument);
    }
    return new CurrencyPair(split[0], split[1]);
  }

  /**
   * there are different types of instruments: spot (ie 'ETH-BTC'), future (ie 'BCH-USD-190927'),
   * swap (ie 'ETH-USD-SWAP')
   *
   * @param instrument
   * @return
   */
  public static FuturesContract toFutures(String instrument) {
    String[] split = instrument.split("-");
    if (split == null || split.length < 3) {
      throw new ExchangeException("Not supported instrument: " + instrument);
    }
    return new FuturesContract(new CurrencyPair(split[0], split[1]), split[2]);
  }

  public static Ticker convert(OkexSpotTicker i) {
    return new Ticker.Builder()
        .currencyPair(toPair(i.getInstrumentId()))
        .last(i.getLast())
        .bid(i.getBid())
        .ask(i.getAsk())
        .volume(i.getBaseVolume24h())
        .quoteVolume(i.getQuoteVolume24h())
        .timestamp(i.getTimestamp())
        .build();
  }

  public static LimitOrder convert(OkexOpenOrder o) {
    return new LimitOrder.Builder(
            o.getSide() == Side.sell ? OrderType.ASK : OrderType.BID, toPair(o.getInstrumentId()))
        .id(o.getOrderId())
        .limitPrice(o.getPrice())
        .originalAmount(o.getSize())
        .orderStatus(adaptOrderState(o.getState()))
        .timestamp(o.getCreatedAt())
        .build();
  }

  public static LimitOrder convert(OkexFuturesOpenOrder o) {
    return new LimitOrder.Builder(
            adaptFuturesOrderType(o.getType()), toFutures(o.getInstrumentId()))
        .id(o.getOrderId())
        .limitPrice(o.getPrice())
        .averagePrice(o.getPriceAvg())
        .originalAmount(o.getSize())
        .timestamp(o.getTimestamp())
        .orderStatus(adaptOrderState(o.getState()))
        .cumulativeAmount(o.getFilledQty())
        .fee(o.getFee())
        .build();
  }

  public static FundingRecord adaptFundingRecord(OkexWithdrawalRecord r) {
    return new FundingRecord.Builder()
        .setAddress(r.getTo())
        .setAmount(r.getAmount())
        .setCurrency(Currency.getInstance(r.getCurrency()))
        .setDate(r.getTimestamp())
        .setInternalId(r.getWithdrawalId())
        .setStatus(convertWithdrawalStatus(r.getStatus()))
        .setBlockchainTransactionHash(r.getTxid())
        .setType(Type.WITHDRAWAL)
        .build();
  }

  /**
   * -3:pending cancel; -2: cancelled; -1: failed; 0 :pending; 1 :sending; 2:sent; 3 :email
   * confirmation; 4 :manual confirmation; 5:awaiting identity confirmation
   */
  private static Status convertWithdrawalStatus(String status) {
    switch (status) {
      case "-3":
      case "-2":
        return Status.CANCELLED;
      case "-1":
        return Status.FAILED;
      case "0":
      case "1":
      case "3":
      case "4":
      case "5":
        return Status.PROCESSING;
      case "2":
        return Status.COMPLETE;
      default:
        throw new ExchangeException("Unknown withdrawal status: " + status);
    }
  }

  public static FundingRecord adaptFundingRecord(OkexDepositRecord r) {
    return new FundingRecord.Builder()
        .setAddress(r.getTo())
        .setAmount(r.getAmount())
        .setCurrency(Currency.getInstance(r.getCurrency()))
        .setDate(r.getTimestamp())
        .setStatus(convertDepositStatus(r.getStatus()))
        .setBlockchainTransactionHash(r.getTxid())
        .setType(Type.DEPOSIT)
        .build();
  }
  /**
   * The status of deposits (0: waiting for confirmation; 1: confirmation account; 2: recharge
   * success);
   */
  private static Status convertDepositStatus(String status) {
    switch (status) {
      case "0":
      case "1":
      case "6":
        return Status.PROCESSING;
      case "2":
        return Status.COMPLETE;
      default:
        throw new ExchangeException("Unknown deposit status: " + status);
    }
  }

  public static Trades adaptTrades(OkexTrade[] trades, CurrencyPair currencyPair) {

    List<Trade> tradeList = new ArrayList<>(trades.length);
    for (OkexTrade trade : trades) {
      tradeList.add(adaptTrade(trade, currencyPair));
    }
    long lastTid = trades.length > 0 ? (trades[trades.length - 1].getTradeId()) : 0L;
    return new Trades(tradeList, lastTid, TradeSortType.SortByTimestamp);
  }

  public static Trades adaptFuturesTrades(
      OkexFuturesTrade[] trades, FuturesContract futuresContract) {

    List<Trade> tradeList = new ArrayList<>(trades.length);
    for (OkexFuturesTrade trade : trades) {
      tradeList.add(adaptFuturesTrade(trade, futuresContract));
    }
    long lastTid = trades.length > 0 ? (trades[trades.length - 1].getTradeId()) : 0L;
    return new Trades(tradeList, lastTid, TradeSortType.SortByTimestamp);
  }

  private static Trade adaptFuturesTrade(OkexFuturesTrade trade, FuturesContract futuresContract) {

    return new Trade.Builder()
        .type(trade.getSide().equals("buy") ? OrderType.BID : OrderType.ASK)
        .originalAmount(trade.getQty())
        .currencyPair(futuresContract.underlier)
        .price(trade.getPrice())
        .timestamp(trade.getDate())
        .id("" + trade.getTradeId())
        .build();
  }

  private static Trade adaptTrade(OkexTrade trade, CurrencyPair currencyPair) {

    return new Trade.Builder()
        .type(trade.getSide().equals("buy") ? OrderType.BID : OrderType.ASK)
        .originalAmount(trade.getSize())
        .currencyPair(currencyPair)
        .price(trade.getPrice())
        .timestamp(trade.getDate())
        .id("" + trade.getTradeId())
        .build();
  }

  public static FuturesSwapType adaptFuturesSwapType(OrderType orderType) {

    switch (orderType) {
      case BID:
        return FuturesSwapType.open_long;
      case ASK:
        return FuturesSwapType.open_short;
      case EXIT_ASK:
        return FuturesSwapType.close_short;
      case EXIT_BID:
        return FuturesSwapType.close_long;
      default:
        return null;
    }
  }

  public static OrderType adaptFuturesOrderType(FuturesSwapType orderType) {

    switch (orderType) {
      case open_long:
        return OrderType.BID;
      case open_short:
        return OrderType.ASK;
      case close_short:
        return OrderType.EXIT_ASK;
      case close_long:
        return OrderType.EXIT_BID;
      default:
        return null;
    }
  }

  public static OrderStatus adaptOrderState(String orderState) {
    switch (orderState) {
      case "-2":
        return OrderStatus.REJECTED;
      case "-1":
        return OrderStatus.CANCELED;
      case "0":
        return OrderStatus.NEW;
      case "1":
        return OrderStatus.PARTIALLY_FILLED;
      case "2":
        return OrderStatus.FILLED;
      case "3":
        return OrderStatus.PENDING_NEW;
      case "4":
        return OrderStatus.PENDING_CANCEL;
      default:
        return null;
    }
  }
}
