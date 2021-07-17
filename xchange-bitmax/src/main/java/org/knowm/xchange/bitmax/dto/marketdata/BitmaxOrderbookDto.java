package org.knowm.xchange.bitmax.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Date;
import java.util.List;

public class BitmaxOrderbookDto {

  private final String m;

  private final String symbol;

  private final BitmaxOrderbookData data;

  public BitmaxOrderbookDto(
      @JsonProperty("m") String m,
      @JsonProperty("symbol") String symbol,
      @JsonProperty("data") BitmaxOrderbookData data) {
    this.m = m;
    this.symbol = symbol;
    this.data = data;
  }

  public String getM() {
    return m;
  }

  public String getSymbol() {
    return symbol;
  }

  public BitmaxOrderbookData getData() {
    return data;
  }

  @Override
  public String toString() {
    return "BitmaxOrderbookDto{"
        + "m='"
        + m
        + '\''
        + ", symbol='"
        + symbol
        + '\''
        + ", data="
        + data
        + '}';
  }

  public static class BitmaxOrderbookData {
    private final Long seqnum;

    private final Date ts;

    private final List<BitmaxPublicOrder> asks;

    private final List<BitmaxPublicOrder> bids;

    public BitmaxOrderbookData(
        @JsonProperty("seqnum") Long seqnum,
        @JsonProperty("ts") Long ts,
        @JsonProperty("asks") List<BitmaxPublicOrder> asks,
        @JsonProperty("bids") List<BitmaxPublicOrder> bids) {
      this.seqnum = seqnum;
      this.ts = Date.from(Instant.ofEpochMilli(ts));
      this.asks = asks;
      this.bids = bids;
    }

    public Long getSeqnum() {
      return seqnum;
    }

    public Date getTimestamp() {
      return ts;
    }

    public List<BitmaxPublicOrder> getAsks() {
      return asks;
    }

    public List<BitmaxPublicOrder> getBids() {
      return bids;
    }

    @Override
    public String toString() {
      return "BitmaxOrderbookData{"
          + "seqnum="
          + seqnum
          + ", timestamp="
          + ts
          + ", asks="
          + asks
          + ", bids="
          + bids
          + '}';
    }
  }
}
