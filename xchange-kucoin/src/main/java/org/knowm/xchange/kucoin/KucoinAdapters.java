package org.knowm.xchange.kucoin;

import static java.util.stream.Collectors.toCollection;
import static org.knowm.xchange.dto.Order.OrderStatus.*;
import static org.knowm.xchange.dto.Order.OrderType.ASK;
import static org.knowm.xchange.dto.Order.OrderType.BID;
import static org.knowm.xchange.kucoin.dto.KucoinOrderFlags.*;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Ordering;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.Order.IOrderFlags;
import org.knowm.xchange.dto.Order.OrderStatus;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.FundingRecord;
import org.knowm.xchange.dto.account.FundingRecord.Status;
import org.knowm.xchange.dto.account.FundingRecord.Type;
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
import org.knowm.xchange.dto.trade.StopOrder;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.kucoin.KucoinTradeService.KucoinOrderFlags;
import org.knowm.xchange.kucoin.dto.request.OrderCreateApiRequest;
import org.knowm.xchange.kucoin.dto.response.*;

public class KucoinAdapters {

  private static final String TAKER_FEE_RATE = "takerFeeRate";

  public static String adaptCurrencyPair(CurrencyPair pair) {
    return pair == null ? null : pair.base.getCurrencyCode() + "-" + pair.counter.getCurrencyCode();
  }

  public static CurrencyPair adaptCurrencyPair(String symbol) {
    String[] split = symbol.split("-");
    if (split.length != 2) {
      throw new ExchangeException("Invalid kucoin symbol: " + symbol);
    }
    return new CurrencyPair(split[0], split[1]);
  }

  public static Ticker.Builder adaptTickerFull(CurrencyPair pair, SymbolTickResponse stats) {
    return new Ticker.Builder()
        .currencyPair(pair)
        .bid(stats.getBuy())
        .ask(stats.getSell())
        .last(stats.getLast())
        .high(stats.getHigh())
        .low(stats.getLow())
        .volume(stats.getVol())
        .quoteVolume(stats.getVolValue())
        .open(stats.getOpen())
        .timestamp(new Date(stats.getTime()));
  }

  public static List<Ticker> adaptAllTickers(AllTickersResponse allTickersResponse) {
    return Arrays.stream(allTickersResponse.getTicker())
        .map(
            ticker ->
                new Ticker.Builder()
                    .currencyPair(adaptCurrencyPair(ticker.getSymbol()))
                    .bid(ticker.getBuy())
                    .ask(ticker.getSell())
                    .last(ticker.getLast())
                    .high(ticker.getHigh())
                    .low(ticker.getLow())
                    .volume(ticker.getVol())
                    .quoteVolume(ticker.getVolValue())
                    .percentageChange(
                        ticker.getChangeRate().multiply(new BigDecimal("100"), new MathContext(8)))
                    .build())
        .collect(Collectors.toList());
  }

  /**
   * Imperfect implementation. Kucoin appears to enforce a base <strong>and</strong> quote min
   * <strong>and max</strong> amount that the XChange API current doesn't take account of.
   *
   * @param exchangeMetaData The static exchange metadata.
   * @param currenciesResponse Kucoin currencies
   * @param symbolsResponse Kucoin symbols
   * @param tradeFee Kucoin trade fee (optional)
   * @return Exchange metadata.
   */
  public static ExchangeMetaData adaptMetadata(
      ExchangeMetaData exchangeMetaData, List<CurrenciesResponse> currenciesResponse, List<SymbolResponse> symbolsResponse, TradeFeeResponse tradeFee)
      throws IOException {

    Map<CurrencyPair, CurrencyPairMetaData> currencyPairs = exchangeMetaData.getCurrencyPairs();
    Map<Currency, CurrencyMetaData> currencies = exchangeMetaData.getCurrencies();
    Map<String, CurrencyMetaData> stringCurrencyMetaDataMap =
        adaptCurrencyMetaData(currenciesResponse);

    BigDecimal takerTradingFee = tradeFee != null ? tradeFee.getTakerFeeRate() : null;

    for (SymbolResponse symbol : symbolsResponse) {

      CurrencyPair pair = adaptCurrencyPair(symbol.getSymbol());
      CurrencyPairMetaData staticMetaData = exchangeMetaData.getCurrencyPairs().get(pair);

      BigDecimal minSize = symbol.getBaseMinSize();
      BigDecimal maxSize = symbol.getBaseMaxSize();
      int priceScale = symbol.getQuoteIncrement().stripTrailingZeros().scale();

      CurrencyPairMetaData cpmd =
          new CurrencyPairMetaData(
              takerTradingFee,
              minSize,
              maxSize,
              priceScale,
              staticMetaData != null ? staticMetaData.getFeeTiers() : null);
      currencyPairs.put(pair, cpmd);

      if (!currencies.containsKey(pair.base))
        currencies.put(pair.base, stringCurrencyMetaDataMap.get(pair.base.getCurrencyCode()));
      if (!currencies.containsKey(pair.counter))
        currencies.put(pair.counter, stringCurrencyMetaDataMap.get(pair.counter.getCurrencyCode()));
    }

    return new ExchangeMetaData(
        currencyPairs,
        currencies,
        exchangeMetaData.getPublicRateLimits(),
        exchangeMetaData.getPrivateRateLimits(),
        true);
  }

