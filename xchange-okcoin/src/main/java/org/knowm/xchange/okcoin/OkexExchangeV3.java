package org.knowm.xchange.okcoin;

import java.text.SimpleDateFormat;

import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.okcoin.v3.service.OkexAccountService;
import org.knowm.xchange.okcoin.v3.service.OkexMarketDataService;
import org.knowm.xchange.okcoin.v3.service.OkexTradeService;

import si.mazi.rescu.SynchronizedValueFactory;

public class OkexExchangeV3 extends BaseExchange {

  public static SimpleDateFormat timestampFormatter =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

  @Override
  protected void initServices() {
    this.marketDataService = new OkexMarketDataService(this);
    this.accountService = new OkexAccountService(this);
    this.tradeService = new OkexTradeService(this);
  }

  @Override
  public ExchangeSpecification getDefaultExchangeSpecification() {
    ExchangeSpecification spec = new ExchangeSpecification(this.getClass().getCanonicalName());
    spec.setSslUri("https://www.okex.com");
    spec.setHost("www.okex.com");
    spec.setExchangeName("OKEx");
    spec.setExchangeDescription("OKEx is a globally oriented crypto-currency trading platform.");
    return spec;
  }

  @Override
  public SynchronizedValueFactory<Long> getNonceFactory() {
    throw new ExchangeException("Nonce value not supported at OKEx.");
  }
}
