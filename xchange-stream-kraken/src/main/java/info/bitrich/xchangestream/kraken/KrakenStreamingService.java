package info.bitrich.xchangestream.kraken;

import static info.bitrich.xchangestream.kraken.dto.enums.KrakenEventType.subscribe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.kraken.dto.KrakenSubscriptionConfig;
import info.bitrich.xchangestream.kraken.dto.KrakenSubscriptionMessage;
import info.bitrich.xchangestream.kraken.dto.KrakenSubscriptionStatusMessage;
import info.bitrich.xchangestream.kraken.dto.KrakenSystemStatus;
import info.bitrich.xchangestream.kraken.dto.enums.KrakenEventType;
import info.bitrich.xchangestream.kraken.dto.enums.KrakenSubscriptionName;
import info.bitrich.xchangestream.service.netty.JsonNettyStreamingService;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import info.bitrich.xchangestream.service.netty.WebSocketClientCompressionAllowClientNoContextHandler;
import info.bitrich.xchangestream.service.netty.WebSocketClientHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketClientExtensionHandler;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.kraken.dto.account.KrakenWebsocketToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author makarid, pchertalev */
public class KrakenStreamingService extends JsonNettyStreamingService {
  private static final Logger LOG = LoggerFactory.getLogger(KrakenStreamingService.class);
  private static final String EVENT = "event";
  private final Map<Integer, String> channels = new ConcurrentHashMap<>();
  private final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();
  private final boolean isPrivate;
  private final Supplier<KrakenWebsocketToken> authData;
  private final Map<Integer, String> subscriptionRequestMap = new ConcurrentHashMap<>();
  static final int ORDER_BOOK_SIZE_DEFAULT = 25;
  private static final int[] KRAKEN_VALID_ORDER_BOOK_SIZES = {10, 25, 100, 500, 1000};

  public KrakenStreamingService(
      boolean isPrivate, String uri, final Supplier<KrakenWebsocketToken> authData) {
    super(uri, Integer.MAX_VALUE);
    this.isPrivate = isPrivate;
    this.authData = authData;
  }

  public KrakenStreamingService(
      boolean isPrivate,
      String uri,
      int maxFramePayloadLength,
      Duration connectionTimeout,
      Duration retryDuration,
      int idleTimeoutSeconds,
      final Supplier<KrakenWebsocketToken> authData) {
    super(uri, maxFramePayloadLength, connectionTimeout, retryDuration, idleTimeoutSeconds);
    this.isPrivate = isPrivate;
    this.authData = authData;
  }

  @Override
  public boolean processArrayMessageSeparately() {
    return false;
  }

  @Override
  protected WebSocketClientExtensionHandler getWebSocketClientExtensionHandler() {
    return WebSocketClientCompressionAllowClientNoContextHandler.INSTANCE;
  }

  @Override
  protected void handleMessage(JsonNode message) {
    String channelName = getChannel(message);

    try {
      JsonNode event = message.get(EVENT);
      KrakenEventType krakenEvent;
      if (event != null && (krakenEvent = KrakenEventType.getEvent(event.textValue())) != null) {
        switch (krakenEvent) {
          case pingStatus:
            LOG.info("PingStatus received: {}", message);
            break;
          case pong:
            LOG.debug("Pong received");
            break;
          case heartbeat:
            LOG.debug("Heartbeat received");
            break;
          case systemStatus:
            KrakenSystemStatus systemStatus = mapper.treeToValue(message, KrakenSystemStatus.class);
            LOG.info("System status: {}", systemStatus);
            break;
          case subscriptionStatus:
            LOG.debug("Received subscriptionStatus message {}", message);
            KrakenSubscriptionStatusMessage statusMessage =
                mapper.treeToValue(message, KrakenSubscriptionStatusMessage.class);
            Integer reqid = statusMessage.getReqid();
            if (!isPrivate && reqid != null) channelName = subscriptionRequestMap.remove(reqid);

            switch (statusMessage.getStatus()) {
              case subscribed:
                LOG.info("Channel {} has been subscribed", channelName);

                if (statusMessage.getChannelID() != null)
                  channels.put(statusMessage.getChannelID(), channelName);

                break;
              case unsubscribed:
                LOG.info("Channel {} has been unsubscribed", channelName);
                channels.remove(statusMessage.getChannelID());
                break;
              case error:
                LOG.error(
                    "Channel {} has been failed: {}", channelName, statusMessage.getErrorMessage());
                if ("ESession:Invalid session".equals(statusMessage.getErrorMessage())) {
                  throw new ExchangeException("Issue with session validity");
                }
            }
            break;
          case error:
            LOG.error(
                "Error received: {}",
                message.has("errorMessage")
                    ? message.get("errorMessage").asText()
                    : message.toString());
            break;
          default:
            LOG.warn("Unexpected event type has been received: {}", krakenEvent);
        }
        return;
      }
    } catch (JsonProcessingException e) {
      LOG.error("Error reading message: {}", e.getMessage(), e);
    }

    if (!message.isArray() || channelName == null) {
      LOG.error("Unknown message: {}", message.toString());
      return;
    }

    super.handleMessage(message);
  }

