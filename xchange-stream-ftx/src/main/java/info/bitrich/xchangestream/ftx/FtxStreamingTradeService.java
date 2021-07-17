package info.bitrich.xchangestream.ftx;

import com.fasterxml.jackson.databind.JsonNode;
import info.bitrich.xchangestream.core.StreamingTradeService;
import io.reactivex.Observable;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.trade.UserTrade;

public class FtxStreamingTradeService implements StreamingTradeService {

    private final FtxStreamingService service;
    private final Observable<JsonNode> fills;

    public FtxStreamingTradeService(FtxStreamingService service) {
        this.service = service;
        this.fills = service.subscribeChannel("fills");
    }

    @Override
    public Observable<UserTrade> getUserTrades(CurrencyPair currencyPair, Object... args) {

        return fills
                .filter(jsonNode -> jsonNode.hasNonNull("data"))
                .filter(jsonNode -> new CurrencyPair(jsonNode.get("data").get("market").asText()).equals(currencyPair))
                .map(FtxStreamingAdapters::adaptUserTrade);
    }
}
