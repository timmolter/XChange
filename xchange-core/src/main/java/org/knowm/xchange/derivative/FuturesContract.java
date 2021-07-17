package org.knowm.xchange.derivative;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.instrument.Instrument;

public class FuturesContract extends Instrument
    implements Derivative, Comparable<FuturesContract>, Serializable {

  private static final long serialVersionUID = 6876906648149216819L;

  private static final ThreadLocal<DateFormat> DATE_PARSER =
      ThreadLocal.withInitial(() -> new SimpleDateFormat("yyMMdd"));
  private static final String PERPETUAL = "perpetual";

  private static final Comparator<FuturesContract> COMPARATOR =
      Comparator.comparing(FuturesContract::getCurrencyPair)
          .thenComparing(FuturesContract::getExpireDate);

  /** The CurrencyPair the FuturesContract is based upon */
  private final CurrencyPair currencyPair;

  /** The Date when the FuturesContract expires, when null it is perpetual */
  private final Date expireDate;

  public FuturesContract(CurrencyPair currencyPair, Date expireDate) {
    this.currencyPair = currencyPair;
    this.expireDate = expireDate;
  }

  @JsonCreator
  public FuturesContract(final String symbol) {
    String[] parts = symbol.split("-");
    if (parts.length < 3) {
      throw new IllegalArgumentException("Could not parse futures contract from '" + symbol + "'");
    }

    String base = parts[0];
    String counter = parts[1];
    String expireDate = parts[2];
    this.currencyPair = new CurrencyPair(base, counter);
    if (!PERPETUAL.equalsIgnoreCase(expireDate)) {
      try {
        this.expireDate = DATE_PARSER.get().parse(expireDate);
      } catch (ParseException e) {
        throw new IllegalArgumentException(
            "Could not parse expire date from '"
                + symbol
                + "'. It has to be either a 'yyMMdd' date or 'perpetual'");
      }
    } else {
      this.expireDate = null;
    }
  }

  @Override
  public CurrencyPair getCurrencyPair() {
    return currencyPair;
  }

  public Date getExpireDate() {
    return expireDate;
  }

  public boolean isPerpetual() {
    return this.expireDate == null;
  }

  @Override
  public int compareTo(final FuturesContract that) {
    return COMPARATOR.compare(this, that);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final FuturesContract contract = (FuturesContract) o;
    return Objects.equals(currencyPair, contract.currencyPair)
        && Objects.equals(expireDate, contract.expireDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(currencyPair, expireDate);
  }

  @JsonValue
  @Override
  public String toString() {

    return currencyPair.base + "-" + currencyPair.counter + "-" 
        + (expireDate == null ? PERPETUAL : DATE_PARSER.get().format(this.expireDate));
  }
}
