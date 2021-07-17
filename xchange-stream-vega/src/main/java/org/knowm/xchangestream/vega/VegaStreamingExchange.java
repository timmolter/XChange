package org.knowm.xchangestream.vega;

import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import io.reactivex.Completable;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.vega.VegaExchange;

public class VegaStreamingExchange extends VegaExchange implements StreamingExchange {
    private VegaStreamingService streamingService;
    private VegaStreamingMarketDataService streamingMarketDataService;

    @Override
    public Completable connect(ProductSubscription... args) {
        ExchangeSpecification exchangeSpec = getExchangeSpecification();

        this.streamingService = new VegaStreamingService(exchangeSpec);
        this.streamingMarketDataService = new VegaStreamingMarketDataService(streamingService);

        //        streamingService.subscribeMultipleCurrencyPairs(args);
        return streamingService.connect();
    }

    @Override
    public StreamingMarketDataService getStreamingMarketDataService() {
        return streamingMarketDataService;
    }

    @Override
    public Completable disconnect() {
        return null;
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public void useCompressedMessages(boolean compressedMessages) {

    }
}
