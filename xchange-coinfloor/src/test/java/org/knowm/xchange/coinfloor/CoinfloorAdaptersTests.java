package org.knowm.xchange.coinfloor;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.junit.Test;
import org.knowm.xchange.coinfloor.dto.account.CoinfloorBalance;
import org.knowm.xchange.coinfloor.dto.markedata.CoinfloorOrderBook;
import org.knowm.xchange.coinfloor.dto.markedata.CoinfloorTicker;
import org.knowm.xchange.coinfloor.dto.markedata.CoinfloorTransaction;
import org.knowm.xchange.coinfloor.dto.trade.CoinfloorOrder;
import org.knowm.xchange.coinfloor.dto.trade.CoinfloorUserTransaction;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.marketdata.Trades.TradeSortType;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.dto.trade.UserTrades;

public class CoinfloorAdaptersTests {

  @Test
  public void adaptTickerTest() throws JsonParseException, JsonMappingException, IOException {
    InputStream is =
        getClass()
            .getResourceAsStream("/org/knowm/xchange/coinfloor/dto/marketdata/example-ticker.json");
    ObjectMapper mapper = new ObjectMapper();
    CoinfloorTicker rawTicker = mapper.readValue(is, CoinfloorTicker.class);

    Ticker ticker = CoinfloorAdapters.adaptTicker(rawTicker, CurrencyPair.BTC_GBP);
    assertThat(ticker.getCurrencyPair()).isEqualTo(CurrencyPair.BTC_GBP);
    assertThat(ticker.getLast()).isNull();
    assertThat(ticker.getHigh()).isEqualTo("834.00");
    assertThat(ticker.getLow()).isEqualTo("761.00");
    assertThat(ticker.getVwap()).isEqualTo("800.37");
    assertThat(ticker.getVolume()).isEqualTo("590.1081");
    assertThat(ticker.getBid()).isEqualTo("827.00");
    assertThat(ticker.getAsk()).isEqualTo("832.00");
  }

  @Test
  public void adaptOrderBookTest() throws JsonParseException, JsonMappingException, IOException {
    InputStream is =
        getClass()
            .getResourceAsStream(
                "/org/knowm/xchange/coinfloor/dto/marketdata/example-order-book.json");
    ObjectMapper mapper = new ObjectMapper();
    CoinfloorOrderBook rawOrderBook = mapper.readValue(is, CoinfloorOrderBook.class);

    OrderBook orderBook = CoinfloorAdapters.adaptOrderBook(rawOrderBook, CurrencyPair.BTC_GBP);

    assertThat(orderBook.getBids()).hasSize(68);
    assertThat(orderBook.getAsks()).hasSize(103);

    assertThat(orderBook.getBids().get(1).getCurrencyPair()).isEqualTo(CurrencyPair.BTC_GBP);
    assertThat(orderBook.getBids().get(1).getOriginalAmount()).isEqualTo("5.1322");
    assertThat(orderBook.getAsks().get(0).getLimitPrice()).isEqualTo("801.00");
  }

  @Test
  public void adaptTransactionTest() throws JsonParseException, JsonMappingException, IOException {
    InputStream is =
        getClass()
            .getResourceAsStream(
                "/org/knowm/xchange/coinfloor/dto/marketdata/example-transactions.json");
    ObjectMapper mapper = new ObjectMapper();
    CoinfloorTransaction[] rawTransactions = mapper.readValue(is, CoinfloorTransaction[].class);

    Trades trades = CoinfloorAdapters.adaptTrades(rawTransactions, CurrencyPair.BTC_GBP);

    assertThat(trades.getTrades()).hasSize(58);

    Trade trade = trades.getTrades().get(2);
    assertThat(trade.getCurrencyPair()).isEqualTo(CurrencyPair.BTC_GBP);
    assertThat(trade.getTimestamp().getTime()).isEqualTo(1489938696000L);
    assertThat(trade.getId()).isEqualTo("1489938696270805");
    assertThat(trade.getPrice()).isEqualTo("827.00");
    assertThat(trade.getOriginalAmount()).isEqualTo("0.2028");
  }

