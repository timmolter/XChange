package org.knowm.xchange.instrument;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.Serializable;
import org.knowm.xchange.utils.jackson.InstrumentDeserializer;

/**
 * Base object for financial instruments supported in xchange such as CurrencyPair, Future or Option
 */
@JsonDeserialize(using = InstrumentDeserializer.class)
public abstract class Instrument implements Serializable {

  private static final long serialVersionUID = 414711266389792746L;
}
