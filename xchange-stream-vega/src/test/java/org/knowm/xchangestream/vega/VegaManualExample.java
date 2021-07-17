package org.knowm.xchangestream.vega;

import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VegaManualExample {
    private static final Logger LOG = LoggerFactory.getLogger(VegaManualExample.class);

    public static void main(String[] args) {
        StreamingExchange exchange = StreamingExchangeFactory.INSTANCE.createExchange(VegaStreamingExchange.class);
        exchange.connect().blockingAwait();

        exchange
                .getStreamingMarketDataService()
                .getOrderBook(new CurrencyPair("AAVE", "DAI"))
                .subscribe(
                        orderBook -> {
                            if (orderBook.getAsks().size() > 0 && orderBook.getBids().size() > 0) {
                                LOG.info("First ask: {}", orderBook.getAsks().get(0));
                                LOG.info("First bid: {}", orderBook.getBids().get(0));
                            }
                        },
                        throwable -> LOG.error("ERROR in getting order book: ", throwable));

        exchange
                .getStreamingMarketDataService()
                .getTrades(new CurrencyPair("AAVE", "DAI"))
                .subscribe(
                        trade -> {
                            LOG.info("TRADE: {}", trade);
                        },
                        throwable -> LOG.error("ERROR in getting trades: ", throwable));

        while (true) {}
    }
}
