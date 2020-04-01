package org.knowm.xchange.okcoin.v3.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.knowm.xchange.instrument.FuturesContract;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.instrument.SwapContract;
import org.knowm.xchange.okcoin.OkexAdaptersV3;
import org.knowm.xchange.okcoin.OkexExchangeV3;
import org.knowm.xchange.okcoin.v3.dto.account.FuturesLeverageResponse;
import org.knowm.xchange.okcoin.v3.dto.trade.FuturesMultipleOrderCancellationResponse;
import org.knowm.xchange.okcoin.v3.dto.trade.FuturesMultipleOrderPlacementRequest;
import org.knowm.xchange.okcoin.v3.dto.trade.FuturesOpenOrdersResponse;
import org.knowm.xchange.okcoin.v3.dto.trade.FuturesOrderPlacementRequest;
import org.knowm.xchange.okcoin.v3.dto.trade.OkexFuturePriceLimit;
import org.knowm.xchange.okcoin.v3.dto.trade.OkexFuturesOpenOrder;
import org.knowm.xchange.okcoin.v3.dto.trade.OkexFuturesTransaction;
import org.knowm.xchange.okcoin.v3.dto.trade.OkexOpenOrder;
import org.knowm.xchange.okcoin.v3.dto.trade.OkexSwapOpenOrder;
import org.knowm.xchange.okcoin.v3.dto.trade.OkexSwapTransaction;
import org.knowm.xchange.okcoin.v3.dto.trade.OkexTransaction;
import org.knowm.xchange.okcoin.v3.dto.trade.OrderBatchCancellationRequest;
import org.knowm.xchange.okcoin.v3.dto.trade.OrderCancellationRequest;
import org.knowm.xchange.okcoin.v3.dto.trade.OrderCancellationResponse;
import org.knowm.xchange.okcoin.v3.dto.trade.OrderPlacementResponse;
import org.knowm.xchange.okcoin.v3.dto.trade.SpotOrderPlacementRequest;
import org.knowm.xchange.okcoin.v3.dto.trade.SwapFuturesMultipleOrderPlacementResponse;
import org.knowm.xchange.okcoin.v3.dto.trade.SwapMultipleOrderCancellationResponse;
import org.knowm.xchange.okcoin.v3.dto.trade.SwapMultipleOrderPlacementRequest;
import org.knowm.xchange.okcoin.v3.dto.trade.SwapOpenOrdersResponse;
import org.knowm.xchange.okcoin.v3.dto.trade.SwapOrderBatchCancellationRequest;
import org.knowm.xchange.okcoin.v3.dto.trade.SwapOrderPlacementRequest;

public class OkexTradeServiceRaw extends OkexBaseService {

  protected OkexTradeServiceRaw(OkexExchangeV3 exchange) {
    super(exchange);
  }

  /** ******************************** Spot Token Trading API ********************************* */
  public OrderPlacementResponse spotPlaceOrder(SpotOrderPlacementRequest req) throws IOException {
    OrderPlacementResponse res = okex.spotPlaceOrder(apikey, digest, timestamp(), passphrase, req);
    res.checkResult();
    return res;
  }

  public Map<String, List<OrderPlacementResponse>> spotPlaceMultipleOrders(
      List<SpotOrderPlacementRequest> req) throws IOException {
    return okex.spotPlaceMultipleOrders(apikey, digest, timestamp(), passphrase, req);
  }

  public OrderCancellationResponse spotCancelOrder(String orderId, OrderCancellationRequest req)
      throws IOException {
    OrderCancellationResponse res =
        okex.spotCancelOrder(apikey, digest, timestamp(), passphrase, orderId, req);
    res.checkResult();
    return res;
  }

  public Map<String, List<OrderCancellationResponse>> spotCancelMultipleOrders(
      List<OrderBatchCancellationRequest> req) throws IOException {
    return okex.spotCancelMultipleOrders(apikey, digest, timestamp(), passphrase, req);
  }

  public OkexOpenOrder getSpotOrder(String instrumentId, String orderId) throws IOException {
    return okex.getSpotOrder(apikey, digest, timestamp(), passphrase, instrumentId, orderId);
  }

  public List<OkexOpenOrder> getSpotOrderList(
      String instrumentId, String from, String to, Integer limit, String state) throws IOException {
    return okex.getSpotOrderList(
        apikey, digest, timestamp(), passphrase, instrumentId, from, to, limit, state);
  }

  public List<OkexTransaction> getSpotTransactionDetails(
      String orderId, String instrumentId, String from, String to, Integer limit)
      throws IOException {
    return okex.getSpotTransactionDetails(
        apikey, digest, timestamp(), passphrase, orderId, instrumentId, from, to, limit);
  }

  /** ******************************** Futures Trading API ********************************* */
  public OkexFuturesOpenOrder getFuturesOrder(String instrumentId, String orderId)
      throws IOException {
    return okex.getFuturesOrder(apikey, digest, timestamp(), passphrase, instrumentId, orderId);
  }

  public List<OkexFuturesOpenOrder> getFuturesOrderList(
      String instrumentId, String from, String to, Integer limit, String state) throws IOException {
    FuturesOpenOrdersResponse res =
        okex.getFuturesOrderList(
            apikey, digest, timestamp(), passphrase, instrumentId, from, to, limit, state);
    res.checkResult();
    return res.getOrders();
  }

  public FuturesLeverageResponse getFuturesLeverage(String currency) throws IOException {
    FuturesLeverageResponse res =
        okex.getFuturesLeverage(apikey, digest, timestamp(), passphrase, currency);
    res.checkResult();
    return res;
  }

