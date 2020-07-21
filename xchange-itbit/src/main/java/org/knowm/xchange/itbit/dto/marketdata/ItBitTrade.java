package org.knowm.xchange.itbit.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class ItBitTrade {

  private final BigDecimal amount;
  private final String timestamp;
  private final BigDecimal price;
  private final String matchNumber;

  public ItBitTrade(
      @JsonProperty("amount") BigDecimal amount,
      @JsonProperty("timestamp") String timestamp,
      @JsonProperty("price") BigDecimal price,
      @JsonProperty("matchNumber") String matchNumber) {

    this.amount = amount;
    this.timestamp = timestamp;
    this.price = price;
    this.matchNumber = matchNumber;
  }

  public BigDecimal getAmount() {

    return amount;
  }

  public String getTimestamp() {

    return timestamp;
  }

  public BigDecimal getPrice() {

    return price;
  }

  public String getMatchNumber() {

    return matchNumber;
  }

  @Override
  public String toString() {

    StringBuilder builder = new StringBuilder();
    builder.append("ItBitTrade [amount=");
    builder.append(amount);
    builder.append(", timestamp=");
    builder.append(timestamp);
    builder.append(", price=");
    builder.append(price);
    builder.append(", matchNumber=");
    builder.append(matchNumber);
    builder.append("]");
    return builder.toString();
  }
}
