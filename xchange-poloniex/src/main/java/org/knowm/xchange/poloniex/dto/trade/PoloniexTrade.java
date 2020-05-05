package org.knowm.xchange.poloniex.dto.trade;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({"tradeId", "date", "rate", "amount", "total", "fee", "orderNumber", "type"})
public class PoloniexTrade {

  @JsonProperty("tradeID")
  private String tradeID;

  @JsonProperty("date")
  private String date;

  @JsonProperty("rate")
  private BigDecimal rate;

  @JsonProperty("amount")
  private BigDecimal amount;

  @JsonProperty("total")
  private BigDecimal total;

  @JsonProperty("fee")
  private BigDecimal fee;

  @JsonProperty("orderNumber")
  private String orderNumber;

  @JsonProperty("type")
  private String type;

  @JsonIgnore private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("tradeID")
  public String getTradeID() {

    return tradeID;
  }

  @JsonProperty("tradeID")
  public void setTradeID(String tradeID) {

    this.tradeID = tradeID;
  }

  @JsonProperty("date")
  public String getDate() {

    return date;
  }

  @JsonProperty("date")
  public void setDate(String date) {

    this.date = date;
  }

  @JsonProperty("rate")
  public BigDecimal getRate() {

    return rate;
  }

  @JsonProperty("rate")
  public void setRate(BigDecimal rate) {

    this.rate = rate;
  }

  @JsonProperty("amount")
  public BigDecimal getAmount() {

    return amount;
  }

  @JsonProperty("amount")
  public void setAmount(BigDecimal amount) {

    this.amount = amount;
  }

  @JsonProperty("total")
  public BigDecimal getTotal() {

    return total;
  }

  @JsonProperty("total")
  public void setTotal(BigDecimal total) {

    this.total = total;
  }

  @JsonProperty("fee")
  public BigDecimal getFee() {

    return fee;
  }

  @JsonProperty("fee")
  public void setFee(BigDecimal fee) {

    this.fee = fee;
  }

  @JsonProperty("orderNumber")
  public String getOrderNumber() {

    return orderNumber;
  }

  @JsonProperty("orderNumber")
  public void setOrderNumber(String orderNumber) {

    this.orderNumber = orderNumber;
  }

  @JsonProperty("type")
  public String getType() {

    return type;
  }

  @JsonProperty("type")
  public void setType(String type) {

    this.type = type;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {

    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {

    this.additionalProperties.put(name, value);
  }

  @Override
  public String toString() {

    return "PoloniexUserTrade [tradeID= "
        + tradeID
        + ", date="
        + date
        + ", rate="
        + rate
        + ", amount="
        + amount
        + ", total="
        + total
        + ", fee="
        + fee
        + ", orderNumber="
        + orderNumber
        + ", type="
        + type
        + ", additionalProperties="
        + additionalProperties
        + "]";
  }
}
