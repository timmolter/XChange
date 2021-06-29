package info.bitrich.xchangestream.poloniex2.dto;

import java.math.BigDecimal;

/** Created by Marcin Rabiej 22.05.2019 */
public class PrivateTradeEvent {
  private String tradeId;
  private BigDecimal price;
  private BigDecimal amount;
  private BigDecimal feeMultiplier;
  private String fundingType;
  private String orderNumber;
  private BigDecimal totalFee;
  private String date;

  public PrivateTradeEvent(
      String tradeId,
      BigDecimal price,
      BigDecimal amount,
      BigDecimal feeMultiplier,
      String fundingType,
      String orderNumber,
      BigDecimal totalFee,
      String date) {
    this.tradeId = tradeId;
    this.price = price;
    this.amount = amount;
    this.feeMultiplier = feeMultiplier;
    this.fundingType = fundingType;
    this.orderNumber = orderNumber;
    this.totalFee = totalFee;
    this.date = date;
  }

  public String getTradeId() {
    return tradeId;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public BigDecimal getFeeMultiplier() {
    return feeMultiplier;
  }

  public String getFundingType() {
    return fundingType;
  }

  public String getOrderNumber() {
    return orderNumber;
  }

  public BigDecimal getTotalFee() {
    return totalFee;
  }

  public String getDate() {
    return date;
  }

  @Override
  public String toString() {
    return "PrivateTradeEvent{"
        + "tradeId='"
        + tradeId
        + '\''
        + ", price="
        + price
        + ", amount="
        + amount
        + ", feeMultiplier="
        + feeMultiplier
        + ", fundingType='"
        + fundingType
        + '\''
        + ", orderNumber='"
        + orderNumber
        + '\''
        + ", totalFee="
        + totalFee
        + ", date='"
        + date
        + '\''
        + '}';
  }
}
