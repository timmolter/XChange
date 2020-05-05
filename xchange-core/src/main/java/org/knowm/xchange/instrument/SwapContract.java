package org.knowm.xchange.instrument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;

public class SwapContract extends Instrument implements Comparable<SwapContract>, Serializable {
  /** */
  private static final long serialVersionUID = -8400518639933611025L;

  public final String maturity;
  public final CurrencyPair underlier;

  /**
   * Full constructor In general the prompt is an approximation of the swap maturity you're wanting
   * to buy/sell.
   *
   * @param prompt The approximated maturity of the swap contract
   */
  public SwapContract(CurrencyPair underlier, String maturity) {
    this.underlier = underlier;
    this.maturity = maturity;
  }

  @JsonCreator
  public SwapContract(String swapContract) {

    String[] parts = swapContract.split("/");
    if (parts.length < 3) {
      throw new IllegalArgumentException(
          "Could not parse swap contract from '" + swapContract + "'");
    }

    String base = parts[0];
    String counter = parts[1];
    String maturity = parts[2];
    this.underlier = new CurrencyPair(base, counter);
    this.maturity = maturity;
  }

  @JsonValue
  @Override
  public String toString() {

    return underlier + "/" + maturity;
  }

  public boolean contains(Currency currency) {
    return underlier.contains(currency);
  }

  public boolean contains(String maturity) {
    return this.maturity.equals(maturity);
  }

  @Override
  public int hashCode() {

    final int prime = 31;
    int result = 1;
    result = prime * result + ((maturity == null) ? 0 : maturity.hashCode());
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
    SwapContract other = (SwapContract) obj;
    if (maturity == null) {
      if (other.maturity != null) {
        return false;
      }
    } else if (!maturity.equals(other.maturity)) {
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
  public int compareTo(SwapContract o) {
    return (maturity.compareTo(o.maturity) << 16) + underlier.compareTo(o.underlier);
  }
}
