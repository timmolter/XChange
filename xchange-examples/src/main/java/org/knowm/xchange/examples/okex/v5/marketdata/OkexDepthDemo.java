package org.knowm.xchange.examples.okex.v5.marketdata;

import java.io.IOException;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.instrument.FuturesContract;
import org.knowm.xchange.okex.v5.OkexExchange;
import org.knowm.xchange.service.marketdata.MarketDataService;

public class OkexDepthDemo {

  public static void main(String[] args) throws IOException {

    ExchangeSpecification exSpec = new ExchangeSpecification(OkexExchange.class);
     Exchange okexExchange = ExchangeFactory.INSTANCE.createExchange(exSpec);
    generic(okexExchange);
  }

  private static void generic(Exchange okexExchange) throws IOException {

    // Interested in the public market data feed (no authentication)
    MarketDataService marketDataService = okexExchange.getMarketDataService();

    FuturesContract contract = new FuturesContract(CurrencyPair.BTC_USDT, "SWAP");

    // Get the latest full order book data for BTC/USDT Perpetual Swap
    OrderBook orderBook = marketDataService.getOrderBook(contract);
    System.out.println(orderBook.toString());
    System.out.println(
        "full orderbook size: " + (orderBook.getAsks().size() + orderBook.getBids().size()));
  }
}
