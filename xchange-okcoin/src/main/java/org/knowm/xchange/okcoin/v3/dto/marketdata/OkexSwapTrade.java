package org.knowm.xchange.okcoin.v3.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import org.knowm.xchange.okcoin.OkexExchangeV3;

public class OkexSwapTrade {

  private Date timestamp;
  private final BigDecimal price;
  private final BigDecimal size;
  private final long trade_id;
  private final String side;

  /**
   * Constructor
   *
   * @param timestamp
   * @param price
   * @param size
   * @param trade_id
   * @param side
   */
  public OkexSwapTrade(
      @JsonProperty("timestamp") final String timestamp,
      @JsonProperty("price") final BigDecimal price,
      @JsonProperty("size") final BigDecimal size,
      @JsonProperty("trade_id") final long trade_id,
      @JsonProperty("side") final String side) {

    try {

      this.timestamp = OkexExchangeV3.timestampFormatter.parse(timestamp);
    } catch (ParseException e) {
      this.timestamp = null;
    }
    this.price = price;
    this.size = size;
    this.trade_id = trade_id;
    this.side = side;
  }

  public Date getDate() {

    return timestamp;
  }

  public BigDecimal getPrice() {

    return price;
  }

  public BigDecimal getSize() {

    return size;
  }

  public long getTradeId() {

    return trade_id;
  }

  public String getSide() {

    return side;
  }

  @Override
  public String toString() {

    return "OkCoinFuturesTrade [date="
        + timestamp
        + ", price="
        + price
        + ", size="
        + size
        + ", tradeId="
        + trade_id
        + ", side="
        + side
        + "]";
  }
}
