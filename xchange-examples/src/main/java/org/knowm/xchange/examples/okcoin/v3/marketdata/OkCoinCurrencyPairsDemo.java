package org.knowm.xchange.examples.okcoin.v3.marketdata;

import java.io.IOException;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.okcoin.OkCoinExchange;
import org.knowm.xchange.service.marketdata.MarketDataService;

public class OkCoinCurrencyPairsDemo {

  public static void main(String[] args) throws IOException {

    ExchangeSpecification exSpec = new ExchangeSpecification(OkCoinExchange.class);

    // flag to set Use_Intl (USD) or China (default)
    exSpec.setExchangeSpecificParametersItem("Use_Intl", true);
    Exchange okcoinExchange = ExchangeFactory.INSTANCE.createExchange(exSpec);

    generic(okcoinExchange);
  }

  private static void generic(Exchange okcoinExchange) throws IOException {

    // Interested in the public market data feed (no authentication)
    MarketDataService marketDataService = okcoinExchange.getMarketDataService();
  }
}
