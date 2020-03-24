package org.knowm.xchange.dsx.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/** @author Mikhail Wall */
public class ClientDeal {

  private final Long number;
  private final String pair;
  private final String type;
  private final BigDecimal volume;
  private final BigDecimal rate;
  private final Long orderId;
  private final Long timestamp;
  private final BigDecimal commission;
  private final String commissionCurrency;

  public ClientDeal(
      @JsonProperty("number") Long number,
      @JsonProperty("pair") String pair,
      @JsonProperty("type") String type,
      @JsonProperty("volume") BigDecimal volume,
      @JsonProperty("rate") BigDecimal rate,
      @JsonProperty("orderId") long orderId,
      @JsonProperty("timestamp") long timestamp,
      @JsonProperty("commission") BigDecimal commission,
      @JsonProperty("commissionCurrency") String commissionCurrency) {
    this.number = number;
    this.pair = pair;
    this.type = type;
    this.volume = volume;
    this.rate = rate;
    this.timestamp = timestamp;
    this.orderId = orderId;
    this.commission = commission;
    this.commissionCurrency = commissionCurrency;
  }

  public Long getNumber() {
    return number;
  }

  public String getPair() {
    return pair;
  }

  public String getType() {
    return type;
  }

  public BigDecimal getVolume() {
    return volume;
  }

  public BigDecimal getRate() {
    return rate;
  }

  public Long getOrderId() {
    return orderId;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public BigDecimal getCommission() {
    return commission;
  }

  public String getCommissionCurrency() {
    return commissionCurrency;
  }
}