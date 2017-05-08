package org.known.xchange.dsx.service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.knowm.xchange.Exchange;
import org.known.xchange.dsx.DSXAuthenticated;
import org.known.xchange.dsx.dto.trade.DSXActiveOrdersReturn;
import org.known.xchange.dsx.dto.trade.DSXCancelOrderResult;
import org.known.xchange.dsx.dto.trade.DSXCancelOrderReturn;
import org.known.xchange.dsx.dto.trade.DSXOrder;
import org.known.xchange.dsx.dto.trade.DSXOrderHistoryResult;
import org.known.xchange.dsx.dto.trade.DSXOrderHistoryReturn;
import org.known.xchange.dsx.dto.trade.DSXTradeHistoryResult;
import org.known.xchange.dsx.dto.trade.DSXTradeHistoryReturn;
import org.known.xchange.dsx.dto.trade.DSXTradeResult;
import org.known.xchange.dsx.dto.trade.DSXTradeReturn;
import org.known.xchange.dsx.dto.trade.DSXTransHistoryResult;
import org.known.xchange.dsx.dto.trade.DSXTransHistoryReturn;

/**
 * @author Mikhail Wall
 */

public class DSXTradeServiceRaw extends DSXBaseService {

  private static final String MSG_NO_TRADES = "no trades";
  private static final String MSG_BAD_STATUS = "bad status";

  /**
   * Constructor
   *
   * @param exchange
   */
  protected DSXTradeServiceRaw(Exchange exchange) {

    super(exchange);
  }

  /**
   * @param pair The pair to display the orders e.g. btcusd (null: all pairs)
   * @return Active orders map
   * @throws IOException
   */
  public Map<Long, DSXOrder> getDSXActiveOrders(String pair) throws IOException {
    DSXActiveOrdersReturn orders = dsx.ActiveOrders(apiKey, signatureCreator, System.currentTimeMillis(), pair);
    if ("no orders".equals(orders.getError())) {
      return new HashMap<>();
    }
    checkResult(orders);
    return orders.getReturnValue();
  }

  /**
   * @param order DSXOrder object
   * @return DSXTradeResult object
   * @throws IOException
   */
  public DSXTradeResult tradeDSX(DSXOrder order) throws IOException {

    String pair = order.getPair().toLowerCase();
    DSXTradeReturn ret = dsx.Trade(apiKey, signatureCreator, System.currentTimeMillis(), order.getType(), order.getRate(),
        order.getAmount(), pair);
    checkResult(ret);
    return ret.getReturnValue();
  }

  public DSXCancelOrderResult cancelDSXOrder(long orderId) throws IOException {

    DSXCancelOrderReturn ret = dsx.CancelOrder(apiKey, signatureCreator, System.currentTimeMillis(), orderId);
    if (MSG_BAD_STATUS.equals(ret.getError())) {
      return null;
    }

    checkResult(ret);
    return ret.getReturnValue();
  }

  /**
   * Get Map of trade history from DSX exchange. All parameters are nullable
   *
   * @param from ID of the first trade of the selection
   * @param count Number of trades to display
   * @param fromId ID of the first trade of the selection
   * @param endId ID of the last trade of the selection
   * @param order Order in which transactions shown. Possible values: «asc» — from first to last, «desc» — from last to first. Default value is «desc»
   * @param since Time from which start selecting trades by trade time(UNIX time). If this value is not null order will become «asc»
   * @param end 	Time to which start selecting trades by trade time(UNIX time). If this value is not null order will become «asc»
   * @param pair Currency pair
   * @return Map of trade history result
   * @throws IOException
   */
  public Map<Long, DSXTradeHistoryResult> getDSXTradeHistory(Long from, Long count, Long fromId, Long endId, DSXAuthenticated.SortOrder order,
      Long since, Long end, String pair) throws IOException {

    DSXTradeHistoryReturn dsxTradeHistory = dsx.TradeHistory(apiKey, signatureCreator, System.currentTimeMillis(), from, count, fromId, endId,
        order, since, end, pair);
    String error = dsxTradeHistory.getError();
    if (MSG_NO_TRADES.equals(error)) {
      return Collections.emptyMap();
    }

    checkResult(dsxTradeHistory);
    return dsxTradeHistory.getReturnValue();
  }

  /**
   * Get Map of transaction history from DSX exchange. All parameters are nullable
   *
   * @param from ID of the first transaction of the selection
   * @param count Number of transactions to display. Default value is 1000
   * @param fromId ID of the first transaction of the selection
   * @param endId ID of the last transaction of the selection
   * @param order Order in which transactions shown. Possible values: «asc» — from first to last, «desc» — from last to first. Default value is «desc»
   * @param since Time from which start selecting transaction by transaction time(UNIX time). If this value is not null order will become «asc»
   * @param end Time to which start selecting transaction by transaction time(UNIX time). If this value is not null order will become «asc»
   * @return Map of transaction history
   * @throws IOException
   */
  public Map<Long, DSXTransHistoryResult> getDSXTransHistory(Long from, Long count, Long fromId, Long endId, DSXAuthenticated.SortOrder order,
      Long since, Long end) throws IOException {

    DSXTransHistoryReturn dsxTransHistory = dsx.TransHistory(apiKey, signatureCreator, System.currentTimeMillis(), from, count, fromId, endId,
        order, since, end);
    String error = dsxTransHistory.getError();
    if (MSG_NO_TRADES.equals(error)) {
      return Collections.emptyMap();
    }

    checkResult(dsxTransHistory);
    return dsxTransHistory.getReturnValue();
  }

  /**
   * Get Map of order history from DSX exchange. All parameters are nullable
   * @param from ID of the first order of the selection
   * @param count Number of orders to display. Default value is 1000
   * @param fromId ID of the first order of the selection
   * @param endId ID of the last order of the selection
   * @param order Order in which transactions shown. Possible values: «asc» — from first to last, «desc» — from last to first. Default value is «desc»
   * @param since Time from which start selecting orders by trade time(UNIX time). If this value is not null order will become «asc»
   * @param end Time to which start selecting orders by trade time(UNIX time). If this value is not null order will become «asc»
   * @param pair Currency pair
   * @return Map of order history
   * @throws IOException
   */
  public Map<Long, DSXOrderHistoryResult> getDSXOrderHistory(Long from, Long count, Long fromId, Long endId, DSXAuthenticated.SortOrder order,
      Long since, Long end, String pair) throws IOException {

    DSXOrderHistoryReturn dsxOrderHistory = dsx.OrderHistory(apiKey, signatureCreator, System.currentTimeMillis(), from, count, fromId,
        endId, order, since, end, pair);
    String error = dsxOrderHistory.getError();
    if (MSG_NO_TRADES.equals(error)) {
      return Collections.emptyMap();
    }

    checkResult(dsxOrderHistory);
    return dsxOrderHistory.getReturnValue();
  }
}
