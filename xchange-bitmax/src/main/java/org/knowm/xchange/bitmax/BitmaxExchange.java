package org.knowm.xchange.bitmax;

import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.bitmax.service.BitmaxAccountService;
import org.knowm.xchange.bitmax.service.BitmaxMarketDataService;
import org.knowm.xchange.bitmax.service.BitmaxMarketDataServiceRaw;
import org.knowm.xchange.bitmax.service.BitmaxTradeService;
import org.knowm.xchange.exceptions.ExchangeException;

import java.io.IOException;

/**
 * @author makarid
 */

public class BitmaxExchange extends BaseExchange implements Exchange {

  @Override
  protected void initServices() {
    this.marketDataService = new BitmaxMarketDataService(this);
    this.accountService = new BitmaxAccountService(this);
    this.tradeService = new BitmaxTradeService(this);
  }

  @Override
  public ExchangeSpecification getDefaultExchangeSpecification() {
    ExchangeSpecification exchangeSpecification = new ExchangeSpecification(this.getClass());
    exchangeSpecification.setSslUri("https://bitmax.io/");
    exchangeSpecification.setExchangeName("Bitmax");
    exchangeSpecification.setExchangeDescription(
        "Bitmax is a Bitcoin exchange with spot and future markets.");
    return exchangeSpecification;
  }

  @Override
  public void remoteInit() throws IOException, ExchangeException {
    BitmaxMarketDataServiceRaw raw = ((BitmaxMarketDataServiceRaw) getMarketDataService());

    exchangeMetaData =
        BitmaxAdapters.adaptExchangeMetaData(raw.getAllAssets(), raw.getAllProducts());
  }
}
