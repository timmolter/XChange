package org.knowm.xchange.examples.vega.marketdata;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.vega.VegaExchange;

import java.io.IOException;

public class VegaTickerDemo {
    public static void main(String[] args) throws IOException {
        Exchange vegaExchange = ExchangeFactory.INSTANCE.createExchange(VegaExchange.class);

        generic(vegaExchange);
    }

    private static void generic(Exchange exchange) throws IOException {

        // Interested in the public market data feed (no authentication)
        MarketDataService marketDataService = exchange.getMarketDataService();

        // Get the latest ticker data showing BTC to EUR
        Ticker ticker = marketDataService.getTicker(new CurrencyPair("AAVE", "DAI"));

        System.out.println("Ticker: " + ticker.toString());
        System.out.println("Currency: " + ticker.getInstrument().toString());
        System.out.println("Last: " + ticker.getLast().toString());
        System.out.println("Volume: " + ticker.getVolume().toString());
        System.out.println("High: " + ticker.getHigh().toString());
        System.out.println("Low: " + ticker.getLow().toString());
    }
}
