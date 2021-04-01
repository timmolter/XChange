package org.knowm.xchange.bitfinex.v2.dto.marketdata;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

/**
 * @see https://docs.bitfinex.com/reference#rest-public-book
 */
@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class BitfinexTradingOrder {
  /** Price level  */
  BigDecimal price;
  /** Number of orders at that price level */
  int count;
  /**
   * Total amount available at that price level.
   * if AMOUNT > 0 then bid else as
   */
  BigDecimal amount;
}
