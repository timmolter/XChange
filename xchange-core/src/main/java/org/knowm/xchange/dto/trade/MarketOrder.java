package org.knowm.xchange.dto.trade;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.instrument.Instrument;

/**
 * DTO representing a market order
 *
 * <p>A market order is a buy or sell order to be executed immediately at current market prices. As
 * long as there are willing sellers and buyers, market orders are filled. Market orders are
 * therefore used when certainty of execution is a priority over price of execution. <strong>Use
 * market orders with caution, and review {@link LimitOrder} in case it is more suitable.</strong>
 */
@JsonDeserialize(builder = MarketOrder.Builder.class)
public class MarketOrder extends Order {

  private static final long serialVersionUID = -3393286268772319210L;

  /**
   * @param type Either BID (buying) or ASK (selling)
   * @param originalAmount The amount to trade
   * @param instrument The identifier (e.g. BTC/USD)
   * @param id An id (usually provided by the exchange)
   * @param timestamp a Date object representing the order's timestamp according to the exchange's
   *     server, null if not provided
   * @param averagePrice the weighted average price of any fills belonging to the order
   * @param cumulativeAmount the amount that has been filled
   * @param fee the fee associated with this order
   * @param status the status of the order at the exchange or broker
   */
  public MarketOrder(
      OrderType type,
      BigDecimal originalAmount,
      Instrument instrument,
      String id,
      Date timestamp,
      BigDecimal averagePrice,
      BigDecimal cumulativeAmount,
      BigDecimal fee,
      OrderStatus status,
      String userReference) {
    super(
        type,
        originalAmount,
        instrument,
        id,
        timestamp,
        averagePrice,
        cumulativeAmount,
        fee,
        status,
        userReference);
  }

  /**
   * @param type Either BID (buying) or ASK (selling)
   * @param originalAmount The amount to trade
   * @param instrument The identifier (e.g. BTC/USD)
   * @param id An id (usually provided by the exchange)
   * @param timestamp a Date object representing the order's timestamp according to the exchange's
   *     server, null if not provided
   * @param averagePrice the weighted average price of any fills belonging to the order
   * @param cumulativeAmount the amount that has been filled
   * @param fee the fee associated with this order
   * @param status the status of the order at the exchange or broker
   */
  public MarketOrder(
      OrderType type,
      BigDecimal originalAmount,
      Instrument instrument,
      String id,
      Date timestamp,
      BigDecimal averagePrice,
      BigDecimal cumulativeAmount,
      BigDecimal fee,
      OrderStatus status) {
    super(
        type,
        originalAmount,
        instrument,
        id,
        timestamp,
        averagePrice,
        cumulativeAmount,
        fee,
        status);
  }

  /**
   * @param type Either BID (buying) or ASK (selling)
   * @param originalAmount The amount to trade
   * @param instrument The identifier (e.g. BTC/USD)
   * @param id An id (usually provided by the exchange)
   * @param timestamp the absolute time for this order
   */
  public MarketOrder(
      OrderType type, BigDecimal originalAmount, Instrument instrument, String id, Date timestamp) {

    super(type, originalAmount, instrument, id, timestamp);
  }

  /**
   * @param type Either BID (buying) or ASK (selling)
   * @param originalAmount The amount to trade
   * @param instrument The identifier (e.g. BTC/USD)
   * @param timestamp the absolute time for this order
   */
  public MarketOrder(
      OrderType type, BigDecimal originalAmount, Instrument instrument, Date timestamp) {

    super(type, originalAmount, instrument, "", timestamp);
  }

  /**
   * @param type Either BID (buying) or ASK (selling)
   * @param originalAmount The amount to trade
   * @param instrument The identifier (e.g. BTC/USD)
   */
  public MarketOrder(OrderType type, BigDecimal originalAmount, Instrument instrument) {

    super(type, originalAmount, instrument, "", null);
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static class Builder extends Order.Builder {

    @JsonCreator
    public Builder(
        @JsonProperty("orderType") OrderType orderType,
        @JsonProperty("instrument") Instrument instrument) {

      super(orderType, instrument);
    }

    public static Builder from(Order order) {

      return new Builder(order.getType(), order.getInstrument())
          .originalAmount(order.getOriginalAmount())
          .cumulativeAmount(order.getCumulativeAmount())
          .timestamp(order.getTimestamp())
          .id(order.getId())
          .flags(order.getOrderFlags())
          .averagePrice(order.getAveragePrice())
          .fee(order.getFee())
          .userReference(order.getUserReference())
          .orderStatus(order.getStatus());
    }

    @Override
    public Builder orderType(OrderType orderType) {

      return (Builder) super.orderType(orderType);
    }

    @Override
    public Builder orderStatus(OrderStatus status) {

      return (Builder) super.orderStatus(status);
    }

    @Override
    public Builder averagePrice(BigDecimal averagePrice) {

      return (Builder) super.averagePrice(averagePrice);
    }

    @Override
    public Builder cumulativeAmount(BigDecimal cumulativeAmount) {

      return (Builder) super.cumulativeAmount(cumulativeAmount);
    }

    @Override
    public Builder fee(BigDecimal fee) {

      return (Builder) super.fee(fee);
    }

    @Override
    public Builder originalAmount(BigDecimal originalAmount) {

      return (Builder) super.originalAmount(originalAmount);
    }

    @Override
    @Deprecated
    public Builder currencyPair(CurrencyPair currencyPair) {

      return (Builder) super.currencyPair(currencyPair);
    }

    @Override
    public Builder instrument(Instrument instrument) {

      return (Builder) super.instrument(instrument);
    }

    @Override
    public Builder id(String id) {

      return (Builder) super.id(id);
    }

    @Override
    public Builder userReference(String userReference) {

      return (Builder) super.userReference(userReference);
    }

    @Override
    public Builder timestamp(Date timestamp) {

      return (Builder) super.timestamp(timestamp);
    }

    @Override
    public Builder flags(Set<IOrderFlags> flags) {

      return (Builder) super.flags(flags);
    }

    @Override
    public Builder flag(IOrderFlags flag) {

      return (Builder) super.flag(flag);
    }

    @Override
    public MarketOrder build() {

      MarketOrder order =
          new MarketOrder(
              orderType,
              originalAmount,
              instrument,
              id,
              timestamp,
              averagePrice,
              cumulativeAmount,
              fee,
              status,
              userReference);
      order.setOrderFlags(flags);
      order.setLeverage(leverage);
      return order;
    }
  }
}
