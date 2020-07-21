package org.knowm.xchange.coinfloor.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.coinfloor.CoinfloorExchange;
import org.knowm.xchange.coinfloor.service.CoinfloorMarketDataServiceRaw.CoinfloorInterval;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.service.marketdata.MarketDataService;

public class CoinfloorPublicTradesIntegration {

  @Test
  public void fetchTransactionTest() throws IOException {
    Exchange exchange = ExchangeFactory.INSTANCE.createExchange(CoinfloorExchange.class.getName());
    MarketDataService service = exchange.getMarketDataService();

    Trades trades = service.getTrades(CurrencyPair.BTC_GBP, CoinfloorInterval.HOUR);
    assertThat(trades.getTrades()).isNotEmpty();

    int tradeCount = trades.getTrades().size();
    Trade mostRecentTrade = trades.getTrades().get(tradeCount - 1);
    assertThat(mostRecentTrade.getPrice()).isGreaterThan(BigDecimal.ZERO);
    assertThat(mostRecentTrade.getOriginalAmount()).isGreaterThan(BigDecimal.ZERO);

    assertThat(trades.getlastID()).isEqualTo(mostRecentTrade.getId());
  }
}
