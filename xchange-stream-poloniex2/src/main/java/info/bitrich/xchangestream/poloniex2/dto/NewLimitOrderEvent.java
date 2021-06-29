package info.bitrich.xchangestream.poloniex2.dto;

import java.math.BigDecimal;

/** Created by Marcin Rabiej 22.05.2019 */
public class NewLimitOrderEvent {
  private String currencyPairId;
  private String orderId;
  private String orderType;
  private BigDecimal price;
  private BigDecimal amount;
  private String date;

  public NewLimitOrderEvent(
      String currencyPairId,
      String orderId,
      String orderType,
      BigDecimal price,
      BigDecimal amount,
      String date) {
    this.currencyPairId = currencyPairId;
    this.orderId = orderId;
    this.orderType = orderType;
    this.price = price;
    this.amount = amount;
    this.date = date;
  }

  public String getCurrencyPairId() {
    return currencyPairId;
  }

  public String getOrderId() {
    return orderId;
  }

  public String getOrderType() {
    return orderType;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getDate() {
    return date;
  }

  @Override
  public String toString() {
    return "NewLimitOrderEvent{"
        + "currencyPairId='"
        + currencyPairId
        + '\''
        + ", orderId='"
        + orderId
        + '\''
        + ", orderType='"
        + orderType
        + '\''
        + ", price="
        + price
        + ", amount="
        + amount
        + ", date='"
        + date
        + '\''
        + '}';
  }
}
