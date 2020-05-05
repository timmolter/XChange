package org.knowm.xchange.instrument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;

/** Delivery dates for future date currencies */
public class FuturesContract extends Instrument
    implements Comparable<FuturesContract>, Serializable {
  /** */
  private static final long serialVersionUID = -8400518639933611025L;

  public final String prompt;
  public final CurrencyPair underlier;

  /**
   * Full constructor In general the prompt is an approximation of the futures exipry you're wanting
   * to buy/sell.
   *
   * @param prompt The approximated expiry of the futures contract
   */
  public FuturesContract(CurrencyPair underlier, String prompt) {
    this.underlier = underlier;
    this.prompt = prompt;
  }

  @JsonCreator
  public FuturesContract(String futuresContract) {

    String[] parts = futuresContract.split("/");
    if (parts.length < 3) {
      throw new IllegalArgumentException(
          "Could not parse futures contract from '" + futuresContract + "'");
    }

    String base = parts[0];
    String counter = parts[1];
    String prompt = parts[2];
    this.underlier = new CurrencyPair(base, counter);
    this.prompt = prompt;
  }

  @JsonValue
  @Override
  public String toString() {

    return underlier + "/" + prompt;
  }

  public boolean contains(Currency currency) {
    return underlier.contains(currency);
  }

  public boolean contains(String prompt) {
    return this.prompt.equals(prompt);
  }

  @Override
  public int hashCode() {

    final int prime = 31;
    int result = 1;
    result = prime * result + ((prompt == null) ? 0 : prompt.hashCode());
    result = prime * result + ((underlier == null) ? 0 : underlier.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    FuturesContract other = (FuturesContract) obj;
    if (prompt == null) {
      if (other.prompt != null) {
        return false;
      }
    } else if (!prompt.equals(other.prompt)) {
      return false;
    }
    if (underlier == null) {
      if (other.underlier != null) {
        return false;
      }
    } else if (!underlier.equals(other.underlier)) {
      return false;
    }

    return true;
  }

  @Override
  public int compareTo(FuturesContract o) {
    return (prompt.compareTo(o.prompt) << 16) + underlier.compareTo(o.underlier);
  }
}
