package org.knowm.xchange.deribit.v2.service;

import java.io.IOException;
import org.knowm.xchange.deribit.v2.DeribitAdapters;
import org.knowm.xchange.deribit.v2.DeribitExchange;
import org.knowm.xchange.deribit.v2.dto.DeribitException;
import org.knowm.xchange.deribit.v2.dto.marketdata.DeribitOrderBook;
import org.knowm.xchange.deribit.v2.dto.marketdata.DeribitTicker;
import org.knowm.xchange.deribit.v2.dto.marketdata.DeribitTrades;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.service.marketdata.MarketDataService;

/**
 * Implementation of the market data service for Bitmex
 *
 * <ul>
 *   <li>Provides access to various market data values
 * </ul>
 */
public class DeribitMarketDataService extends DeribitMarketDataServiceRaw
    implements MarketDataService {

  /**
   * Constructor
   *
   * @param exchange
   */
  public DeribitMarketDataService(DeribitExchange exchange) {

    super(exchange);
  }

  @Override
  public Ticker getTicker(Instrument instrument, Object... args) throws IOException {
    String deribitInstrumentName = DeribitAdapters.adaptInstrumentName(instrument);
    DeribitTicker deribitTicker;

    try {
      deribitTicker = super.getDeribitTicker(deribitInstrumentName);
    } catch (DeribitException ex) {
      throw DeribitAdapters.adapt(ex);
    }
    return DeribitAdapters.adaptTicker(deribitTicker);
  }

  @Override
  public OrderBook getOrderBook(Instrument instrument, Object... args) throws IOException {
    String deribitInstrumentName = DeribitAdapters.adaptInstrumentName(instrument);
    DeribitOrderBook deribitOrderBook;
    try {
      deribitOrderBook = super.getDeribitOrderBook(deribitInstrumentName, null);
    } catch (DeribitException ex) {
      throw new ExchangeException(ex);
    }

    return DeribitAdapters.adaptOrderBook(deribitOrderBook);
  }

  @Override
  public Trades getTrades(Instrument instrument, Object... args) throws IOException {
    String deribitInstrumentName = DeribitAdapters.adaptInstrumentName(instrument);
    DeribitTrades deribitTrades;

    try {
      deribitTrades =
          super.getLastTradesByInstrument(deribitInstrumentName, null, null, null, null, null);
    } catch (DeribitException ex) {
      throw new ExchangeException(ex);
    }

    return DeribitAdapters.adaptTrades(deribitTrades, instrument);
  }
}
