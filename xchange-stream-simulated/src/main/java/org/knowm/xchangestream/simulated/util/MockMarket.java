package org.knowm.xchangestream.simulated.util;

import static org.knowm.xchange.dto.Order.OrderType.ASK;
import static org.knowm.xchange.dto.Order.OrderType.BID;

import java.io.IOException;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchangestream.simulated.SimulatedStreamingExchange;
import org.knowm.xchangestream.simulated.SimulatedStreamingTradeService;

public class MockMarket {

  private final SimulatedStreamingExchange exchange;
  private final SimulatedStreamingTradeService tradeService;
  private CurrencyPair currencyPair;

  public MockMarket(SimulatedStreamingExchange exchange, CurrencyPair currencyPair) {
    this.exchange = exchange;
    this.tradeService = exchange.getTradeService();
    this.currencyPair = currencyPair;
  }

  public MockMarket apiKey(String apiKey) {
    exchange.getExchangeSpecification().setApiKey(apiKey);
    return this;
  }

  public MockMarket deposit(Number base, Number counter) {
    return deposit(currencyPair.base, BigDecimalUtils.toBigDecimal(base))
        .deposit(currencyPair.counter, BigDecimalUtils.toBigDecimal(counter));
  }

  public MockMarket deposit(Currency currency, Number amount) {
    exchange.getAccountService().deposit(currency, BigDecimalUtils.toBigDecimal(amount));
    return this;
  }

  public MockMarket pair(CurrencyPair currencyPair) {
    this.currencyPair = currencyPair;
    return this;
  }

  public MockMarket placeAsk(Number price, Number amount) throws IOException {
    return placeOrder(ASK, price, amount);
  }

  public MockMarket placeBid(Number price, Number amount) throws IOException {
    return placeOrder(BID, price, amount);
  }

  public MockMarket placeOrder(OrderType orderType, Number price, Number amount)
      throws IOException {
    tradeService.placeLimitOrderUnrestricted(
        new LimitOrder.Builder(orderType, currencyPair)
            .limitPrice(BigDecimalUtils.toBigDecimal(price))
            .originalAmount(BigDecimalUtils.toBigDecimal(amount))
            .build());
    return this;
  }

  public OrderBook getOrderBook() throws IOException {
    return exchange.getMarketDataService().getOrderBook(currencyPair);
  }

  public MockMarket placeOrders() throws IOException {
    return placeAsk(10000, 200)
        .placeAsk(1000, 0.1d)
        .placeAsk(99, 0.05d)
        .placeAsk(99, 0.25d)
        .placeAsk(98, 0.3d) // ///////////
        .placeBid(97, 0.4d)
        .placeBid(96, 0.25d)
        .placeBid(95, 0.6d)
        .placeBid(94, 0.7d)
        .placeBid(93, 0.8d)
        .placeBid(1, 1002);
  }
}
