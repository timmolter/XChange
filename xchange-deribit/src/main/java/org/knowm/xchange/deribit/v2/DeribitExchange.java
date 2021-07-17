package org.knowm.xchange.deribit.v2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.deribit.v2.dto.Kind;
import org.knowm.xchange.deribit.v2.dto.marketdata.DeribitCurrency;
import org.knowm.xchange.deribit.v2.dto.marketdata.DeribitInstrument;
import org.knowm.xchange.deribit.v2.service.DeribitAccountService;
import org.knowm.xchange.deribit.v2.service.DeribitMarketDataService;
import org.knowm.xchange.deribit.v2.service.DeribitMarketDataServiceRaw;
import org.knowm.xchange.deribit.v2.service.DeribitTradeService;
import org.knowm.xchange.derivative.FuturesContract;
import org.knowm.xchange.derivative.OptionsContract;
import org.knowm.xchange.dto.meta.CurrencyMetaData;
import org.knowm.xchange.dto.meta.DerivativeMetaData;
import org.knowm.xchange.instrument.Instrument;

public class DeribitExchange extends BaseExchange implements Exchange {

  @Override
  public void applySpecification(ExchangeSpecification exchangeSpecification) {

    super.applySpecification(exchangeSpecification);
  }

  @Override
  protected void initServices() {
    this.marketDataService = new DeribitMarketDataService(this);
    this.accountService = new DeribitAccountService(this);
    this.tradeService = new DeribitTradeService(this);
  }

  @Override
  public ExchangeSpecification getDefaultExchangeSpecification() {

    ExchangeSpecification exchangeSpecification = new ExchangeSpecification(this.getClass());
    exchangeSpecification.setSslUri("https://www.deribit.com");
    exchangeSpecification.setHost("deribit.com");
    //    exchangeSpecification.setPort(80);
    exchangeSpecification.setExchangeName("Deribit");
    exchangeSpecification.setExchangeDescription("Deribit is a Bitcoin futures exchange");
    return exchangeSpecification;
  }

  public ExchangeSpecification getSandboxExchangeSpecification() {

    ExchangeSpecification exchangeSpecification = new ExchangeSpecification(this.getClass());
    exchangeSpecification.setSslUri("https://test.deribit.com/");
    exchangeSpecification.setHost("test.deribit.com");
    //    exchangeSpecification.setPort(80);
    return exchangeSpecification;
  }

  @Override
  public void remoteInit() throws IOException {
    updateExchangeMetaData();
  }

  public void updateExchangeMetaData() throws IOException {

    Map<Currency, CurrencyMetaData> currencies = exchangeMetaData.getCurrencies();
    Map<FuturesContract, DerivativeMetaData> futures = exchangeMetaData.getFutures();
    Map<OptionsContract, DerivativeMetaData> options = exchangeMetaData.getOptions();

    List<DeribitCurrency> activeDeribitCurrencies =
        ((DeribitMarketDataServiceRaw) marketDataService).getDeribitCurrencies();

    currencies.clear();
    futures.clear();
    options.clear();

    for (DeribitCurrency deribitCurrency : activeDeribitCurrencies) {
      currencies.put(new Currency(deribitCurrency.getCurrency()), DeribitAdapters.adaptMeta(deribitCurrency));

      List<DeribitInstrument> deribitInstruments = ((DeribitMarketDataServiceRaw) marketDataService)
              .getDeribitInstruments(deribitCurrency.getCurrency(), null, null);

      for(DeribitInstrument deribitInstrument : deribitInstruments) {
        if (deribitInstrument.getKind() == Kind.future) {
          futures.put(DeribitAdapters.adaptFuturesContract(deribitInstrument), DeribitAdapters.adaptMeta(deribitInstrument));
        } else {
          options.put(DeribitAdapters.adaptOptionsContract(deribitInstrument), DeribitAdapters.adaptMeta(deribitInstrument));
        }
      }
    }

  }

    @Override
    public List<Instrument> getExchangeInstruments() {
        ArrayList<Instrument> instruments = new ArrayList<>();
        instruments.addAll(getExchangeMetaData().getFutures().keySet());
        instruments.addAll(getExchangeMetaData().getOptions().keySet());
        return instruments;
    }
}
