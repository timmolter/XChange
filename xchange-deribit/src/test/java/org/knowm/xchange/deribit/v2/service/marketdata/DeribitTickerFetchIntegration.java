package org.knowm.xchange.deribit.v2.service.marketdata;

import org.junit.BeforeClass;
import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.deribit.v2.DeribitExchange;
import org.knowm.xchange.deribit.v2.dto.marketdata.DeribitTicker;
import org.knowm.xchange.deribit.v2.dto.marketdata.DeribitTrades;
import org.knowm.xchange.deribit.v2.service.DeribitMarketDataService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class DeribitTickerFetchIntegration {

  private static Exchange exchange;
  private static DeribitMarketDataService deribitMarketDataService;

  @BeforeClass
  public static void setUp() {
    exchange = ExchangeFactory.INSTANCE.createExchange(DeribitExchange.class);
    exchange.applySpecification(((DeribitExchange) exchange).getSandboxExchangeSpecification());
    deribitMarketDataService = (DeribitMarketDataService) exchange.getMarketDataService();
  }

  @Test
  public void getDeribitCurrenciesTest() throws Exception {
    DeribitTicker ticker = deribitMarketDataService.getTicker("BTC-PERPETUAL");

    assertThat(ticker).isNotNull();
    assertThat(ticker.getInstrumentName()).isEqualTo("BTC-PERPETUAL");
    assertThat(ticker.getLastPrice()).isGreaterThan(new BigDecimal("0"));
  }
}