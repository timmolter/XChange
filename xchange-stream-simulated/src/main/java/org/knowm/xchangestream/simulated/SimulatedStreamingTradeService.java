package org.knowm.xchangestream.simulated;

import java.io.IOException;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.simulated.SimulatedTradeService;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for SimulatedTradeService that allows to fire a reactive flow
 *
 * @author mrmx
 */
public class SimulatedStreamingTradeService implements TradeService {
  private static final org.slf4j.Logger log =
      LoggerFactory.getLogger(SimulatedStreamingTradeService.class);
  private final SimulatedStreamingExchange exchange;
  private final SimulatedTradeService tradeService;

  SimulatedStreamingTradeService(SimulatedStreamingExchange exchange) {
    this.exchange = exchange;
    this.tradeService = exchange.getSimulatedExchange().getTradeService();
  }

  @Override
  public String placeLimitOrder(LimitOrder limitOrder) throws IOException {
    String orderId = tradeService.placeLimitOrder(limitOrder);
    publishTicker(limitOrder);
    return orderId;
  }

  /**
   * Use instead of {@link #placeLimitOrder(LimitOrder)} to bypass rate limitations and transient
   * errors when building up a simulated order book.
   *
   * @param limitOrder The limit order.
   * @return The id of the resulting order.
   */
  public String placeLimitOrderUnrestricted(LimitOrder limitOrder) {
    String orderId = tradeService.placeLimitOrderUnrestricted(limitOrder);
    publishTicker(limitOrder);
    return orderId;
  }

  @Override
  public String placeMarketOrder(MarketOrder marketOrder) throws IOException {
    String orderId = tradeService.placeMarketOrder(marketOrder);
    publishTicker(marketOrder);
    return orderId;
  }

  private void publishTicker(Order order) {
    try {
      CurrencyPair currencyPair = (CurrencyPair) order.getInstrument();
      Ticker ticker = exchange.getMarketDataService().getTicker(currencyPair);
      exchange.getStreamingMarketDataService().publish(currencyPair, ticker);
    } catch (IOException ex) {
      log.error("publishing ticker", ex);
    }
  }
}
