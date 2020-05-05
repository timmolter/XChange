package org.knowm.xchange.poloniex.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.marketdata.Trades.TradeSortType;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.poloniex.PoloniexAdapters;
import org.knowm.xchange.poloniex.PoloniexErrorAdapter;
import org.knowm.xchange.poloniex.PoloniexUtils;
import org.knowm.xchange.poloniex.dto.PoloniexException;
import org.knowm.xchange.poloniex.dto.trade.PoloniexLimitOrder;
import org.knowm.xchange.poloniex.dto.trade.PoloniexOpenOrder;
import org.knowm.xchange.poloniex.dto.trade.PoloniexTrade;
import org.knowm.xchange.poloniex.dto.trade.PoloniexTradeResponse;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.CancelOrderByIdParams;
import org.knowm.xchange.service.trade.params.CancelOrderParams;
import org.knowm.xchange.service.trade.params.TradeHistoryParamCurrencyPair;
import org.knowm.xchange.service.trade.params.TradeHistoryParamLimit;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.TradeHistoryParamsAll;
import org.knowm.xchange.service.trade.params.TradeHistoryParamsTimeSpan;
import org.knowm.xchange.service.trade.params.orders.DefaultOpenOrdersParamCurrencyPair;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParamCurrencyPair;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParams;
import org.knowm.xchange.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PoloniexTradeService extends PoloniexTradeServiceRaw implements TradeService {

  private static final Logger LOG = LoggerFactory.getLogger(PoloniexTradeService.class);

  private PoloniexMarketDataService poloniexMarketDataService;

  public PoloniexTradeService(
      Exchange exchange, PoloniexMarketDataService poloniexMarketDataService) {

    super(exchange);
    this.poloniexMarketDataService = poloniexMarketDataService;
  }

  @Override
  public OpenOrders getOpenOrders() throws IOException {
    return getOpenOrders(createOpenOrdersParams());
  }

  @Override
  public OpenOrders getOpenOrders(OpenOrdersParams params) throws ExchangeException, IOException {
    try {
      CurrencyPair currencyPair = null;
      if (params instanceof OpenOrdersParamCurrencyPair) {
        currencyPair = ((OpenOrdersParamCurrencyPair) params).getCurrencyPair();
      }

      final Map<String, PoloniexOpenOrder[]> poloniexOpenOrders;
      if (currencyPair == null) {
        poloniexOpenOrders = returnOpenOrders();
      } else {
        final PoloniexOpenOrder[] cpOpenOrders = returnOpenOrders(currencyPair);
        poloniexOpenOrders = new HashMap<>(1);
        poloniexOpenOrders.put(PoloniexUtils.toPairString(currencyPair), cpOpenOrders);
      }
      return PoloniexAdapters.adaptPoloniexOpenOrders(poloniexOpenOrders);
    } catch (PoloniexException e) {
      throw PoloniexErrorAdapter.adapt(e);
    }
  }

  @Override
  public String placeMarketOrder(MarketOrder marketOrder) throws IOException {
    throw new NotAvailableFromExchangeException();
  }

  @Override
  public String placeLimitOrder(LimitOrder limitOrder) throws IOException {

    try {
      PoloniexTradeResponse response;
      if (limitOrder.getType() == OrderType.BID || limitOrder.getType() == OrderType.EXIT_ASK) {
        response = buy(limitOrder);
      } else {
        response = sell(limitOrder);
      }

      // The return value contains details of any trades that have been immediately executed as a
      // result
      // of this order. Make these available to the application if it has provided a
      // PoloniexLimitOrder.
      if (limitOrder instanceof PoloniexLimitOrder) {
        PoloniexLimitOrder raw = (PoloniexLimitOrder) limitOrder;
        raw.setResponse(response);
      }

      return response.getOrderNumber().toString();
    } catch (PoloniexException e) {
      throw PoloniexErrorAdapter.adapt(e);
    }
  }

  @Override
  public boolean cancelOrder(String orderId) throws IOException {

    try {
      return cancel(orderId);
    } catch (PoloniexException e) {
      throw PoloniexErrorAdapter.adapt(e);
    }
  }

  @Override
  public boolean cancelOrder(CancelOrderParams orderParams) throws IOException {
    try {
      if (orderParams instanceof CancelOrderByIdParams) {
        return cancelOrder(((CancelOrderByIdParams) orderParams).getOrderId());
      } else {
        return false;
      }
    } catch (PoloniexException e) {
      throw PoloniexErrorAdapter.adapt(e);
    }
  }

  /**
   * @param params Can optionally implement {@link TradeHistoryParamCurrencyPair} and {@link
   *     TradeHistoryParamsTimeSpan}. All other TradeHistoryParams types will be ignored.
   */
  @Override
  public UserTrades getTradeHistory(TradeHistoryParams params) throws IOException {
    try {
      CurrencyPair currencyPair = null;
      Date startTime = null;
      Date endTime = null;

      if (params instanceof TradeHistoryParamCurrencyPair) {
        currencyPair = ((TradeHistoryParamCurrencyPair) params).getCurrencyPair();
      }
      if (params instanceof TradeHistoryParamsTimeSpan) {
        startTime = ((TradeHistoryParamsTimeSpan) params).getStartTime();
        endTime = ((TradeHistoryParamsTimeSpan) params).getEndTime();
      }

      Integer limit = 500;
      if (params instanceof TradeHistoryParamLimit) {
        TradeHistoryParamLimit tradeHistoryParamLimit = (TradeHistoryParamLimit) params;
        limit = tradeHistoryParamLimit.getLimit();
      }

      return getTradeHistory(
          currencyPair,
          DateUtils.toUnixTimeNullSafe(startTime),
          DateUtils.toUnixTimeNullSafe(endTime),
          limit);
    } catch (PoloniexException e) {
      throw PoloniexErrorAdapter.adapt(e);
    }
  }

  public BigDecimal getMakerFee() throws IOException {
    try {
      String value = getFeeInfo().get("makerFee");
      return new BigDecimal(value);
    } catch (PoloniexException e) {
      throw PoloniexErrorAdapter.adapt(e);
    }
  }

  public BigDecimal getTakerFee() throws IOException {
    try {
      String value = getFeeInfo().get("takerFee");
      return new BigDecimal(value);
    } catch (PoloniexException e) {
      throw PoloniexErrorAdapter.adapt(e);
    }
  }

  private UserTrades getTradeHistory(
      CurrencyPair currencyPair, final Long startTime, final Long endTime, Integer limit)
      throws IOException {

    try {
      List<UserTrade> trades = new ArrayList<>();
      if (currencyPair == null) {
        HashMap<String, PoloniexTrade[]> poloniexUserTrades =
            returnTradeHistory(startTime, endTime, limit);
        if (poloniexUserTrades != null) {
          for (Map.Entry<String, PoloniexTrade[]> mapEntry : poloniexUserTrades.entrySet()) {
            currencyPair = PoloniexUtils.toCurrencyPair(mapEntry.getKey());
            for (PoloniexTrade poloniexTrade : mapEntry.getValue()) {
              trades.add(PoloniexAdapters.adaptPoloniexUserTrade(poloniexTrade, currencyPair));
            }
          }
        }
      } else {
        PoloniexTrade[] poloniexTrades =
            returnTradeHistory(currencyPair, startTime, endTime, limit);
        if (poloniexTrades != null) {
          for (PoloniexTrade poloniexTrade : poloniexTrades) {
            trades.add(PoloniexAdapters.adaptPoloniexUserTrade(poloniexTrade, currencyPair));
          }
        }
      }

      return new UserTrades(trades, TradeSortType.SortByTimestamp);
    } catch (PoloniexException e) {
      throw PoloniexErrorAdapter.adapt(e);
    }
  }

  /**
   * Create {@link TradeHistoryParams} that supports {@link TradeHistoryParamsTimeSpan} and {@link
   * TradeHistoryParamCurrencyPair}.
   */
  @Override
  public TradeHistoryParams createTradeHistoryParams() {

    return new PoloniexTradeHistoryParams();
  }

  @Override
  public OpenOrdersParams createOpenOrdersParams() {
    return new DefaultOpenOrdersParamCurrencyPair();
  }

  @Override
  public Collection<Order> getOrder(String... orderIds) throws IOException {

    List<String> orderIdList = Arrays.asList(orderIds);

    OpenOrders openOrders = getOpenOrders();
    List<Order> returnValue =
        openOrders.getOpenOrders().stream()
            .filter(f -> orderIdList.contains(f.getId()))
            .collect(Collectors.toList());

    returnValue.addAll(
        orderIdList.stream()
            .filter(
                f -> !returnValue.stream().filter(a -> a.getId().equals(f)).findFirst().isPresent())
            .map(
                f -> {
                  try {
                    return PoloniexAdapters.adaptUserTradesToOrderStatus(f, returnOrderTrades(f));
                  } catch (IOException e) {
                    LOG.error("Unable to find status for Poloniex order id: " + f, e);
                  }
                  return null;
                })
            .filter(f -> f != null)
            .collect(Collectors.toList()));

    return returnValue;
  }

  public final UserTrades getOrderTrades(Order order) throws IOException {
    return getOrderTrades(order.getId(), order.getCurrencyPair());
  }

  public UserTrades getOrderTrades(String orderId, CurrencyPair currencyPair) throws IOException {

    try {
      List<UserTrade> trades = new ArrayList<>();

      PoloniexTrade[] poloniexTrades = returnOrderTrades(orderId);
      if (poloniexTrades != null) {
        for (PoloniexTrade poloniexTrade : poloniexTrades) {
          poloniexTrade.setOrderNumber(orderId); // returnOrderTrades doesn't fill in orderId
          trades.add(PoloniexAdapters.adaptPoloniexUserTrade(poloniexTrade, currencyPair));
        }
      }

      return new UserTrades(trades, TradeSortType.SortByTimestamp);
    } catch (PoloniexException e) {
      throw PoloniexErrorAdapter.adapt(e);
    }
  }

  public static class PoloniexTradeHistoryParams
      implements TradeHistoryParamCurrencyPair, TradeHistoryParamsTimeSpan {

    private final TradeHistoryParamsAll all = new TradeHistoryParamsAll();

    @Override
    public CurrencyPair getCurrencyPair() {

      return all.getCurrencyPair();
    }

    @Override
    public void setCurrencyPair(CurrencyPair value) {

      all.setCurrencyPair(value);
    }

    @Override
    public Date getStartTime() {

      return all.getStartTime();
    }

    @Override
    public void setStartTime(Date value) {

      all.setStartTime(value);
    }

    @Override
    public Date getEndTime() {

      return all.getEndTime();
    }

    @Override
    public void setEndTime(Date value) {

      all.setEndTime(value);
    }
  }
}
