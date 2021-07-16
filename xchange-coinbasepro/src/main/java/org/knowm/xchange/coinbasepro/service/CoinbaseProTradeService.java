package org.knowm.xchange.coinbasepro.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.coinbasepro.CoinbaseProAdapters;
import org.knowm.xchange.coinbasepro.CoinbaseProExchange;
import org.knowm.xchange.coinbasepro.dto.trade.CoinbaseProTradeHistoryParams;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.StopOrder;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.exceptions.FundsExceededException;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.CancelOrderByIdParams;
import org.knowm.xchange.service.trade.params.CancelOrderParams;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.orders.DefaultOpenOrdersParamCurrencyPair;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParamCurrencyPair;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParams;
import org.knowm.xchange.service.trade.params.orders.OrderQueryParams;

public class CoinbaseProTradeService extends CoinbaseProTradeServiceRaw implements TradeService {

  public CoinbaseProTradeService(
      CoinbaseProExchange exchange, ResilienceRegistries resilienceRegistries) {
    super(exchange, resilienceRegistries);
  }

  @Override
  public OpenOrders getOpenOrders() throws IOException {
    return CoinbaseProAdapters.adaptOpenOrders(getCoinbaseProOpenOrders());
  }

  @Override
  public OpenOrdersParams createOpenOrdersParams() {
    return new DefaultOpenOrdersParamCurrencyPair();
  }

  @Override
  public OpenOrders getOpenOrders(OpenOrdersParams params) throws IOException {
    if (params instanceof OpenOrdersParamCurrencyPair) {
      OpenOrdersParamCurrencyPair pairParams = (OpenOrdersParamCurrencyPair) params;
      String productId = CoinbaseProAdapters.adaptProductID(pairParams.getCurrencyPair());
      return CoinbaseProAdapters.adaptOpenOrders(getCoinbaseProOpenOrders(productId));
    }
    return CoinbaseProAdapters.adaptOpenOrders(getCoinbaseProOpenOrders());
  }

  @Override
  public String placeMarketOrder(MarketOrder marketOrder) throws IOException {
    return placeCoinbaseProOrder(CoinbaseProAdapters.adaptCoinbaseProPlaceMarketOrder(marketOrder))
        .getId();
  }

  @Override
  public String placeLimitOrder(LimitOrder limitOrder) throws IOException, FundsExceededException {
    return placeCoinbaseProOrder(CoinbaseProAdapters.adaptCoinbaseProPlaceLimitOrder(limitOrder))
        .getId();
  }

  @Override
  public String placeStopOrder(StopOrder stopOrder) throws IOException, FundsExceededException {
    return placeCoinbaseProOrder(CoinbaseProAdapters.adaptCoinbaseProStopOrder(stopOrder)).getId();
  }

  @Override
  public boolean cancelOrder(String orderId) throws IOException {
    return cancelCoinbaseProOrder(orderId);
  }

  @Override
  public boolean cancelOrder(CancelOrderParams orderParams) throws IOException {
    if (orderParams instanceof CancelOrderByIdParams) {
      return cancelOrder(((CancelOrderByIdParams) orderParams).getOrderId());
    } else {
      return false;
    }
  }

  @Override
  public UserTrades getTradeHistory(TradeHistoryParams params) throws IOException {
    return CoinbaseProAdapters.adaptTradeHistory(getCoinbaseProFills(params));
  }

  @Override
  public TradeHistoryParams createTradeHistoryParams() {
    return new CoinbaseProTradeHistoryParams();
  }

  @Override
  public Collection<Order> getOrder(String... orderIds) throws IOException {
    Collection<Order> orders = new ArrayList<>(orderIds.length);

    for (String orderId : orderIds) {
      orders.add(CoinbaseProAdapters.adaptOrder(super.getOrder(orderId)));
    }

    return orders;
  }

  @Override
  public Collection<Order> getOrder(OrderQueryParams... orderQueryParams) throws IOException {
    return getOrder(
        Arrays.stream(orderQueryParams).map(OrderQueryParams::getOrderId).toArray(String[]::new));
  }
}
