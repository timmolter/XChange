package info.bitrich.xchangestream.poloniex2.dto;

import java.math.BigDecimal;

/** Created by Marcin Rabiej 22.05.2019 */
public class OrderUpdatedEvent {

  private String orderId;
  private BigDecimal amount;

  public OrderUpdatedEvent(String orderId, BigDecimal amount) {
    this.orderId = orderId;
    this.amount = amount;
  }

  public String getOrderId() {
    return orderId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  @Override
  public String toString() {
    return "OrderUpdatedEvent{" + "orderId='" + orderId + '\'' + ", amount=" + amount + '}';
  }
}
