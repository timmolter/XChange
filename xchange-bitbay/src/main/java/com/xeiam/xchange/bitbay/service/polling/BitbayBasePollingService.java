package com.xeiam.xchange.bitbay.service.polling;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.service.BaseExchangeService;
import com.xeiam.xchange.service.polling.BasePollingService;

/**
 * @author kpysniak
 */
public class BitbayBasePollingService extends BaseExchangeService implements BasePollingService {

  public static final List<CurrencyPair> CURRENCY_PAIRS = Arrays.asList(

  CurrencyPair.BTC_USD,

  CurrencyPair.BTC_PLN,

  CurrencyPair.LTC_USD,

  new CurrencyPair(Currencies.LTC, Currencies.PLN)

  );

  /**
   * Constructor Initialize common properties from the exchange specification
   * 
   * @param exchangeSpecification The {@link com.xeiam.xchange.ExchangeSpecification}
   */
  protected BitbayBasePollingService(ExchangeSpecification exchangeSpecification) {

    super(exchangeSpecification);
  }

  @Override
  public Collection<CurrencyPair> getExchangeSymbols() throws IOException {

    return CURRENCY_PAIRS;
  }
}
