package org.knowm.xchange.gateio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.knowm.xchange.gateio.dto.GateioWebSocketSubscriptionMessage;
import org.knowm.xchange.gateio.dto.response.GateioOrderBookResponse;
import org.knowm.xchange.gateio.dto.response.GateioTradesResponse;
import org.knowm.xchange.gateio.dto.response.GateioWebSocketTransaction;
import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.service.netty.JsonNettyStreamingService;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import info.bitrich.xchangestream.service.netty.WebSocketClientCompressionAllowClientNoContextAndServerNoContextHandler;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketClientExtensionHandler;
import io.reactivex.Observable;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Author: Max Gao (gaamox@tutanota.com) Created: 05-05-2021 */
public class GateioStreamingService extends JsonNettyStreamingService {
  private static final Logger LOG = LoggerFactory.getLogger(GateioStreamingService.class);
  private static final String SUBSCRIBE = "subscribe";
  private static final String UNSUBSCRIBE = "unsubscribe";
  private static final String CHANNEL_NAME_DELIMITER = "-";

  public static final String SPOT_ORDERBOOK_CHANNEL = "spot.order_book";
  public static final String SPOT_TRADES_CHANNEL = "spot.trades";
  public static final String SPOT_TICKERS_CHANNEL = "spot.tickers";

  private static final int MAX_DEPTH_DEFAULT = 5;
  private static final int UPDATE_INTERVAL_DEFAULT = 100;

  private final String apiUri;
  private ProductSubscription productSubscription;
  private ExchangeSpecification exchangeSpecification;

  private final Map<String, Observable<JsonNode>> subscriptions = new ConcurrentHashMap<>();
  private final Map<String, String> channelSubscriptionMessages = new ConcurrentHashMap<>();

  public GateioStreamingService(String apiUri, ExchangeSpecification exchangeSpecification) {
    super(apiUri, Integer.MAX_VALUE);
    this.apiUri = apiUri;
    this.exchangeSpecification = exchangeSpecification;
  }

  public Observable<GateioWebSocketTransaction> getRawWebSocketTransactions(
      CurrencyPair currencyPair, String channelName, Object... args) {
    final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();

    return subscribeChannel(channelName, currencyPair, args)
        .map(
            msg -> {
              switch (channelName) {
                case SPOT_ORDERBOOK_CHANNEL:
                  return mapper.readValue(msg.toString(), GateioOrderBookResponse.class);
                case SPOT_TRADES_CHANNEL:
                  return mapper.readValue(msg.toString(), GateioTradesResponse.class);
              }
              return mapper.readValue(msg.toString(), GateioWebSocketTransaction.class);
            })
        .filter(t -> currencyPair.equals(t.getCurrencyPair()));
  }

  public void subscribeMultipleCurrencyPairs(ProductSubscription... products) {
    this.productSubscription = products[0];
  }

  public ProductSubscription getProduct() {
    return this.productSubscription;
  }

  @Override
  protected String getChannelNameFromMessage(JsonNode message) {
    String channel = message.path("channel") != null ? message.path("channel").asText() : "";
    String currencyPairOrderBook =
        message.path("result").path("s") != null ? message.path("result").path("s").asText() : "";
    String currencyPairTradesTickers =
        message.path("result").path("currency_pair") != null
            ? message.path("result").path("currency_pair").asText()
            : "";

    return new StringBuilder(channel)
        .append(CHANNEL_NAME_DELIMITER)
        .append(currencyPairOrderBook)
        .append(currencyPairTradesTickers)
        .toString();
  }

  @Override
  public Observable<JsonNode> subscribeChannel(String channelName, Object... args) {
    final CurrencyPair currencyPair =
        (args.length > 0 && args[0] instanceof CurrencyPair) ? ((CurrencyPair) args[0]) : null;

    String currencyPairChannelName =
        String.format("%s-%s", channelName, currencyPair.toString().replace('/', '_'));

    try {
      // Example channel name key: spot.order_book_update-ETH_USDT, spot.trades-BTC_USDT
      if (!channels.containsKey(currencyPairChannelName)
          && !subscriptions.containsKey(currencyPairChannelName)) {
        subscriptions.put(
            currencyPairChannelName, super.subscribeChannel(currencyPairChannelName, args));
        channelSubscriptionMessages.put(
            currencyPairChannelName, getSubscribeMessage(currencyPairChannelName, currencyPair));
      }
    } catch (IOException e) {
      LOG.error("Failed to subscribe to channel: {}", currencyPairChannelName);
    }

    return subscriptions.get(currencyPairChannelName);
  }

  /**
   * Returns a JSON String containing the subscription message.
   *
   * @param channelName
   * @param args CurrencyPair to subscribe to
   * @return
   * @throws IOException
   */
  @Override
  public String getSubscribeMessage(String channelName, Object... args) throws IOException {
    final CurrencyPair currencyPair =
        (args.length > 0 && args[0] instanceof CurrencyPair) ? ((CurrencyPair) args[0]) : null;

    final int maxDepth =
        exchangeSpecification.getExchangeSpecificParametersItem("maxDepth") != null
            ? (int) exchangeSpecification.getExchangeSpecificParametersItem("maxDepth")
            : MAX_DEPTH_DEFAULT;
    final int msgInterval =
        exchangeSpecification.getExchangeSpecificParametersItem("updateInterval") != null
            ? (int) exchangeSpecification.getExchangeSpecificParametersItem("updateInterval")
            : UPDATE_INTERVAL_DEFAULT;

    GateioWebSocketSubscriptionMessage subscribeMessage =
        new GateioWebSocketSubscriptionMessage(
            channelName.split(CHANNEL_NAME_DELIMITER)[0],
            SUBSCRIBE,
            currencyPair,
            msgInterval,
            maxDepth);

    return objectMapper.writeValueAsString(subscribeMessage);
  }

  @Override
  protected WebSocketClientExtensionHandler getWebSocketClientExtensionHandler() {
    return WebSocketClientCompressionAllowClientNoContextAndServerNoContextHandler.INSTANCE;
  }

  @Override
  public String getUnsubscribeMessage(String channelName) throws IOException {
    GateioWebSocketSubscriptionMessage unsubscribeMessage =
        objectMapper.readValue(
            channelSubscriptionMessages.get(channelName), GateioWebSocketSubscriptionMessage.class);
    unsubscribeMessage.setEvent(UNSUBSCRIBE);
    return objectMapper.writeValueAsString(unsubscribeMessage);
  }
}
