package org.knowm.xchange.binance;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binance.dto.meta.BinanceCurrencyPairMetaData;
import org.knowm.xchange.binance.dto.meta.exchangeinfo.BinanceExchangeInfo;
import org.knowm.xchange.binance.dto.meta.exchangeinfo.Filter;
import org.knowm.xchange.binance.dto.meta.exchangeinfo.Symbol;
import org.knowm.xchange.binance.service.BinanceAccountService;
import org.knowm.xchange.binance.service.BinanceMarketDataService;
import org.knowm.xchange.binance.service.BinanceTradeService;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.meta.CurrencyMetaData;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.utils.AuthUtils;
import org.knowm.xchange.utils.nonce.AtomicLongCurrentTimeIncrementalNonceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.mazi.rescu.RestProxyFactory;
import si.mazi.rescu.SynchronizedValueFactory;

public class BinanceExchange extends BaseExchange {

  private static final Logger LOG = LoggerFactory.getLogger(BinanceExchange.class);

  private static final int DEFAULT_PRECISION = 8;

  private SynchronizedValueFactory<Long> nonceFactory =
      new AtomicLongCurrentTimeIncrementalNonceFactory();
  private BinanceExchangeInfo exchangeInfo;
  private Long deltaServerTimeExpire;
  private Long deltaServerTime;

  @Override
  protected void initServices() {
    this.marketDataService = new BinanceMarketDataService(this);
    this.tradeService = new BinanceTradeService(this);
    this.accountService = new BinanceAccountService(this);
  }

  @Override
  public SynchronizedValueFactory<Long> getNonceFactory() {

    return nonceFactory;
  }

  @Override
  public ExchangeSpecification getDefaultExchangeSpecification() {
    ExchangeSpecification spec = new ExchangeSpecification(this.getClass().getCanonicalName());
    spec.setSslUri("https://api.binance.com");
    spec.setHost("www.binance.com");
    spec.setPort(80);
    spec.setExchangeName("Binance");
    spec.setExchangeDescription("Binance Exchange.");
    AuthUtils.setApiAndSecretKey(spec, "binance");
    return spec;
  }

  public BinanceExchangeInfo getExchangeInfo() {
    return exchangeInfo;
  }

  @Override
  public void remoteInit() {
    try {
      // populate currency pair keys only, exchange does not provide any other metadata for download
      Map<CurrencyPair, CurrencyPairMetaData> currencyPairs = exchangeMetaData.getCurrencyPairs();
      Map<Currency, CurrencyMetaData> currencies = exchangeMetaData.getCurrencies();

      BinanceMarketDataService marketDataService =
          (BinanceMarketDataService) this.marketDataService;
      exchangeInfo = marketDataService.getExchangeInfo();

      for (Symbol symbol : exchangeInfo.getSymbols()) {

        CurrencyPair pair = new CurrencyPair(symbol.getBaseAsset(), symbol.getQuoteAsset());
        // defaults
        BigDecimal tradingFee = BigDecimal.ZERO;
        BigDecimal minAmount = BigDecimal.ZERO;
        BigDecimal maxAmount = BigDecimal.ZERO;
        Integer priceScale = DEFAULT_PRECISION;
        BigDecimal minNotional = BigDecimal.ZERO;

        CurrencyPairMetaData pairMetaData = currencyPairs.get(pair);
        if (pairMetaData != null) { // use old values as defaults where available.
          tradingFee = pairMetaData.getTradingFee();
          minAmount = pairMetaData.getMinimumAmount();
          maxAmount = pairMetaData.getMaximumAmount();
          priceScale = pairMetaData.getPriceScale();
          if (pairMetaData instanceof BinanceCurrencyPairMetaData) {
            minNotional = ((BinanceCurrencyPairMetaData) pairMetaData).getMinNotional();
          }
        }

        for (Filter filter : symbol.getFilters()) { // replace with the new values where available.
          switch (filter.getFilterType()) {
            case "PRICE_FILTER":
              priceScale = new BigDecimal(filter.getTickSize()).stripTrailingZeros().scale();
              break;
            case "LOT_SIZE":
              minAmount = new BigDecimal(filter.getMinQty());
              maxAmount = new BigDecimal(filter.getMaxQty());
              break;
            case "MIN_NOTIONAL":
              minNotional = new BigDecimal(filter.getMinNotional());
              break;
          }
        }
        pairMetaData =
            new BinanceCurrencyPairMetaData(
                tradingFee, minAmount, maxAmount, priceScale, minNotional);

        currencyPairs.put(pair, pairMetaData);

        CurrencyMetaData baseMetaData = currencies.get(pair.base);
        if (baseMetaData == null) {
          Integer basePrecision = Integer.parseInt(symbol.getBaseAssetPrecision());
          currencies.put(pair.base, new CurrencyMetaData(basePrecision, BigDecimal.ZERO));
        }
        CurrencyMetaData counterMetaData = currencies.get(pair.base);
        if (counterMetaData == null) {
          Integer counterPrecision = Integer.parseInt(symbol.getQuotePrecision());
          currencies.put(pair.counter, new CurrencyMetaData(counterPrecision, BigDecimal.ZERO));
        }
      }
    } catch (Exception e) {
      logger.warn("An exception occurred while loading the metadata", e);
    }
  }

  private int numberOfDecimals(String value) {
    try {
      double d = Double.parseDouble(value);
      String s = new DecimalFormat("#.############").format(d);
      return s.split("\\.")[1].length();
    } catch (ArrayIndexOutOfBoundsException e) {
      return DEFAULT_PRECISION;
    }
  }

  public void clearDeltaServerTime() {
    deltaServerTime = null;
  }

  public long deltaServerTime() throws IOException {

    if (deltaServerTime == null || deltaServerTimeExpire <= System.currentTimeMillis()) {

      // Do a little warm up
      Binance binance =
          RestProxyFactory.createProxy(Binance.class, getExchangeSpecification().getSslUri());
      Date serverTime = new Date(binance.time().getServerTime().getTime());
      serverTime = new Date(binance.time().getServerTime().getTime());

      // Assume that we are closer to the server time when we get the repose
      Date systemTime = new Date(System.currentTimeMillis());

      // Expire every 10min
      deltaServerTimeExpire = systemTime.getTime() + TimeUnit.MINUTES.toMillis(10);
      deltaServerTime = serverTime.getTime() - systemTime.getTime();

      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
      LOG.trace(
          "deltaServerTime: {} - {} => {}",
          df.format(serverTime),
          df.format(systemTime),
          deltaServerTime);
    }

    return deltaServerTime;
  }
}
