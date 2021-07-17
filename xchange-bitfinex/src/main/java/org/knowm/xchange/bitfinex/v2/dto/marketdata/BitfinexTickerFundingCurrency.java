package org.knowm.xchange.bitfinex.v2.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder({"symbol", "frr", "bid", "bidPeriod", "bidSize", "ask", "askPeriod", "askSize", "dailyChange", "dailyChangePerc", "lastPrice", "volume", "high", "low", "placeHolder0", "placeHolder1", "frrAmountAvailable"})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class BitfinexTickerFundingCurrency implements BitfinexTicker {

  private String symbol;
  private BigDecimal frr;
  private BigDecimal bid;
  private BigDecimal bidPeriod;
  private BigDecimal bidSize;
  private BigDecimal ask;
  private BigDecimal askPeriod;
  private BigDecimal askSize;
  private BigDecimal dailyChange;
  private BigDecimal dailyChangePerc;
  private BigDecimal lastPrice;
  private BigDecimal volume;
  private BigDecimal high;
  private BigDecimal low;
  private Object placeHolder0;
  private Object placeHolder1;
  private BigDecimal frrAmountAvailable;

  @Override
  public boolean isFundingCurrency() {
    return true;
  }

  @Override
  public String getSymbol() {
    return symbol;
  }

  @Override
  public BigDecimal getBid() {
    return bid;
  }

  @Override
  public BigDecimal getBidSize() {
    return bidSize;
  }

  @Override
  public BigDecimal getAsk() {
    return ask;
  }

  @Override
  public BigDecimal getAskSize() {
    return askSize;
  }

  @Override
  public BigDecimal getDailyChange() {
    return dailyChange;
  }

  @Override
  public BigDecimal getDailyChangePerc() {
    return dailyChangePerc;
  }

  @Override
  public BigDecimal getLastPrice() {
    return lastPrice;
  }

  @Override
  public BigDecimal getVolume() {
    return volume;
  }

  @Override
  public BigDecimal getHigh() {
    return high;
  }

  @Override
  public BigDecimal getLow() {
    return low;
  }

  public BigDecimal getFrr() {
    return frr;
  }

  public BigDecimal getBidPeriod() {
    return bidPeriod;
  }

  public BigDecimal getAskPeriod() {
    return askPeriod;
  }

  public Object getPlaceHolder0() {
    return placeHolder0;
  }

  public Object getPlaceHolder1() {
    return placeHolder1;
  }

  public BigDecimal getFrrAmountAvailable() {
    return frrAmountAvailable;
  }
}
