package org.knowm.xchange.bitfinex.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.knowm.xchange.bitfinex.BitfinexExchange;
import org.knowm.xchange.bitfinex.dto.BitfinexException;
import org.knowm.xchange.bitfinex.v1.dto.marketdata.BitfinexTicker;
import org.knowm.xchange.bitfinex.v1.dto.marketdata.*;
import org.knowm.xchange.bitfinex.v2.dto.marketdata.*;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import si.mazi.rescu.HttpStatusIOException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.knowm.xchange.bitfinex.BitfinexResilience.BITFINEX_RATE_LIMITER;

/**
 * Implementation of the market data service for Bitfinex
 *
 * <ul>
 *   <li>Provides access to various market data values
 * </ul>
 */
public class BitfinexMarketDataServiceRaw extends BitfinexBaseService {

  /**
   * Constructor
   *
   * @param exchange
   */
  public BitfinexMarketDataServiceRaw(
      BitfinexExchange exchange, ResilienceRegistries resilienceRegistries) {
    super(exchange, resilienceRegistries);
  }

  public BitfinexTicker getBitfinexTicker(String pair) throws IOException {
    BitfinexTicker bitfinexTicker =
        decorateApiCall(() -> bitfinex.getTicker(pair))
            .withRetry(retry("market-ticker"))
            .withRateLimiter(rateLimiter(BITFINEX_RATE_LIMITER))
            .call();
    return bitfinexTicker;
  }

  public BitfinexDepth getBitfinexOrderBook(String pair, Integer limitBids, Integer limitAsks)
      throws IOException {
    BitfinexDepth bitfinexDepth;
    if (limitBids == null && limitAsks == null) {
      bitfinexDepth =
          decorateApiCall(() -> bitfinex.getBook(pair))
              .withRetry(retry("market-book"))
              .withRateLimiter(rateLimiter(BITFINEX_RATE_LIMITER))
              .call();
    } else {
      bitfinexDepth =
          decorateApiCall(() -> bitfinex.getBook(pair, limitBids, limitAsks))
              .withRetry(retry("market-book"))
              .withRateLimiter(rateLimiter(BITFINEX_RATE_LIMITER))
              .call();
    }
    return bitfinexDepth;
  }

  public BitfinexLendDepth getBitfinexLendBook(String currency, int limitBids, int limitAsks)
      throws IOException {
    BitfinexLendDepth bitfinexLendDepth =
        decorateApiCall(() -> bitfinex.getLendBook(currency, limitBids, limitAsks))
            .withRetry(retry("market-lendBook"))
            .withRateLimiter(rateLimiter(BITFINEX_RATE_LIMITER))
            .call();
    return bitfinexLendDepth;
  }

  public BitfinexTrade[] getBitfinexTrades(String pair, long sinceTimestamp) throws IOException {
    BitfinexTrade[] bitfinexTrades =
        decorateApiCall(() -> bitfinex.getTrades(pair, sinceTimestamp))
            .withRetry(retry("market-trades"))
            .withRateLimiter(rateLimiter(BITFINEX_RATE_LIMITER))
            .call();
    return bitfinexTrades;
  }

  public BitfinexLend[] getBitfinexLends(String currency, long sinceTimestamp, int limitTrades)
      throws IOException {
    BitfinexLend[] bitfinexLends =
        decorateApiCall(() -> bitfinex.getLends(currency, sinceTimestamp, limitTrades))
            .withRetry(retry("market-lends"))
            .withRateLimiter(rateLimiter(BITFINEX_RATE_LIMITER))
            .call();
    return bitfinexLends;
  }

  public Collection<String> getBitfinexSymbols() throws IOException {
    return decorateApiCall(() -> bitfinex.getSymbols())
        .withRetry(retry("market-symbols"))
        .withRateLimiter(rateLimiter(BITFINEX_RATE_LIMITER))
        .call();
  }

  public List<CurrencyPair> getExchangeSymbols() throws IOException {
    List<CurrencyPair> currencyPairs = new ArrayList<>();
    for (String symbol : bitfinex.getSymbols()) {
      currencyPairs.add(BitfinexAdapters.adaptCurrencyPair(symbol));
    }
    return currencyPairs;
  }

  public List<BitfinexSymbolDetail> getSymbolDetails() throws IOException {
    return decorateApiCall(() -> bitfinex.getSymbolsDetails())
        .withRetry(retry("market-symbolDetail"))
        .withRateLimiter(rateLimiter(BITFINEX_RATE_LIMITER))
        .call();
  }

