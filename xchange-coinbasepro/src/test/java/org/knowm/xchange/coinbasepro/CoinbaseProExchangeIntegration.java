package org.knowm.xchange.coinbasepro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.coinbasepro.dto.CoinbaseProTrades;
import org.knowm.xchange.coinbasepro.service.CoinbaseProMarketDataServiceRaw;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.service.marketdata.MarketDataService;

public class CoinbaseProExchangeIntegration {

  @Test
  public void testCreateExchangeShouldApplyDefaultSpecification() {
    final Exchange exchange = ExchangeFactory.INSTANCE.createExchange(CoinbaseProExchange.class);

    assertThat(exchange.getExchangeSpecification().getSslUri())
        .isEqualTo("https://api.pro.coinbase.com");
    assertThat(exchange.getExchangeSpecification().getHost()).isEqualTo("api.pro.coinbase.com");
  }

  @Test
  public void coinbaseShouldBeInstantiatedWithoutAnExceptionWhenUsingDefaultSpecification() {

    ExchangeFactory.INSTANCE.createExchange(CoinbaseProExchange.class.getCanonicalName());
  }

  @Test
  public void shouldSupportEthUsdByRemoteInit() throws Exception {

    Exchange ex =
        ExchangeFactory.INSTANCE.createExchange(CoinbaseProExchange.class.getCanonicalName());
    ex.remoteInit();

    CurrencyPair currencyPair = new CurrencyPair("ETH", "USD");
    Assert.assertTrue(
        ((CoinbaseProMarketDataServiceRaw) ex.getMarketDataService())
            .checkProductExists(currencyPair));
  }

  @Test
  public void testExtendedGetTrades() throws IOException {

    final MarketDataService marketDataService;
    final CoinbaseProMarketDataServiceRaw marketDataServiceRaw;
    final CurrencyPair currencyPair = new CurrencyPair("BTC", "EUR");
    final Exchange exchange;

    exchange = ExchangeFactory.INSTANCE.createExchange(CoinbaseProExchange.class);
    marketDataService = exchange.getMarketDataService();
    marketDataServiceRaw = (CoinbaseProMarketDataServiceRaw) exchange.getMarketDataService();

    // get latest trades
    CoinbaseProTrades trades1 =
        marketDataServiceRaw.getCoinbaseProTradesExtended(
            currencyPair, new Long(Integer.MAX_VALUE), null);
    assertEquals("Unexpected trades list length (1000)", 1000, trades1.size());

    // get latest 10 trades
    CoinbaseProTrades trades2 =
        marketDataServiceRaw.getCoinbaseProTradesExtended(
            currencyPair, new Long(Integer.MAX_VALUE), 10);
    assertEquals("Unexpected trades list length (10)", 10, trades2.size());

    Trades trades3 = marketDataService.getTrades(currencyPair, new Long(0), new Long(1005));
    assertEquals("Unexpected trades list length (100)", 1004, trades3.getTrades().size());
  }

  @Test
  public void testExchangeMetaData() {
    final Exchange exchange = ExchangeFactory.INSTANCE.createExchange(CoinbaseProExchange.class);

    ExchangeMetaData exchangeMetaData = exchange.getExchangeMetaData();

    Assert.assertNotNull(exchangeMetaData);
    Assert.assertNotNull(exchangeMetaData.getCurrencies());
    Assert.assertNotNull(
        "USDC is not defined", exchangeMetaData.getCurrencies().get(new Currency("USDC")));
  }
}
