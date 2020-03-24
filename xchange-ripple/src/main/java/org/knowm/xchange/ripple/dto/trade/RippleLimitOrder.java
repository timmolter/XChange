package org.knowm.xchange.ripple.dto.trade;

import java.math.BigDecimal;
import java.util.Date;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;

public class RippleLimitOrder extends LimitOrder {

  private final String baseCounterparty;
  private final String counterCounterparty;

  public RippleLimitOrder(
      final OrderType type,
      final BigDecimal originalAmount,
      final CurrencyPair currencyPair,
      final String id,
      final Date timestamp,
      final BigDecimal limitPrice,
      final String baseCounterparty,
      final String counterCounterparty) {
    super(type, originalAmount, currencyPair, id, timestamp, limitPrice);
    this.baseCounterparty = baseCounterparty;
    this.counterCounterparty = counterCounterparty;
  }

  public String getBaseCounterparty() {
    return baseCounterparty;
  }

  public String getCounterCounterparty() {
    return counterCounterparty;
  }

  public static class Builder extends LimitOrder.Builder {

    private String baseCounterparty = "";
    private String counterCounterparty = "";

    public Builder(final OrderType type, final CurrencyPair currencyPair) {
      super(type, currencyPair);
    }

    public static Builder from(final Order order) {
      final Builder builder = new Builder(order.getType(), order.getCurrencyPair());
      builder
          .id(order.getId())
          .orderType(order.getType())
          .originalAmount(order.getOriginalAmount())
          .currencyPair(order.getCurrencyPair())
          .timestamp(order.getTimestamp())
          .id(order.getId())
          .flags(order.getOrderFlags());

      if (order instanceof LimitOrder) {
        final LimitOrder limitOrder = (LimitOrder) order;
        builder.limitPrice(limitOrder.getLimitPrice());
      }

      if (order instanceof RippleLimitOrder) {
        final RippleLimitOrder ripple = (RippleLimitOrder) order;
        builder
            .baseCounterparty(ripple.getBaseCounterparty())
            .counterCounterparty(ripple.getCounterCounterparty());
      }

      return builder;
    }

    public Builder baseCounterparty(final String value) {
      baseCounterparty = value;
      return this;
    }

    public Builder counterCounterparty(final String value) {
      counterCounterparty = value;
      return this;
    }

    public RippleLimitOrder build() {
      final RippleLimitOrder order =
          new RippleLimitOrder(
              orderType,
              originalAmount,
              (CurrencyPair) instrument,
              id,
              timestamp,
              limitPrice,
              baseCounterparty,
              counterCounterparty);
      order.setOrderFlags(flags);
      return order;
    }
  }
}