  //////// v2
  public Integer[] getBitfinexPlatformStatus() throws IOException {
    return decorateApiCall(
            bitfinexV2::getPlatformStatus)
            .withRetry(retry("platform-status"))
            .withRateLimiter(rateLimiter(BITFINEX_RATE_LIMITER))
            .call();
  }

  public org.knowm.xchange.bitfinex.v2.dto.marketdata.BitfinexTicker[] getBitfinexTickers(
      Collection<CurrencyPair> currencyPairs) throws IOException {
    List<ArrayNode> tickers =
        decorateApiCall(
                () ->
                    bitfinexV2.getTickers(
                        BitfinexAdapters.adaptCurrencyPairsToTickersParam(currencyPairs)))
            .withRetry(retry("market-tickers"))
            .withRateLimiter(rateLimiter(BITFINEX_RATE_LIMITER))
            .call();
    return BitfinexAdapters.adoptBitfinexTickers(tickers);
  }

  public org.knowm.xchange.bitfinex.v2.dto.marketdata.BitfinexTicker getBitfinexTickerV2(
      CurrencyPair currencyPair) throws IOException {
    List<ArrayNode> tickers =
        decorateApiCall(
                () ->
                    bitfinexV2.getTickers(
                        BitfinexAdapters.adaptCurrencyPairsToTickersParam(
                            Collections.singletonList(currencyPair))))
            .withRetry(retry("market-ticker"))
            .withRateLimiter(rateLimiter(BITFINEX_RATE_LIMITER))
            .call();
    org.knowm.xchange.bitfinex.v2.dto.marketdata.BitfinexTicker[] ticker =
        BitfinexAdapters.adoptBitfinexTickers(tickers);
    if (ticker.length == 0) {
      throw new BitfinexException("Unknown Symbol");
    } else {
      return ticker[0];
    }
  }

  public BitfinexPublicTrade[] getBitfinexPublicTrades(
      CurrencyPair currencyPair, int limitTrades, long startTimestamp, long endTimestamp, int sort)
      throws IOException {
    try {
      return decorateApiCall(
              () ->
                  bitfinexV2.getPublicTrades(
                      BitfinexAdapters.adaptCurrencyPair(currencyPair),
                      limitTrades,
                      startTimestamp,
                      endTimestamp,
                      sort))
          .withRetry(retry("market-trades"))
          .withRateLimiter(rateLimiter(BITFINEX_RATE_LIMITER))
          .call();
    } catch (HttpStatusIOException e) {
      throw new BitfinexException(e.getHttpBody());
    }
  }

  public BitfinexPublicFundingTrade[] getBitfinexPublicFundingTrades(
      Currency currency, int limitTrades, long startTimestamp, long endTimestamp, int sort)
      throws IOException {
    try {
      return decorateApiCall(
              () ->
                  bitfinexV2.getPublicFundingTrades(
                      "f" + currency.toString(), limitTrades, startTimestamp, endTimestamp, sort))
          .withRetry(retry("market-fundingTrades"))
          .withRateLimiter(rateLimiter(BITFINEX_RATE_LIMITER))
          .call();
    } catch (HttpStatusIOException e) {
      throw new BitfinexException(e.getHttpBody());
    }
  }

  public List<Status> getStatus(List<CurrencyPair> pairs) throws IOException {
    try {
      return decorateApiCall(
              () ->
                  bitfinexV2.getStatus(
                      "deriv", BitfinexAdapters.adaptCurrencyPairsToTickersParam(pairs)))
          .withRetry(retry("market-status"))
          .withRateLimiter(rateLimiter(BITFINEX_RATE_LIMITER))
          .call();
    } catch (HttpStatusIOException e) {
      throw new BitfinexException(e.getHttpBody());
    }
  }

  public List<BitfinexCandle> getFundingHistoricCandles(
      String candlePeriod, String pair, int fundingPeriod, int numOfCandles) throws IOException {
    final String fundingPeriodStr = "p" + fundingPeriod;
    return decorateApiCall(
            () ->
                bitfinexV2.getHistoricFundingCandles(
                    candlePeriod, pair, fundingPeriodStr, numOfCandles))
        .withRetry(retry("market-fundingHistoricCandles"))
        .withRateLimiter(rateLimiter(BITFINEX_RATE_LIMITER))
        .call();
  }

  public List<BitfinexCandle> getHistoricCandles(
      String candlePeriod,
      CurrencyPair currencyPair,
      Integer limit,
      Long startTimestamp,
      Long endTimestamp,
      Integer sort)
      throws IOException {
    return decorateApiCall(
            () ->
                bitfinexV2.getHistoricCandles(
                    candlePeriod,
                    BitfinexAdapters.adaptCurrencyPair(currencyPair),
                    limit,
                    startTimestamp,
                    endTimestamp,
                    sort))
        .withRetry(retry("market-historicCandles"))
        .withRateLimiter(rateLimiter(BITFINEX_RATE_LIMITER))
        .call();
  }

