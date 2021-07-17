package org.knowm.xchange.okex.v5.dto.marketdata;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OkexTrade {

  private final String tradeId;
  private final String instId;
  private final BigDecimal px;
  private final String side;
  private final BigDecimal sz;
  private final Date ts;

  public OkexTrade(
      @JsonProperty("tradeId") String tradeId,
      @JsonProperty("instId") String instId,
      @JsonProperty("px") BigDecimal px,
      @JsonProperty("sz") BigDecimal sz,
      @JsonProperty("side") String side,
      @JsonProperty("ts") Date ts) {
    this.tradeId = tradeId;
    this.instId = instId;
    this.px = px;
    this.sz = sz;
    this.side = side;
    this.ts = ts;
  }

  public String getTradeId() {
    return tradeId;
  }

  public String getInstId() {
    return instId;
  }

  public BigDecimal getPx() {
    return px;
  }

  public BigDecimal getSz() {
    return sz;
  }

  public String getSide() {
    return side;
  }

  public Date getTs() {
    return ts;
  }

  @Override
  public String toString() {
    return "OkexTrade{"
        + "tradeId='"
        + tradeId
        + '\''
        + ", instId="
        + instId
        + ", px="
        + px
        + ", side="
        + side
        + ", sz="
        + sz
        + ", ts="
        + ts
        + '}';
  }
}
