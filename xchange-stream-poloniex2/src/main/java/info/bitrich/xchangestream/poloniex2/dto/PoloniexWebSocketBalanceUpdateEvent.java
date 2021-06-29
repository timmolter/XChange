package info.bitrich.xchangestream.poloniex2.dto;

/** Created by Marcin Rabiej on 22.05.2019 */
public class PoloniexWebSocketBalanceUpdateEvent extends PoloniexWebSocketEvent {
  private final BalanceUpdatedEvent balanceUpdatedEvent;

  public PoloniexWebSocketBalanceUpdateEvent(BalanceUpdatedEvent balanceUpdatedEvent) {
    super("b");
    this.balanceUpdatedEvent = balanceUpdatedEvent;
  }

  public BalanceUpdatedEvent getBalanceUpdatedEvent() {
    return balanceUpdatedEvent;
  }

  @Override
  public String toString() {
    return "PoloniexWebSocketBalanceUpdateEvent{"
        + "balanceUpdatedEvent="
        + balanceUpdatedEvent
        + '}';
  }
}
