package org.knowm.xchange.service.trade.params;

import org.knowm.xchange.instrument.Instrument;

public interface CancelOrderByInstrument extends CancelOrderParams {

  public Instrument getInstrument();
}
