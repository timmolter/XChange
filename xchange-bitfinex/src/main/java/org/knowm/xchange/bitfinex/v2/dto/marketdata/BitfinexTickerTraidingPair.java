package org.knowm.xchange.bitfinex.v2.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder({"symbol", "bid", "bidSize", "ask", "askSize", "dailyChange", "dailyChangePerc", "lastPrice", "volume", "high", "low"})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class BitfinexTickerTraidingPair implements BitfinexTicker {

  private String symbol;
  private BigDecimal bid;
  private BigDecimal bidSize;
  private BigDecimal ask;
  private BigDecimal askSize;
  private BigDecimal dailyChange;
  private BigDecimal dailyChangePerc;
  private BigDecimal lastPrice;
  private BigDecimal volume;
  private BigDecimal high;
  private BigDecimal low;

  @Override
  public boolean isTradingPair() {
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
    return null;
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
}
