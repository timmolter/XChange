package info.bitrich.xchangestream.poloniex2.dto;

/** Created by Marcin Rabiej on 22.05.2019 */
public class PoloniexWebSocketOrderUpdateEvent extends PoloniexWebSocketEvent {
  private final OrderUpdatedEvent orderUpdatedEvent;

  public PoloniexWebSocketOrderUpdateEvent(OrderUpdatedEvent orderUpdatedEvent) {
    super("o");
    this.orderUpdatedEvent = orderUpdatedEvent;
  }

  public OrderUpdatedEvent getOrderUpdatedEvent() {
    return orderUpdatedEvent;
  }

  @Override
  public String toString() {
    return "PoloniexWebSocketOrderUpdateEvent{" + "orderUpdatedEvent=" + orderUpdatedEvent + '}';
  }
}
