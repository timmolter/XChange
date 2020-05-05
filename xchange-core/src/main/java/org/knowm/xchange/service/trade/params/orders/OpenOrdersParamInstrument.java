package org.knowm.xchange.service.trade.params.orders;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.instrument.Instrument;

public interface OpenOrdersParamInstrument extends OpenOrdersParams {
  @Override
  default boolean accept(LimitOrder order) {
    return accept((Order) order);
  }

  @Override
  default boolean accept(Order order) {
    return order != null
        && (getInstrument() == null || getInstrument().equals(order.getInstrument()));
  }

  CurrencyPair getInstrument();

  void setInstrument(Instrument instrument);
}
