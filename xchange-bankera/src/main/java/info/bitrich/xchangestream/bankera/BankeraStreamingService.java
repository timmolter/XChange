package info.bitrich.xchangestream.bankera;

import com.fasterxml.jackson.databind.JsonNode;
import info.bitrich.xchangestream.service.netty.JsonNettyStreamingService;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketClientExtensionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BankeraStreamingService extends JsonNettyStreamingService {

	private final Logger LOGGER = LoggerFactory.getLogger(BankeraStreamingService.class);

	public BankeraStreamingService(String uri) {
		super(uri, Integer.MAX_VALUE);
	}

	@Override
	protected String getChannelNameFromMessage(JsonNode message) throws IOException {
		LOGGER.debug("getChannelNameFromMessage: {}", message.toString());
		return "market-orderbook";
	}

	@Override
	public String getSubscribeMessage(String channelName, Object... args) throws IOException {
		LOGGER.debug("getSubscribeMessage: {}", channelName);
		return null;
	}

	@Override
	public String getUnsubscribeMessage(String channelName) throws IOException {
		LOGGER.debug("getUnsubscribeMessage: {}", channelName);
		return null;
	}

	@Override
	protected WebSocketClientExtensionHandler getWebSocketClientExtensionHandler() {
		return null;
	}

}
