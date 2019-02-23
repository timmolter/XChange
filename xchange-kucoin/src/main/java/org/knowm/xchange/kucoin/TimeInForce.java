package org.knowm.xchange.kucoin;

import org.knowm.xchange.dto.Order.IOrderFlags;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TimeInForce implements IOrderFlags {
  GTC,
  GTT,
  FOK,
  IOC;

  @JsonCreator
  static TimeInForce getTimeInForce(String s) {
    try {
      return TimeInForce.valueOf(s);
    } catch (Exception e) {
      throw new RuntimeException("Unknown ordtime in force " + s + ".");
    }
  }
}
