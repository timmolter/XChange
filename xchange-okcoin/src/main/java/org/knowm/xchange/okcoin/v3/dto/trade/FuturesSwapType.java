package org.knowm.xchange.okcoin.v3.dto.trade;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

/** 1:open long 2:open short 3:close long 4:close short */
@AllArgsConstructor
public enum FuturesSwapType {
  open_long("1"),
  open_short("2"),
  close_long("3"),
  close_short("4");

  private final String value;

  @JsonValue
  public String getValue() {
    return value;
  }

  public boolean sell() {
    return this == open_short || this == close_long;
  }
}
