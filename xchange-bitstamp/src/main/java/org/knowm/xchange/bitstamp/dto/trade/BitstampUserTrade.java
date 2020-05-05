package org.knowm.xchange.bitstamp.dto.trade;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.UserTrade;

public class BitstampUserTrade extends UserTrade {

  private BigDecimal originalVolume;

  public BitstampUserTrade(
      final Order.OrderType type,
      final BigDecimal originalAmount,
      final BigDecimal originalVolume,
      final CurrencyPair currencyPair,
      final BigDecimal price,
      final Date timestamp,
      final String id,
      final String orderId,
      final BigDecimal feeAmount,
      final Currency feeCurrency,
      final String orderUserReference) {
    super(
        type,
        originalAmount,
        currencyPair,
        price,
        timestamp,
        id,
        orderId,
        feeAmount,
        feeCurrency,
        orderUserReference);
    this.originalVolume = originalVolume;
  }

  public BigDecimal getOriginalVolume() {
    return originalVolume;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    final BitstampUserTrade that = (BitstampUserTrade) o;
    return Objects.equals(originalVolume, that.originalVolume);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), originalVolume);
  }

  @Override
  public String toString() {
    return "BitstampUserTrade[type="
        + type
        + ", originalAmount="
        + originalAmount
        + ", originalVolume="
        + originalVolume
        + ", currencyPair="
        + currencyPair
        + ", price="
        + price
        + ", timestamp="
        + timestamp
        + ", id="
        + id
        + ", orderId='"
        + getOrderId()
        + '\''
        + ", feeAmount="
        + getFeeAmount()
        + ", feeCurrency='"
        + getFeeCurrency()
        + '\''
        + ", orderUserReference='"
        + getOrderUserReference()
        + '\''
        + "]";
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static class Builder extends UserTrade.Builder {
    protected BigDecimal originalVolume;

    public static Builder from(BitstampUserTrade trade) {
      return new Builder()
          .type(trade.getType())
          .originalAmount(trade.getOriginalAmount())
          .originalVolume(trade.getOriginalVolume())
          .currencyPair(trade.getCurrencyPair())
          .price(trade.getPrice())
          .timestamp(trade.getTimestamp())
          .id(trade.getId())
          .orderId(trade.getOrderId())
          .feeAmount(trade.getFeeAmount())
          .feeCurrency(trade.getFeeCurrency())
          .orderUserReference(trade.getOrderUserReference());
    }

    @Override
    public Builder type(Order.OrderType type) {
      return (Builder) super.type(type);
    }

    @Override
    public Builder originalAmount(BigDecimal originalAmount) {
      return (Builder) super.originalAmount(originalAmount);
    }

    public Builder originalVolume(final BigDecimal originalVolume) {
      this.originalVolume = originalVolume;
      return this;
    }

    @Override
    public Builder currencyPair(CurrencyPair currencyPair) {
      return (Builder) super.currencyPair(currencyPair);
    }

    @Override
    public Builder price(BigDecimal price) {
      return (Builder) super.price(price);
    }

    @Override
    public Builder timestamp(Date timestamp) {
      return (Builder) super.timestamp(timestamp);
    }

    @Override
    public Builder id(String id) {
      return (Builder) super.id(id);
    }

    @Override
    public Builder orderId(String orderId) {
      return (Builder) super.orderId(orderId);
    }

    @Override
    public Builder feeAmount(BigDecimal feeAmount) {
      return (Builder) super.feeAmount(feeAmount);
    }

    @Override
    public Builder feeCurrency(Currency feeCurrency) {
      return (Builder) super.feeCurrency(feeCurrency);
    }

    @Override
    public Builder orderUserReference(String orderUserReference) {
      return (Builder) super.orderUserReference(orderUserReference);
    }

    @Override
    public BitstampUserTrade build() {
      return new BitstampUserTrade(
          type,
          originalAmount,
          originalVolume,
          currencyPair,
          price,
          timestamp,
          id,
          orderId,
          feeAmount,
          feeCurrency,
          orderUserReference);
    }
  }
}
