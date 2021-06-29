package info.bitrich.xchangestream.kraken;

import static info.bitrich.xchangestream.kraken.dto.enums.KrakenEventType.addOrderStatus;
import static info.bitrich.xchangestream.kraken.dto.enums.KrakenEventType.cancelOrderStatus;
import static info.bitrich.xchangestream.kraken.dto.enums.KrakenEventType.subscribe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.kraken.dto.*;
import info.bitrich.xchangestream.kraken.dto.enums.KrakenAddOrderStatus;
import info.bitrich.xchangestream.kraken.dto.enums.KrakenCancelOrderStatus;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.kraken.dto.account.KrakenWebsocketToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author makarid, pchertalev, marcinrabiej */
public class KrakenStreamingService extends JsonNettyStreamingService {
  private static final Logger LOG = LoggerFactory.getLogger(KrakenStreamingService.class);
  private static final String EVENT = "event";
  private final Map<Integer, String> channels = new ConcurrentHashMap<>();
  private final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();
  private final boolean isPrivate;
  private final Supplier<KrakenWebsocketToken> authData;
  private final Map<Integer, String> subscriptionRequestMap = new ConcurrentHashMap<>();
  private final Map<Integer, ObservableEmitter<KrakenAddOrderStatusMessage>>
          addOrderStatusEmitters = new ConcurrentHashMap<>();

  private final Map<Integer, ObservableEmitter<KrakenCancelOrderStatusMessage>>
          cancelOrderStatusEmitters = new ConcurrentHashMap<>();

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
          case addOrderStatus:
            LOG.info("addOrderStatus: {}", message);
            handleAddOrderStatusMessage(
                    mapper.treeToValue(message, KrakenAddOrderStatusMessage.class));
            break;
          case cancelOrderStatus:
            LOG.info("cancelOrderStatus: {}", message);
            handleCancelOrderStatusMessage(
                    mapper.treeToValue(message, KrakenCancelOrderStatusMessage.class));
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

    if (LOG.isDebugEnabled()) {
      LOG.debug("ChannelName {}", StringUtils.isBlank(channelName) ? "not defined" : channelName);
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

      Integer depth = null;
      if (args.length > 0 && args[0] != null) {
        depth = (Integer) args[0];
      }
      subscriptionRequestMap.put(reqID, channelName);

      KrakenSubscriptionMessage subscriptionMessage =
          new KrakenSubscriptionMessage(
              reqID,
              subscribe,
              Collections.singletonList(pair),
              new KrakenSubscriptionConfig(subscriptionName, depth, null));
      return objectMapper.writeValueAsString(subscriptionMessage);
    }
  }

  @Override
  public String getUnsubscribeMessage(String channelName) throws IOException {
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
              new KrakenSubscriptionConfig(subscriptionName));
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

  String submitLimitOrder(KrakenStreamingLimitOrder krakenStreamingLimitOrder) {
    krakenStreamingLimitOrder.setToken(authData.get().getToken());
    Observable<KrakenAddOrderStatusMessage> responseObservable;
    responseObservable =
            Observable.create(
                    (e) -> addOrderStatusEmitters.put(krakenStreamingLimitOrder.getReqid(), e));
    sendObjectMessage(krakenStreamingLimitOrder);
    KrakenAddOrderStatusMessage addOrderStatusMessage =
            responseObservable
                    .timeout(60, TimeUnit.SECONDS, getAddOrderTimeoutObservable(krakenStreamingLimitOrder))
                    .blockingFirst();
    if (addOrderStatusMessage.getStatus() == KrakenAddOrderStatus.ok) {
      return addOrderStatusMessage.getTxid();
    } else {
      throw new RuntimeException(addOrderStatusMessage.getErrorMessage());
    }
  }

  Boolean cancelOrder(KrakenStreamingCancelOrder krakenStreamingCancelOrder) {
    krakenStreamingCancelOrder.setToken(authData.get().getToken());
    Observable<KrakenCancelOrderStatusMessage> responseObservable;
    responseObservable =
            Observable.create(
                    (e) -> cancelOrderStatusEmitters.put(krakenStreamingCancelOrder.getReqid(), e));
    sendObjectMessage(krakenStreamingCancelOrder);
    KrakenCancelOrderStatusMessage cancelOrderStatusMessage =
            responseObservable
                    .timeout(
                            1, TimeUnit.SECONDS, getCancelOrderTimeoutObservable(krakenStreamingCancelOrder))
                    .blockingFirst();
    if (cancelOrderStatusMessage.getStatus() == KrakenCancelOrderStatus.ok) {
      return true;
    } else {
      throw new RuntimeException(cancelOrderStatusMessage.getErrorMessage());
    }
  }


  private Observable<KrakenAddOrderStatusMessage> getAddOrderTimeoutObservable(
          KrakenStreamingLimitOrder krakenStreamingLimitOrder) {
    return Observable.fromCallable(
            () -> {
              addOrderStatusEmitters.remove(krakenStreamingLimitOrder.getReqid());
              return new KrakenAddOrderStatusMessage(
                      addOrderStatus,
                      null,
                      KrakenAddOrderStatus.error,
                      null,
                      krakenStreamingLimitOrder.getReqid(),
                      "TIMEOUT: No response in 60 seconds");
            });
  }


  private Observable<KrakenCancelOrderStatusMessage> getCancelOrderTimeoutObservable(
          KrakenStreamingCancelOrder krakenStreamingCancelOrder) {
    return Observable.fromCallable(
            () -> {
              cancelOrderStatusEmitters.remove(krakenStreamingCancelOrder.getReqid());
              return new KrakenCancelOrderStatusMessage(
                      cancelOrderStatus,
                      KrakenCancelOrderStatus.error,
                      krakenStreamingCancelOrder.getReqid(),
                      "TIMEOUT: No response in 1 second");
            });
  }
  private void handleCancelOrderStatusMessage(
          KrakenCancelOrderStatusMessage krakenCancelOrderStatusMessage) {
    ObservableEmitter<KrakenCancelOrderStatusMessage> cancelOrderStatusMessageObservableEmitter =
            cancelOrderStatusEmitters.get(krakenCancelOrderStatusMessage.getReqid());
    if (cancelOrderStatusMessageObservableEmitter != null) {
      cancelOrderStatusMessageObservableEmitter.onNext(krakenCancelOrderStatusMessage);
    }
  }

  private void handleAddOrderStatusMessage(
          KrakenAddOrderStatusMessage krakenAddOrderStatusMessage) {
    ObservableEmitter<KrakenAddOrderStatusMessage> addOrderStatusMessageObservableEmitter =
            addOrderStatusEmitters.get(krakenAddOrderStatusMessage.getReqid());
    if (addOrderStatusMessageObservableEmitter != null) {
      addOrderStatusMessageObservableEmitter.onNext(krakenAddOrderStatusMessage);
    }
  }

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
}
