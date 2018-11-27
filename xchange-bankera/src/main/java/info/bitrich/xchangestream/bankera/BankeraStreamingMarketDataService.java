package info.bitrich.xchangestream.bankera;

import info.bitrich.xchangestream.core.StreamingMarketDataService;
import io.reactivex.Observable;
import org.knowm.xchange.bankera.BankeraAdapters;
import org.knowm.xchange.bankera.dto.BankeraException;
import org.knowm.xchange.bankera.dto.marketdata.*;
import org.knowm.xchange.bankera.service.BankeraMarketDataService;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;


public class BankeraStreamingMarketDataService implements StreamingMarketDataService {

  private final BankeraStreamingService service;
  private final BankeraMarketDataService marketDataService;

  public BankeraStreamingMarketDataService(BankeraStreamingService service, BankeraMarketDataService marketDataService) {
    this.service = service;
    this.marketDataService = marketDataService;
  }

  @Override
  public Observable<OrderBook> getOrderBook(CurrencyPair currencyPair, Object... args) {
    BankeraMarket market = getMarketInfo(currencyPair);
    return service.subscribeChannel("market-orderbook", market.getId())
        .map(o -> {
          List<BankeraOrderBook.OrderBookOrder> listBids = new ArrayList<>();
          List<BankeraOrderBook.OrderBookOrder> listAsks = new ArrayList<>();
          o.get("data").get("buy")
              .forEach(b -> listBids.add(new BankeraOrderBook.OrderBookOrder(
                  0, b.get("price").asText(), b.get("amount").asText())));
          o.get("data").get("sell")
              .forEach(b -> listAsks.add(new BankeraOrderBook.OrderBookOrder(
                  0, b.get("price").asText(), b.get("amount").asText())));
          return BankeraAdapters.adaptOrderBook(new BankeraOrderBook(listBids, listAsks), currencyPair);
        });
  }

  @Override
  public Observable<Ticker> getTicker(CurrencyPair currencyPair, Object... args) {
    BankeraMarket market = getMarketInfo(currencyPair);
    return service.subscribeChannel("market-ohlcv-candle", market.getId())
      .map(t -> new Ticker.Builder()
          .currencyPair(currencyPair)
          .high(new BigDecimal(t.get("data").get("h").asText()))
          .low(new BigDecimal(t.get("data").get("l").asText()))
          .open(new BigDecimal(t.get("data").get("o").asText()))
          .last(new BigDecimal(t.get("data").get("c").asText()))
          .volume(new BigDecimal(t.get("data").get("v").asText()))
          .timestamp(new Date(t.get("data").get("ts").asLong()))
          .build()
      );
  }

  @Override
  public Observable<Trade> getTrades(CurrencyPair currencyPair, Object... args) {
    BankeraMarket market = getMarketInfo(currencyPair);
    return service.subscribeChannel("market-trade", market.getId())
      .map(t -> new Trade.Builder()
          .currencyPair(currencyPair)
          .id("-1")
          .price(new BigDecimal(t.get("data").get("price").asText()))
          .originalAmount(new BigDecimal(t.get("data").get("amount").asText()))
          .timestamp(new Date(t.get("data").get("time").asLong()))
          .type(t.get("data").get("side").asText().equals("SELL") ? Order.OrderType.ASK : Order.OrderType.BID)
          .build()
      );
  }

  private BankeraMarket getMarketInfo(CurrencyPair currencyPair) {
    try {
      BankeraMarketInfo info = this.marketDataService.getMarketInfo();
      Optional<BankeraMarket> market = info.getMarkets().stream().filter(
          m -> m.getName().equals(currencyPair.toString().replace("/", "-"))
      ).findFirst();

      if (market.isPresent()) {
        return market.get();
      }
      throw new BankeraException(404, "Unable to find market.");
    } catch (IOException e) {
      throw new BankeraException(404, "Unable to find market.");
    }
  }
}
