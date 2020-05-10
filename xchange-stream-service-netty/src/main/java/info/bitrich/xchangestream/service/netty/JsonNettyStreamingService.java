package info.bitrich.xchangestream.service.netty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JsonNettyStreamingService extends NettyStreamingService<JsonNode> {
  private static final Logger LOG = LoggerFactory.getLogger(JsonNettyStreamingService.class);
  protected final ObjectMapper objectMapper = StreamingObjectMapperHelper.getObjectMapper();

  public JsonNettyStreamingService(String apiUrl) {
    super(apiUrl);
  }

  public JsonNettyStreamingService(String apiUrl, int maxFramePayloadLength) {
    super(apiUrl, maxFramePayloadLength);
  }

  public JsonNettyStreamingService(
      String apiUrl,
      int maxFramePayloadLength,
      Duration connectionTimeout,
      Duration retryDuration,
      int idleTimeoutSeconds) {
    super(apiUrl, maxFramePayloadLength, connectionTimeout, retryDuration, idleTimeoutSeconds);
  }

  public boolean processArrayMassageSeparately() {
    return true;
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

    if (processArrayMassageSeparately() && jsonNode.isArray()) {
      // In case of array - handle every message separately.
      for (JsonNode node : jsonNode) {
        handleMessage(node);
      }
    } else {
      handleMessage(jsonNode);
    }
  }

  /**
   * Sends a message on the socket. This is a non-blocking operation, so if you want to wait
   * until the message has been sent, call {@link Completable#blockingAwait()} on the result.
   *
   * @param message The object message to send.
   * @return
   */
  protected Completable sendObjectMessage(Object message) {
    return Single.fromCallable(() -> objectMapper.writeValueAsString(message))
        .flatMapCompletable(super::sendMessage)
        .doOnError(e -> LOG.error("Error creating json message: {}", e.getMessage()))
        .cache();
  }
}
