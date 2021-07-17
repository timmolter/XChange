package org.knowm.xchange.huobi;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.Order.OrderStatus;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.FundingRecord;
import org.knowm.xchange.dto.account.FundingRecord.Status;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trades.TradeSortType;
import org.knowm.xchange.dto.meta.*;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.StopOrder;
import org.knowm.xchange.dto.trade.StopOrder.Intention;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.huobi.dto.account.HuobiBalanceRecord;
import org.knowm.xchange.huobi.dto.account.HuobiBalanceSum;
import org.knowm.xchange.huobi.dto.account.HuobiFundingRecord;
import org.knowm.xchange.huobi.dto.marketdata.*;
import org.knowm.xchange.huobi.dto.trade.HuobiOrder;

public class HuobiAdapters {
  private static final String ONLINE = "allowed";
  private static final String DELISTED = "delisted";
  private static final String TARGET_NETWORK = "ERC20";
  private static BigDecimal fee = new BigDecimal("0.002"); // Trading fee at Huobi is 0.2 %

  public static Ticker adaptTicker(HuobiTicker huobiTicker, CurrencyPair currencyPair) {
    Ticker.Builder builder = new Ticker.Builder();
    builder.open(huobiTicker.getOpen());
    builder.ask(huobiTicker.getAsk().getPrice());
    builder.bid(huobiTicker.getBid().getPrice());
    builder.last(huobiTicker.getClose());
    builder.high(huobiTicker.getHigh());
    builder.low(huobiTicker.getLow());
    builder.volume(huobiTicker.getVol());
    builder.timestamp(huobiTicker.getTs());
    builder.currencyPair(currencyPair);
    return builder.build();
  }

  public static List<Ticker> adaptAllTickers(HuobiAllTicker[] allTickers) {

    return Arrays.stream(allTickers)
        .filter(
            huobiTicker ->
                !"hb10".equals(huobiTicker.getSymbol()) // Fix on data error retrieved from api
            )
        .map(
            huobiTicker ->
                new Ticker.Builder()
                    .currencyPair(adaptCurrencyPair(huobiTicker.getSymbol()))
                    .open(huobiTicker.getOpen())
                    .ask(huobiTicker.getAsk().getPrice())
                    .bid(huobiTicker.getBid().getPrice())
                    .askSize(huobiTicker.getAsk().getVolume())
                    .bidSize(huobiTicker.getBid().getVolume())
                    .last(huobiTicker.getClose())
                    .high(huobiTicker.getHigh())
                    .low(huobiTicker.getLow())
                    .volume(huobiTicker.getVol())
                    .timestamp(huobiTicker.getTs())
                    .build())
        .collect(Collectors.toList());
  }

  static ExchangeMetaData adaptToExchangeMetaData(
          HuobiAssetPair[] assetPairs, HuobiAsset[] assets, ExchangeMetaData staticMetaData, HuobiCurrencyWrapper[] currencyWrapper) {

    HuobiUtils.setHuobiAssets(assets);
    HuobiUtils.setHuobiAssetPairs(assetPairs);

    Map<CurrencyPair, CurrencyPairMetaData> pairsMetaData = staticMetaData.getCurrencyPairs();
    Map<CurrencyPair, CurrencyPairMetaData> pairs = new HashMap<>();
    for (HuobiAssetPair assetPair : assetPairs) {
      CurrencyPair pair = adaptCurrencyPair(assetPair.getKey());
      pairs.put(pair, adaptPair(assetPair, pairsMetaData.getOrDefault(pair, null)));
    }

    Map<Currency, CurrencyMetaData> currencies = new HashMap<>();
    for (HuobiCurrencyWrapper huobiCurrencyWrapper : currencyWrapper) {
      if(huobiCurrencyWrapper.getHuobiCurrencies().length != 0) {
        boolean isDelisted = DELISTED.equals(huobiCurrencyWrapper.getInstStatus());
        CurrencyMetaData currencyMetaData = adaptCurrencyMetaData(huobiCurrencyWrapper, isDelisted);
        Currency currency = HuobiUtils.translateHuobiCurrencyCode(huobiCurrencyWrapper.getCurrency());
        currencies.put(currency, currencyMetaData);
      }
    }

    return new ExchangeMetaData(pairs, currencies, null, null, false);
  }

