package info.bitrich.xchangestream.poloniex2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Created by Marcin Rabiej 22.05.2019 */
public class PoloniexWebSocketAccountNotificationsSubscriptionMessage {

  private static final String COMMAND = "command";
  private static final String CHANNEL = "channel";
  private static final String KEY = "key";
  private static final String PAYLOAD = "payload";
  private static final String SIGN = "sign";

  @JsonProperty(COMMAND)
  private String command;

  @JsonProperty(CHANNEL)
  private String channel;

  @JsonProperty(KEY)
  private String key;

  @JsonProperty(PAYLOAD)
  private String payload;

  @JsonProperty(SIGN)
  private String sign;

  public PoloniexWebSocketAccountNotificationsSubscriptionMessage(
      String command, String channel, String key, String payload, String sign) {
    this.command = command;
    this.channel = channel;
    this.key = key;
    this.payload = payload;
    this.sign = sign;
  }

  @Override
  public String toString() {
    return "PoloniexWebSocketAccountNotificationsSubscriptionMessage{"
        + "command='"
        + command
        + '\''
        + ", channel='"
        + channel
        + '\''
        + ", key='"
        + key
        + '\''
        + ", payload='"
        + payload
        + '\''
        + ", sign='"
        + sign
        + '\''
        + '}';
  }
}