  static HashMap<String, CurrencyMetaData> adaptCurrencyMetaData(List<CurrenciesResponse> list) {
    HashMap<String, CurrencyMetaData> stringCurrencyMetaDataMap = new HashMap<>();
    for (CurrenciesResponse currenciesResponse : list) {
      BigDecimal precision = currenciesResponse.getPrecision();
      String withdrawalMinFee = currenciesResponse.getWithdrawalMinFee();
      String withdrawalMinSize = currenciesResponse.getWithdrawalMinSize();
      WalletHealth walletHealth = getWalletHealth(currenciesResponse);
      CurrencyMetaData currencyMetaData =
          new CurrencyMetaData(
              precision.intValue(),
              new BigDecimal(withdrawalMinFee),
              new BigDecimal(withdrawalMinSize),
              walletHealth);
      stringCurrencyMetaDataMap.put(currenciesResponse.getCurrency(), currencyMetaData);
    }
    return stringCurrencyMetaDataMap;
  }

  /**
   * @param currenciesResponse currency response which holds wallet status information
   * @return WalletHealth
   */
  private static WalletHealth getWalletHealth(CurrenciesResponse currenciesResponse) {
    WalletHealth walletHealth = WalletHealth.ONLINE;
    if (!currenciesResponse.isWithdrawEnabled() && !currenciesResponse.isDepositEnabled()) {
      walletHealth = WalletHealth.OFFLINE;
    } else if (!currenciesResponse.isDepositEnabled()) {
      walletHealth = WalletHealth.DEPOSITS_DISABLED;
    } else if (!currenciesResponse.isWithdrawEnabled()) {
      walletHealth = WalletHealth.WITHDRAWALS_DISABLED;
    }
    return walletHealth;
  }

  public static OrderBook adaptOrderBook(CurrencyPair currencyPair, OrderBookResponse kc) {
    Date timestamp = new Date(kc.getTime());
    List<LimitOrder> asks =
        kc.getAsks().stream()
            .map(PriceAndSize::new)
            .sorted(Ordering.natural().onResultOf(s -> s.price))
            .map(s -> adaptLimitOrder(currencyPair, ASK, s, timestamp))
            .collect(toCollection(LinkedList::new));
    List<LimitOrder> bids =
        kc.getBids().stream()
            .map(PriceAndSize::new)
            .sorted(Ordering.natural().onResultOf((PriceAndSize s) -> s.price).reversed())
            .map(s -> adaptLimitOrder(currencyPair, BID, s, timestamp))
            .collect(toCollection(LinkedList::new));
    return new OrderBook(timestamp, asks, bids);
  }

  private static LimitOrder adaptLimitOrder(
      CurrencyPair currencyPair, OrderType orderType, PriceAndSize priceAndSize, Date timestamp) {
    return new LimitOrder.Builder(orderType, currencyPair)
        .limitPrice(priceAndSize.price)
        .originalAmount(priceAndSize.size)
        .orderStatus(NEW)
        .build();
  }

