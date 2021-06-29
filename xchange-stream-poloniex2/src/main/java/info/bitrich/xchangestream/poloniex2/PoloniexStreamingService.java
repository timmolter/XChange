package info.bitrich.xchangestream.poloniex2;

import com.fasterxml.jackson.databind.JsonNode;
import info.bitrich.xchangestream.poloniex2.dto.*;
import info.bitrich.xchangestream.service.netty.JsonNettyStreamingService;
import io.reactivex.Completable;
import io.reactivex.Observable;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.mazi.rescu.SynchronizedValueFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by Lukas Zaoralek on 10.11.17, marcinrabiej */
public class PoloniexStreamingService extends JsonNettyStreamingService {
  private static final Logger LOG = LoggerFactory.getLogger(PoloniexStreamingService.class);

  private static final String HEARTBEAT = "1010";
  public static final String ACCOUNT_NOTIFICATIONS_CHANNEL = "1000";

  private final Map<String, String> subscribedChannels = new ConcurrentHashMap<>();
  private final Map<String, Observable<JsonNode>> subscriptions = new ConcurrentHashMap<>();

  public PoloniexStreamingService(String apiUrl) {
    super(apiUrl, Integer.MAX_VALUE, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_RETRY_DURATION, 2);
  }

  @Override
  protected void handleMessage(JsonNode message) {

    if (message.isArray()) {
      if (message.size() < 3) {
        if (message.get(0).asText().equals(HEARTBEAT)) return;
        else if ("1002".equals(message.get(0).asText())) return;
      }
      int channelId = Integer.parseInt(message.get(0).toString());
      if (channelId > 0 && channelId < 1000) {
        JsonNode events = message.get(2);
        if (events != null && events.isArray()) {
          JsonNode event = events.get(0);
          if (event.get(0).toString().equals("\"i\"")) {
            if (event.get(1).has("orderBook")) {
              subscribedChannels.compute(
                  String.valueOf(channelId),
                  (key, oldValue) -> {
                    String currencyPair = event.get(1).get("currencyPair").asText();
                    if (oldValue != null && !oldValue.equals(currencyPair)) {
                      throw new RuntimeException("Attempted currency pair channel id reassignment");
                    }
                    if (oldValue == null) {
                      LOG.info("Register {} as {}", channelId, currencyPair);
                    } else {
                      LOG.debug("Order book reinitialization {} {}", channelId, currencyPair);
                    }
                    return currencyPair;
                  });
            }
          }
        }
      }
    }
    if (message.has("error")) {
      LOG.error("Error with message: " + message.get("error").asText());
      return;
    }
    super.handleMessage(message);
  }

  @Override
  public boolean processArrayMessageSeparately() {
    return false;
  }

  @Override
  public void messageHandler(String message) {
    LOG.debug("Received message: {}", message);
    JsonNode jsonNode;

    // Parse incoming message to JSON
    try {
      jsonNode = objectMapper.readTree(message);
    } catch (IOException e) {
      LOG.error("Error parsing incoming message to JSON: {}", message);
      return;
    }

    handleMessage(jsonNode);
  }

  @Override
  public synchronized Observable<JsonNode> subscribeChannel(String channelName, Object... args) {
    if (!channels.containsKey(channelName)) {
      subscriptions.put(channelName, super.subscribeChannel(channelName, args));
    }
    return subscriptions.get(channelName);
  }

  public Observable<JsonNode> subscribeAccountNotificationsChannel(
      String apiKey, String secretKey, SynchronizedValueFactory<Long> nonceFactory) {
    return subscribeChannel(ACCOUNT_NOTIFICATIONS_CHANNEL, apiKey, secretKey, nonceFactory);
  }

  public Observable<List<PoloniexWebSocketEvent>> subscribeCurrencyPairChannel(
      CurrencyPair currencyPair) {
    String channelName = currencyPair.counter.toString() + "_" + currencyPair.base.toString();
    return subscribeChannel(channelName)
        .map(
            jsonNode ->
                objectMapper.treeToValue(jsonNode, PoloniexWebSocketEventsTransaction.class))
        .scan(
            (poloniexWebSocketEventsTransactionOld, poloniexWebSocketEventsTransactionNew) -> {
              final boolean initialSnapshot =
                  poloniexWebSocketEventsTransactionNew.getEvents().stream()
                      .anyMatch(PoloniexWebSocketOrderbookModifiedEvent.class::isInstance);
              final boolean sequenceContinuous =
                  poloniexWebSocketEventsTransactionOld.getSeqId() + 1
                      == poloniexWebSocketEventsTransactionNew.getSeqId();
              if (!initialSnapshot || sequenceContinuous) {
                return poloniexWebSocketEventsTransactionNew;
              } else {
                throw new RuntimeException(
                    String.format(
                        "Invalid sequencing, old: %s new: %s",
                        objectMapper.writeValueAsString(poloniexWebSocketEventsTransactionOld),
                        objectMapper.writeValueAsString(poloniexWebSocketEventsTransactionNew)));
              }
            })
        .map(PoloniexWebSocketEventsTransaction::getEvents)
        .share();
  }

  @Override
  protected String getChannelNameFromMessage(JsonNode message) {
    String strChannelId = message.get(0).asText();
    int channelId = Integer.parseInt(strChannelId);
    if (channelId >= 1000) return strChannelId;
    else return subscribedChannels.get(message.get(0).asText());
  }

  @Override
  public String getSubscribeMessage(String channelName, Object... args) throws IOException {
    Object subscribeMessage;
    if (ACCOUNT_NOTIFICATIONS_CHANNEL.equals(channelName)) {
      validateArgs(args);
      subscribeMessage =
          getAccountNotificationsSubscription(
              (String) args[0], (String) args[1], (SynchronizedValueFactory<Long>) args[2]);
    } else {
      subscribeMessage = new PoloniexWebSocketSubscriptionMessage("subscribe", channelName);
    }
    return objectMapper.writeValueAsString(subscribeMessage);
  }

  private void validateArgs(Object... args) {
    if (args == null || args.length < 3 || args[0] == null || args[1]==null|| args[2]==null ||
                !String.class.isAssignableFrom(args[0].getClass())||
                !String.class.isAssignableFrom(args[1].getClass())||
                !SynchronizedValueFactory.class.isAssignableFrom(args[2].getClass()))
    {
      throw new IllegalArgumentException("Need at least 3 non null args, first three: String, String, SynchronizedValueFactory");
    }
  }

  private PoloniexWebSocketAccountNotificationsSubscriptionMessage
      getAccountNotificationsSubscription(
          String apiKey, String secretKey, SynchronizedValueFactory<Long> nonceFactory) {
    String nonce = "nonce=" + nonceFactory.createValue();
    String signature = new PoloniexSigner(secretKey).getSignature(nonce.getBytes());
    PoloniexWebSocketAccountNotificationsSubscriptionMessage subscriptionMessage =
        new PoloniexWebSocketAccountNotificationsSubscriptionMessage(
            "subscribe", ACCOUNT_NOTIFICATIONS_CHANNEL, apiKey, nonce, signature);
    return subscriptionMessage;
  }

  @Override
  public String getUnsubscribeMessage(String channelName) throws IOException {
    PoloniexWebSocketSubscriptionMessage subscribeMessage =
        new PoloniexWebSocketSubscriptionMessage("unsubscribe", channelName);
    return objectMapper.writeValueAsString(subscribeMessage);
  }
}
