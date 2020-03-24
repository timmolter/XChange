package org.knowm.xchange.gemini.v1.dto.trade;

import java.math.BigDecimal;
import java.util.Date;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.trade.LimitOrder;

/**
 * Poloniex order response contains details of any trades that have just executed in the order entry
 * return value. If a LimitOrder of this type is supplied to the trade service orderEntry method it
 * will be populated with this information.
 */
public class GeminiLimitOrder extends LimitOrder {

  private GeminiOrderStatusResponse response = null;

  public GeminiLimitOrder(
      OrderType type,
      BigDecimal originalAmount,
      CurrencyPair currencyPair,
      String id,
      Date timestamp,
      BigDecimal limitPrice) {
    super(type, originalAmount, currencyPair, id, timestamp, limitPrice);
  }

  public GeminiOrderStatusResponse getResponse() {
    return response;
  }

  public void setResponse(GeminiOrderStatusResponse value) {
    response = value;
  }

  public static class Builder extends LimitOrder.Builder {

    public Builder(OrderType orderType, CurrencyPair currencyPair) {
      super(orderType, currencyPair);
    }

    public GeminiLimitOrder build() {
      final GeminiLimitOrder order =
          new GeminiLimitOrder(
              orderType, originalAmount, (CurrencyPair) instrument, id, timestamp, limitPrice);
      order.setOrderFlags(flags);
      return order;
    }
  }
}