  public OrderPlacementResponse futuresPlaceOrder(FuturesOrderPlacementRequest req)
      throws IOException {
    OrderPlacementResponse res =
        okex.futuresPlaceOrder(apikey, digest, timestamp(), passphrase, req);
    res.checkResult();
    return res;
  }

  public SwapFuturesMultipleOrderPlacementResponse futuresPlaceMultipleOrders(
      FuturesMultipleOrderPlacementRequest req) throws IOException {
    SwapFuturesMultipleOrderPlacementResponse res =
        okex.futuresPlaceMultipleOrders(apikey, digest, timestamp(), passphrase, req);
    res.checkResult();
    return res;
  }

  public OrderCancellationResponse futuresCancelOrder(String instrumentId, String orderId)
      throws IOException {
    OrderCancellationResponse res =
        okex.futuresCancelOrder(apikey, digest, timestamp(), passphrase, instrumentId, orderId);
    res.checkResult();
    return res;
  }

  public FuturesMultipleOrderCancellationResponse futuresCancelMultipleOrders(
      String instrumentId, OrderBatchCancellationRequest req) throws IOException {
    FuturesMultipleOrderCancellationResponse res =
        okex.futuresCancelMultipleOrders(
            apikey, digest, timestamp(), passphrase, instrumentId, req);
    res.checkResult();
    return res;
  }

  public List<OkexFuturesTransaction> getFuturesTransactionDetails(
      String orderId, String instrumentId, String from, String to, Integer limit)
      throws IOException {
    return okex.getFuturesTransactionDetails(
        apikey, digest, timestamp(), passphrase, orderId, instrumentId, from, to, limit);
  }

  public OkexFuturePriceLimit getFuturesPriceLimits(Instrument instrument) throws IOException {
    if (instrument instanceof FuturesContract) {
      FuturesContract futuresContract = (FuturesContract) instrument;
      return okex.getFuturesPriceLimit(OkexAdaptersV3.toFuturesInstrument(futuresContract));
    }
    if (instrument instanceof SwapContract) {
      SwapContract swapContract = (SwapContract) instrument;
      return okex.getFuturesPriceLimit(OkexAdaptersV3.toSwapInstrument(swapContract));
    }
    return null;
  }

  /** ******************************** SWAP Trading API ********************************* */
  public OkexSwapOpenOrder getSwapOrder(String instrumentId, String orderId) throws IOException {
    return okex.getSwapOrder(apikey, digest, timestamp(), passphrase, instrumentId, orderId);
  }

  public List<OkexSwapOpenOrder> getSwapOrderList(
      String instrumentId, String from, String to, Integer limit, String state) throws IOException {
    SwapOpenOrdersResponse res =
        okex.getSwapOrderList(
            apikey, digest, timestamp(), passphrase, instrumentId, from, to, limit, state);
    res.checkResult();
    return res.getOrders();
  }

  public OrderPlacementResponse swapPlaceOrder(SwapOrderPlacementRequest req) throws IOException {
    OrderPlacementResponse res = okex.swapPlaceOrder(apikey, digest, timestamp(), passphrase, req);
    res.checkResult();
    return res;
  }

  public SwapFuturesMultipleOrderPlacementResponse swapPlaceMultipleOrders(
      SwapMultipleOrderPlacementRequest req) throws IOException {
    SwapFuturesMultipleOrderPlacementResponse res =
        okex.swapPlaceMultipleOrders(apikey, digest, timestamp(), passphrase, req);
    res.checkResult();
    return res;
  }

  public OrderCancellationResponse swapCancelOrder(String instrumentId, String orderId)
      throws IOException {
    OrderCancellationResponse res =
        okex.swapCancelOrder(apikey, digest, timestamp(), passphrase, instrumentId, orderId);
    res.checkResult();
    return res;
  }

  public SwapMultipleOrderCancellationResponse swapCancelMultipleOrders(
      String instrumentId, SwapOrderBatchCancellationRequest req) throws IOException {
    SwapMultipleOrderCancellationResponse res =
        okex.swapCancelMultipleOrders(apikey, digest, timestamp(), passphrase, instrumentId, req);
    res.checkResult();
    return res;
  }

  public List<OkexSwapTransaction> getSwapTransactionDetails(
      String orderId, String instrumentId, String from, String to, Integer limit)
      throws IOException {
    return okex.getSwapTransactionDetails(
        apikey, digest, timestamp(), passphrase, orderId, instrumentId, from, to, limit);
  }

  /** ******************************** MARGIN Trading API ********************************* */
  public OrderPlacementResponse marginPlaceOrder(SpotOrderPlacementRequest req) throws IOException {
    req.setMarginTrading("2");
    OrderPlacementResponse res =
        okex.marginPlaceOrder(apikey, digest, timestamp(), passphrase, req);
    res.checkResult();
    return res;
  }

  public List<OkexOpenOrder> getMarginOrderList(
      String instrumentId, String from, String to, Integer limit, String state) throws IOException {
    return okex.getMarginOrderList(
        apikey, digest, timestamp(), passphrase, instrumentId, from, to, limit, state);
  }

  public List<OkexTransaction> getMarginTransactionDetails(
      String orderId, String instrumentId, String from, String to, Integer limit)
      throws IOException {
    return okex.getMarginTransactionDetails(
        apikey, digest, timestamp(), passphrase, orderId, instrumentId, from, to, limit);
  }
}
