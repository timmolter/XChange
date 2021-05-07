package org.knowm.xchange.bittrex.service;

import static org.knowm.xchange.bittrex.BittrexResilience.GET_CLOSED_ORDERS_RATE_LIMITER;

import java.io.IOException;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.knowm.xchange.bittrex.BittrexAdapters;
import org.knowm.xchange.bittrex.BittrexAuthenticated;
import org.knowm.xchange.bittrex.BittrexConstants;
import org.knowm.xchange.bittrex.BittrexExchange;
import org.knowm.xchange.bittrex.BittrexUtils;
import org.knowm.xchange.bittrex.dto.batch.BatchResponse;
import org.knowm.xchange.bittrex.dto.batch.order.BatchOrder;
import org.knowm.xchange.bittrex.dto.batch.order.neworder.TimeInForce;
import org.knowm.xchange.bittrex.dto.trade.BittrexExecution;
import org.knowm.xchange.bittrex.dto.trade.BittrexNewOrder;
import org.knowm.xchange.bittrex.dto.trade.BittrexOrder;
import org.knowm.xchange.bittrex.dto.trade.BittrexOrders;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParams;

public class BittrexTradeServiceRaw extends BittrexBaseService {

  /**
   * Constructor
   *
   * @param exchange
   */
  public BittrexTradeServiceRaw(
      BittrexExchange exchange,
      BittrexAuthenticated bittrex,
      ResilienceRegistries resilienceRegistries) {
    super(exchange, bittrex, resilienceRegistries);
  }

  public String placeBittrexLimitOrder(LimitOrder limitOrder) throws IOException {
    return placeBittrexLimitOrder(limitOrder, TimeInForce.GOOD_TIL_CANCELLED);
  }

  public String placeBittrexLimitOrder(LimitOrder limitOrder, TimeInForce type) throws IOException {
    BittrexNewOrder bittrexNewOrder =
        new BittrexNewOrder(
            BittrexUtils.toPairString(limitOrder.getCurrencyPair()),
            OrderType.BID.equals(limitOrder.getType())
                ? BittrexConstants.BUY
                : BittrexConstants.SELL,
            BittrexConstants.LIMIT,
            limitOrder.getRemainingAmount().toPlainString(),
            null,
            limitOrder.getLimitPrice().toPlainString(),
            type.toString(),
            null,
            null);
    return bittrexAuthenticated
        .placeOrder(
            apiKey, System.currentTimeMillis(), contentCreator, signatureCreator, bittrexNewOrder)
        .getId();
  }

  public String placeBittrexMarketOrder(MarketOrder marketOrder) throws IOException {
    return placeBittrexMarketOrder(marketOrder, TimeInForce.IMMEDIATE_OR_CANCEL);
  }

  public String placeBittrexMarketOrder(MarketOrder marketOrder, TimeInForce type)
      throws IOException {
    BittrexNewOrder bittrexNewOrder =
        new BittrexNewOrder(
            BittrexUtils.toPairString(marketOrder.getCurrencyPair()),
            OrderType.BID.equals(marketOrder.getType())
                ? BittrexConstants.BUY
                : BittrexConstants.SELL,
            BittrexConstants.MARKET,
            marketOrder.getRemainingAmount().toPlainString(),
            null,
            null,
            type.toString(),
            null,
            null);
    return bittrexAuthenticated
        .placeOrder(
            apiKey, System.currentTimeMillis(), contentCreator, signatureCreator, bittrexNewOrder)
        .getId();
  }

  public BittrexOrder cancelBittrexLimitOrder(String orderId) throws IOException {
    return bittrexAuthenticated.cancelOrder(
        apiKey, System.currentTimeMillis(), contentCreator, signatureCreator, orderId);
  }

  public List<BittrexOrder> getBittrexOpenOrders(OpenOrdersParams params) throws IOException {
    return bittrexAuthenticated.getOpenOrders(
        apiKey, System.currentTimeMillis(), contentCreator, signatureCreator);
  }

  public SequencedOpenOrders getBittrexSequencedOpenOrders(OpenOrdersParams params)
      throws IOException {
    BittrexOrders openOrders =
        bittrexAuthenticated.getOpenOrders(
            apiKey, System.currentTimeMillis(), contentCreator, signatureCreator);
    return new SequencedOpenOrders(
        openOrders.getSequence(), BittrexAdapters.adaptOpenOrders(openOrders));
  }

  public List<BittrexOrder> getBittrexUserTradeHistory(CurrencyPair currencyPair)
      throws IOException {
    return decorateApiCall(
            () ->
                bittrexAuthenticated.getClosedOrders(
                    apiKey,
                    System.currentTimeMillis(),
                    contentCreator,
                    signatureCreator,
                    BittrexUtils.toPairString(currencyPair),
                    200))
        .withRetry(retry("getClosedOrders"))
        .withRateLimiter(rateLimiter(GET_CLOSED_ORDERS_RATE_LIMITER))
        .call();
  }

  public List<BittrexOrder> getBittrexUserTradeHistory() throws IOException {
    return getBittrexUserTradeHistory(null);
  }

  public BittrexOrder getBittrexOrder(String orderId) throws IOException {
    return bittrexAuthenticated.getOrder(
        apiKey, System.currentTimeMillis(), contentCreator, signatureCreator, orderId);
  }

  public BatchResponse[] executeOrdersBatch(BatchOrder[] batchOrders) throws IOException {
    return bittrexAuthenticated.executeOrdersBatch(
        apiKey, System.currentTimeMillis(), contentCreator, signatureCreator, batchOrders);
  }

  public List<BittrexExecution> getBittrexOrderExecutions(String orderId) throws IOException {
    return bittrexAuthenticated.getOrderExecutions(
        apiKey, System.currentTimeMillis(), contentCreator, signatureCreator, orderId);
  }

  @AllArgsConstructor
  @Getter
  public static class SequencedOpenOrders {
    private final String sequence;
    private final List<LimitOrder> openOrders;
  }
}
