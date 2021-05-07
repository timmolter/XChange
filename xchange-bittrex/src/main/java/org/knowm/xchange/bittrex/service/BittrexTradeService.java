package org.knowm.xchange.bittrex.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.knowm.xchange.bittrex.BittrexAdapters;
import org.knowm.xchange.bittrex.BittrexAuthenticated;
import org.knowm.xchange.bittrex.BittrexConstants;
import org.knowm.xchange.bittrex.BittrexErrorAdapter;
import org.knowm.xchange.bittrex.BittrexExchange;
import org.knowm.xchange.bittrex.dto.BittrexException;
import org.knowm.xchange.bittrex.dto.trade.BittrexExecution;
import org.knowm.xchange.bittrex.dto.trade.BittrexOrder;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.CancelOrderByIdParams;
import org.knowm.xchange.service.trade.params.CancelOrderParams;
import org.knowm.xchange.service.trade.params.DefaultTradeHistoryParamCurrencyPair;
import org.knowm.xchange.service.trade.params.TradeHistoryParamCurrencyPair;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.orders.DefaultOpenOrdersParamCurrencyPair;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParams;

public class BittrexTradeService extends BittrexTradeServiceRaw implements TradeService {

  /**
   * Constructor
   *
   * @param exchange
   */
  public BittrexTradeService(
      BittrexExchange exchange,
      BittrexAuthenticated bittrex,
      ResilienceRegistries resilienceRegistries) {
    super(exchange, bittrex, resilienceRegistries);
  }

  @Override
  public String placeLimitOrder(LimitOrder limitOrder) throws IOException {
    try {
      return placeBittrexLimitOrder(limitOrder);
    } catch (BittrexException e) {
      throw BittrexErrorAdapter.adapt(e);
    }
  }

  @Override
  public String placeMarketOrder(MarketOrder marketOrder) throws IOException {
    try {
      return placeBittrexMarketOrder(marketOrder);
    } catch (BittrexException e) {
      throw BittrexErrorAdapter.adapt(e);
    }
  }

  @Override
  public OpenOrders getOpenOrders() throws IOException {
    return getOpenOrders(createOpenOrdersParams());
  }

  @Override
  public OpenOrders getOpenOrders(OpenOrdersParams params) throws IOException {
    try {
      return new OpenOrders(BittrexAdapters.adaptOpenOrders(getBittrexOpenOrders(params)));
    } catch (BittrexException e) {
      throw BittrexErrorAdapter.adapt(e);
    }
  }

  @Override
  public boolean cancelOrder(String orderId) throws IOException {
    try {
      return BittrexConstants.CLOSED.equalsIgnoreCase(cancelBittrexLimitOrder(orderId).getStatus());
    } catch (BittrexException e) {
      throw BittrexErrorAdapter.adapt(e);
    }
  }

  @Override
  public boolean cancelOrder(CancelOrderParams orderParams) throws IOException {
    if (orderParams instanceof CancelOrderByIdParams) {
      return cancelOrder(((CancelOrderByIdParams) orderParams).getOrderId());
    }
    return false;
  }

  @Override
  public UserTrades getTradeHistory(TradeHistoryParams params) throws IOException {
    try {
      List<BittrexOrder> tradeHistory =
          (params instanceof TradeHistoryParamCurrencyPair)
              ? getBittrexUserTradeHistory(
                  ((TradeHistoryParamCurrencyPair) params).getCurrencyPair())
              : getBittrexUserTradeHistory();
      return new UserTrades(
          BittrexAdapters.adaptUserTrades(tradeHistory), Trades.TradeSortType.SortByTimestamp);
    } catch (BittrexException e) {
      throw BittrexErrorAdapter.adapt(e);
    }
  }

  @Override
  public TradeHistoryParams createTradeHistoryParams() {
    return new DefaultTradeHistoryParamCurrencyPair();
  }

  @Override
  public OpenOrdersParams createOpenOrdersParams() {
    return new DefaultOpenOrdersParamCurrencyPair();
  }

  @Override
  public Collection<Order> getOrder(String... orderIds) throws IOException {
    try {
      List<Order> orders = new ArrayList<>();
      for (String orderId : orderIds) {
        BittrexOrder bittrexOrder = getBittrexOrder(orderId);
        if (bittrexOrder == null) {
          continue;
        }
        Order order = BittrexAdapters.adaptOrder(bittrexOrder);
        if (order instanceof MarketOrder) {
          BigDecimal rate =
              decorateApiCall(
                      () -> {
                        List<BittrexExecution> executions =
                            getBittrexOrderExecutions(order.getId());
                        return executions.stream()
                            .map(BittrexExecution::getRate)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(
                                new BigDecimal(executions.size()),
                                new MathContext(8, RoundingMode.FLOOR));
                      })
                  .withRetry(retry("orderExecutionRate"))
                  .call();
          order.setAveragePrice(rate);
        }
        orders.add(order);
      }
      return orders;
    } catch (BittrexException e) {
      throw BittrexErrorAdapter.adapt(e);
    }
  }
}
