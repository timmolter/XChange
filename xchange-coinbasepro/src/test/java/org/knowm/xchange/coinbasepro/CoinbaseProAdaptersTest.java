package org.knowm.xchange.coinbasepro;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.junit.Test;
import org.knowm.xchange.coinbasepro.dto.marketdata.CoinbaseProProductStats;
import org.knowm.xchange.coinbasepro.dto.marketdata.CoinbaseProProductTicker;
import org.knowm.xchange.coinbasepro.dto.trade.CoinbaseProFill;
import org.knowm.xchange.coinbasepro.dto.trade.CoinbaseProOrder;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.StopOrder;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.dto.trade.UserTrades;
import si.mazi.rescu.serialization.jackson.DefaultJacksonObjectMapperFactory;
import si.mazi.rescu.serialization.jackson.JacksonObjectMapperFactory;

public class CoinbaseProAdaptersTest {

  @Test
  public void parseDateTest() {

    assertThat(CoinbaseProAdapters.parseDate("2017-05-02T15:10:03Z").getTime())
        .isEqualTo(1493737803000L);
    assertThat(CoinbaseProAdapters.parseDate("2017-05-02T15:10:03.1Z").getTime())
        .isEqualTo(1493737803100L);
    assertThat(CoinbaseProAdapters.parseDate("2017-05-02T15:10:03.12Z").getTime())
        .isEqualTo(1493737803120L);
    assertThat(CoinbaseProAdapters.parseDate("2017-05-02T15:10:03.123Z").getTime())
        .isEqualTo(1493737803123L);
    assertThat(CoinbaseProAdapters.parseDate("2017-05-02T15:10:03.1234567Z").getTime())
        .isEqualTo(1493737803123L);

    assertThat(CoinbaseProAdapters.parseDate("2017-05-02T15:10:03").getTime())
        .isEqualTo(1493737803000L);
    assertThat(CoinbaseProAdapters.parseDate("2017-05-02T15:10:03.1").getTime())
        .isEqualTo(1493737803100L);
    assertThat(CoinbaseProAdapters.parseDate("2017-05-02T15:10:03.12").getTime())
        .isEqualTo(1493737803120L);
    assertThat(CoinbaseProAdapters.parseDate("2017-05-02T15:10:03.123").getTime())
        .isEqualTo(1493737803123L);
    assertThat(CoinbaseProAdapters.parseDate("2017-05-02T15:10:03.123456").getTime())
        .isEqualTo(1493737803123L);

    assertThat(CoinbaseProAdapters.parseDate("2017-06-21T04:52:01.996Z").getTime())
        .isEqualTo(1498020721996L);
  }

  @Test
  public void testTickerAdapter() throws IOException {

    // Read in the JSON from the example resources
    InputStream is =
        CoinbaseProAdaptersTest.class.getResourceAsStream(
            "/org/knowm/xchange/coinbasepro/dto/marketdata/example-ticker-data.json");
    InputStream is2 =
        CoinbaseProAdaptersTest.class.getResourceAsStream(
            "/org/knowm/xchange/coinbasepro/dto/marketdata/example-stats-data.json");

    // Use Jackson to parse it
    ObjectMapper mapper = new ObjectMapper();
    CoinbaseProProductTicker coinbaseExTicker =
        mapper.readValue(is, CoinbaseProProductTicker.class);
    CoinbaseProProductStats coinbaseExStats = mapper.readValue(is2, CoinbaseProProductStats.class);

    Ticker ticker =
        CoinbaseProAdapters.adaptTicker(coinbaseExTicker, coinbaseExStats, CurrencyPair.BTC_USD);

    assertThat(ticker.getLast().toString()).isEqualTo("246.28000000");
    assertThat(ticker.getOpen().toString()).isEqualTo("254.04000000");
    assertThat(ticker.getBid().toString()).isEqualTo("637");
    assertThat(ticker.getAsk().toString()).isEqualTo("637.11");
    assertThat(ticker.getHigh().toString()).isEqualTo("255.47000000");
    assertThat(ticker.getLow().toString()).isEqualTo("244.29000000");
    assertThat(ticker.getVolume()).isEqualTo(new BigDecimal("4661.70407704"));
    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    f.setTimeZone(TimeZone.getTimeZone("UTC"));
    String dateString = f.format(ticker.getTimestamp());
    assertThat(dateString).isEqualTo("2015-04-08 20:49:06");
  }

