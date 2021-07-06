package org.knowm.xchangestream.simulated;

import info.bitrich.xchangestream.core.StreamingMarketDataService;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;
import org.slf4j.LoggerFactory;

/**
 * Simple simulated StreamingMarketDataService implementation
 *
 * @author mrmx
 */
public class SimulatedStreamingMarketDataService implements StreamingMarketDataService {
  private static final org.slf4j.Logger log =
      LoggerFactory.getLogger(SimulatedStreamingMarketDataService.class);
  private final SimulatedStreamingExchange exchange;
  private final Map<CurrencyPair, Observable<Ticker>> tickerSubscriptions;

  SimulatedStreamingMarketDataService(SimulatedStreamingExchange exchange) {
    this.exchange = exchange;
    this.tickerSubscriptions = new ConcurrentHashMap<>();
  }

  @Override
  public Observable<OrderBook> getOrderBook(CurrencyPair currencyPair, Object... args) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Observable<Ticker> getTicker(CurrencyPair currencyPair, Object... args) {
    return tickerSubscriptions.computeIfAbsent(currencyPair, s -> PublishSubject.<Ticker>create());
  }

  @Override
  public Observable<Trade> getTrades(CurrencyPair currencyPair, Object... args) {
    throw new NotYetImplementedForExchangeException("getTrades");
  }

  void publish(CurrencyPair currencyPair, Ticker ticker) {
    PublishSubject<Ticker> subject = (PublishSubject<Ticker>) getTicker(currencyPair);
    subject.onNext(ticker);
  }
}
