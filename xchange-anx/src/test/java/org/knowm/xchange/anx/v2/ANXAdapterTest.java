package org.knowm.xchange.anx.v2;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.anx.v2.dto.account.ANXAccountInfo;
import org.knowm.xchange.anx.v2.dto.marketdata.ANXDepth;
import org.knowm.xchange.anx.v2.dto.marketdata.ANXTicker;
import org.knowm.xchange.anx.v2.dto.marketdata.ANXTrade;
import org.knowm.xchange.anx.v2.dto.marketdata.ANXTradesWrapper;
import org.knowm.xchange.anx.v2.dto.marketdata.TickerJSONTest;
import org.knowm.xchange.anx.v2.dto.meta.ANXMetaData;
import org.knowm.xchange.anx.v2.dto.trade.ANXOpenOrder;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.trade.LimitOrder;

/** Tests the ANXAdapter class */
public class ANXAdapterTest {

  static ANXMetaData metaData;

  @BeforeClass
  public static void initMetaData() {
    Exchange exchange = ExchangeFactory.INSTANCE.createExchange(ANXExchange.class.getName());
    metaData = ((ANXExchange) exchange).getANXMetaData();
  }

  @Test
  public void testAccountInfoAdapter() throws IOException {

    // Read in the JSON from the example resources
    InputStream is =
        ANXAdapterTest.class.getResourceAsStream("/v2/account/example-accountinfo-data.json");

    // Use Jackson to parse it
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    ANXAccountInfo anxAccountInfo = mapper.readValue(is, ANXAccountInfo.class);

    AccountInfo accountInfo = ANXAdapters.adaptAccountInfo(anxAccountInfo);
    assertThat(accountInfo.getUsername()).isEqualTo("test@anxpro.com");

    assertThat(accountInfo.getWallet().getBalance(Currency.DOGE).getTotal())
        .isEqualTo(new BigDecimal("9999781.09457936"));
    assertThat(accountInfo.getWallet().getBalance(Currency.DOGE).getAvailable())
        .isEqualTo(new BigDecimal("9914833.52608521"));
    assertThat(accountInfo.getWallet().getBalance(Currency.DOGE).getFrozen())
        .isEqualTo(new BigDecimal("84947.56849415"));
  }

  @Test
  public void testOrderAdapterWithOpenOrders() throws IOException {

    // Read in the JSON from the example resources
    InputStream is =
        ANXAdapterTest.class.getResourceAsStream("/v2/trade/example-openorders-data.json");

    // Use Jackson to parse it
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    ANXOpenOrder[] anxOpenOrders = mapper.readValue(is, ANXOpenOrder[].class);

    List<LimitOrder> openorders = ANXAdapters.adaptOrders(anxOpenOrders);
    // assertThat(openorders.size()).isEqualTo(38);
    Assert.assertEquals(2, openorders.size());

    // verify all fields filled
    System.out.println(openorders.get(0).getLimitPrice().toString());
    Assert.assertEquals(new BigDecimal("412.34567"), openorders.get(0).getLimitPrice());
    Assert.assertEquals(OrderType.ASK, openorders.get(0).getType());
    Assert.assertEquals(new BigDecimal("412.34567"), openorders.get(0).getLimitPrice());
    Assert.assertEquals(new BigDecimal("10.00000000"), openorders.get(0).getOriginalAmount());

    Assert.assertEquals("BTC", openorders.get(0).getCurrencyPair().base.getCurrencyCode());
    Assert.assertEquals("HKD", openorders.get(0).getCurrencyPair().counter.getCurrencyCode());

    Assert.assertEquals(new Date(1393411075000L), openorders.get(0).getTimestamp());
  }

