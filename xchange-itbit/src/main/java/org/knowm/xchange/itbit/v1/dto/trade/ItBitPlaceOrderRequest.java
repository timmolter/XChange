package org.knowm.xchange.itbit.v1.dto.trade;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ItBitPlaceOrderRequest {

  @JsonProperty("type")
  protected String type;

  @JsonProperty("currency")
  protected String baseCurrency;

  @JsonProperty("side")
  private String side;

  @JsonProperty("amount")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String amount;

  @JsonProperty("price")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String price;

  @JsonProperty("funds")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String funds;

  @JsonProperty("instrument")
  private String instrument;

  public ItBitPlaceOrderRequest(
      String side,
      String type,
      String baseCurrency,
      String amount,
      String price,
      String instrument) {

    super();
    this.side = side;
    this.type = type;
    this.baseCurrency = baseCurrency;
    this.amount = amount;
    this.price = price;
    this.instrument = instrument;
  }

  public ItBitPlaceOrderRequest(
          String side,
          String type,
          String baseCurrency,
          String funds,
          String instrument) {

    super();
    this.side = side;
    this.type = type;
    this.baseCurrency = baseCurrency;
    this.funds = funds;
    this.instrument = instrument;
  }

  public String getSide() {

    return side;
  }

  public String getType() {

    return type;
  }

  public String getBaseCurrency() {

    return baseCurrency;
  }

  public String getAmount() {

    return amount;
  }

  public String getPrice() {

    return price;
  }

  public String getFunds() {

    return funds;
  }

  public String getInstrument() {

    return instrument;
  }
}