  private static CurrencyMetaData adaptCurrencyMetaData(HuobiCurrencyWrapper huobiCurrencyWrapper, boolean isDelisted) {
    CurrencyMetaData result = null;
    List<HuobiCurrency> huobiCurrencies = Arrays.asList(huobiCurrencyWrapper.getHuobiCurrencies());
    if(!huobiCurrencies.isEmpty()) {
      result = getCurrencyMetaData(huobiCurrencies.get(0), isDelisted);
      for (HuobiCurrency huobiCurrency : huobiCurrencies) {
        if(TARGET_NETWORK.equals(huobiCurrency.getDisplayName())) {
          result = getCurrencyMetaData(huobiCurrency, isDelisted);
          break;
        }
      }
    }
    return result;
  }

  private static CurrencyMetaData getCurrencyMetaData(HuobiCurrency huobiCurrency, boolean isDelisted) {
    int withdrawPrecision = huobiCurrency.getWithdrawPrecision();
    BigDecimal transactFeeWithdraw = new BigDecimal(huobiCurrency.getTransactFeeWithdraw());
    BigDecimal minWithdrawAmt = new BigDecimal(huobiCurrency.getMinWithdrawAmt());
    WalletHealth walletHealthStatus = isDelisted ? WalletHealth.OFFLINE : getWalletHealthStatus(huobiCurrency);
    return new CurrencyMetaData(withdrawPrecision, transactFeeWithdraw, minWithdrawAmt, walletHealthStatus);
  }

  private static WalletHealth getWalletHealthStatus(HuobiCurrency huobiCurrency) {
    WalletHealth walletHealth = WalletHealth.ONLINE;
    if(!ONLINE.equals(huobiCurrency.getDepositStatus()) && !ONLINE.equals(huobiCurrency.getWithdrawStatus())) {
      walletHealth = WalletHealth.OFFLINE;
    }else if(!ONLINE.equals(huobiCurrency.getDepositStatus())) {
      walletHealth = WalletHealth.DEPOSITS_DISABLED;
    }else if(!ONLINE.equals(huobiCurrency.getWithdrawStatus())) {
      walletHealth = WalletHealth.WITHDRAWALS_DISABLED;
    }
    return walletHealth;
  }

  private static CurrencyPair adaptCurrencyPair(String currencyPair) {
    return HuobiUtils.translateHuobiCurrencyPair(currencyPair);
  }

  private static CurrencyPairMetaData adaptPair(
      HuobiAssetPair pair, CurrencyPairMetaData metadata) {
    BigDecimal minQty =
        metadata == null
            ? null
            : metadata.getMinimumAmount().setScale(pair.getAmountPrecision(), RoundingMode.DOWN);
    FeeTier[] feeTiers = metadata == null ? null : metadata.getFeeTiers();
    return new CurrencyPairMetaData(
        fee,
        minQty,
        null,
        null,
        null,
        new Integer(pair.getAmountPrecision()),
        new Integer(pair.getPricePrecision()),
        null,
        feeTiers,
        null,
        null,
        true);
  }

  private static Currency adaptCurrency(String currency) {
    return HuobiUtils.translateHuobiCurrencyCode(currency);
  }

  public static Wallet adaptWallet(Map<String, HuobiBalanceSum> huobiWallet) {
    List<Balance> balances = new ArrayList<>(huobiWallet.size());
    for (Map.Entry<String, HuobiBalanceSum> record : huobiWallet.entrySet()) {
      try {
        Currency currency = adaptCurrency(record.getKey());
        if (currency == null) {
          // Avoid creating Balance objects with null currency.
          continue;
        }
        Balance balance =
            new Balance(
                currency,
                record.getValue().getTotal(),
                record.getValue().getAvailable(),
                record.getValue().getFrozen());
        balances.add(balance);
      } catch (ExchangeException e) {
        // It might be a new currency. Ignore the exception and continue with other currency.
      }
    }
    return Wallet.Builder.from(balances).build();
  }

