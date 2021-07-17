package info.bitrich.xchangestream.ftx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.ftx.dto.FtxAuthenticationMessage;
import info.bitrich.xchangestream.ftx.dto.FtxStreamRequest;
import info.bitrich.xchangestream.service.netty.JsonNettyStreamingService;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import info.bitrich.xchangestream.service.netty.WebSocketClientCompressionAllowClientNoContextHandler;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketClientExtensionHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.ftx.service.FtxDigest;
import org.knowm.xchange.utils.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;

public class FtxStreamingService extends JsonNettyStreamingService {

  private static final Logger LOG = LoggerFactory.getLogger(FtxStreamingService.class);
  private final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();
  private boolean isLoggedIn = false;
  private ExchangeSpecification exchangeSpecification = null;

  public FtxStreamingService(String apiUrl) {
    super(apiUrl);
  }

  public FtxStreamingService(String apiUrl, ExchangeSpecification exchangeSpecification) {
    super(apiUrl);
    this.exchangeSpecification = exchangeSpecification;
  }

  @Override
  protected WebSocketClientExtensionHandler getWebSocketClientExtensionHandler() {
    return WebSocketClientCompressionAllowClientNoContextHandler.INSTANCE;
  }

  private FtxAuthenticationMessage getAuthMessage(){
    Mac mac = FtxDigest.createInstance(exchangeSpecification.getSecretKey()).getMac();

    try {
      Long nonce = System.currentTimeMillis();
      String message = nonce + "websocket_login";

      mac.update(message.getBytes(StandardCharsets.UTF_8));

      return new FtxAuthenticationMessage(
              new FtxAuthenticationMessage.FtxAuthenticationArgs(
                      exchangeSpecification.getApiKey(),
                      DigestUtils.bytesToHex(mac.doFinal()).toLowerCase(),
                      nonce,
                      exchangeSpecification.getUserName()));
    } catch (Exception e) {
      throw new ExchangeException("Digest encoding exception", e);
    }
  }

  @Override
  protected void handleMessage(JsonNode message) {
    if (message.hasNonNull("type")) {
      if ("error".equals(message.get("type").asText())) {
        setLoggedInToFalse();
      }
    }
    super.handleMessage(message);
  }

  @Override
  protected String getChannelNameFromMessage(JsonNode message) {
    String channelName = "";

    if ("fills".equals(message.get("channel").asText())) {
      channelName = message.get("channel").asText();
    } else if ("orderbook".equals(message.get("channel").asText())) {
      channelName = message.get("channel").asText() + ":" + message.get("market").asText();
    }

    LOG.trace("GetChannelNameFromMessage: " + channelName);

    return channelName;
  }

  @Override
  public String getSubscribeMessage(String channelName, Object... args) throws IOException {
    String channel = "";
    String market = "";

    if (exchangeSpecification != null && !isLoggedIn) {
      FtxAuthenticationMessage message = getAuthMessage();
      LOG.info("Sending authentication message: "+ message);
      sendObjectMessage(message);
      isLoggedIn = true;
    }

    if (channelName.contains("orderbook")) {
      channel = channelName.substring(0, channelName.indexOf(":"));
      market = channelName.substring(channelName.indexOf(":") + 1);
    } else if (channelName.contains("fills")) {
      channel = "fills";
      market = null;
    }

    LOG.debug("GetSubscribeMessage channel: " + channel);
    LOG.debug("GetSubscribeMessage market: " + market);

    return mapper.writeValueAsString(new FtxStreamRequest(channel, market, "subscribe"));
  }

  @Override
  public String getUnsubscribeMessage(String channelName, Object... args) throws IOException {
    String channel = "";
    String market = "";

    if (channelName.contains("orderbook")) {
      channel = channelName.substring(0, channelName.indexOf(":"));
      market = channelName.substring(channelName.indexOf(":") + 1);
    } else if (channelName.contains("fills")) {
      channel = "fills";
      market = null;
    }

    setLoggedInToFalse();

    LOG.debug("GetUnSubscribeMessage channel: " + channel);
    LOG.debug("GetUnSubscribeMessage market: " + market);

    return objectMapper.writeValueAsString(new FtxStreamRequest(channel, market, "unsubscribe"));
  }

  @Override
  public void resubscribeChannels() {
    setLoggedInToFalse();
    super.resubscribeChannels();
  }

  private void setLoggedInToFalse(){
    if (exchangeSpecification != null && isLoggedIn) {
      isLoggedIn = false;
      LOG.info("IsLoggedIn is "+false);
    }
  }
}
