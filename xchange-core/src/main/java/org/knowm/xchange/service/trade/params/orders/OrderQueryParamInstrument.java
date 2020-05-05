package org.knowm.xchange.service.trade.params.orders;

import org.knowm.xchange.instrument.Instrument;

public interface OrderQueryParamInstrument extends OrderQueryParams {
  Instrument getInstrument();

  void setInstrument(Instrument instrument);
}
