package org.knowm.xchange.bitcoinde;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import org.junit.Test;
import org.knowm.xchange.bitcoinde.dto.marketdata.BitcoindeOrderbookWrapper;
import org.knowm.xchange.bitcoinde.dto.marketdata.BitcoindeTradesWrapper;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Trades;

/** @author matthewdowney */
public class BitcoindeAdapterTest {

  @Test
  public void testOrderBookAdapter() throws IOException {

    // Read in the JSON from the example resources
    InputStream is =
        BitcoindeAdapterTest.class.getResourceAsStream(
            "/org/knowm/xchange/bitcoinde/dto/orderbook.json");

    // Use Jackson to parse it
    ObjectMapper mapper = new ObjectMapper();
    BitcoindeOrderbookWrapper bitcoindeOrderBook =
        mapper.readValue(is, BitcoindeOrderbookWrapper.class);

    // Create a generic OrderBook object from a Bitcoinde specific OrderBook
    OrderBook orderBook =
        BitcoindeAdapters.adaptOrderBook(bitcoindeOrderBook, CurrencyPair.BTC_EUR);

    // verify all fields are filled correctly
    assertThat(orderBook.getBids().get(0).getLimitPrice().toString()).isEqualTo("2406.11");
    assertThat(orderBook.getBids().get(0).getType()).isEqualTo(OrderType.BID);
    assertThat(orderBook.getBids().get(0).getOriginalAmount()).isEqualTo(new BigDecimal("1.745"));
    assertThat(orderBook.getBids().get(0).getInstrument()).isEqualTo(CurrencyPair.BTC_EUR);
  }

  @Test
  public void testTradesAdapter() throws IOException {

    // Read in the JSON from the example resources
    InputStream is =
        BitcoindeAdapterTest.class.getResourceAsStream(
            "/org/knowm/xchange/bitcoinde/dto/trades.json");

    // Use Jackson to parse it
    ObjectMapper mapper = new ObjectMapper();
    BitcoindeTradesWrapper bitcoindeTradesWrapper =
        mapper.readValue(is, BitcoindeTradesWrapper.class);

    // Use our adapter to get a generic Trades object from a
    // BitcoindeTrade[] object
    Trades trades = BitcoindeAdapters.adaptTrades(bitcoindeTradesWrapper, CurrencyPair.BTC_EUR);

    // Make sure the adapter got all the data
    assertThat(trades.getTrades().size()).isEqualTo(92);
    assertThat(trades.getlastID()).isEqualTo("2844384");

    // Verify that all fields are filled
    assertThat(trades.getTrades().get(0).getId()).isEqualTo("2844111");
    assertThat(trades.getTrades().get(0).getPrice()).isEqualTo(new BigDecimal("2395"));
    assertThat(trades.getTrades().get(0).getOriginalAmount())
        .isEqualTo(new BigDecimal("0.08064516"));
    assertThat(trades.getTrades().get(0).getCurrencyPair()).isEqualTo(CurrencyPair.BTC_EUR);

    // Verify that the date is correct
    Date date = new Date();
    date.setTime(1500717160L * 1000); // Create the expected date for trade
    // 0
    assertThat(trades.getTrades().get(0).getTimestamp()).isEqualTo(date); // Make
    // sure
    // the
    // dates
    // match
  }
}
