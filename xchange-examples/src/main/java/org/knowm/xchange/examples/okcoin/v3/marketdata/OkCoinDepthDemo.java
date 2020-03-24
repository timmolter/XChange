package org.knowm.xchange.examples.okcoin.v3.marketdata;

import java.io.IOException;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.okcoin.OkCoinExchange;
import org.knowm.xchange.okcoin.dto.marketdata.OkCoinDepth;
import org.knowm.xchange.okcoin.service.OkCoinMarketDataServiceRaw;
import org.knowm.xchange.service.marketdata.MarketDataService;

public class OkCoinDepthDemo {

  public static void main(String[] args) throws IOException {

    ExchangeSpecification exSpec = new ExchangeSpecification(OkCoinExchange.class);

    // flag to set Use_Intl (USD) or China (default)
    exSpec.setExchangeSpecificParametersItem("Use_Intl", true);
    Exchange okcoinExchange = ExchangeFactory.INSTANCE.createExchange(exSpec);

    generic(okcoinExchange);
    raw(okcoinExchange);
  }

  private static void generic(Exchange okcoinExchange) throws IOException {

    // Interested in the public market data feed (no authentication)
    MarketDataService marketDataService = okcoinExchange.getMarketDataService();

    // Get the latest full order book data for NMC/XRP
    OrderBook orderBook = marketDataService.getOrderBook(CurrencyPair.BTC_CNY);
    System.out.println(orderBook.toString());
    System.out.println(
        "full orderbook size: " + (orderBook.getAsks().size() + orderBook.getBids().size()));
  }

  private static void raw(Exchange okcoinExchange) throws IOException {

    // Interested in the public market data feed (no authentication)
    OkCoinMarketDataServiceRaw okCoinMarketDataServiceRaw =
        (OkCoinMarketDataServiceRaw) okcoinExchange.getMarketDataService();

    // Get the latest full order book data
    OkCoinDepth depth = okCoinMarketDataServiceRaw.getDepth(CurrencyPair.BTC_CNY);
    System.out.println(depth.toString());
    System.out.println("size: " + (depth.getAsks().length + depth.getBids().length));
  }
}