  @Test
  public void testOrderAdapterWithDepth() throws IOException {

    // Read in the JSON from the example resources
    InputStream is =
        ANXAdapterTest.class.getResourceAsStream("/v2/marketdata/example-fulldepth-data.json");

    // Use Jackson to parse it
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    ANXDepth anxDepth = mapper.readValue(is, ANXDepth.class);

    List<LimitOrder> asks =
        ANXAdapters.adaptOrders(anxDepth.getAsks(), "BTC", "USD", "ask", "id_567");
    Assert.assertEquals(3, asks.size());

    // Verify all fields filled
    assertThat(asks.get(0).getType()).isEqualTo(OrderType.ASK);

    Assert.assertEquals(new BigDecimal("16.00000000"), asks.get(0).getOriginalAmount());
    Assert.assertEquals(new BigDecimal("3260.40000"), asks.get(0).getLimitPrice());

    Assert.assertEquals("BTC", asks.get(0).getCurrencyPair().base.getCurrencyCode());
    Assert.assertEquals("USD", asks.get(0).getCurrencyPair().counter.getCurrencyCode());
  }

  @Test
  public void testTradeAdapter() throws IOException {

    // Read in the JSON from the example resources
    InputStream is =
        TickerJSONTest.class.getResourceAsStream("/v2/marketdata/example-trades-data.json");

    // Use Jackson to parse it
    ObjectMapper mapper = new ObjectMapper();

    ANXTradesWrapper anxTradesWrapper = mapper.readValue(is, ANXTradesWrapper.class);
    List<ANXTrade> anxTrades = anxTradesWrapper.getANXTrades();

    Trades trades = ANXAdapters.adaptTrades(anxTrades);
    assertThat(trades.getlastID()).isEqualTo("1402189349725");

    List<Trade> tradeList = trades.getTrades();
    assertThat(tradeList.size()).isEqualTo(2);

    Trade trade = tradeList.get(0);
    assertThat(trade.getOriginalAmount()).isEqualTo("0.25");
    assertThat(trade.getCurrencyPair()).isEqualTo(CurrencyPair.BTC_USD);
    assertThat(trade.getPrice()).isEqualTo("655");
    assertThat(trade.getId()).isEqualTo("1402189342525");
    assertThat(trade.getType()).isEqualTo(OrderType.BID);
    assertThat(trade.getTimestamp().getTime()).isEqualTo(1402189342525L);
  }

  @Test
  public void testWalletAdapter() throws IOException {

    // Read in the JSON from the example resources
    InputStream is =
        ANXAdapterTest.class.getResourceAsStream("/v2/account/example-accountinfo-data.json");

    // Use Jackson to parse it
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    ANXAccountInfo anxAccountInfo = mapper.readValue(is, ANXAccountInfo.class);

    // in Wallet, only wallets from ANXAccountInfo.getBalancesList that contained data are NOT null.
    Collection<Balance> balances =
        ANXAdapters.adaptWallet(anxAccountInfo.getWallets()).getBalances().values();
    Assert.assertEquals(22, balances.size());

    Assert.assertTrue(
        balances.contains(
            new Balance(
                Currency.CAD, new BigDecimal("100000.00000"), new BigDecimal("100000.00000"))));
    Assert.assertTrue(
        balances.contains(
            new Balance(
                Currency.BTC,
                new BigDecimal("100000.01988000"),
                new BigDecimal("100000.01988000"))));
    Assert.assertTrue(
        balances.contains(
            new Balance(
                Currency.DOGE,
                new BigDecimal("9999781.09457936"),
                new BigDecimal("9914833.52608521"))));
  }

  @Test
  public void testTickerAdapter() throws IOException {

    // Read in the JSON from the example resources
    InputStream is =
        ANXAdapterTest.class.getResourceAsStream("/v2/marketdata/example-ticker-data.json");

    // Use Jackson to parse it
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    ANXTicker anxTicker = mapper.readValue(is, ANXTicker.class);

    Ticker ticker = ANXAdapters.adaptTicker(anxTicker);

    Assert.assertEquals(new BigDecimal("725.38123"), ticker.getLast());
    Assert.assertEquals(new BigDecimal("725.38123"), ticker.getHigh());
    Assert.assertEquals(new BigDecimal("380.00000"), ticker.getLow());
    Assert.assertEquals(new BigDecimal("7.00000000"), ticker.getVolume());
    Assert.assertEquals(new BigDecimal("725.38123"), ticker.getLast());
    Assert.assertEquals(new BigDecimal("38.85148"), ticker.getBid());
    Assert.assertEquals(new BigDecimal("897.25596"), ticker.getAsk());

    Assert.assertEquals(new Date(1393388594814L), ticker.getTimestamp());
  }
}
