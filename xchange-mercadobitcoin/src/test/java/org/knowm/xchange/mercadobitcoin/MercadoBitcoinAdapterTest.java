package org.knowm.xchange.mercadobitcoin;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.mercadobitcoin.dto.MercadoBitcoinBaseTradeApiResult;
import org.knowm.xchange.mercadobitcoin.dto.account.MercadoBitcoinAccountInfo;
import org.knowm.xchange.mercadobitcoin.dto.marketdata.MercadoBitcoinOrderBook;
import org.knowm.xchange.mercadobitcoin.dto.marketdata.MercadoBitcoinTicker;
import org.knowm.xchange.mercadobitcoin.dto.marketdata.MercadoBitcoinTransaction;
import org.knowm.xchange.mercadobitcoin.dto.trade.MercadoBitcoinUserOrders;

/**
 * Tests the MercadoBitcoinAdapter class
 *
 * @author Felipe Micaroni Lalli
 */
public class MercadoBitcoinAdapterTest {

  @Test
  public void testOrderBookAdapter() throws IOException {

    // Read in the JSON from the example resources
    InputStream is =
        MercadoBitcoinAdapterTest.class.getResourceAsStream(
            "/org/knowm/xchange/mercadobitcoin/dto/marketdata/example-full-depth-data.json");

    // Use Jackson to parse it
    ObjectMapper mapper = new ObjectMapper();
    MercadoBitcoinOrderBook mercadoBitcoinOrderBook =
        mapper.readValue(is, MercadoBitcoinOrderBook.class);

    OrderBook orderBook =
        MercadoBitcoinAdapters.adaptOrderBook(mercadoBitcoinOrderBook, CurrencyPair.BTC_BRL);
    assertThat(orderBook.getBids().size()).isEqualTo(127);

    // verify all fields filled
    assertThat(orderBook.getBids().get(0).getLimitPrice().toString()).isEqualTo("1004.16826");
    assertThat(orderBook.getBids().get(0).getType()).isEqualTo(OrderType.BID);
    assertThat(orderBook.getBids().get(0).getOriginalAmount()).isEqualTo(new BigDecimal("0.16614"));
    assertThat(orderBook.getBids().get(0).getCurrencyPair()).isEqualTo(CurrencyPair.BTC_BRL);
  }

  @Test
  public void testTradesAdapter() throws IOException {

    // Read in the JSON from the example resources
    InputStream is =
        MercadoBitcoinAdapterTest.class.getResourceAsStream(
            "/org/knowm/xchange/mercadobitcoin/dto/marketdata/example-trades-data.json");

    // Use Jackson to parse it
    ObjectMapper mapper = new ObjectMapper();
    MercadoBitcoinTransaction[] transactions =
        mapper.readValue(is, MercadoBitcoinTransaction[].class);

    Trades trades = MercadoBitcoinAdapters.adaptTrades(transactions, CurrencyPair.BTC_BRL);
    assertThat(trades.getTrades().size()).isEqualTo(1000);
    assertThat(trades.getlastID()).isEqualTo("99518");
    // verify all fields filled
    assertThat(trades.getTrades().get(0).getId()).isEqualTo("98519");
    assertThat(trades.getTrades().get(0).getPrice().toString()).isEqualTo("1015");
    assertThat(trades.getTrades().get(0).getType() == OrderType.BID);
    assertThat(trades.getTrades().get(0).getOriginalAmount()).isEqualTo(new BigDecimal("1"));
    assertThat(trades.getTrades().get(0).getCurrencyPair()).isEqualTo(CurrencyPair.BTC_BRL);
  }