  @Override
  protected String getChannelNameFromMessage(JsonNode message) throws IOException {
    String channelName = null;
    if (message.has("channelID")) {
      channelName = channels.get(message.get("channelID").asInt());
    }
    if (message.has("channelName")) {
      channelName = message.get("channelName").asText();
    }

    if (message.isArray()) {
      if (message.get(0).isInt()) {
        channelName = channels.get(message.get(0).asInt());
      }
      if (message.get(1).isTextual()) {
        channelName = message.get(1).asText();
      }
    }

    if (LOG.isTraceEnabled()) {
      LOG.trace("ChannelName {}", StringUtils.isBlank(channelName) ? "not defined" : channelName);
    }
    return channelName;
  }

  @Override
  public String getSubscribeMessage(String channelName, Object... args) throws IOException {
    int reqID = Math.abs(UUID.randomUUID().hashCode());
    String[] channelData =
        channelName.split(KrakenStreamingMarketDataService.KRAKEN_CHANNEL_DELIMITER);
    KrakenSubscriptionName subscriptionName = KrakenSubscriptionName.valueOf(channelData[0]);

    if (isPrivate) {
      final String token = authData.get().getToken();

      KrakenSubscriptionMessage subscriptionMessage =
          new KrakenSubscriptionMessage(
              reqID, subscribe, null, new KrakenSubscriptionConfig(subscriptionName, null, token));

      return objectMapper.writeValueAsString(subscriptionMessage);
    } else {
      String pair = channelData[1];

      subscriptionRequestMap.put(reqID, channelName);

      KrakenSubscriptionMessage subscriptionMessage =
          new KrakenSubscriptionMessage(
              reqID,
              subscribe,
              Collections.singletonList(pair),
              new KrakenSubscriptionConfig(subscriptionName, parseOrderBookSize(args),null));
      return objectMapper.writeValueAsString(subscriptionMessage);
    }
  }

  @Override
  public String getUnsubscribeMessage(String channelName, Object... args) throws IOException {
    int reqID = Math.abs(UUID.randomUUID().hashCode());
    String[] channelData =
        channelName.split(KrakenStreamingMarketDataService.KRAKEN_CHANNEL_DELIMITER);
    KrakenSubscriptionName subscriptionName = KrakenSubscriptionName.valueOf(channelData[0]);

    if (isPrivate) {
      KrakenSubscriptionMessage subscriptionMessage =
          new KrakenSubscriptionMessage(
              reqID,
              KrakenEventType.unsubscribe,
              null,
              new KrakenSubscriptionConfig(subscriptionName, null, null));
      return objectMapper.writeValueAsString(subscriptionMessage);
    } else {
      String pair = channelData[1];

      subscriptionRequestMap.put(reqID, channelName);
      KrakenSubscriptionMessage subscriptionMessage =
          new KrakenSubscriptionMessage(
              reqID,
              KrakenEventType.unsubscribe,
              Collections.singletonList(pair),
              new KrakenSubscriptionConfig(subscriptionName, parseOrderBookSize(args), null));
      return objectMapper.writeValueAsString(subscriptionMessage);
    }
  }

  @Override
  protected WebSocketClientHandler getWebSocketClientHandler(
      WebSocketClientHandshaker handshaker,
      WebSocketClientHandler.WebSocketMessageHandler handler) {
    LOG.info("Registering KrakenWebSocketClientHandler");
    return new KrakenWebSocketClientHandler(handshaker, handler);
  }

  private final WebSocketClientHandler.WebSocketMessageHandler channelInactiveHandler = null;

  /**
   * Custom client handler in order to execute an external, user-provided handler on channel events.
   * This is useful because it seems Kraken unexpectedly closes the web socket connection.
   */
  class KrakenWebSocketClientHandler extends NettyWebSocketClientHandler {

    public KrakenWebSocketClientHandler(
        WebSocketClientHandshaker handshaker, WebSocketMessageHandler handler) {
      super(handshaker, handler);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
      super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
      super.channelInactive(ctx);
      if (channelInactiveHandler != null) {
        channelInactiveHandler.onMessage("WebSocket Client disconnected!");
      }
    }
  }

  static Integer parseOrderBookSize(Object[] args) {
    if (args != null && args.length > 0) {
      Object obSizeParam = args[0];
      LOG.debug("Specified Kraken order book size: {}", obSizeParam);
      if (Number.class.isAssignableFrom(obSizeParam.getClass())) {
        int obSize = ((Number) obSizeParam).intValue();
        if (ArrayUtils.contains(KRAKEN_VALID_ORDER_BOOK_SIZES, obSize)) {
          return obSize;
        }
        LOG.error(
                "Invalid order book size {}. Valid values: {}. Default order book size has been used: {}",
                obSize,
                ArrayUtils.toString(KRAKEN_VALID_ORDER_BOOK_SIZES),
                ORDER_BOOK_SIZE_DEFAULT);
        return ORDER_BOOK_SIZE_DEFAULT;
      }
    }
    return null;
  }
}
