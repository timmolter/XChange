package info.bitrich.xchangestream.btcmarkets.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BTCMarketsWebSocketUnsubscribeMessage {
    @JsonProperty("messageType")
    public final String messageType = "removeSubscription";

    @JsonProperty("marketIds")
    public final List<String> marketIds;

    @JsonProperty("channels")
    public final List<String> channels;

    @JsonProperty("timestamp")
    public final Long timestamp;

    @JsonProperty("key")
    public final String key;

    @JsonProperty("signature")
    public final String signature;

    public BTCMarketsWebSocketUnsubscribeMessage(List<String> marketIds, List<String> channels, Long timestamp, String key, String signature) {
        this.marketIds = marketIds;
        this.channels = channels;
        this.timestamp = timestamp;
        this.key = key;
        this.signature = signature;
    }
}