  public static Trades adaptTrades(
      CurrencyPair currencyPair, List<TradeHistoryResponse> kucoinTrades) {
    return new Trades(
        kucoinTrades.stream().map(o -> adaptTrade(currencyPair, o)).collect(Collectors.toList()),
        TradeSortType.SortByTimestamp);
  }

  public static Balance adaptBalance(AccountBalancesResponse a) {
    return new Balance(Currency.getInstance(a.getCurrency()), a.getBalance(), a.getAvailable());
  }

  private static Trade adaptTrade(CurrencyPair currencyPair, TradeHistoryResponse trade) {
    return new Trade.Builder()
        .currencyPair(currencyPair)
        .originalAmount(trade.getSize())
        .price(trade.getPrice())
        .timestamp(new Date(Long.parseLong(trade.getSequence())))
        .type(adaptSide(trade.getSide()))
        .build();
  }

  private static OrderType adaptSide(String side) {
    return "sell".equals(side) ? ASK : BID;
  }

  private static String adaptSide(OrderType type) {
    return type.equals(ASK) ? "sell" : "buy";
  }

  public static Order adaptOrder(OrderResponse order) {

    OrderType orderType = adaptSide(order.getSide());
    CurrencyPair currencyPair = adaptCurrencyPair(order.getSymbol());

    OrderStatus status;
    if (order.isCancelExist()) {
      status = CANCELED;
    } else if (order.isActive()) {
      if (order.getDealSize().signum() == 0) {
        status = NEW;
      } else {
        status = PARTIALLY_FILLED;
      }
    } else {
      status = UNKNOWN;
    }

    Order.Builder builder;
    if (StringUtils.isNotEmpty(order.getStop())) {
      BigDecimal limitPrice = order.getPrice();
      if (limitPrice != null && limitPrice.compareTo(BigDecimal.ZERO) == 0) {
        limitPrice = null;
      }
      builder =
          new StopOrder.Builder(orderType, currencyPair)
              .limitPrice(limitPrice)
              .stopPrice(order.getStopPrice());
    } else {
      builder = new LimitOrder.Builder(orderType, currencyPair).limitPrice(order.getPrice());
    }
    builder =
        builder
            .averagePrice(
                order.getDealSize().compareTo(BigDecimal.ZERO) == 0
                    ? MoreObjects.firstNonNull(order.getPrice(), order.getStopPrice())
                    : order.getDealFunds().divide(order.getDealSize(), RoundingMode.HALF_UP))
            .cumulativeAmount(order.getDealSize())
            .fee(order.getFee())
            .id(order.getId())
            .orderStatus(status)
            .originalAmount(order.getSize())
            .remainingAmount(order.getSize().subtract(order.getDealSize()))
            .timestamp(order.getCreatedAt());

    if (StringUtils.isNotEmpty(order.getTimeInForce())) {
      builder.flag(TimeInForce.getTimeInForce(order.getTimeInForce()));
    }

    return StopOrder.Builder.class.isInstance(builder)
        ? ((StopOrder.Builder) builder).build()
        : ((LimitOrder.Builder) builder).build();
  }

  public static UserTrade adaptUserTrade(TradeResponse trade) {
    return new UserTrade.Builder()
        .currencyPair(adaptCurrencyPair(trade.getSymbol()))
        .feeAmount(trade.getFee())
        .feeCurrency(Currency.getInstance(trade.getFeeCurrency()))
        .id(trade.getTradeId())
        .orderId(trade.getOrderId())
        .originalAmount(trade.getSize())
        .price(trade.getPrice())
        .timestamp(trade.getTradeCreatedAt())
        .type(adaptSide(trade.getSide()))
        .build();
  }

  public static UserTrade adaptHistOrder(HistOrdersResponse histOrder) {
    CurrencyPair currencyPair = adaptCurrencyPair(histOrder.getSymbol());
    return new UserTrade.Builder()
        .currencyPair(currencyPair)
        .feeAmount(histOrder.getFee())
        .feeCurrency(currencyPair.base)
        .id(histOrder.getId())
        .originalAmount(histOrder.getAmount())
        .price(histOrder.getPrice())
        .timestamp(histOrder.getTradeCreatedAt())
        .type(adaptSide(histOrder.getSide()))
        .build();
  }

