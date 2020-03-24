package org.knowm.xchange.bitfinex.v1.dto.trade;

import java.math.BigDecimal;
import java.util.Date;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.trade.LimitOrder;

/**
 * Bitfinex new order entry returns details of order status. If a LimitOrder object of this type is
 * supplied to the trade service orderEntry method it will be populated with this information.
 */
public class BitfinexLimitOrder extends LimitOrder {
  private BigDecimal myOcoStopLimit;

  private BitfinexOrderStatusResponse response = null;

  public BitfinexLimitOrder(
      OrderType type,
      BigDecimal originalAmount,
      CurrencyPair currencyPair,
      String id,
      Date timestamp,
      BigDecimal limitPrice) {
    this(type, originalAmount, currencyPair, id, timestamp, limitPrice, null);
  }

  public BitfinexLimitOrder(
      OrderType type,
      BigDecimal originalAmount,
      CurrencyPair currencyPair,
      String id,
      Date timestamp,
      BigDecimal limitPrice,
      BigDecimal ocoStopLimit) {
    super(type, originalAmount, currencyPair, id, timestamp, limitPrice);
    myOcoStopLimit = ocoStopLimit;
  }

  public BitfinexOrderStatusResponse getResponse() {
    return response;
  }

  public void setResponse(BitfinexOrderStatusResponse value) {
    response = value;
  }

  public BigDecimal getOcoStopLimit() {
    return myOcoStopLimit;
  }

  public void setOcoStopLimit(BigDecimal ocoStopLimit) {
    myOcoStopLimit = ocoStopLimit;
  }

  public static class Builder extends LimitOrder.Builder {

    public Builder(OrderType orderType, CurrencyPair currencyPair) {
      super(orderType, currencyPair);
    }

    public BitfinexLimitOrder build() {
      final BitfinexLimitOrder order =
          new BitfinexLimitOrder(
              orderType, originalAmount, (CurrencyPair) instrument, id, timestamp, limitPrice);
      order.setOrderFlags(flags);
      return order;
    }
  }
}
