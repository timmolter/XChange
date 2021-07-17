package org.knowm.xchange.deribit.v2;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import org.junit.Test;
import org.knowm.xchange.deribit.v2.dto.marketdata.DeribitOrderBook;
import org.knowm.xchange.deribit.v2.dto.marketdata.DeribitTicker;
import org.knowm.xchange.deribit.v2.dto.marketdata.DeribitTrade;
import org.knowm.xchange.derivative.FuturesContract;
import org.knowm.xchange.derivative.OptionsContract;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.instrument.Instrument;

public class DeribitAdaptersTest {
  
  @Test
  public void adaptInstrument() {
    Instrument instrument = DeribitAdapters.adaptInstrument("BTC-USDT-PERPETUAL-F");
    assertThat(instrument).isExactlyInstanceOf(FuturesContract.class);
    assertThat(instrument).isEqualTo(new FuturesContract("BTC-USDT-PERPETUAL"));

    instrument = DeribitAdapters.adaptInstrument("ETH-PERPETUAL");
    assertThat(instrument).isExactlyInstanceOf(FuturesContract.class);
    assertThat(instrument).isEqualTo(new FuturesContract("ETH-USD-perpetual"));

    instrument = DeribitAdapters.adaptInstrument("ETH-31DEC21");
    assertThat(instrument).isExactlyInstanceOf(FuturesContract.class);
    assertThat(instrument).isEqualTo(new FuturesContract("ETH-USD-211231"));

    instrument = DeribitAdapters.adaptInstrument("ETH-24SEP21-2040-P");
    assertThat(instrument).isExactlyInstanceOf(OptionsContract.class);
    assertThat(instrument).isEqualTo(new OptionsContract("ETH-USD-210924-2040-P"));

    instrument = DeribitAdapters.adaptInstrument("BTC-PERPETUAL");
    assertThat(instrument).isExactlyInstanceOf(FuturesContract.class);
    assertThat(instrument).isEqualTo(new FuturesContract("BTC-USD-perpetual"));

    instrument = DeribitAdapters.adaptInstrument("BTC-25MAR22");
    assertThat(instrument).isExactlyInstanceOf(FuturesContract.class);
    assertThat(instrument).isEqualTo(new FuturesContract("BTC-USD-220325"));

    instrument = DeribitAdapters.adaptInstrument("BTC-24SEP21-7000-P");
    assertThat(instrument).isExactlyInstanceOf(OptionsContract.class);
    assertThat(instrument).isEqualTo(new OptionsContract("BTC-USD-210924-7000-P"));
  }

  @Test
  public void adaptTicker() throws IOException {
    // given
    InputStream is =
        DeribitTrade.class.getResourceAsStream(
            "/org/knowm/xchange/deribit/v2/dto/marketdata/example-ticker.json");
    ObjectMapper mapper = new ObjectMapper();
    DeribitTicker deribitTicker = mapper.readValue(is, DeribitTicker.class);

    // when
    Ticker ticker = DeribitAdapters.adaptTicker(deribitTicker);

    // then
    assertThat(ticker).isNotNull();
    assertThat(ticker.getInstrument()).isEqualTo(new OptionsContract("BTC-USD-190503-5000-P"));
    assertThat(ticker.getOpen()).isEqualTo(new BigDecimal("0.5"));
    assertThat(ticker.getLast()).isEqualTo(new BigDecimal("0.0075"));
    assertThat(ticker.getBid()).isEqualTo(new BigDecimal("0.01"));
    assertThat(ticker.getAsk()).isEqualTo(new BigDecimal("0.0135"));
    assertThat(ticker.getHigh()).isEqualTo(new BigDecimal("0.0625"));
    assertThat(ticker.getLow()).isEqualTo(new BigDecimal("0.0005"));
    assertThat(ticker.getVolume()).isEqualTo(new BigDecimal("0.5"));
    assertThat(ticker.getBidSize()).isEqualTo(new BigDecimal("5"));
    assertThat(ticker.getAskSize()).isEqualTo(new BigDecimal("5"));
    assertThat(ticker.getTimestamp().getTime()).isEqualTo(1556125162701L);
  }

  @Test
  public void adaptOrderBook() throws IOException {
    // given
    InputStream is =
        DeribitTrade.class.getResourceAsStream(
            "/org/knowm/xchange/deribit/v2/dto/marketdata/example-orderbook.json");
    ObjectMapper mapper = new ObjectMapper();
    DeribitOrderBook deribitOrderbook = mapper.readValue(is, DeribitOrderBook.class);

    // when
    OrderBook orderBook = DeribitAdapters.adaptOrderBook(deribitOrderbook);

    // then
    assertThat(orderBook).isNotNull();
    assertThat(orderBook.getTimeStamp().getTime()).isEqualTo(1550757626706L);
    assertThat(orderBook.getBids()).isNotEmpty();
    assertThat(orderBook.getBids().get(0).getType()).isEqualTo(Order.OrderType.BID);
    assertThat(orderBook.getBids().get(0).getLimitPrice()).isEqualTo(new BigDecimal("3955.75"));
    assertThat(orderBook.getBids().get(0).getOriginalAmount()).isEqualTo(new BigDecimal("30.0"));
    assertThat(orderBook.getBids().get(0).getInstrument())
            .isEqualTo(new FuturesContract("BTC-USD-PERPETUAL"));
    assertThat(orderBook.getBids().get(1).getType()).isEqualTo(Order.OrderType.BID);
    assertThat(orderBook.getBids().get(1).getLimitPrice()).isEqualTo(new BigDecimal("3940.75"));
    assertThat(orderBook.getBids().get(1).getOriginalAmount())
        .isEqualTo(new BigDecimal("102020.0"));
    assertThat(orderBook.getBids().get(1).getInstrument())
            .isEqualTo(new FuturesContract("BTC-USD-PERPETUAL"));
    assertThat(orderBook.getBids().get(2).getType()).isEqualTo(Order.OrderType.BID);
    assertThat(orderBook.getBids().get(2).getLimitPrice()).isEqualTo(new BigDecimal("3423.0"));
    assertThat(orderBook.getBids().get(2).getOriginalAmount()).isEqualTo(new BigDecimal("42840.0"));
    assertThat(orderBook.getBids().get(2).getInstrument())
            .isEqualTo(new FuturesContract("BTC-USD-PERPETUAL"));
    assertThat(orderBook.getAsks()).isEmpty();
  }

  @Test
  public void adaptTrade() throws IOException {
    // given
    InputStream is =
        DeribitTrade.class.getResourceAsStream(
            "/org/knowm/xchange/deribit/v2/dto/marketdata/example-trade.json");
    ObjectMapper mapper = new ObjectMapper();
    DeribitTrade deribitTrade = mapper.readValue(is, DeribitTrade.class);

    // when
    Trade trade = DeribitAdapters.adaptTrade(deribitTrade, new FuturesContract("BTC-USD-PERPETUAL"));

    // then
    assertThat(trade).isNotNull();
    assertThat(trade.getType()).isEqualTo(Order.OrderType.ASK);
    assertThat(trade.getOriginalAmount()).isEqualTo(new BigDecimal("10"));
    assertThat(trade.getPrice()).isEqualTo(new BigDecimal("3610"));

    assertThat(trade.getTimestamp().getTime()).isEqualTo(1550050591859L);
    assertThat(trade.getId()).isEqualTo("48470");
  }
}
