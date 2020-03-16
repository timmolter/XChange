package org.knowm.xchange.bithumb.service;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.bithumb.BithumbAdapters;
import org.knowm.xchange.bithumb.BithumbErrorAdapter;
import org.knowm.xchange.bithumb.BithumbException;
import org.knowm.xchange.bithumb.dto.trade.BithumbOpenOrdersParam;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.CancelOrderByPairAndIdParams;
import org.knowm.xchange.service.trade.params.CancelOrderParams;
import org.knowm.xchange.service.trade.params.TradeHistoryParamCurrencyPair;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParamCurrencyPair;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParams;
import org.knowm.xchange.service.trade.params.orders.OrderQueryParamCurrencyPair;
import org.knowm.xchange.service.trade.params.orders.OrderQueryParams;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class BithumbTradeService extends BithumbTradeServiceRaw implements TradeService {

  public BithumbTradeService(Exchange exchange) {
    super(exchange);
  }

  @Override
  public OpenOrders getOpenOrders() throws IOException {
    throw new NotAvailableFromExchangeException();
  }

  @Override
  public OpenOrders getOpenOrders(OpenOrdersParams params) throws IOException {

    final CurrencyPair currencyPair =
        Optional.ofNullable(params)
            .filter(p -> p instanceof OpenOrdersParamCurrencyPair)
            .map(p -> ((OpenOrdersParamCurrencyPair) p).getCurrencyPair())
            .orElse(null);

    try {
      return BithumbAdapters.adaptOrders(getBithumbOrders(currencyPair).getData());
    } catch (BithumbException e) {
      throw BithumbErrorAdapter.adapt(e);
    }
  }

  @Override
  public String placeMarketOrder(MarketOrder marketOrder) throws IOException {
    try {
      return placeBithumbMarketOrder(marketOrder).getOrderId();
    } catch (BithumbException e) {
      throw BithumbErrorAdapter.adapt(e);
    }
  }

  @Override
  public String placeLimitOrder(LimitOrder limitOrder) throws IOException {
    try {
      return placeBithumbLimitOrder(limitOrder).getOrderId();
    } catch (BithumbException e) {
      throw BithumbErrorAdapter.adapt(e);
    }
  }

  @Override
  public boolean cancelOrder(CancelOrderParams orderParams) throws IOException {

    if (orderParams instanceof CancelOrderByPairAndIdParams) {
      try {
        final CancelOrderByPairAndIdParams params = (CancelOrderByPairAndIdParams) orderParams;
        return cancelBithumbOrder(params.getOrderId(), params.getCurrencyPair());
      } catch (BithumbException e) {
        throw BithumbErrorAdapter.adapt(e);
      }
    } else {
      throw new NotYetImplementedForExchangeException(
          "Only CancelOrderByPairAndIdParams supported");
    }
  }

  @Override
  public UserTrades getTradeHistory(TradeHistoryParams params) throws IOException {

    final CurrencyPair currencyPair =
        Optional.ofNullable(params)
            .filter(p -> p instanceof TradeHistoryParamCurrencyPair)
            .map(p -> ((TradeHistoryParamCurrencyPair) p).getCurrencyPair())
            .orElse(null);
    try {
      return BithumbAdapters.adaptUserTrades(
          getBithumbUserTransactions(currencyPair).getData(), currencyPair);
    } catch (BithumbException e) {
      throw BithumbErrorAdapter.adapt(e);
    }
  }

  @Override
  public Collection<Order> getOrder(OrderQueryParams... orderQueryParams) throws IOException {
    /* This only works for executed orders */
    return Arrays.stream(orderQueryParams)
        .filter(oq -> oq instanceof OrderQueryParamCurrencyPair)
        .map(oq -> (OrderQueryParamCurrencyPair) oq)
        .flatMap(
            oq -> {
              try {
                return getBithumbOrderDetail(oq.getOrderId(), oq.getCurrencyPair()).getData()
                    .stream()
                    .map(detail -> BithumbAdapters.adaptOrderDetail(detail, oq.getOrderId()));

              } catch (IOException e) {
                return null;
              } catch (BithumbException e) {
                throw BithumbErrorAdapter.adapt(e);
              }
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public TradeHistoryParams createTradeHistoryParams() {
    return new BithumbTradeHistoryParams();
  }

  @Override
  public OpenOrdersParams createOpenOrdersParams() {
    return new BithumbOpenOrdersParam();
  }
}
