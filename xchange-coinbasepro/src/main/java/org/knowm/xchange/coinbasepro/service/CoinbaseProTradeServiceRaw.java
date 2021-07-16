package org.knowm.xchange.coinbasepro.service;

import static org.knowm.xchange.coinbasepro.CoinbaseProResilience.PRIVATE_REST_ENDPOINT_RATE_LIMITER;

import java.io.IOException;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.coinbasepro.CoinbaseProAdapters;
import org.knowm.xchange.coinbasepro.CoinbaseProExchange;
import org.knowm.xchange.coinbasepro.dto.CoinbasePagedResponse;
import org.knowm.xchange.coinbasepro.dto.CoinbaseProException;
import org.knowm.xchange.coinbasepro.dto.trade.*;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.service.trade.params.TradeHistoryParamCurrencyPair;
import org.knowm.xchange.service.trade.params.TradeHistoryParamLimit;
import org.knowm.xchange.service.trade.params.TradeHistoryParamTransactionId;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.utils.timestamp.UnixTimestampFactory;

public class CoinbaseProTradeServiceRaw extends CoinbaseProBaseService {

  public CoinbaseProTradeServiceRaw(
      CoinbaseProExchange exchange, ResilienceRegistries resilienceRegistries) {
    super(exchange, resilienceRegistries);
  }

  /** https://docs.pro.coinbase.com/#list-orders */
  public CoinbaseProOrder[] getCoinbaseProOpenOrders() throws IOException {
    try {
      return decorateApiCall(
              () ->
                  coinbasePro.getListOrders(
                      apiKey, digest, UnixTimestampFactory.INSTANCE.createValue(), passphrase))
          .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
          .call();
    } catch (CoinbaseProException e) {
      throw handleError(e);
    }
  }

  /** https://docs.pro.coinbase.com/#list-orders */
  public CoinbaseProOrder[] getCoinbaseProOpenOrders(String productId) throws IOException {
    try {
      return decorateApiCall(
              () ->
                      coinbasePro.getListOrders(
                              apiKey,
                              digest,
                              UnixTimestampFactory.INSTANCE.createValue(),
                              passphrase,
                              "open",
                              productId))
              .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
              .call();
    } catch (CoinbaseProException e) {
      throw handleError(e);
    }
  }

  /** https://docs.pro.coinbase.com/#fills */
  public CoinbasePagedResponse<CoinbaseProFill> getCoinbaseProFills(
      TradeHistoryParams tradeHistoryParams) throws IOException {

    String orderId = null;
    String productId = null;
    Integer afterTradeId = null;
    Integer beforeTradeId = null;
    Integer limit = null;

    if (tradeHistoryParams instanceof CoinbaseProTradeHistoryParams) {
      CoinbaseProTradeHistoryParams historyParams =
          (CoinbaseProTradeHistoryParams) tradeHistoryParams;
      afterTradeId = historyParams.getAfterTradeId();
      beforeTradeId = historyParams.getBeforeTradeId();
    }

    if (tradeHistoryParams instanceof TradeHistoryParamTransactionId) {
      TradeHistoryParamTransactionId tnxIdParams =
          (TradeHistoryParamTransactionId) tradeHistoryParams;
      orderId = tnxIdParams.getTransactionId();
    }

    if (tradeHistoryParams instanceof TradeHistoryParamCurrencyPair) {
      TradeHistoryParamCurrencyPair ccyPairParams =
          (TradeHistoryParamCurrencyPair) tradeHistoryParams;
      CurrencyPair currencyPair = ccyPairParams.getCurrencyPair();
      if (currencyPair != null) {
        productId = CoinbaseProAdapters.adaptProductID(currencyPair);
      }
    }

    if (tradeHistoryParams instanceof TradeHistoryParamLimit) {
      TradeHistoryParamLimit limitParams = (TradeHistoryParamLimit) tradeHistoryParams;
      limit = limitParams.getLimit();
    }

    try {
      return coinbasePro.getFills(
          apiKey,
          digest,
          UnixTimestampFactory.INSTANCE.createValue(),
          passphrase,
          afterTradeId,
          beforeTradeId,
          limit,
          orderId,
          productId);
    } catch (CoinbaseProException e) {
      throw handleError(e);
    }
  }

  /** https://docs.pro.coinbase.com/#place-a-new-order */
  public CoinbaseProIdResponse placeCoinbaseProOrder(CoinbaseProPlaceOrder order)
      throws IOException {
    try {
      return decorateApiCall(
              () ->
                  coinbasePro.placeOrder(
                      order,
                      apiKey,
                      digest,
                      UnixTimestampFactory.INSTANCE.createValue(),
                      passphrase))
          .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
          .call();
    } catch (CoinbaseProException e) {
      throw handleError(e);
    }
  }

  /** https://docs.pro.coinbase.com/#cancel-an-order */
  public boolean cancelCoinbaseProOrder(String id) throws IOException {
    try {

      return decorateApiCall(
              () -> {
                coinbasePro.cancelOrder(
                    id, apiKey, digest, UnixTimestampFactory.INSTANCE.createValue(), passphrase);
                return true;
              })
          .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
          .call();
    } catch (CoinbaseProException e) {
      throw handleError(e);
    }
  }

  /** https://docs.pro.coinbase.com/#get-an-order */
  public CoinbaseProOrder getOrder(String id) throws IOException {
    try {
      return decorateApiCall(
              () ->
                  coinbasePro.getOrder(
                      id, apiKey, digest, UnixTimestampFactory.INSTANCE.createValue(), passphrase))
          .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
          .call();
    } catch (CoinbaseProException e) {
      throw handleError(e);
    }
  }

  /** https://docs.pro.coinbase.com/#list-orders */
  public CoinbasePagedResponse<CoinbaseProOrder> getOrders(
      String status, Integer limit, String after) throws IOException {
    try {
      return decorateApiCall(
              () ->
                  coinbasePro.getListOrders(
                      apiKey,
                      digest,
                      UnixTimestampFactory.INSTANCE.createValue(),
                      passphrase,
                      status,
                      limit,
                      after))
          .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
          .call();
    } catch (CoinbaseProException e) {
      throw handleError(e);
    }
  }
}
