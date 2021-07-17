package org.knowm.xchange.okex.v5.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.exceptions.FundsExceededException;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.okex.v5.OkexAdapters;
import org.knowm.xchange.okex.v5.OkexExchange;
import org.knowm.xchange.okex.v5.dto.OkexException;
import org.knowm.xchange.okex.v5.dto.OkexResponse;
import org.knowm.xchange.okex.v5.dto.trade.OkexCancelOrderRequest;
import org.knowm.xchange.okex.v5.dto.trade.OkexOrderDetails;
import org.knowm.xchange.okex.v5.dto.trade.OkexOrderResponse;
import org.knowm.xchange.okex.v5.dto.trade.OkexTradeParams;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.CancelOrderByInstrumentAndIdParams;
import org.knowm.xchange.service.trade.params.CancelOrderParams;

/** Author: Max Gao (gaamox@tutanota.com) Created: 08-06-2021 */
public class OkexTradeService extends OkexTradeServiceRaw implements TradeService {
  public OkexTradeService(OkexExchange exchange, ResilienceRegistries resilienceRegistries) {
    super(exchange, resilienceRegistries);
  }

  @Override
  public OpenOrders getOpenOrders() throws IOException {
    return OkexAdapters.adaptOpenOrders(
        getOkexPendingOrder(null, null, null, null, null, null, null, null).getData());
  }

  public Order getOrder(Instrument instrument, String orderId) throws IOException {
    List<OkexOrderDetails> orderResults =
        getOkexOrder(OkexAdapters.adaptInstrumentId(instrument), orderId).getData();

    if (!orderResults.isEmpty()) {
      return OkexAdapters.adaptOrder(orderResults.get(0));
    } else {
      return null;
    }
  }

  @Override
  public String placeLimitOrder(LimitOrder limitOrder) throws IOException, FundsExceededException {
    OkexResponse<List<OkexOrderResponse>> okexResponse =
        placeOkexOrder(OkexAdapters.adaptOrder(limitOrder));

    if (okexResponse.isSuccess()) return okexResponse.getData().get(0).getOrderId();
    else
      throw new OkexException(
          okexResponse.getData().get(0).getMessage(),
          Integer.valueOf(okexResponse.getData().get(0).getCode()));
  }

  public List<String> placeLimitOrder(List<LimitOrder> limitOrders)
      throws IOException, FundsExceededException {
    return placeOkexOrder(
            limitOrders
                .stream()
                .map(order -> OkexAdapters.adaptOrder(order))
                .collect(Collectors.toList()))
        .getData()
        .stream()
        .map(result -> result.getOrderId())
        .collect(Collectors.toList());
  }

  @Override
  public String changeOrder(LimitOrder limitOrder) throws IOException, FundsExceededException {
    return amendOkexOrder(OkexAdapters.adaptAmendOrder(limitOrder)).getData().get(0).getOrderId();
  }

  public List<String> changeOrder(List<LimitOrder> limitOrders)
      throws IOException, FundsExceededException {
    return amendOkexOrder(
            limitOrders
                .stream()
                .map(order -> OkexAdapters.adaptAmendOrder(order))
                .collect(Collectors.toList()))
        .getData()
        .stream()
        .map(result -> result.getOrderId())
        .collect(Collectors.toList());
  }

  @Override
  public boolean cancelOrder(CancelOrderParams params) throws IOException {
    if (params instanceof CancelOrderByInstrumentAndIdParams) {

      String id = ((CancelOrderByInstrumentAndIdParams) params).getOrderId();
      String instrumentId =
          OkexAdapters.adaptInstrumentId(
              ((CancelOrderByInstrumentAndIdParams) params).getInstrument());

      OkexCancelOrderRequest req =
          OkexCancelOrderRequest.builder().instrumentId(instrumentId).orderId(id).build();

      return "0".equals(cancelOkexOrder(req).getData().get(0).getCode());
    } else {
      throw new IOException(
          "CancelOrderParams must implement CancelOrderByInstrumentAndIdParams interface.");
    }
  }

  public List<Boolean> cancelOrder(List<CancelOrderParams> params) throws IOException {
      return cancelOkexOrder(
              params
                  .stream()
                  .map(
                      param ->
                          OkexCancelOrderRequest.builder()
                              .orderId(((CancelOrderByInstrumentAndIdParams) param).getOrderId())
                              .instrumentId(
                                  OkexAdapters.adaptInstrumentId(
                                      ((CancelOrderByInstrumentAndIdParams) param)
                                          .getInstrument()))
                              .build())
                  .collect(Collectors.toList()))
          .getData()
          .stream()
          .map(result -> "0".equals(result.getCode()))
          .collect(Collectors.toList());
  }
}
