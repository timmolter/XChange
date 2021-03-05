package info.bitrich.xchangestream.btcmarkets;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import info.bitrich.xchangestream.btcmarkets.dto.BTCMarketsWebSocketSubscribeMessage;
import info.bitrich.xchangestream.btcmarkets.dto.BTCMarketsWebSocketUnsubscribeMessage;
import info.bitrich.xchangestream.service.netty.JsonNettyStreamingService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BTCMarketsStreamingService extends JsonNettyStreamingService {
    static final String CHANNEL_ORDERBOOK = "orderbook";
    static final String CHANNEL_TICK = "tick";
    static final String CHANNEL_HEARTBEAT = "heartbeat";
    private static final Logger LOG = LoggerFactory.getLogger(BTCMarketsStreamingService.class);
    private final Set<String> subscribe = Sets.newConcurrentHashSet();
    private final Set<String> unsubscribe = Sets.newConcurrentHashSet();

    public BTCMarketsStreamingService(String apiUrl) {
        super(apiUrl);
    }

    private BTCMarketsWebSocketSubscribeMessage buildSubscribeMessage() {
        return new BTCMarketsWebSocketSubscribeMessage(
                new ArrayList<>(subscribe),
                Lists.newArrayList(CHANNEL_ORDERBOOK, CHANNEL_HEARTBEAT, CHANNEL_TICK),
                null,
                null,
                null);
    }

    private BTCMarketsWebSocketUnsubscribeMessage buildUnsubscribeMessage() {
        return new BTCMarketsWebSocketUnsubscribeMessage(
                new ArrayList<>(unsubscribe),
                Lists.newArrayList(CHANNEL_ORDERBOOK, CHANNEL_HEARTBEAT, CHANNEL_TICK),
                null,
                null,
                null);
    }

    @Override
    protected String getChannelNameFromMessage(JsonNode message) {
        final String messageType = message.get("messageType").asText();
        if (messageType.startsWith(CHANNEL_TICK)) {
            return messageType + ":" + message.get("marketId").asText();
        } else if (messageType.startsWith(CHANNEL_ORDERBOOK)) {
            return messageType + ":" + message.get("marketId").asText();
        } else
            return messageType;
    }

    @Override
    public String getSubscribeMessage(String channelName, Object... args) throws IOException {
        if (args[0] instanceof String) {
            if (CHANNEL_TICK.equals(channelName)) {
                subscribe.add(args[0].toString());
                LOG.debug("Now subscribed to Ticker {}", subscribe);
                return objectMapper.writeValueAsString(buildSubscribeMessage());
            } else if (CHANNEL_ORDERBOOK.equals(channelName)) {
                subscribe.add(args[0].toString());
                LOG.debug("Now subscribed to Orderbook {}", subscribe);
                return objectMapper.writeValueAsString(buildSubscribeMessage());
            } else {
                throw new IllegalArgumentException(
                        "Can't create subscribe messsage for channel " + channelName);
            }
        } else {
            LOG.debug("arg[0] is not a String type");
        }
        return null;
    }

    public String getSubscriptionUniqueId(String channelName, Object... args) {
        switch (channelName) {
            case CHANNEL_TICK:
            case CHANNEL_ORDERBOOK:
                return channelName + ":" + args[0].toString();
            default:
                return channelName;
        }
    }

    @Override
    public String getUnsubscribeMessage(String channelName) throws IOException {
        if (channelName.startsWith(CHANNEL_TICK)) {
            subscribe.remove(channelName);
            LOG.debug("Now Unsubscribing the Ticker {}", subscribe);
            return objectMapper.writeValueAsString(buildUnsubscribeMessage());
        } else if (channelName.startsWith(CHANNEL_ORDERBOOK)) {
            subscribe.remove(channelName);
            LOG.debug("Now Unsubscribing the Orderbook {}", subscribe);
            return objectMapper.writeValueAsString(buildUnsubscribeMessage());
        } else {
            return null;
        }
    }
}
