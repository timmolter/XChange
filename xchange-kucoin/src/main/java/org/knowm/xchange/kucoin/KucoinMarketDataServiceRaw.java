package org.knowm.xchange.kucoin;

import static org.knowm.xchange.kucoin.KucoinExceptionClassifier.classifyingExceptions;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.kucoin.dto.response.*;

public class KucoinMarketDataServiceRaw extends KucoinBaseService {

  protected KucoinMarketDataServiceRaw(KucoinExchange exchange) {
    super(exchange);
  }

  public TickerResponse getKucoinTicker(CurrencyPair pair) throws IOException {
    return classifyingExceptions(() -> symbolApi.getTicker(KucoinAdapters.adaptCurrencyPair(pair)));
  }

  public AllTickersResponse getKucoinTickers() throws IOException {
    return classifyingExceptions(symbolApi::getTickers);
  }

  public SymbolTickResponse getKucoin24hrStats(CurrencyPair pair) throws IOException {
    return classifyingExceptions(
        () -> symbolApi.getMarketStats(KucoinAdapters.adaptCurrencyPair(pair)));
  }

  public Map<String, BigDecimal> getKucoinPrices() throws IOException {
    return classifyingExceptions(symbolApi::getPrices);
  }

  public TradeFeeResponse getKucoinBaseFee() throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () -> tradingFeeAPI.getBaseFee(apiKey, digest, nonceFactory, passphrase));
  }

  public List<TradeFeeResponse> getKucoinTradeFee(String symbols) throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
            () -> tradingFeeAPI.getTradeFee(apiKey, digest, nonceFactory, passphrase, symbols));
  }

  public List<SymbolResponse> getKucoinSymbols() throws IOException {
    return classifyingExceptions(symbolApi::getSymbols);
  }

  public List<CurrenciesResponse> getKucoinCurrencies() throws IOException {
    return classifyingExceptions(symbolApi::getCurrencies);
  }

  public OrderBookResponse getKucoinOrderBookPartial(CurrencyPair pair) throws IOException {
    return classifyingExceptions(
        () -> orderBookApi.getPartOrderBookAggregated(KucoinAdapters.adaptCurrencyPair(pair)));
  }

  public OrderBookResponse getKucoinOrderBookFull(CurrencyPair pair) throws IOException {
    return classifyingExceptions(
        () -> orderBookApi.getFullOrderBookAggregated(KucoinAdapters.adaptCurrencyPair(pair)));
  }

  public List<TradeHistoryResponse> getKucoinTrades(CurrencyPair pair) throws IOException {
    return classifyingExceptions(
        () -> historyApi.getTradeHistories(KucoinAdapters.adaptCurrencyPair(pair)));
  }
}