  public static OrderCreateApiRequest adaptLimitOrder(LimitOrder limitOrder) {
    return ((OrderCreateApiRequest.OrderCreateApiRequestBuilder) adaptOrder(limitOrder))
        .type("limit")
        .price(limitOrder.getLimitPrice())
        .postOnly(limitOrder.hasFlag(POST_ONLY))
        .hidden(limitOrder.hasFlag(HIDDEN))
        .iceberg(limitOrder.hasFlag(ICEBERG))
        .build();
  }

  public static OrderCreateApiRequest adaptStopOrder(StopOrder stopOrder) {
    return ((OrderCreateApiRequest.OrderCreateApiRequestBuilder) adaptOrder(stopOrder))
        .type(stopOrder.getLimitPrice() == null ? "market" : "limit")
        .price(stopOrder.getLimitPrice())
        .stop(stopOrder.getType().equals(ASK) ? "loss" : "entry")
        .stopPrice(stopOrder.getStopPrice())
        .build();
  }

  public static OrderCreateApiRequest adaptMarketOrder(MarketOrder marketOrder) {
    return ((OrderCreateApiRequest.OrderCreateApiRequestBuilder) adaptOrder(marketOrder))
        .type("market")
        .build();
  }

  /**
   * Returns {@code Object} instead of the Lombok builder in order to avoid a Lombok limitation with
   * Javadoc.
   */
  private static Object adaptOrder(Order order) {
    OrderCreateApiRequest.OrderCreateApiRequestBuilder request = OrderCreateApiRequest.builder();
    boolean hasClientId = false;
    for (IOrderFlags flag : order.getOrderFlags()) {
      if (flag instanceof KucoinOrderFlags) {
        request.clientOid(((KucoinOrderFlags) flag).getClientId());
        hasClientId = true;
      } else if (flag instanceof TimeInForce) {
        request.timeInForce(((TimeInForce) flag).name());
      }
    }
    if (!hasClientId) {
      request.clientOid(UUID.randomUUID().toString());
    }
    return request
        .symbol(adaptCurrencyPair(order.getCurrencyPair()))
        .size(order.getOriginalAmount())
        .side(adaptSide(order.getType()));
  }

  private static final class PriceAndSize {

    final BigDecimal price;
    final BigDecimal size;

    PriceAndSize(List<String> data) {
      this.price = new BigDecimal(data.get(0));
      this.size = new BigDecimal(data.get(1));
    }
  }

  public static FundingRecord adaptFundingRecord(WithdrawalResponse wr) {
    FundingRecord.Builder b = new FundingRecord.Builder();
    return b.setAddress(wr.getAddress())
        .setAmount(wr.getAmount())
        .setCurrency(Currency.getInstance(wr.getCurrency()))
        .setFee(wr.getFee())
        .setType(Type.WITHDRAWAL)
        .setStatus(convertStatus(wr.getStatus()))
        .setInternalId(wr.getId())
        .setBlockchainTransactionHash(wr.getWalletTxId())
        .setDescription(wr.getMemo())
        .setDate(wr.getCreatedAt())
        .build();
  }

  private static Status convertStatus(String status) {
    if (status == null) {
      return null;
    }
    switch (status) {
      case "WALLET_PROCESSING":
      case "PROCESSING":
        return Status.PROCESSING;
      case "SUCCESS":
        return Status.COMPLETE;
      case "FAILURE":
        return Status.FAILED;
      default:
        throw new ExchangeException("Not supported status: " + status);
    }
  }

  public static FundingRecord adaptFundingRecord(DepositResponse dr) {
    FundingRecord.Builder b = new FundingRecord.Builder();
    return b.setAddress(dr.getAddress())
        .setAmount(dr.getAmount())
        .setCurrency(Currency.getInstance(dr.getCurrency()))
        .setFee(dr.getFee())
        .setType(Type.DEPOSIT)
        .setStatus(convertStatus(dr.getStatus()))
        .setBlockchainTransactionHash(dr.getWalletTxId())
        .setDescription(dr.getMemo())
        .setDate(dr.getCreatedAt())
        .build();
  }
}
