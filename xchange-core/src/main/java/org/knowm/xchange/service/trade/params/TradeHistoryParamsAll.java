package org.knowm.xchange.service.trade.params;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.service.trade.TradeService;

/**
 * Generic {@link TradeHistoryParams} implementation that implements all the interfaces in the
 * hierarchy and can be safely (without getting exceptions, if that all the required fields are
 * non-null) passed to any implementation of {@link
 * TradeService#getTradeHistory(TradeHistoryParams)} .
 */
public class TradeHistoryParamsAll
    implements TradeHistoryParamsTimeSpan,
        TradeHistoryParamPaging,
        TradeHistoryParamsIdSpan,
        TradeHistoryParamOffset,
        TradeHistoryParamCurrencyPair,
        TradeHistoryParamInstrument,
        TradeHistoryParamMultiCurrencyPair,
        TradeHistoryParamMultiInstrument,
        TradeHistoryParamLimit {

  private Integer pageLength;
  private Integer pageNumber;
  private String startId;
  private String endId;
  private Date startTime;
  private Date endTime;
  private Long offset;
  private Instrument instrument;
  private Collection<Instrument> instruments = Collections.emptySet();
  private Integer limit;

  @Override
  public Integer getPageLength() {

    return pageLength;
  }

  @Override
  public void setPageLength(Integer count) {

    this.pageLength = count;
  }

  @Override
  public String getStartId() {

    return startId;
  }

  @Override
  public void setStartId(String from) {

    startId = from;
  }

  @Override
  public String getEndId() {

    return endId;
  }

  @Override
  public void setEndId(String endId) {

    this.endId = endId;
  }

  @Override
  public Date getEndTime() {

    return endTime;
  }

  @Override
  public void setEndTime(Date to) {

    endTime = to;
  }

  @Override
  public Date getStartTime() {

    return startTime;
  }

  @Override
  public void setStartTime(Date startTime) {

    this.startTime = startTime;
  }

  @Override
  public Long getOffset() {

    if (offset != null || pageLength == null || pageNumber == null) return offset;
    else return (long) pageLength * pageNumber;
  }

  @Override
  public void setOffset(Long offset) {

    this.offset = offset;
  }

  @Override
  public Integer getPageNumber() {

    return pageNumber;
  }

  @Override
  public void setPageNumber(Integer pageNumber) {

    this.pageNumber = pageNumber;
  }

  @Override
  public Instrument getInstrument() {
    return instrument;
  }

  @Override
  public void setInstrument(Instrument instrument) {

    this.instrument = instrument;
  }

  @Override
  public Collection<Instrument> getInstruments() {
    return instruments;
  }

  @Override
  public void setInstruments(Collection<Instrument> value) {
    instruments = value;
  }

  /**
   * @deprecated CurrencyPair is a subtype of Instrument - this method will throw an exception if
   *     the instrument is not a CurrencyPair
   *     <p>use {@link #getInstrument()} instead
   */
  @Override
  @Deprecated
  public CurrencyPair getCurrencyPair() {
    if (!(instrument instanceof CurrencyPair)) {
      throw new IllegalStateException(
          "The instrument of this order is not a currency pair: " + instrument);
    }
    return (CurrencyPair) instrument;
  }

  /**
   * @deprecated CurrencyPair is a subtype of Instrument - this method will return an empty
   *     collection if none of the instruments are a CurrencyPair
   *     <p>use {@link #getInstruments()} instead
   */
  @Deprecated
  @Override
  public void setCurrencyPair(CurrencyPair pair) {

    this.instrument = pair;
  }

  /**
   * @deprecated CurrencyPair is a subtype of Instrument - this method will return an empty
   *     collection if none of the instruments are a CurrencyPair
   *     <p>use {@link #getInstruments()} instead
   */
  @Override
  @Deprecated
  public Collection<CurrencyPair> getCurrencyPairs() {
    ArrayList<CurrencyPair> paris = new ArrayList<CurrencyPair>();
    for (Instrument instrument : instruments)
      if ((instrument instanceof CurrencyPair)) {
        paris.add((CurrencyPair) instrument);
      }
    return paris;
  }
  /**
   * @deprecated CurrencyPair is a subtype of Instrument
   *     <p>use {@link #setInstruments()} instead
   */
  @Override
  @Deprecated
  public void setCurrencyPairs(Collection<CurrencyPair> value) {
    instruments = (Collection<Instrument>) (Collection<?>) value;
  }

  @Override
  public Integer getLimit() {
    return limit;
  }

  @Override
  public void setLimit(Integer limit) {
    this.limit = limit;
  }
}
