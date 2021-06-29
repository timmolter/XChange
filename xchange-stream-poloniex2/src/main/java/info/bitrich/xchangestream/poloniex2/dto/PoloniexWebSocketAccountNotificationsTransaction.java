package info.bitrich.xchangestream.poloniex2.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Created by Marcin Rabiej 22.05.2019 */
@JsonFormat(shape = JsonFormat.Shape.ARRAY)
public class PoloniexWebSocketAccountNotificationsTransaction {
  public String channelId;
  public String seqId;
  public JsonNode[] jsonEvents;

  public PoloniexWebSocketEvent[] getEvents() {
    if (jsonEvents == null) {
      return new PoloniexWebSocketEvent[0];
    }
    List<PoloniexWebSocketEvent> events = new ArrayList<>(jsonEvents.length);
    for (JsonNode jsonNode : jsonEvents) {
      String eventType = jsonNode.get(0).asText();
      switch (eventType) {
        case "b":
          BalanceUpdatedEvent balanceUpdatedEvent =
              new BalanceUpdatedEvent(
                  jsonNode.get(1).asText(),
                  jsonNode.get(2).asText(),
                  new BigDecimal(jsonNode.get(3).asText()));
          events.add(new PoloniexWebSocketBalanceUpdateEvent(balanceUpdatedEvent));
          break;
        case "o":
          OrderUpdatedEvent orderUpdatedEvent =
              new OrderUpdatedEvent(
                  jsonNode.get(1).asText(), new BigDecimal(jsonNode.get(2).asText()));
          events.add(new PoloniexWebSocketOrderUpdateEvent(orderUpdatedEvent));
          break;
        case "t":
          PrivateTradeEvent privateTradeEvent =
              new PrivateTradeEvent(
                  jsonNode.get(1).asText(),
                  new BigDecimal(jsonNode.get(2).asText()),
                  new BigDecimal(jsonNode.get(3).asText()),
                  new BigDecimal(jsonNode.get(4).asText()),
                  jsonNode.get(5).asText(),
                  jsonNode.get(6).asText(),
                  new BigDecimal(jsonNode.get(7).asText()),
                  jsonNode.get(8).asText());
          events.add(new PoloniexWebSocketPrivateTradeEvent(privateTradeEvent));
          break;
        case "n":
          NewLimitOrderEvent newLimitOrderEvent =
              new NewLimitOrderEvent(
                  jsonNode.get(1).asText(),
                  jsonNode.get(2).asText(),
                  jsonNode.get(3).asText(),
                  new BigDecimal(jsonNode.get(4).asText()),
                  new BigDecimal(jsonNode.get(5).asText()),
                  jsonNode.get(6).asText());
          events.add(new PoloniexWebSocketNewLimitOrderEvent(newLimitOrderEvent));
          break;
        default:
          break;
      }
    }

    return events.toArray(new PoloniexWebSocketEvent[0]);
  }

  public String getChannelId() {
    return channelId;
  }

  public String getSeqId() {
    return seqId;
  }
}
