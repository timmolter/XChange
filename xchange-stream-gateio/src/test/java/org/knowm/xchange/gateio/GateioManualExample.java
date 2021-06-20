package org.knowm.xchange.gateio;

import org.knowm.xchange.gateio.GateioStreamingExchange;
import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import io.reactivex.disposables.Disposable;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GateioManualExample {
  private static final Logger LOG = LoggerFactory.getLogger(GateioManualExample.class);

  public static void main(String[] args) throws Exception {
    ProductSubscription productSubscription =
        ProductSubscription.create()
            .addOrderbook(CurrencyPair.ETH_USDT)
            .addOrderbook(CurrencyPair.BTC_USDT)
            .addTrades(CurrencyPair.ETH_USDT)
            .build();

    ExchangeSpecification spec =
        StreamingExchangeFactory.INSTANCE
            .createExchangeWithoutSpecification(GateioStreamingExchange.class)
            .getDefaultExchangeSpecification();
    spec.setShouldLoadRemoteMetaData(false);

    GateioStreamingExchange exchange =
        (GateioStreamingExchange) StreamingExchangeFactory.INSTANCE.createExchange(spec);

    exchange.connect(productSubscription).blockingAwait();

    Disposable sub1 =
        exchange
            .getStreamingMarketDataService()
            .getOrderBook(CurrencyPair.ETH_USDT)
            .subscribe(
                orderBook -> {
                  LOG.info("First ask: {}", orderBook.getAsks().get(0));
                  LOG.info("First bid: {}", orderBook.getBids().get(0));
                },
                throwable -> LOG.error("ERROR in getting order book: ", throwable));
    Disposable sub2 =
        exchange
            .getStreamingMarketDataService()
            .getOrderBook(CurrencyPair.BTC_USDT)
            .subscribe(
                orderBook -> {
                  LOG.info("First ask: {}", orderBook.getAsks().get(0));
                  LOG.info("First bid: {}", orderBook.getBids().get(0));
                },
                throwable -> LOG.error("ERROR in getting order book: ", throwable));
    Disposable sub3 =
        exchange
            .getStreamingMarketDataService()
            .getTrades(CurrencyPair.BTC_USDT)
            .subscribe(
                trade -> {
                  LOG.info("Trade Price: {}", trade.getPrice());
                  LOG.info("Trade Amount: {}", trade.getOriginalAmount());
                },
                throwable -> LOG.error("ERROR in getting trade: ", throwable));

    Thread.sleep(1000);
    sub1.dispose();
    sub2.dispose();
    sub3.dispose();
  }
}