  @Test
  public void testTradeHistoryAdapter() throws IOException {

    JacksonObjectMapperFactory factory = new DefaultJacksonObjectMapperFactory();
    ObjectMapper mapper = factory.createObjectMapper();

    InputStream is =
        getClass()
            .getResourceAsStream("/org/knowm/xchange/coinbasepro/dto/trade/example-fills.json");
    List<CoinbaseProFill> fills =
        mapper.readValue(is, new TypeReference<List<CoinbaseProFill>>() {});

    UserTrades trades = CoinbaseProAdapters.adaptTradeHistory(fills);

    assertThat(trades.getUserTrades()).hasSize(1);

    UserTrade trade = trades.getUserTrades().get(0);

    assertThat(trade.getId()).isEqualTo("470768");
    assertThat(trade.getCurrencyPair()).isEqualTo(CurrencyPair.ETH_BTC);
    assertThat(trade.getPrice()).isEqualTo("0.05915000");
    assertThat(trade.getOriginalAmount()).isEqualTo("0.01000000");
    assertThat(trade.getOrderId()).isEqualTo("b4b3bbb1-e0e3-4532-9413-23123448ce35");
    assertThat(trade.getTimestamp().getTime()).isEqualTo(1493623910243L);
    assertThat(trade.getFeeAmount()).isEqualTo("0.0000017745000000");
    assertThat(trade.getType()).isEqualTo(OrderType.BID);
  }