  @Test
  public void adaptAccountInfoTest() throws JsonParseException, JsonMappingException, IOException {
    ObjectMapper mapper = new ObjectMapper();

    InputStream btcgbpStream =
        getClass()
            .getResourceAsStream(
                "/org/knowm/xchange/coinfloor/dto/account/example-balance-btcgbp.json");
    CoinfloorBalance btcgbp = mapper.readValue(btcgbpStream, CoinfloorBalance.class);

    InputStream btcusdStream =
        getClass()
            .getResourceAsStream(
                "/org/knowm/xchange/coinfloor/dto/account/example-balance-btcusd.json");
    CoinfloorBalance btcusd = mapper.readValue(btcusdStream, CoinfloorBalance.class);

    Currency[] currencies = {Currency.BTC, Currency.GBP, Currency.USD, Currency.EUR};
    CoinfloorBalance[] rawBalances = {btcgbp, btcusd};
    AccountInfo info =
        CoinfloorAdapters.adaptAccountInfo(Arrays.asList(currencies), Arrays.asList(rawBalances));

    assertThat(info.getWallet().getBalances()).hasSize(3);

    Balance btc = info.getWallet().getBalance(Currency.BTC);
    assertThat(btc.getTotal()).isEqualTo("120.3500");
    assertThat(btc.getFrozen()).isEqualTo("0");
    assertThat(btc.getAvailable()).isEqualTo("120.3500");

    Balance gbp = info.getWallet().getBalance(Currency.GBP);
    assertThat(gbp.getTotal()).isEqualTo("50000.00");
    assertThat(gbp.getFrozen()).isEqualTo("20000.00");
    assertThat(gbp.getAvailable()).isEqualTo("0.00");

    Balance usd = info.getWallet().getBalance(Currency.USD);
    assertThat(usd.getTotal()).isEqualTo("700.00");
    assertThat(usd.getFrozen()).isEqualTo("0");
    assertThat(usd.getAvailable()).isEqualTo("700.00");
  }

  @Test
  public void adaptTradeHistoryTest() throws JsonParseException, JsonMappingException, IOException {
    InputStream is =
        getClass()
            .getResourceAsStream(
                "/org/knowm/xchange/coinfloor/dto/trade/example-user-transactions.json");
    ObjectMapper mapper = new ObjectMapper();
    CoinfloorUserTransaction[] transactions =
        mapper.readValue(is, CoinfloorUserTransaction[].class);

    UserTrades trades = CoinfloorAdapters.adaptTradeHistory(Arrays.asList(transactions));

    assertThat(trades.getTrades()).hasSize(2);
    assertThat(trades.getlastID()).isEqualTo("2489586572518770");
    assertThat(trades.getTradeSortType()).isEqualTo(TradeSortType.SortByID);

    UserTrade trade0 = (UserTrade) trades.getTrades().get(0);
    assertThat(trade0.getTimestamp().getTime()).isEqualTo(1489284172000L);
    assertThat(trade0.getId()).isEqualTo("2489586572518170");
    assertThat(trade0.getOriginalAmount()).isEqualTo("0.1660");
    assertThat(trade0.getPrice()).isEqualTo("1020.00");
    assertThat(trade0.getType()).isEqualTo(OrderType.BID);
    assertThat(trade0.getFeeAmount()).isEqualTo("1.22");
    assertThat(trade0.getFeeCurrency()).isEqualTo(Currency.GBP);
    assertThat(trade0.getOrderId()).isEqualTo("66574450");

    UserTrade trade1 = (UserTrade) trades.getTrades().get(1);
    assertThat(trade1.getTimestamp().getTime()).isEqualTo(1491184972000L);
    assertThat(trade1.getId()).isEqualTo("2489586572518770");
    assertThat(trade1.getOriginalAmount()).isEqualTo("1.8340");
    assertThat(trade1.getPrice()).isEqualTo("1027.00");
    assertThat(trade1.getType()).isEqualTo(OrderType.ASK);
    assertThat(trade1.getFeeAmount()).isEqualTo("0.00");
    assertThat(trade1.getFeeCurrency()).isEqualTo(Currency.GBP);
    assertThat(trade1.getOrderId()).isEqualTo("66564380");
  }

  @Test
  public void adaptOpenOrderTest() throws JsonParseException, JsonMappingException, IOException {
    InputStream is =
        getClass()
            .getResourceAsStream("/org/knowm/xchange/coinfloor/dto/trade/example-open-orders.json");
    ObjectMapper mapper = new ObjectMapper();
    CoinfloorOrder[] transactions = mapper.readValue(is, CoinfloorOrder[].class);

    OpenOrders orders = CoinfloorAdapters.adaptOpenOrders(Arrays.asList(transactions));

    LimitOrder order0 = orders.getOpenOrders().get(0);
    assertThat(order0.getId()).isEqualTo("66688608");
    assertThat(order0.getTimestamp().getTime()).isEqualTo(1490009484000L);
    assertThat(order0.getType()).isEqualTo(OrderType.ASK);
    assertThat(order0.getLimitPrice()).isEqualTo("2301.00");
    assertThat(order0.getOriginalAmount()).isEqualTo("0.0001");

    LimitOrder order1 = orders.getOpenOrders().get(1);
    assertThat(order1.getId()).isEqualTo("66688691");
    assertThat(order1.getTimestamp().getTime()).isEqualTo(1491392044000L);
    assertThat(order1.getType()).isEqualTo(OrderType.BID);
    assertThat(order1.getLimitPrice()).isEqualTo("2303.00");
    assertThat(order1.getOriginalAmount()).isEqualTo("1.0001");
  }
}
