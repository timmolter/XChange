package org.knowm.xchangestream.simulated;

import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingExchange;
import io.reactivex.Completable;
import io.reactivex.subjects.CompletableSubject;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.RandomUtils;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.simulated.SimulatedAccountService;
import org.knowm.xchange.simulated.SimulatedExchange;
import si.mazi.rescu.SynchronizedValueFactory;

/**
 * Simple SimulatedStreamingExchange implementation
 *
 * @author mrmx
 */
public class SimulatedStreamingExchange implements StreamingExchange {

  private final AtomicBoolean alive = new AtomicBoolean(false);
  private final SimulatedExchange exchange;
  private final SimulatedStreamingMarketDataService streamingMarketDataService;
  private SimulatedStreamingTradeService streamingTradeService;

  public SimulatedStreamingExchange() {
    exchange = new SimulatedExchange();
    streamingMarketDataService = new SimulatedStreamingMarketDataService(this);
  }

  @Override
  public Completable connect(ProductSubscription... args) {
    return Completable.timer(RandomUtils.nextInt(100, 1000), TimeUnit.MILLISECONDS)
        .andThen(
            CompletableSubject.create(
                emitter -> {
                  alive.set(true);
                  emitter.onComplete();
                }));
  }

  @Override
  public Completable disconnect() {
    return Completable.timer(RandomUtils.nextInt(10, 100), TimeUnit.MILLISECONDS);
  }

  @Override
  public boolean isAlive() {
    return alive.get();
  }

  @Override
  public void useCompressedMessages(boolean compressedMessages) {
    throw new NotYetImplementedForExchangeException("useCompressedMessages");
  }

  @Override
  public SimulatedStreamingMarketDataService getStreamingMarketDataService() {
    return streamingMarketDataService;
  }

  @Override
  public void applySpecification(ExchangeSpecification exchangeSpecification) {
    exchange.applySpecification(exchangeSpecification);
  }

  @Override
  public ExchangeSpecification getDefaultExchangeSpecification() {
    ExchangeSpecification defaultExchangeSpecification = exchange.getDefaultExchangeSpecification();
    defaultExchangeSpecification.setExchangeName("SimulatedStreaming");
    return defaultExchangeSpecification;
  }

  @Override
  public ExchangeSpecification getExchangeSpecification() {
    return exchange.getExchangeSpecification();
  }

  @Override
  public ExchangeMetaData getExchangeMetaData() {
    return exchange.getExchangeMetaData();
  }

  @Override
  public List<CurrencyPair> getExchangeSymbols() {
    return exchange.getExchangeSymbols();
  }

  @Override
  public SynchronizedValueFactory<Long> getNonceFactory() {
    return exchange.getNonceFactory();
  }

  @Override
  public MarketDataService getMarketDataService() {
    return exchange.getMarketDataService();
  }

  @Override
  public SimulatedAccountService getAccountService() {
    return exchange.getAccountService();
  }

  @Override
  public SimulatedStreamingTradeService getTradeService() {
    if (streamingTradeService == null) {
      streamingTradeService = new SimulatedStreamingTradeService(this);
    }
    return streamingTradeService;
  }

  @Override
  public void remoteInit() throws IOException, ExchangeException {
    exchange.remoteInit();
  }

  SimulatedExchange getSimulatedExchange() {
    return exchange;
  }
}