  @Test
  public void testOrderStatusMarketOrderFilled() throws IOException {

    JacksonObjectMapperFactory factory = new DefaultJacksonObjectMapperFactory();
    ObjectMapper mapper = factory.createObjectMapper();

    InputStream is =
        getClass()
            .getResourceAsStream(
                "/org/knowm/xchange/coinbasepro/dto/order/example-market-order-filled.json");
    CoinbaseProOrder coinbaseProOrder = mapper.readValue(is, CoinbaseProOrder.class);

    Order order = CoinbaseProAdapters.adaptOrder(coinbaseProOrder);

    assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.FILLED);
    assertThat(order.getId()).isEqualTo(coinbaseProOrder.getId());
    assertThat(order.getCurrencyPair()).isEqualTo((CurrencyPair.BTC_USD));
    assertThat(order.getOriginalAmount()).isEqualByComparingTo(new BigDecimal("1.00000000"));
    assertThat(order.getCumulativeAmount()).isEqualByComparingTo(new BigDecimal("0.01291771"));
    assertThat(order.getRemainingAmount())
        .isEqualByComparingTo(new BigDecimal("1.0").subtract(new BigDecimal("0.01291771")));
    assertThat(order.getFee()).isEqualTo(new BigDecimal("0.0249376391550000"));
    assertThat(MarketOrder.class.isAssignableFrom(order.getClass())).isTrue();
    assertThat(order.getType()).isEqualTo(OrderType.BID);
    assertThat(order.getTimestamp()).isEqualTo(new Date(1481227745508L));
    assertThat(order.getAveragePrice())
        .isEqualByComparingTo(
            new BigDecimal("9.9750556620000000")
                .divide(new BigDecimal("0.01291771"), new MathContext(8)));
  }

  @Test
  public void testOrderStatusLimitOrderFilled() throws IOException {

    JacksonObjectMapperFactory factory = new DefaultJacksonObjectMapperFactory();
    ObjectMapper mapper = factory.createObjectMapper();

    InputStream is =
        getClass()
            .getResourceAsStream(
                "/org/knowm/xchange/coinbasepro/dto/order/example-limit-order-filled.json");
    CoinbaseProOrder coinbaseProOrder = mapper.readValue(is, CoinbaseProOrder.class);

    Order order = CoinbaseProAdapters.adaptOrder(coinbaseProOrder);

    assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.FILLED);
    assertThat(order.getId()).isEqualTo("b2cdd7fe-1f4a-495e-8b96-7a4be368f43c");
    assertThat(order.getCurrencyPair()).isEqualTo((CurrencyPair.BTC_USD));
    assertThat(order.getOriginalAmount()).isEqualByComparingTo(new BigDecimal("0.07060351"));
    assertThat(order.getCumulativeAmount()).isEqualByComparingTo(new BigDecimal("0.07060351"));
    assertThat(order.getRemainingAmount()).isEqualByComparingTo(new BigDecimal("0.00000000"));
    assertThat(order.getFee()).isEqualTo(new BigDecimal("2.6256545174247500"));
    assertThat(LimitOrder.class.isAssignableFrom(order.getClass())).isTrue();
    assertThat(order.getType()).isEqualTo(OrderType.ASK);
    assertThat(order.getTimestamp()).isEqualTo(new Date(1515434144454L));
    assertThat(order.getAveragePrice())
        .isEqualByComparingTo(
            new BigDecimal("1050.2618069699000000")
                .divide(new BigDecimal("0.07060351"), new MathContext(8)));
  }

  @Test
  public void testOrderStatusLimitOrderSettled() throws IOException {

    final JacksonObjectMapperFactory factory = new DefaultJacksonObjectMapperFactory();
    final ObjectMapper mapper = factory.createObjectMapper();

    final InputStream is =
        getClass()
            .getResourceAsStream(
                "/org/knowm/xchange/coinbasepro/dto/order/example-limit-order-settled.json");
    final CoinbaseProOrder coinbaseProOrder = mapper.readValue(is, CoinbaseProOrder.class);

    final Order order = CoinbaseProAdapters.adaptOrder(coinbaseProOrder);

    assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.FILLED);
    assertThat(order.getId()).isEqualTo("b2cdd7fe-1f4a-495e-8b96-7a4be368f43c");
    assertThat(order.getCurrencyPair()).isEqualTo((CurrencyPair.BTC_USD));
    assertThat(order.getOriginalAmount()).isEqualByComparingTo(new BigDecimal("0.07060351"));
    assertThat(order.getCumulativeAmount()).isEqualByComparingTo(new BigDecimal("0.07060351"));
    assertThat(order.getRemainingAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(LimitOrder.class.isAssignableFrom(order.getClass())).isTrue();
    assertThat(order.getType()).isEqualTo(OrderType.ASK);
    assertThat(order.getTimestamp()).isEqualTo(new Date(1515434144454L));
    assertThat(order.getAveragePrice())
        .isEqualByComparingTo(
            new BigDecimal("1050.2618069699000000")
                .divide(new BigDecimal("0.07060351"), new MathContext(8)));
  }

  @Test
  public void testOrderStatusLimitOrderNew() throws IOException {

    final JacksonObjectMapperFactory factory = new DefaultJacksonObjectMapperFactory();
    final ObjectMapper mapper = factory.createObjectMapper();

    final InputStream is =
        getClass()
            .getResourceAsStream(
                "/org/knowm/xchange/coinbasepro/dto/order/example-limit-order-new.json");
    final CoinbaseProOrder coinbaseProOrder = mapper.readValue(is, CoinbaseProOrder.class);

    final Order order = CoinbaseProAdapters.adaptOrder(coinbaseProOrder);

    assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.NEW);
    assertThat(order.getId()).isEqualTo("b2cdd7fe-1f4a-495e-8b96-7a4be368f43c");
    assertThat(order.getCurrencyPair()).isEqualTo((CurrencyPair.BTC_USD));
    assertThat(order.getOriginalAmount()).isEqualByComparingTo(new BigDecimal("0.07060351"));
    assertThat(order.getCumulativeAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(order.getRemainingAmount()).isEqualByComparingTo(new BigDecimal("0.07060351"));
    assertThat(order.getType()).isEqualTo(OrderType.ASK);
    assertThat(order.getTimestamp()).isEqualTo(new Date(1515434144454L));
    assertThat(order.getAveragePrice()).isEqualByComparingTo(BigDecimal.ZERO);

    assertThat(LimitOrder.class.isAssignableFrom(order.getClass())).isTrue();
    LimitOrder limitOrder = (LimitOrder) order;
    assertThat(limitOrder.getLimitPrice()).isEqualByComparingTo(new BigDecimal("14839.76"));
  }

  @Test
  public void testOrderStatusStopOrderNew()
      throws JsonParseException, JsonMappingException, IOException {

    final JacksonObjectMapperFactory factory = new DefaultJacksonObjectMapperFactory();
    final ObjectMapper mapper = factory.createObjectMapper();

    final InputStream is =
        getClass()
            .getResourceAsStream(
                "/org/knowm/xchange/coinbasepro/dto/order/example-stop-order-new.json");
    final CoinbaseProOrder coinbaseProOrder = mapper.readValue(is, CoinbaseProOrder.class);

    final Order order = CoinbaseProAdapters.adaptOrder(coinbaseProOrder);

    assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.NEW);
    assertThat(order.getId()).isEqualTo("853a9989-7dd9-40f8-9392-64237a9eccc4");
    assertThat(order.getCurrencyPair()).isEqualTo((CurrencyPair.BTC_EUR));
    assertThat(order.getOriginalAmount()).isEqualByComparingTo(new BigDecimal("0.01"));
    assertThat(order.getCumulativeAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(order.getRemainingAmount()).isEqualByComparingTo(new BigDecimal("0.01"));
    assertThat(order.getType()).isEqualTo(OrderType.ASK);
    assertThat(order.getTimestamp()).isEqualTo(new Date(1515434144454L));
    assertThat(order.getAveragePrice()).isEqualByComparingTo(BigDecimal.ZERO);

    assertThat(StopOrder.class.isAssignableFrom(order.getClass())).isTrue();
    StopOrder stop = (StopOrder) order;
    assertThat(stop.getStopPrice()).isEqualByComparingTo("6364.31");
  }

  @Test
  public void testOrderStatusStopOrderStopped()
      throws JsonParseException, JsonMappingException, IOException {

    final JacksonObjectMapperFactory factory = new DefaultJacksonObjectMapperFactory();
    final ObjectMapper mapper = factory.createObjectMapper();

    final InputStream is =
        getClass()
            .getResourceAsStream(
                "/org/knowm/xchange/coinbasepro/dto/order/example-stop-order-stopped.json");
    final CoinbaseProOrder coinbaseProOrder = mapper.readValue(is, CoinbaseProOrder.class);

    final Order order = CoinbaseProAdapters.adaptOrder(coinbaseProOrder);

    assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.STOPPED);
    assertThat(order.getId()).isEqualTo("853a9989-7dd9-40f8-9392-64237a9eccc4");
    assertThat(order.getCurrencyPair()).isEqualTo((CurrencyPair.BTC_EUR));
    assertThat(order.getOriginalAmount()).isEqualByComparingTo(new BigDecimal("0.01"));
    assertThat(order.getCumulativeAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(order.getRemainingAmount()).isEqualByComparingTo(new BigDecimal("0.01"));
    assertThat(order.getType()).isEqualTo(OrderType.ASK);
    assertThat(order.getTimestamp()).isEqualTo(new Date(1515434144454L));
    assertThat(order.getAveragePrice()).isEqualByComparingTo(BigDecimal.ZERO);

    assertThat(StopOrder.class.isAssignableFrom(order.getClass())).isTrue();
    StopOrder stop = (StopOrder) order;
    assertThat(stop.getStopPrice()).isEqualByComparingTo("6364.31");
  }

  @Test
  public void testOrderStatusStopOrderFilled()
      throws JsonParseException, JsonMappingException, IOException {

    final JacksonObjectMapperFactory factory = new DefaultJacksonObjectMapperFactory();
    final ObjectMapper mapper = factory.createObjectMapper();

    final InputStream is =
        getClass()
            .getResourceAsStream(
                "/org/knowm/xchange/coinbasepro/dto/order/example-stop-order-filled.json");
    final CoinbaseProOrder coinbaseProOrder = mapper.readValue(is, CoinbaseProOrder.class);

    final Order order = CoinbaseProAdapters.adaptOrder(coinbaseProOrder);

    assertStopOrderFilled(order);
  }

  @Test
  public void testOrderStatusLimitOrderPending() throws IOException {

    final JacksonObjectMapperFactory factory = new DefaultJacksonObjectMapperFactory();
    final ObjectMapper mapper = factory.createObjectMapper();

    final InputStream is =
        getClass()
            .getResourceAsStream(
                "/org/knowm/xchange/coinbasepro/dto/order/example-limit-order-pending.json");
    final CoinbaseProOrder coinbaseProOrder = mapper.readValue(is, CoinbaseProOrder.class);

    final Order order = CoinbaseProAdapters.adaptOrder(coinbaseProOrder);

    assertLimitOrderPending(order);
  }

  @Test
  public void testOrders() throws JsonParseException, JsonMappingException, IOException {
    final JacksonObjectMapperFactory factory = new DefaultJacksonObjectMapperFactory();
    final ObjectMapper mapper = factory.createObjectMapper();

    InputStream is =
        new SequenceInputStream(
            new ByteArrayInputStream("[".getBytes(StandardCharsets.UTF_8)),
            new SequenceInputStream(
                getClass()
                    .getResourceAsStream(
                        "/org/knowm/xchange/coinbasepro/dto/order/example-limit-order-pending.json"),
                new SequenceInputStream(
                    new ByteArrayInputStream(", ".getBytes(StandardCharsets.UTF_8)),
                    new SequenceInputStream(
                        getClass()
                            .getResourceAsStream(
                                "/org/knowm/xchange/coinbasepro/dto/order/example-stop-order-filled.json"),
                        new ByteArrayInputStream("]".getBytes(StandardCharsets.UTF_8))))));

    final CoinbaseProOrder[] coinbaseProOrders = mapper.readValue(is, CoinbaseProOrder[].class);

    OpenOrders openOrders = CoinbaseProAdapters.adaptOpenOrders(coinbaseProOrders);

    assertThat(openOrders.getOpenOrders()).hasSize(1);
    assertThat(openOrders.getHiddenOrders()).hasSize(1);
    assertStopOrderFilled(openOrders.getHiddenOrders().get(0));
    assertLimitOrderPending(openOrders.getOpenOrders().get(0));
  }

  @Test
  public void testAdaptProductID() {
    String productID = CoinbaseProAdapters.adaptProductID(CurrencyPair.ETH_BTC);

    assertThat(productID).isEqualTo(Currency.ETH + "-" + Currency.BTC);
  }

  @Test
  public void testAdaptProductIDHandlesNull() {
    String productID = CoinbaseProAdapters.adaptProductID(null);

    assertThat(productID).isEqualTo(null);
  }

  private void assertStopOrderFilled(final Order order) {
    assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.FILLED);
    assertThat(order.getId()).isEqualTo("a9098e25-9d4d-4e2c-ab5e-8c057cc4cbee");
    assertThat(order.getCurrencyPair()).isEqualTo((CurrencyPair.BTC_EUR));
    assertThat(order.getOriginalAmount()).isEqualByComparingTo(new BigDecimal("0.08871972"));
    assertThat(order.getCumulativeAmount()).isEqualByComparingTo(new BigDecimal("0.08871972"));
    assertThat(order.getRemainingAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(order.getType()).isEqualTo(OrderType.BID);
    assertThat(order.getTimestamp()).isEqualTo(new Date(1515434144454L));
    assertThat(order.getAveragePrice())
        .isEqualByComparingTo(
            new BigDecimal("639.3107535312")
                .divide(new BigDecimal("0.08871972"), new MathContext(8)));

    assertThat(StopOrder.class.isAssignableFrom(order.getClass())).isTrue();
    StopOrder stop = (StopOrder) order;
    assertThat(stop.getStopPrice()).isEqualByComparingTo("7205");
  }

  private void assertLimitOrderPending(final Order order) {
    assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.PENDING_NEW);
    assertThat(order.getId()).isEqualTo("b2cdd7fe-1f4a-495e-8b96-7a4be368f43c");
    assertThat(order.getCurrencyPair()).isEqualTo((CurrencyPair.BTC_USD));
    assertThat(order.getOriginalAmount()).isEqualByComparingTo(new BigDecimal("0.07060351"));
    assertThat(order.getCumulativeAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(order.getRemainingAmount()).isEqualByComparingTo(new BigDecimal("0.07060351"));
    assertThat(LimitOrder.class.isAssignableFrom(order.getClass())).isTrue();
    assertThat(order.getType()).isEqualTo(OrderType.ASK);
    assertThat(order.getTimestamp()).isEqualTo(new Date(1515434144454L));
    assertThat(order.getAveragePrice()).isEqualByComparingTo(BigDecimal.ZERO);
  }
}