  /**
   * @see https://docs.bitfinex.com/reference#rest-public-stats1 The Stats endpoint provides various
   *     statistics on a specified trading pair or funding currency. Use the available keys to
   *     specify which statistic you wish to retrieve. Please note that the "Side" path param is
   *     only required for the pos.size key.
   * @param key Allowed values: "funding.size", "credits.size", "credits.size.sym", "pos.size",
   *     "vol.1d", "vol.7d", "vol.30d", "vwap"
   * @param size Available values: "30m", "1d", '1m'
   * @param symbol The symbol you want information about. (e.g. tBTCUSD, tETHUSD, fUSD, fBTC)
   * @param side Available values: "long", "short". Only for non-funding queries.
   * @param sort if = 1 it sorts results returned with old > new
   * @param startTimestamp Millisecond start time
   * @param endTimestamp Millisecond end time
   * @param limit Number of records (Max: 10000)
   * @return
   * @throws IOException
   */
  public List<BitfinexStats> getStats(
      String key,
      String size,
      String symbol,
      String side,
      Integer sort,
      Long startTimestamp,
      Long endTimestamp,
      Integer limit)
      throws IOException {
    return decorateApiCall(
            () ->
                bitfinexV2.getStats(
                    key, size, symbol, side, sort, startTimestamp, endTimestamp, limit))
        .withRetry(retry("market-stats"))
        .withRateLimiter(rateLimiter(BITFINEX_RATE_LIMITER))
        .call();
  }

  /**
   * @see https://docs.bitfinex.com/reference#rest-public-book
   * @param symbol The symbol you want information about. (e.g. tBTCUSD, tETHUSD, fUSD, fBTC)
   * @param precision Level of price aggregation (P0, P1, P2, P3, P4, R0)
   * @param len Number of price points ("1", "25", "100")
   * @return list of orders in the book
   * @throws IOException
   */
  public List<BitfinexTradingOrder> tradingBook(String symbol, BookPrecision precision, Integer len)
      throws IOException {
    return decorateApiCall(() -> bitfinexV2.tradingBook(symbol, precision, len))
        .withRetry(retry("market-tradingBook"))
        .withRateLimiter(rateLimiter(BITFINEX_RATE_LIMITER))
        .call();
  }
  /**
   * @see https://docs.bitfinex.com/reference#rest-public-book
   * @param symbol The symbol you want information about. (e.g. tBTCUSD, tETHUSD, fUSD, fBTC)
   * @param len Number of price points ("1", "25", "100")
   * @return list of orders in the book
   * @throws IOException
   */
  public List<BitfinexTradingRawOrder> tradingBookRaw(String symbol, Integer len)
      throws IOException {
    return decorateApiCall(() -> bitfinexV2.tradingBookRaw(symbol, len))
        .withRetry(retry("market-tradingBook"))
        .withRateLimiter(rateLimiter(BITFINEX_RATE_LIMITER))
        .call();
  }

  /**
   * @see https://docs.bitfinex.com/reference#rest-public-book
   * @param symbol The symbol you want information about. (e.g. tBTCUSD, tETHUSD, fUSD, fBTC)
   * @param precision Level of price aggregation (P0, P1, P2, P3, P4, R0)
   * @param len Number of price points ("1", "25", "100")
   * @return list of orders in the book
   * @throws IOException
   */
  public List<BitfinexFundingOrder> fundingBook(String symbol, BookPrecision precision, Integer len)
      throws IOException {
    return decorateApiCall(() -> bitfinexV2.fundingBook(symbol, precision, len))
        .withRetry(retry("market-fundingBook"))
        .withRateLimiter(rateLimiter(BITFINEX_RATE_LIMITER))
        .call();
  }
  /**
   * @see https://docs.bitfinex.com/reference#rest-public-book
   * @param symbol The symbol you want information about. (e.g. tBTCUSD, tETHUSD, fUSD, fBTC)
   * @param len Number of price points ("1", "25", "100")
   * @return list of orders in the book
   * @throws IOException
   */
  public List<BitfinexFundingRawOrder> fundingBookRaw(String symbol, Integer len)
      throws IOException {
    return decorateApiCall(() -> bitfinexV2.fundingBookRaw(symbol, len))
        .withRetry(retry("market-fundingBook"))
        .withRateLimiter(rateLimiter(BITFINEX_RATE_LIMITER))
        .call();
  }
}