  @Test
  public void testTickerAdapter() throws IOException {

    // Read in the JSON from the example resources
    InputStream is =
        MercadoBitcoinAdapterTest.class.getResourceAsStream(
            "/org/knowm/xchange/mercadobitcoin/dto/marketdata/example-ticker-data.json");

    // Use Jackson to parse it
    ObjectMapper mapper = new ObjectMapper();
    MercadoBitcoinTicker mercadoBitcoinTicker = mapper.readValue(is, MercadoBitcoinTicker.class);

    Ticker ticker = MercadoBitcoinAdapters.adaptTicker(mercadoBitcoinTicker, CurrencyPair.BTC_BRL);

    assertThat(ticker.getLast().toString()).isEqualTo("1019.99999");
    assertThat(ticker.getBid().toString()).isEqualTo("1019.99999");
    assertThat(ticker.getAsk().toString()).isEqualTo("1020");
    assertThat(ticker.getVolume()).isEqualTo(new BigDecimal("6.90157391"));
    assertThat(ticker.getTimestamp()).isEqualTo(new Date(1417226432L * 1000L));
  }

  @Test
  public void testAccountInfoAdapter() throws IOException {

    // Read in the JSON from the example resources
    InputStream is =
        MercadoBitcoinAdapterTest.class.getResourceAsStream(
            "/org/knowm/xchange/mercadobitcoin/dto/account/example-accountinfo-data.json");

    // Use Jackson to parse it
    ObjectMapper mapper = new ObjectMapper();
    MercadoBitcoinBaseTradeApiResult<MercadoBitcoinAccountInfo> mercadoBitcoinAccountInfo =
        mapper.readValue(
            is,
            new TypeReference<MercadoBitcoinBaseTradeApiResult<MercadoBitcoinAccountInfo>>() {});

    AccountInfo accountInfo =
        MercadoBitcoinAdapters.adaptAccountInfo(mercadoBitcoinAccountInfo, "Nina Tufão & Bit");
    assertThat(accountInfo.getUsername()).isEqualTo("Nina Tufão & Bit");
    assertThat(accountInfo.getWallet().getBalances()).hasSize(3);
    assertThat(accountInfo.getWallet().getBalance(Currency.BRL).getCurrency())
        .isEqualTo(Currency.BRL);
    assertThat(accountInfo.getWallet().getBalance(Currency.BRL).getTotal())
        .isEqualTo(new BigDecimal("248.29516"));
    assertThat(accountInfo.getWallet().getBalance(Currency.BTC).getCurrency())
        .isEqualTo(Currency.BTC);
    assertThat(accountInfo.getWallet().getBalance(Currency.BTC).getTotal())
        .isEqualTo(new BigDecimal("0.25000000"));
    assertThat(accountInfo.getWallet().getBalance(Currency.LTC).getCurrency())
        .isEqualTo(Currency.LTC);
    assertThat(accountInfo.getWallet().getBalance(Currency.LTC).getTotal())
        .isEqualTo(new BigDecimal("0.00000000"));
  }

  @Test
  public void testOrdersAdapter() throws IOException {

    // Read in the JSON from the example resources
    InputStream is =
        MercadoBitcoinAdapterTest.class.getResourceAsStream(
            "/org/knowm/xchange/mercadobitcoin/dto/trade/example-userorders.json");

    // Use Jackson to parse it
    ObjectMapper mapper = new ObjectMapper();
    MercadoBitcoinBaseTradeApiResult<MercadoBitcoinUserOrders> apiResult =
        mapper.readValue(
            is, new TypeReference<MercadoBitcoinBaseTradeApiResult<MercadoBitcoinUserOrders>>() {});

    List<LimitOrder> orders =
        MercadoBitcoinAdapters.adaptOrders(new CurrencyPair(Currency.LTC, Currency.BRL), apiResult);

    Map<String, LimitOrder> orderById = new HashMap<>();

    for (LimitOrder order : orders) {
      orderById.put(order.getId(), order);
    }

    assertThat(orderById.get("1212").getType()).isEqualTo(OrderType.ASK);
    assertThat(orderById.get("1212").getTimestamp()).isEqualTo(new Date(1378929161000L));
    assertThat(orderById.get("1212").getLimitPrice()).isEqualTo(new BigDecimal("6.00000"));
    assertThat(orderById.get("1212").getOriginalAmount()).isEqualTo(new BigDecimal("165.47309607"));
    assertThat(orderById.get("1212").getCurrencyPair())
        .isEqualTo(new CurrencyPair(Currency.LTC, Currency.BRL));
  }
}
