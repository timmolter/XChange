package org.knowm.xchange.kucoin;

import static org.knowm.xchange.kucoin.KucoinExceptionClassifier.classifyingExceptions;

import java.io.IOException;
import java.util.List;

import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.kucoin.dto.response.CurrenciesResponse;
import org.knowm.xchange.kucoin.dto.response.SymbolResponse;
import org.knowm.xchange.kucoin.dto.response.TradeFeeResponse;
import org.knowm.xchange.kucoin.dto.response.WebsocketResponse;

public class KucoinExchange extends BaseExchange implements Exchange {

  /**
   * Use with {@link ExchangeSpecification#getExchangeSpecificParametersItem(String)} to specify
   * that connection should be made to the Kucoin sandbox instead of the live API.
   */
  public static final String PARAM_SANDBOX = "Use_Sandbox";

  static final String SANDBOX_HOST = "openapi-sandbox.kucoin.com";
  static final String SANDBOX_URI = "https://" + SANDBOX_HOST;

  static final String LIVE_HOST = "openapi-v2.kucoin.com";
  static final String LIVE_URI = "https://" + LIVE_HOST;

  private void concludeHostParams(ExchangeSpecification exchangeSpecification) {
    if (exchangeSpecification.getExchangeSpecificParameters() != null) {
      if (Boolean.TRUE.equals(
          exchangeSpecification.getExchangeSpecificParametersItem(PARAM_SANDBOX))) {
        logger.debug("Connecting to sandbox");
        exchangeSpecification.setSslUri(KucoinExchange.SANDBOX_URI);
        exchangeSpecification.setHost(KucoinExchange.SANDBOX_HOST);
      } else {
        logger.debug("Connecting to live");
      }
    }
  }

  @Override
  public void applySpecification(ExchangeSpecification exchangeSpecification) {
    super.applySpecification(exchangeSpecification);
    concludeHostParams(exchangeSpecification);
  }

  @Override
  protected void initServices() {
    concludeHostParams(exchangeSpecification);
    this.marketDataService = new KucoinMarketDataService(this);
    this.accountService = new KucoinAccountService(this);
    this.tradeService = new KucoinTradeService(this);
  }

  @Override
  public ExchangeSpecification getDefaultExchangeSpecification() {
    ExchangeSpecification exchangeSpecification = new ExchangeSpecification(this.getClass());
    exchangeSpecification.setSslUri(LIVE_URI);
    exchangeSpecification.setHost(LIVE_HOST);
    exchangeSpecification.setPort(80);
    exchangeSpecification.setExchangeName("Kucoin");
    exchangeSpecification.setExchangeDescription("Kucoin is a bitcoin and altcoin exchange.");
    return exchangeSpecification;
  }

  @Override
  public void remoteInit() throws IOException, ExchangeException {

    // fetch fee only if authenticated
    TradeFeeResponse fee = null;
    if (exchangeSpecification.getApiKey() != null) {
      fee = getMarketDataService().getKucoinBaseFee();
    }

    List<CurrenciesResponse> currenciesResponses = getMarketDataService().getKucoinCurrencies();
    List<SymbolResponse> symbolsResponse = getMarketDataService().getKucoinSymbols();

    this.exchangeMetaData =
        KucoinAdapters.adaptMetadata(this.exchangeMetaData, currenciesResponses, symbolsResponse, fee);
  }

  @Override
  public KucoinMarketDataService getMarketDataService() {
    return (KucoinMarketDataService) super.getMarketDataService();
  }

  @Override
  public KucoinTradeService getTradeService() {
    return (KucoinTradeService) super.getTradeService();
  }

  @Override
  public KucoinAccountService getAccountService() {
    return (KucoinAccountService) super.getAccountService();
  }

  public WebsocketResponse getPublicWebsocketConnectionDetails() throws IOException {
    return classifyingExceptions(getAccountService().websocketAPI::getPublicWebsocketDetails);
  }

  public WebsocketResponse getPrivateWebsocketConnectionDetails() throws IOException {
    getAccountService().checkAuthenticated();

    return classifyingExceptions(
        () ->
            getAccountService()
                .websocketAPI
                .getPrivateWebsocketDetails(
                    getAccountService().apiKey,
                    getAccountService().digest,
                    getAccountService().nonceFactory,
                    getAccountService().passphrase));
  }
}
