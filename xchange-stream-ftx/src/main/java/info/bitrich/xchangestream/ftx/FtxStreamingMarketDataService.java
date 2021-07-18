package info.bitrich.xchangestream.ftx;

import info.bitrich.xchangestream.core.StreamingMarketDataService;
import io.reactivex.Observable;
import java.util.ArrayList;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.ftx.FtxAdapters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FtxStreamingMarketDataService implements StreamingMarketDataService {

  private static final Logger LOG = LoggerFactory.getLogger(FtxStreamingMarketDataService.class);

  private final FtxStreamingService service;

  public FtxStreamingMarketDataService(FtxStreamingService service) {
    this.service = service;
  }

  @Override
  public Observable<OrderBook> getOrderBook(CurrencyPair currencyPair, Object... args) {
    OrderBook orderBook = new OrderBook(null, new ArrayList<>(), new ArrayList<>());

    return service
        .subscribeChannel("orderbook:" + FtxAdapters.adaptCurrencyPairToFtxMarket(currencyPair))
        .map(res -> FtxStreamingAdapters.adaptOrderbookMessage(orderBook, currencyPair, res));
  }

  @Override
  public Observable<Ticker> getTicker(CurrencyPair currencyPair, Object... args) {
    return service
        .subscribeChannel("ticker:" + FtxAdapters.adaptCurrencyPairToFtxMarket(currencyPair))
        .map(res -> FtxStreamingAdapters.adaptTickerMessage(currencyPair, res))
        .filter(ticker -> ticker != FtxStreamingAdapters.NULL_TICKER);  // lets not send these backs
  }

  @Override
  public Observable<Trade> getTrades(CurrencyPair currencyPair, Object... args) {
    return service
        .subscribeChannel("trades:" + FtxAdapters.adaptCurrencyPairToFtxMarket(currencyPair))
        .flatMapIterable(res -> FtxStreamingAdapters.adaptTradesMessage(currencyPair, res));
  }
}
