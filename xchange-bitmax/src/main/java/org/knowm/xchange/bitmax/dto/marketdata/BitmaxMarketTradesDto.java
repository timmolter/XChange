package org.knowm.xchange.bitmax.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class BitmaxMarketTradesDto implements Serializable {

  private final String m;

  private final String symbol;

  private final List<BitmaxMarketTradesData> data;

  @JsonCreator
  public BitmaxMarketTradesDto(
      @JsonProperty("m") String m,
      @JsonProperty("symbol") String symbol,
      @JsonProperty("data") List<BitmaxMarketTradesData> data) {
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

  public List<BitmaxMarketTradesData> getData() {
    return data;
  }

  @Override
  public String toString() {
    return "BitmaxMarketTradesDto{"
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

  public static class BitmaxMarketTradesData {
    private final String seqnum;

    private final BigDecimal price;

    private final BigDecimal quantity;

    private final Date timestamp;

    private final boolean isBuyerMaker;

    @JsonCreator
    public BitmaxMarketTradesData(
        @JsonProperty("seqnum") String seqnum,
        @JsonProperty("p") BigDecimal price,
        @JsonProperty("q") BigDecimal quantity,
        @JsonProperty("ts") Date timestamp,
        @JsonProperty("bm") boolean isBuyerMaker) {
      this.seqnum = seqnum;
      this.price = price;
      this.quantity = quantity;
      this.timestamp = timestamp;
      this.isBuyerMaker = isBuyerMaker;
    }

    public String getSeqnum() {
      return seqnum;
    }

    public BigDecimal getPrice() {
      return price;
    }

    public BigDecimal getQuantity() {
      return quantity;
    }

    public Date getTimestamp() {
      return timestamp;
    }

    public boolean isBuyerMaker() {
      return isBuyerMaker;
    }

    @Override
    public String toString() {
      return "BitmaxMarketTradesData{"
          + "seqnum='"
          + seqnum
          + '\''
          + ", price="
          + price
          + ", quantity="
          + quantity
          + ", timestamp="
          + timestamp
          + ", isBuyerMaker="
          + isBuyerMaker
          + '}';
    }
  }
}