  public static Map<String, HuobiBalanceSum> adaptBalance(HuobiBalanceRecord[] huobiBalance) {
    Map<String, HuobiBalanceSum> map = new HashMap<>();
    for (HuobiBalanceRecord record : huobiBalance) {
      HuobiBalanceSum sum = map.get(record.getCurrency());
      if (sum == null) {
        sum = new HuobiBalanceSum();
        map.put(record.getCurrency(), sum);
      }
      if (record.getType().equals("trade")) {
        sum.setAvailable(record.getBalance());
      } else if (record.getType().equals("frozen")) {
        sum.setFrozen(record.getBalance());
      }
    }
    return map;
  }

  public static OpenOrders adaptOpenOrders(HuobiOrder[] openOrders) {
    List<LimitOrder> limitOrders = new ArrayList<>();
    for (HuobiOrder openOrder : openOrders) {
      if (openOrder.isLimit()) {
        LimitOrder order = (LimitOrder) adaptOrder(openOrder);
        limitOrders.add(order);
      }
    }
    return new OpenOrders(limitOrders);
  }

  private static Order adaptOrder(HuobiOrder openOrder) {
    Order order = null;
    OrderType orderType = adaptOrderType(openOrder.getType());
    CurrencyPair currencyPair = adaptCurrencyPair(openOrder.getSymbol());
    BigDecimal openOrderAvgPrice;
    if (openOrder.getFieldAmount().compareTo(BigDecimal.ZERO) == 0) {
      openOrderAvgPrice = BigDecimal.ZERO;
    } else {
      openOrderAvgPrice =
          openOrder
              .getFieldCashAmount()
              .divide(openOrder.getFieldAmount(), 8, BigDecimal.ROUND_DOWN);
    }
    if (openOrder.isMarket()) {
      order =
          new MarketOrder(
              orderType,
              openOrder.getAmount(),
              currencyPair,
              String.valueOf(openOrder.getId()),
              openOrder.getCreatedAt(),
              openOrderAvgPrice,
              openOrder.getFieldAmount(),
              openOrder.getFieldFees(),
              adaptOrderStatus(openOrder.getState()),
              openOrder.getClOrdId());
    }
    if (openOrder.isLimit()) {
      order =
          new LimitOrder(
              orderType,
              openOrder.getAmount(),
              currencyPair,
              String.valueOf(openOrder.getId()),
              openOrder.getCreatedAt(),
              openOrder.getPrice(),
              openOrderAvgPrice,
              openOrder.getFieldAmount(),
              openOrder.getFieldFees(),
              adaptOrderStatus(openOrder.getState()),
              openOrder.getClOrdId());
    }
    if (openOrder.isStop()) {
      order =
          new StopOrder(
              orderType,
              openOrder.getAmount(),
              currencyPair,
              String.valueOf(openOrder.getId()),
              openOrder.getCreatedAt(),
              openOrder.getStopPrice(),
              openOrder.getPrice(),
              openOrderAvgPrice,
              openOrder.getFieldAmount(),
              openOrder.getFieldFees(),
              adaptOrderStatus(openOrder.getState()),
              openOrder.getClOrdId(),
              openOrder.getOperator().equals("lte") ? Intention.STOP_LOSS : Intention.TAKE_PROFIT);
    }

    order.setAveragePrice(openOrderAvgPrice);
    return order;
  }

  /**
   * Huobi currently doesn't have trade history API. We simulate it by using the orders history.
   *
   * @param order
   * @return
   */
  private static UserTrade adaptTrade(LimitOrder order) {
    BigDecimal feeAmount =
        order
            .getCumulativeAmount()
            .multiply(order.getLimitPrice())
            .multiply(fee)
            .setScale(8, RoundingMode.DOWN);
    return new UserTrade.Builder()
        .type(order.getType())
        .originalAmount(order.getCumulativeAmount())
        .currencyPair(order.getCurrencyPair())
        .price(order.getLimitPrice())
        .timestamp(order.getTimestamp())
        .id("") // Trade id
        .orderId(order.getId()) // Original order id
        .feeAmount(feeAmount)
        .feeCurrency(order.getCurrencyPair().counter)
        .build();
  }

