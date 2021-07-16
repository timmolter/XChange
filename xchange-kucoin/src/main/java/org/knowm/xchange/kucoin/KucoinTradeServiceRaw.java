package org.knowm.xchange.kucoin;

import static org.knowm.xchange.kucoin.KucoinExceptionClassifier.classifyingExceptions;

import java.io.IOException;
import java.util.List;

import org.knowm.xchange.kucoin.dto.request.OrderCreateApiRequest;
import org.knowm.xchange.kucoin.dto.response.*;

public class KucoinTradeServiceRaw extends KucoinBaseService {

  protected KucoinTradeServiceRaw(KucoinExchange exchange) {
    super(exchange);
  }

  public Pagination<OrderResponse> getKucoinOpenOrders(String symbol, int page, int pageSize)
      throws IOException {
    return getKucoinOrders(symbol, "active", page, pageSize);
  }

  public Pagination<OrderResponse> getKucoinClosedOrders(String symbol, int page, int pageSize) throws IOException {
    return getKucoinOrders(symbol, "done", page, pageSize);
  }

  public Pagination<OrderResponse> getKucoinOrders(String symbol, String status, int page, int pageSize)
          throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
            () ->
                    orderApi.queryOrders(
                            apiKey,
                            digest,
                            nonceFactory,
                            passphrase,
                            symbol,
                            null,
                            null,
                            status,
                            null,
                            null,
                            pageSize,
                            page));
  }

  public OrderResponse getKucoinOrder(String id) throws IOException {
    checkAuthenticated();
    return classifyingExceptions(() -> orderApi.getOrder(apiKey, digest, nonceFactory, passphrase, id));
  }

  public Pagination<TradeResponse> getKucoinFills(
      String symbol, String orderId, int page, int pageSize, Long startAt, Long endAt) throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () ->
            fillApi.queryTrades(
                apiKey,
                digest,
                nonceFactory,
                passphrase,
                symbol,
                orderId,
                null,
                null,
                startAt,
                endAt,
                pageSize,
                page));
  }

  public Pagination<HistOrdersResponse> getKucoinHistOrders(
      String symbol, int page, int pageSize, Long startAt, Long endAt) throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () ->
            histOrdersApi.queryHistOrders(
                apiKey,
                digest,
                nonceFactory,
                passphrase,
                symbol,
                null,
                startAt,
                endAt,
                pageSize,
                page));
  }

  public OrderCancelResponse kucoinCancelAllOrders(String symbol) throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () -> orderApi.cancelOrders(apiKey, digest, nonceFactory, passphrase, symbol));
  }

  public OrderCancelResponse kucoinCancelOrder(String orderId) throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () -> orderApi.cancelOrder(apiKey, digest, nonceFactory, passphrase, orderId));
  }

  public OrderCreateResponse kucoinCreateOrder(OrderCreateApiRequest opsRequest)
      throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () -> orderApi.createOrder(apiKey, digest, nonceFactory, passphrase, opsRequest));
  }

  public List<OrderResponse> getKucoinRecentOrders() throws IOException {
    this.checkAuthenticated();
    return classifyingExceptions(() -> limitOrderAPI.getRecentOrders(
            this.apiKey,
            this.digest,
            this.nonceFactory,
            this.passphrase
    ));
  }

}
