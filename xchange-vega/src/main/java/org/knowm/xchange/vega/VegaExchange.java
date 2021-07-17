package org.knowm.xchange.vega;

import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.vega.service.VegaMarketDataService;

import java.io.IOException;
import java.util.List;

public class VegaExchange extends BaseExchange implements Exchange {

    @Override
    protected void initServices() {
        this.marketDataService = new VegaMarketDataService(this);
//        this.tradeService = new BitstampTradeService(this);
//        this.accountService = new BitstampAccountService(this);
    }

    @Override
    public ExchangeSpecification getDefaultExchangeSpecification() {
        ExchangeSpecification exchangeSpecification = new ExchangeSpecification(this.getClass());
        exchangeSpecification.setHost("n06.testnet.vega.xyz");
        exchangeSpecification.setPort(3002);
        exchangeSpecification.setExchangeName("Vega");
        exchangeSpecification.setExchangeDescription(
                "Vega is a protocol for creating and trading margined financial products on a fully decentralised network.");
        return exchangeSpecification;
    }

    @Override
    public void remoteInit() throws IOException {
        List<CurrencyPair> currencyPairs =
                ((VegaMarketDataService) marketDataService).getExchangeSymbols();

        exchangeMetaData = VegaAdapters.adaptMetaData(currencyPairs);
    }

    @Override
    public void applySpecification(ExchangeSpecification exchangeSpecification) {
        super.applySpecification(exchangeSpecification);
    }
}
