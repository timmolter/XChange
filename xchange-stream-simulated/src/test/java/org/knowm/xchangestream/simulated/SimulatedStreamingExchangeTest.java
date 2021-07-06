package org.knowm.xchangestream.simulated;

import static org.assertj.core.api.Assertions.*;
import static org.knowm.xchange.currency.Currency.BTC;
import static org.knowm.xchange.currency.Currency.USD;
import static org.knowm.xchange.currency.CurrencyPair.BTC_USD;
import static org.knowm.xchange.dto.Order.OrderType.BID;
import static org.knowm.xchange.simulated.SimulatedExchange.ACCOUNT_FACTORY_PARAM;
import static org.knowm.xchange.simulated.SimulatedExchange.ENGINE_FACTORY_PARAM;

import io.reactivex.Completable;
import io.reactivex.observers.TestObserver;
import java.io.IOException;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.simulated.AccountFactory;
import org.knowm.xchange.simulated.MatchingEngineFactory;
import org.knowm.xchangestream.simulated.util.MockMarket;

/** @author mrmx */
public class SimulatedStreamingExchangeTest {

  private static final BigDecimal INITIAL_BALANCE = new BigDecimal(1000);

  private SimulatedStreamingExchange exchange;
  private MatchingEngineFactory matchingEngineFactory;
  private AccountFactory accountFactory;

  @Before
  public void setup() throws IOException {
    // By default, the matching engines are scoped to each instance of the Exchange. This ensures
    // that all instances share the same engine within the scope of each test.
    accountFactory = new AccountFactory();
    matchingEngineFactory = new MatchingEngineFactory(accountFactory);
    // As a market maker, fill the order book with buy/sell orders
    mockMarket();

    // This is what we'll use for trade testing
    ExchangeSpecification exchangeSpecification =
        new ExchangeSpecification(SimulatedStreamingExchange.class);
    exchangeSpecification.setApiKey("Tester");
    exchangeSpecification.setExchangeSpecificParametersItem(
        ENGINE_FACTORY_PARAM, matchingEngineFactory);
    exchangeSpecification.setExchangeSpecificParametersItem(ACCOUNT_FACTORY_PARAM, accountFactory);
    exchange =
        (SimulatedStreamingExchange) ExchangeFactory.INSTANCE.createExchange(exchangeSpecification);

    // Provide an initial balance
    exchange.getAccountService().deposit(USD, INITIAL_BALANCE);
    exchange.getAccountService().deposit(BTC, INITIAL_BALANCE);
  }

  /** Test of connect method, of class SimulatedStreamingExchange. */
  @Test
  public void testConnect() {
    System.out.println("connect");
    Completable result = exchange.connect();
    assertThat(result).isNotNull();
    assertThat(result.blockingGet()).isNull();
  }

  /** Test of disconnect method, of class SimulatedStreamingExchange. */
  @Test
  public void testDisconnect() {
    System.out.println("disconnect");
    Completable result = exchange.disconnect();
    assertThat(result).isNotNull();
    assertThat(result.blockingGet()).isNull();
  }

  /** Test of isAlive method, of class SimulatedStreamingExchange. */
  @Test
  public void testIsAlive() {
    System.out.println("isAlive");
    assertThat(exchange.isAlive()).isFalse();
    assertThat(exchange.connect().blockingGet()).isNull();
    assertThat(exchange.isAlive()).isTrue();
  }

  /** Test of getStreamingMarketDataService method, of class SimulatedStreamingExchange. */
  @Test
  public void testGetStreamingMarketDataService() {
    System.out.println("getStreamingMarketDataService");
    assertThat(exchange.connect().blockingGet()).isNull();
    SimulatedStreamingMarketDataService streamingMarketDataService =
        exchange.getStreamingMarketDataService();
    assertThat(streamingMarketDataService).isNotNull();
  }

  @Test
  public void testGetStreamingMarketDataServiceTickerSubscription() throws IOException {
    System.out.println("testGetStreamingMarketDataServiceTickerSubscription");
    exchange.connect().blockingAwait();
    SimulatedStreamingMarketDataService streamingMarketDataService =
        exchange.getStreamingMarketDataService();
    assertThat(streamingMarketDataService).isNotNull();
    TestObserver<Ticker> test = streamingMarketDataService.getTicker(BTC_USD).test();
    test.assertEmpty();
    String orderId =
        exchange
            .getTradeService()
            .placeLimitOrder(
                new LimitOrder.Builder(BID, BTC_USD)
                    .limitPrice(new BigDecimal("97.51"))
                    .originalAmount(new BigDecimal("0.7"))
                    .build());
    assertThat(orderId).isNotBlank();
    assertThat(test.valueCount()).isEqualTo(1);
    Ticker ticker = test.values().get(0);
    assertThat(ticker).isEqualTo(exchange.getMarketDataService().getTicker(BTC_USD));
    assertThat(ticker.getBid()).isEqualByComparingTo("97.51");
  }

  private void mockMarket() throws IOException {
    ExchangeSpecification exchangeSpecification =
        new ExchangeSpecification(SimulatedStreamingExchange.class);
    exchangeSpecification.setApiKey("MarketMakers");
    exchangeSpecification.setExchangeSpecificParametersItem(
        ENGINE_FACTORY_PARAM, matchingEngineFactory);
    exchangeSpecification.setExchangeSpecificParametersItem(ACCOUNT_FACTORY_PARAM, accountFactory);
    SimulatedStreamingExchange marketMakerExchange =
        (SimulatedStreamingExchange) ExchangeFactory.INSTANCE.createExchange(exchangeSpecification);
    new MockMarket(marketMakerExchange, BTC_USD)
        .deposit(BTC, 10000)
        .deposit(USD, 10000)
        .placeOrders();
  }
}
