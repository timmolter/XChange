package org.knowm.xchange.bitmax.service;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.bitmax.BitmaxAdapters;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.service.marketdata.MarketDataService;

import java.io.IOException;

public class BitmaxMarketDataService extends BitmaxMarketDataServiceRaw
    implements MarketDataService {

  public BitmaxMarketDataService(Exchange exchange) {
    super(exchange);
  }

  @Override
  public OrderBook getOrderBook(CurrencyPair currencyPair, Object... args) throws IOException {
    return BitmaxAdapters.adaptOrderBook(getBitmaxOrderbook(currencyPair.toString()));
  }

  @Override
  public Trades getTrades(CurrencyPair currencyPair, Object... args) throws IOException {
    return BitmaxAdapters.adaptTrades(getBitmaxTrades(currencyPair.toString()));
  }
}