  private static OrderStatus adaptOrderStatus(String huobiStatus) {
    OrderStatus result = OrderStatus.UNKNOWN;
    switch (huobiStatus) {
      case "pre-submitted":
        result = OrderStatus.PENDING_NEW;
        break;
      case "submitting":
        result = OrderStatus.PENDING_NEW;
        break;
      case "submitted":
        result = OrderStatus.NEW;
        break;
      case "partial-filled":
        result = OrderStatus.PARTIALLY_FILLED;
        break;
      case "partial-canceled":
        result = OrderStatus.PARTIALLY_CANCELED;
        break;
      case "filled":
        result = OrderStatus.FILLED;
        break;
      case "canceled":
        result = OrderStatus.CANCELED;
        break;
    }
    return result;
  }

  public static OrderType adaptOrderType(String orderType) {
    if (orderType.startsWith("buy")) {
      return OrderType.BID;
    }
    if (orderType.startsWith("sell")) {
      return OrderType.ASK;
    }
    return null;
  }

  public static List<Order> adaptOrders(List<HuobiOrder> huobiOrders) {
    List<Order> orders = new ArrayList<>();
    for (HuobiOrder order : huobiOrders) {
      orders.add(adaptOrder(order));
    }
    return orders;
  }

  public static UserTrades adaptTradeHistory(HuobiOrder[] openOrders) {
    OpenOrders orders = adaptOpenOrders(openOrders);
    List<UserTrade> trades = new ArrayList<>();
    for (LimitOrder order : orders.getOpenOrders()) {
      trades.add(adaptTrade(order));
    }
    return new UserTrades(trades, TradeSortType.SortByTimestamp);
  }

  public static List<FundingRecord> adaptFundingHistory(HuobiFundingRecord[] fundingRecords) {
    List<FundingRecord> records = new ArrayList<>();
    for (HuobiFundingRecord record : fundingRecords) {
      records.add(adaptFundingRecord(record));
    }
    return records;
  }

  public static FundingRecord adaptFundingRecord(HuobiFundingRecord r) {

    return new FundingRecord(
        r.getAddress(),
        r.getCreatedAt(),
        Currency.getInstance(r.getCurrency()),
        r.getAmount(),
        Long.toString(r.getId()),
        r.getTxhash(),
        r.getType(),
        adaptFundingStatus(r),
        null,
        r.getFee(),
        null);
  }

  private static Status adaptFundingStatus(HuobiFundingRecord record) {
    if (record.getType() == FundingRecord.Type.WITHDRAWAL) {
      return adaptWithdrawalStatus(record.getState());
    }
    return adaptDepostStatus(record.getState());
  }

  private static Status adaptWithdrawalStatus(String state) {
    switch (state) {
      case "pre-transfer":
      case "submitted":
      case "reexamine":
      case "pass":
      case "wallet-transfer":
        return Status.PROCESSING;
      case "canceled":
        return Status.CANCELLED;
      case "confirmed":
        return Status.COMPLETE;
      case "wallet-reject":
      case "reject	":
      case "confirm-error":
        return Status.FAILED;
      case "repealed":

      default:
        return null;
    }
  }

  /**
   * List of possible deposit state
   *
   * <p>State Description unknown On-chain transfer has not been received confirming On-chain
   * transfer waits for first confirmation confirmed On-chain transfer confirmed for at least one
   * block safe Multiple on-chain confirmation happened orphan Confirmed but currently in an orphan
   * branch
   */
  private static Status adaptDepostStatus(String state) {
    switch (state) {
      case "confirming":
      case "confirmed":
        return Status.PROCESSING;
      case "safe":
        return Status.COMPLETE;
      case "unknown":
      case "orphan":
        return Status.FAILED;
      default:
        return null;
    }
  }
}
