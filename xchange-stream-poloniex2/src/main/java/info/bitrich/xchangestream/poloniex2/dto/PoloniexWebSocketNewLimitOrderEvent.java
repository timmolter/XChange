package info.bitrich.xchangestream.poloniex2.dto;

/** Created by Marcin Rabiej on 22.05.2019 */
public class PoloniexWebSocketNewLimitOrderEvent extends PoloniexWebSocketEvent {
  private final NewLimitOrderEvent newLimitOrderEvent;

  public PoloniexWebSocketNewLimitOrderEvent(NewLimitOrderEvent newLimitOrderEvent) {
    super("n");
    this.newLimitOrderEvent = newLimitOrderEvent;
  }

  public NewLimitOrderEvent getNewLimitOrderEvent() {
    return newLimitOrderEvent;
  }

  @Override
  public String toString() {
    return "PoloniexWebSocketNewLimitOrderEvent{"
        + "newLimitOrderEvent="
        + newLimitOrderEvent
        + '}';
  }
}
