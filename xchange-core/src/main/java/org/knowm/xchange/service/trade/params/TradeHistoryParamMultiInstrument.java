package org.knowm.xchange.service.trade.params;

import java.util.Collection;
import org.knowm.xchange.instrument.Instrument;

public interface TradeHistoryParamMultiInstrument extends TradeHistoryParams {

  Collection<Instrument> getInstruments();

  void setInstruments(Collection<Instrument> instruments);
}
