package org.knowm.xchange.kucoin;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.io.IOException;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.Order.IOrderFlags;
import org.knowm.xchange.dto.marketdata.Trades.TradeSortType;
import org.knowm.xchange.dto.trade.*;
import org.knowm.xchange.kucoin.dto.response.*;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.*;
import org.knowm.xchange.service.trade.params.orders.DefaultOpenOrdersParamCurrencyPair;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParamCurrencyPair;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KucoinTradeService extends KucoinTradeServiceRaw implements TradeService {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  private static final int TRADE_HISTORIES_TO_FETCH = 500;
  private static final int ORDERS_TO_FETCH = 500;
  private static final long cutoffHistOrdersMillis =
      Date.from(java.time.LocalDate.of(2019, 2, 18).atStartOfDay(ZoneId.of("UTC+8")).toInstant())
          .getTime();
  // Although API doc says 7 days max timespan, KuCoin actually allows (almost) 8 days :)
  private static final long oneWeekMillis = (8 * 24 * 60 * 60 * 1000) - 1000;

  protected KucoinTradeService(KucoinExchange exchange) {
    super(exchange);
  }

  @Override
  public OpenOrders getOpenOrders() throws IOException {
    return convertOpenOrders(getKucoinOpenOrders(null, 1, ORDERS_TO_FETCH).getItems(), null);
  }

  @Override
  public OpenOrders getOpenOrders(OpenOrdersParams params) throws IOException {
    String symbol = null;
    if (params instanceof OpenOrdersParamCurrencyPair) {
      OpenOrdersParamCurrencyPair pairParams = (OpenOrdersParamCurrencyPair) params;
      symbol = KucoinAdapters.adaptCurrencyPair(pairParams.getCurrencyPair());
    }
    return convertOpenOrders(
        getKucoinOpenOrders(symbol, 1, TRADE_HISTORIES_TO_FETCH).getItems(), params);
  }

  @Override
  public UserTrades getTradeHistory(TradeHistoryParams tradeHistoryParams) throws IOException {
    String symbol = null;
    Long startTime = null;
    Long endTime = null;
    int page = 1;

    if (tradeHistoryParams != null) {
      TradeHistoryParamCurrencyPair params = (TradeHistoryParamCurrencyPair) tradeHistoryParams;
      symbol = KucoinAdapters.adaptCurrencyPair(params.getCurrencyPair());

      if (tradeHistoryParams instanceof TradeHistoryParamsTimeSpan) {
        if (((TradeHistoryParamsTimeSpan) tradeHistoryParams).getStartTime() != null) {
          startTime = ((TradeHistoryParamsTimeSpan) tradeHistoryParams).getStartTime().getTime();
        }
        if (((TradeHistoryParamsTimeSpan) tradeHistoryParams).getEndTime() != null) {
          endTime = ((TradeHistoryParamsTimeSpan) tradeHistoryParams).getEndTime().getTime();
        }
      }

      if (tradeHistoryParams instanceof TradeHistoryParamNextPageCursor) {
        String nextPageCursor = ((TradeHistoryParamNextPageCursor) params).getNextPageCursor();
        try {
          if (nextPageCursor != null) {
            page = Integer.parseInt(nextPageCursor);
          }
        } catch (NumberFormatException e) {
          logger.warn(
              "Could not parse next page cursor [{}]. Should be a page number (integer). {}",
              nextPageCursor,
              e.getMessage());
        }
      }
    }

    /*
      KuCoin restricts time spans to 7 days (as per API docs) on new fills API but not hist-orders. I.e. you could request
       a whole year of trades before 2019-2-18 but only 7 days of trades after that date.
    */
    if (startTime != null) {
      if (endTime == null) {
        if (startTime < cutoffHistOrdersMillis) {
          endTime = cutoffHistOrdersMillis - 1;
        } else {
          endTime = startTime + oneWeekMillis;
        }
        logger.warn(
            "End time not specified, adjusted to the following time span {} - {}",
            new Date(startTime),
            new Date(endTime));
      } else if (startTime < cutoffHistOrdersMillis && endTime > cutoffHistOrdersMillis) {
        endTime = cutoffHistOrdersMillis - 1;
        logger.warn(
            "End time after old API cutoff date, adjusted to the following time span {} - {}",
            new Date(startTime),
            new Date(endTime));
      } else if (startTime >= cutoffHistOrdersMillis && endTime - startTime > oneWeekMillis) {
        endTime = startTime + oneWeekMillis;
        logger.warn(
            "End time more than one week from start time, adjusted to the following time span {} - {}",
            new Date(startTime),
            new Date(endTime));
      }
    }

    if (endTime != null && startTime == null) {
      if (endTime > cutoffHistOrdersMillis) {
        startTime = Math.max(cutoffHistOrdersMillis, endTime - oneWeekMillis);
        logger.warn(
            "Start time not specified, adjusted to the following time span {} - {}",
            new Date(startTime),
            new Date(endTime));
      }
    }
    if (startTime == null && endTime == null) {
      endTime = new Date().getTime();
      startTime = endTime - oneWeekMillis;
      logger.warn(
          "No start or end time for trade history request specified, adjusted to the following time span {} - {}",
          new Date(startTime),
          new Date(endTime));
    }

    List<UserTrade> userTrades;
    String nextPageCursor = null;
    if (startTime != null && startTime >= cutoffHistOrdersMillis) {
      Pagination<TradeResponse> fills =
          getKucoinFills(symbol, null, page, TRADE_HISTORIES_TO_FETCH, startTime, endTime);
      userTrades =
          fills.getItems().stream()
              .map(KucoinAdapters::adaptUserTrade)
              .collect(Collectors.toList());
      if (fills.getTotalPage() > fills.getCurrentPage()) {
        nextPageCursor = Integer.toString(fills.getCurrentPage() + 1);
      }
    } else {
      Pagination<HistOrdersResponse> histOrders =
          getKucoinHistOrders(
              symbol,
              page,
              TRADE_HISTORIES_TO_FETCH,
              startTime != null ? startTime / 1000 : null,
              endTime / 1000);
      userTrades =
          histOrders.getItems().stream()
              .map(KucoinAdapters::adaptHistOrder)
              .collect(Collectors.toList());
      if (histOrders.getTotalPage() > histOrders.getCurrentPage()) {
        nextPageCursor = Integer.toString(histOrders.getCurrentPage() + 1);
      }
    }

    return new UserTrades(userTrades, 0, TradeSortType.SortByTimestamp, nextPageCursor);
  }

  @Override
  public OpenOrdersParamCurrencyPair createOpenOrdersParams() {
    return new DefaultOpenOrdersParamCurrencyPair();
  }

  @Override
  public TradeHistoryParams createTradeHistoryParams() {
    return new KucoinTradeHistoryParams();
  }

  @Override
  public boolean cancelOrder(String orderId) throws IOException {
    OrderCancelResponse response = kucoinCancelOrder(orderId);
    return response.getCancelledOrderIds().contains(orderId);
  }

  @Override
  public boolean cancelOrder(CancelOrderParams genericParams) throws IOException {
    Preconditions.checkNotNull(genericParams, "No parameter supplied");
    Preconditions.checkArgument(
        genericParams instanceof CancelOrderByIdParams,
        "Only order id parameters are currently supported.");
    CancelOrderByIdParams params = (CancelOrderByIdParams) genericParams;
    return cancelOrder(params.getOrderId());
  }

  @Override
  public String placeLimitOrder(LimitOrder limitOrder) throws IOException {
    return kucoinCreateOrder(KucoinAdapters.adaptLimitOrder(limitOrder)).getOrderId();
  }

  @Override
  public String placeMarketOrder(MarketOrder marketOrder) throws IOException {
    return kucoinCreateOrder(KucoinAdapters.adaptMarketOrder(marketOrder)).getOrderId();
  }

  @Override
  public String placeStopOrder(StopOrder stopOrder) throws IOException {
    return kucoinCreateOrder(KucoinAdapters.adaptStopOrder(stopOrder)).getOrderId();
  }

  private OpenOrders convertOpenOrders(Collection<OrderResponse> orders, OpenOrdersParams params) {
    Builder<LimitOrder> openOrders = ImmutableList.builder();
    Builder<Order> hiddenOrders = ImmutableList.builder();
    orders.stream()
        .map(KucoinAdapters::adaptOrder)
        .filter(o -> params == null || params.accept(o))
        .forEach(
            o -> {
              if (o instanceof LimitOrder) {
                openOrders.add((LimitOrder) o);
              } else {
                hiddenOrders.add(o);
              }
            });
    return new OpenOrders(openOrders.build(), hiddenOrders.build());
  }

  /** TODO same as Binance. Should be merged into generic API */
  public interface KucoinOrderFlags extends IOrderFlags {
    String getClientId();
  }
}
