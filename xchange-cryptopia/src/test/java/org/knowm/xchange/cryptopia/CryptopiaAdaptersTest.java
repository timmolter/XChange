package org.knowm.xchange.cryptopia;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import org.junit.Test;
import org.knowm.xchange.cryptopia.dto.CryptopiaBaseResponse;
import org.knowm.xchange.cryptopia.dto.CryptopiaBaseResponseCryptopiaCurrency;
import org.knowm.xchange.cryptopia.dto.CryptopiaBaseResponseCryptopiaMarketHistory;
import org.knowm.xchange.cryptopia.dto.CryptopiaBaseResponseCryptopiaOrderBook;
import org.knowm.xchange.cryptopia.dto.CryptopiaBaseResponseCryptopiaTicker;
import org.knowm.xchange.cryptopia.dto.CryptopiaBaseResponseCryptopiaTradePair;
import org.knowm.xchange.cryptopia.dto.marketdata.CryptopiaCurrency;
import org.knowm.xchange.cryptopia.dto.marketdata.CryptopiaMarketHistory;
import org.knowm.xchange.cryptopia.dto.marketdata.CryptopiaOrderBook;
import org.knowm.xchange.cryptopia.dto.marketdata.CryptopiaTicker;
import org.knowm.xchange.cryptopia.dto.marketdata.CryptopiaTradePair;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.meta.ExchangeMetaData;

/** Tests the CryptopiaAdapters class */
public class CryptopiaAdaptersTest {

  @Test
  public void testOrderBookAdapter() throws IOException {
    // Read in the JSON from the example resources
    InputStream is =
        CryptopiaAdaptersTest.class.getResourceAsStream(
            "/org/knowm/xchange/cryptopia/dto/marketdata/example-full-depth-data.json");

    // Use Jackson to parse it
    ObjectMapper mapper = new ObjectMapper();
    CryptopiaBaseResponse<CryptopiaOrderBook> cryptopiaOrderBook =
        mapper.readValue(is, CryptopiaBaseResponseCryptopiaOrderBook.class);
    is.close();

    OrderBook orderBook =
        CryptopiaAdapters.adaptOrderBook(cryptopiaOrderBook.getData(), CurrencyPair.ETH_BTC);
    assertThat(orderBook).isNotNull();
    assertThat(orderBook.getBids().size()).isEqualTo(93);

    // Verify all fields filled
    assertThat(orderBook.getBids().get(0).getLimitPrice().toString()).isEqualTo("0.07849890");
    assertThat(orderBook.getBids().get(0).getType()).isEqualTo(Order.OrderType.BID);
    assertThat(orderBook.getBids().get(0).getOriginalAmount())
        .isEqualTo(new BigDecimal("1.27390320"));
    assertThat(orderBook.getBids().get(0).getCurrencyPair()).isEqualTo(CurrencyPair.ETH_BTC);
  }

  @Test
  public void testTickerAdapter() throws IOException {
    // Read in the JSON from the example resources
    InputStream is =
        CryptopiaAdaptersTest.class.getResourceAsStream(
            "/org/knowm/xchange/cryptopia/dto/marketdata/example-ticker-data.json");

    // Use Jackson to parse it
    ObjectMapper mapper = new ObjectMapper();
    CryptopiaBaseResponse<CryptopiaTicker> cryptopiaTicker =
        mapper.readValue(is, CryptopiaBaseResponseCryptopiaTicker.class);
    is.close();

    Ticker ticker = CryptopiaAdapters.adaptTicker(cryptopiaTicker.getData(), CurrencyPair.ETH_BTC);
    assertThat(ticker).isNotNull();

    assertThat(ticker.getLast().toString()).isEqualTo("0.07963205");
    assertThat(ticker.getBid().toString()).isEqualTo("0.07963206");
    assertThat(ticker.getAsk().toString()).isEqualTo("0.08003570");
    assertThat(ticker.getVolume()).isEqualTo(new BigDecimal("361.93509400"));
  }

  @Test
  public void testExchangeMetaDataAdapter() throws IOException {
    // Read in the JSON from the example resources
    InputStream is =
        CryptopiaAdaptersTest.class.getResourceAsStream(
            "/org/knowm/xchange/cryptopia/dto/marketdata/example-currencies.json");

    // Use Jackson to parse it
    ObjectMapper mapper = new ObjectMapper();
    CryptopiaBaseResponse<List<CryptopiaCurrency>> cryptopiaCurrencies =
        mapper.readValue(is, CryptopiaBaseResponseCryptopiaCurrency.class);
    is.close();
    assertThat(cryptopiaCurrencies).isNotNull();

    is =
        CryptopiaAdaptersTest.class.getResourceAsStream(
            "/org/knowm/xchange/cryptopia/dto/marketdata/example-trade-pairs.json");
    CryptopiaBaseResponse<List<CryptopiaTradePair>> cryptopiaTradePairs =
        mapper.readValue(is, CryptopiaBaseResponseCryptopiaTradePair.class);
    is.close();
    assertThat(cryptopiaTradePairs).isNotNull();

    ExchangeMetaData exchangeMetaData =
        CryptopiaAdapters.adaptToExchangeMetaData(
            cryptopiaCurrencies.getData(), cryptopiaTradePairs.getData());
    assertThat(exchangeMetaData).isNotNull();

    assertThat(exchangeMetaData.getCurrencies()).containsKey(Currency.ETH);
    assertThat(exchangeMetaData.getCurrencies().get(Currency.ETH).getScale()).isEqualTo(8);
    assertThat(exchangeMetaData.getCurrencyPairs()).containsKey(CurrencyPair.ETH_BTC);
    assertThat(
            exchangeMetaData
                .getCurrencyPairs()
                .get(CurrencyPair.ETH_BTC)
                .getTradingFee()
                .toString())
        .isEqualTo("0.20000000");
  }

  @Test
  public void testTradesAdapter() throws IOException {
    // Read in the JSON from the example resources
    InputStream is =
        CryptopiaAdaptersTest.class.getResourceAsStream(
            "/org/knowm/xchange/cryptopia/dto/marketdata/example-trades.json");

    // Use Jackson to parse it
    ObjectMapper mapper = new ObjectMapper();
    CryptopiaBaseResponse<List<CryptopiaMarketHistory>> cryptopiaMarketHistory =
        mapper.readValue(is, CryptopiaBaseResponseCryptopiaMarketHistory.class);
    is.close();
    assertThat(cryptopiaMarketHistory).isNotNull();

    Trades trades = CryptopiaAdapters.adaptTrades(cryptopiaMarketHistory.getData());
    assertThat(trades).isNotNull();

    assertThat(trades.getlastID()).isEqualTo("1501995183");
    assertThat(trades.getTradeSortType()).isEqualTo(Trades.TradeSortType.SortByTimestamp);
    assertThat(trades.getTrades().size()).isGreaterThan(1);
  }
}
