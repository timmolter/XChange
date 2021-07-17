package org.knowm.xchange.bitfinex;

import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.bitfinex.dto.BitfinexException;
import org.knowm.xchange.bitfinex.service.*;
import org.knowm.xchange.bitfinex.v1.dto.account.BitfinexAccountFeesResponse;
import org.knowm.xchange.bitfinex.v1.dto.marketdata.BitfinexSymbolDetail;
import org.knowm.xchange.bitfinex.v1.dto.trade.BitfinexAccountInfosResponse;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.utils.nonce.AtomicLongIncrementalTime2013NonceFactory;
import si.mazi.rescu.SynchronizedValueFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BitfinexExchange extends BaseExchange implements Exchange {

  private static ResilienceRegistries RESILIENCE_REGISTRIES;

  private SynchronizedValueFactory<Long> nonceFactory =
      new AtomicLongIncrementalTime2013NonceFactory();

  @Override
  protected void initServices() {
    this.marketDataService = new BitfinexMarketDataService(this, getResilienceRegistries());
    this.accountService = new BitfinexAccountService(this, getResilienceRegistries());
    this.tradeService = new BitfinexTradeService(this, getResilienceRegistries());
  }

  @Override
  public ResilienceRegistries getResilienceRegistries() {
    if (RESILIENCE_REGISTRIES == null) {
      RESILIENCE_REGISTRIES = BitfinexResilience.createRegistries();
    }
    return RESILIENCE_REGISTRIES;
  }

  @Override
  public ExchangeSpecification getDefaultExchangeSpecification() {

    ExchangeSpecification exchangeSpecification = new ExchangeSpecification(this.getClass());
    exchangeSpecification.setSslUri("https://api.bitfinex.com/");
    exchangeSpecification.setHost("api.bitfinex.com");
    exchangeSpecification.setPort(80);
    exchangeSpecification.setExchangeName("BitFinex");
    exchangeSpecification.setExchangeDescription("BitFinex is a bitcoin exchange.");
    exchangeSpecification.getResilience().setRateLimiterEnabled(true);
    exchangeSpecification.getResilience().setRetryEnabled(true);

    return exchangeSpecification;
  }

  @Override
  public SynchronizedValueFactory<Long> getNonceFactory() {

    return nonceFactory;
  }

  @Override
  public void remoteInit() throws IOException, ExchangeException {

    try {
      BitfinexMarketDataServiceRaw dataService =
          (BitfinexMarketDataServiceRaw) this.marketDataService;
      List<CurrencyPair> currencyPairs = dataService.getExchangeSymbols();
      exchangeMetaData = BitfinexAdapters.adaptMetaData(currencyPairs, exchangeMetaData);

      // Get the last-price of each pair. It is needed to infer XChange's priceScale out of
      // Bitfinex's pricePercision

      Map<CurrencyPair, BigDecimal> lastPrices =
          Arrays.stream(dataService.getBitfinexTickers(null))
              .map(BitfinexAdapters::adaptTicker)
              .collect(Collectors.toMap(t -> t.getCurrencyPair(), t -> t.getLast()));

      final List<BitfinexSymbolDetail> symbolDetails = dataService.getSymbolDetails();
      exchangeMetaData =
          BitfinexAdapters.adaptMetaData(exchangeMetaData, symbolDetails, lastPrices);

      if (exchangeSpecification.getApiKey() != null
          && exchangeSpecification.getSecretKey() != null) {
        // Bitfinex does not provide any specific wallet health info
        // So instead of wallet status, fetch platform status to get wallet health
        Integer bitfinexPlatformStatus = dataService.getBitfinexPlatformStatus()[0];
        // Additional remoteInit configuration for authenticated instances
        BitfinexAccountService accountService = (BitfinexAccountService) this.accountService;
        final BitfinexAccountFeesResponse accountFees = accountService.getAccountFees();
        exchangeMetaData = BitfinexAdapters.adaptMetaData(accountFees, bitfinexPlatformStatus, exchangeMetaData);

        BitfinexTradeService tradeService = (BitfinexTradeService) this.tradeService;
        final BitfinexAccountInfosResponse[] bitfinexAccountInfos =
            tradeService.getBitfinexAccountInfos();

        exchangeMetaData = BitfinexAdapters.adaptMetaData(bitfinexAccountInfos, exchangeMetaData);
      }
    } catch (BitfinexException e) {
      throw BitfinexErrorAdapter.adapt(e);
    }
  }
}
