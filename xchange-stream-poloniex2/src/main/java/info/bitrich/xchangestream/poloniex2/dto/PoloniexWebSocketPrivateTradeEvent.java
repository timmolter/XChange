package info.bitrich.xchangestream.poloniex2.dto;

/** Created by Marcin Rabiej on 22.05.2019 */
public class PoloniexWebSocketPrivateTradeEvent extends PoloniexWebSocketEvent {
  private final PrivateTradeEvent privateTradeEvent;

  public PoloniexWebSocketPrivateTradeEvent(PrivateTradeEvent privateTradeEvent) {
    super("t");
    this.privateTradeEvent = privateTradeEvent;
  }

  public PrivateTradeEvent getPrivateTradeEvent() {
    return privateTradeEvent;
  }

  @Override
  public String toString() {
    return "PoloniexWebSocketPrivateTradeEvent{" + "privateTradeEvent=" + privateTradeEvent + '}';
  }
}
