package org.knowm.xchange.examples.okex.v5.marketdata;

import java.io.IOException;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.instrument.FuturesContract;
import org.knowm.xchange.okex.v5.OkexExchange;
import org.knowm.xchange.service.marketdata.MarketDataService;

public class OkexTradesDemo {

  public static void main(String[] args) throws IOException {

    ExchangeSpecification exSpec = new ExchangeSpecification(OkexExchange.class);

    Exchange okexExchange = ExchangeFactory.INSTANCE.createExchange(exSpec);

    generic(okexExchange);
  }

  private static void generic(Exchange okexExchange) throws IOException {

    // Interested in the public market data feed (no authentication)

    MarketDataService marketDataService = okexExchange.getMarketDataService();
    FuturesContract contract = new FuturesContract(CurrencyPair.BTC_USDT, "210924");
   
    // Get the latest trades data for BTC_UST Sept 24th Contact
    Trades trades = marketDataService.getTrades(contract);

    System.out.println(trades);
    System.out.println("Trades(0): " + trades.getTrades().get(0).toString());
    System.out.println("Trades size: " + trades.getTrades().size());


  }
}
