package org.knowm.xchange.gateio.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.knowm.xchange.currency.CurrencyPair;

@Getter
@Setter
/** Author: Max Gao (gaamox@tutanota.com) Created: 05-05-2021 */
public abstract class GateioWebSocketTransaction {
  @JsonProperty("time")
  private int time;

  @JsonProperty("channel")
  private String channel;

  @JsonProperty("event")
  private String event;

  public abstract CurrencyPair getCurrencyPair();
}
