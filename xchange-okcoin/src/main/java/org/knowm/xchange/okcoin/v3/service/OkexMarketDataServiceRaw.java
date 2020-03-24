package org.knowm.xchange.okcoin.v3.service;

import java.io.IOException;
import java.util.List;

import org.knowm.xchange.okcoin.OkexExchangeV3;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexDepth;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexFutureInstrument;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexFutureTicker;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexFuturesTrade;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexSpotInstrument;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexSpotTicker;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexSwapInstrument;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexSwapTicker;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexTrade;

public class OkexMarketDataServiceRaw extends OkexBaseService {

  public OkexMarketDataServiceRaw(OkexExchangeV3 exchange) {
    super(exchange);
  }

  public List<OkexSpotInstrument> getAllSpotInstruments() throws IOException {
    return okex.getAllSpotInstruments();
  }

  public List<OkexSpotTicker> getAllSpotTickers() throws IOException {
    return okex.getAllSpotTickers();
  }

  public OkexSpotTicker getSpotTicker(String instrumentID) throws IOException {
    OkexSpotTicker tokenPairInformation = okex.getSpotTicker(instrumentID);
    return tokenPairInformation;
  }

  public OkexTrade[] getTrades(String instrumentID) throws IOException {
    return getTrades(instrumentID, null);
  }

  public OkexTrade[] getTrades(String instrumentID, Long since) throws IOException {
    return okex.getTrades(instrumentID, since);
  }

  public OkexDepth getDepth(String instrumentID) throws IOException {
    return getDepth(instrumentID, null);
  }

  public OkexDepth getDepth(String instrumentID, Integer size) throws IOException {
    size = (size == null || size < 1 || size > 200) ? 200 : size;
    return okex.getDepth(instrumentID, size);
  }

  public List<OkexFutureInstrument> getAllFutureInstruments() throws IOException {
    return okex.getAllFutureInstruments();
  }

  public List<OkexFutureTicker> getAllFutureTickers() throws IOException {
    return okex.getAllFutureTickers();
  }

  public OkexFuturesTrade[] getFuturesTrades(String instrumentID) throws IOException {
    return getFuturesTrades(instrumentID, null);
  }

  public OkexFuturesTrade[] getFuturesTrades(String instrumentID, Long since) throws IOException {
    return okex.getFuturesTrades(instrumentID, since);
  }

  public OkexDepth getFuturesDepth(String instrumentID) throws IOException {
    return getFuturesDepth(instrumentID, null);
  }

  public OkexDepth getFuturesDepth(String instrumentID, Integer size) throws IOException {
    size = (size == null || size < 1 || size > 200) ? 200 : size;
    return okex.getFuturesDepth(instrumentID, size);
  }

  public List<OkexSwapInstrument> getAllSwapInstruments() throws IOException {
    return okex.getAllSwapInstruments();
  }

  public List<OkexSwapTicker> getAllSwapTickers() throws IOException {
    return okex.getAllSwapTickers();
  }
}
