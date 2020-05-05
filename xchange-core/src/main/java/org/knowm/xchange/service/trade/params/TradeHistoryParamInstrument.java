package org.knowm.xchange.service.trade.params;

import org.knowm.xchange.instrument.Instrument;

public interface TradeHistoryParamInstrument extends TradeHistoryParams {

  Instrument getInstrument();

  void setInstrument(Instrument pair);
}
