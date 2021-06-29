package info.bitrich.xchangestream.kraken.dto.enums;

import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

public enum KrakenEventType {
  heartbeat,
  subscribe,
  unsubscribe,
  systemStatus,
  subscriptionStatus,
  pingStatus,
  ping,
  pong,
  error,
  addOrder,
  addOrderStatus,
  cancelOrder,
  cancelOrderStatus;

  public static KrakenEventType getEvent(String event) {
    return Arrays.stream(KrakenEventType.values())
        .filter(e -> StringUtils.equalsIgnoreCase(event, e.name()))
        .findFirst()
        .orElse(null);
  }
}
