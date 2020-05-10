package info.bitrich.xchangestream.lgo;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import info.bitrich.xchangestream.lgo.domain.*;
import io.reactivex.Observable;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.apache.commons.io.IOUtils;
import org.assertj.core.util.Lists;
import org.junit.*;
import org.knowm.xchange.currency.*;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.*;
import org.knowm.xchange.lgo.dto.key.LgoKey;
import org.knowm.xchange.lgo.dto.order.LgoOrderSignature;
import org.knowm.xchange.lgo.service.*;
import org.mockito.ArgumentCaptor;
import si.mazi.rescu.SynchronizedValueFactory;

public class LgoStreamingTradeServiceTest {

  private SimpleDateFormat dateFormat;
  private LgoKeyService keyService;
  private LgoSignatureService signatureService;
  private LgoStreamingTradeService service;
  private LgoStreamingService streamingService;
  private SynchronizedValueFactory<Long> nonceFactory;

  @Before
  public void setUp() {
    dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    keyService = mock(LgoKeyService.class);
    signatureService = mock(LgoSignatureService.class);
    nonceFactory = mock(SynchronizedValueFactory.class);
    streamingService = mock(LgoStreamingService.class);
    service =
        new LgoStreamingTradeService(streamingService, keyService, signatureService, nonceFactory);
  }

  @Test
  public void it_handles_open_orders_updates() throws IOException, ParseException {
    JsonNode snapshot = TestUtils.getJsonContent("/trade/orders-snapshot.json");
    JsonNode update = TestUtils.getJsonContent("/trade/orders-update.json");
    when(streamingService.subscribeChannel(anyString()))
        .thenReturn(Observable.just(snapshot, update));

    Observable<OpenOrders> openOrders = service.getOpenOrders(CurrencyPair.BTC_USD);

    verify(streamingService).subscribeChannel("user-BTC-USD");
    Date date1 = dateFormat.parse("2019-07-23T15:36:14.304Z");
    Date date2 = dateFormat.parse("2019-07-18T12:24:21.891Z");
    Date date3 = dateFormat.parse("2019-07-24T08:37:19.116Z");
    LimitOrder order1 =
        new LimitOrder(
            Order.OrderType.ASK,
            BigDecimal.ONE,
            BigDecimal.ZERO,
            CurrencyPair.BTC_USD,
            "156389617430400001",
            date1,
            new BigDecimal(6000));
    order1.setOrderStatus(Order.OrderStatus.NEW);
    LimitOrder order2 =
        new LimitOrder(
            Order.OrderType.ASK,
            BigDecimal.ONE,
            BigDecimal.ZERO,
            CurrencyPair.BTC_USD,
            "156345266189100001",
            date2,
            new BigDecimal(6000));
    order2.setOrderStatus(Order.OrderStatus.NEW);
    LimitOrder order3 =
        new LimitOrder(
            Order.OrderType.BID,
            new BigDecimal(2),
            BigDecimal.ZERO,
            CurrencyPair.BTC_USD,
            "156395743911600001",
            date3,
            new BigDecimal(8000));
    order3.setOrderStatus(Order.OrderStatus.NEW);
    assertThat(openOrders.blockingFirst())
        .usingRecursiveComparison()
        .isEqualTo(new OpenOrders(Arrays.asList(order2, order1)));
    assertThat(openOrders.blockingLast())
        .usingRecursiveComparison()
        .isEqualTo(new OpenOrders(Arrays.asList(order3, order1)));
  }

  @Test
  public void it_handles_order_changes() throws IOException, ParseException {
    JsonNode snapshot = TestUtils.getJsonContent("/trade/order-changes-snapshot.json");
    JsonNode update = TestUtils.getJsonContent("/trade/order-changes-update.json");
    when(streamingService.subscribeChannel(anyString()))
        .thenReturn(Observable.just(snapshot, update));

    Observable<Order> observable = service.getOrderChanges(CurrencyPair.BTC_USD);

    verify(streamingService).subscribeChannel("user-BTC-USD");
    Date date1 = dateFormat.parse("2019-07-23T15:36:14.304Z");
    Date date2 = dateFormat.parse("2019-07-18T12:24:21.891Z");
    Date date3 = dateFormat.parse("2019-07-24T08:37:19.116Z");
    LimitOrder order1 =
        createOrder(
            Order.OrderType.ASK,
            new BigDecimal("1.00000000"),
            new BigDecimal("0.00000000"),
            CurrencyPair.BTC_USD,
            "156389617430400001",
            date1,
            new BigDecimal("6000.0000"),
            Order.OrderStatus.NEW);
    LimitOrder order2 =
        createOrder(
            Order.OrderType.ASK,
            new BigDecimal("1.00000000"),
            new BigDecimal("0.00000000"),
            CurrencyPair.BTC_USD,
            "156345266189100001",
            date2,
            new BigDecimal("6000.0000"),
            Order.OrderStatus.NEW);
    LimitOrder order2Matched =
        createOrder(
            Order.OrderType.ASK,
            new BigDecimal("1.00000000"),
            new BigDecimal("0.40000000"),
            CurrencyPair.BTC_USD,
            "156345266189100001",
            date2,
            new BigDecimal("6000.0000"),
            Order.OrderStatus.PARTIALLY_FILLED);
    order2Matched.setFee(new BigDecimal("0.2388"));
    LimitOrder order2Matched2 =
        createOrder(
            Order.OrderType.ASK,
            new BigDecimal("1.00000000"),
            new BigDecimal("1.00000000"),
            CurrencyPair.BTC_USD,
            "156345266189100001",
            date2,
            new BigDecimal("6000.0000"),
            Order.OrderStatus.PARTIALLY_FILLED);
    order2Matched2.setFee(new BigDecimal("0.6988"));
    LimitOrder order2Filled =
        createOrder(
            Order.OrderType.ASK,
            new BigDecimal("1.00000000"),
            new BigDecimal("1.00000000"),
            CurrencyPair.BTC_USD,
            "156345266189100001",
            date2,
            new BigDecimal("6000.0000"),
            Order.OrderStatus.FILLED);
    order2Filled.setFee(new BigDecimal("0.6988"));
    LimitOrder order3Pending =
        createOrder(
            Order.OrderType.BID,
            new BigDecimal("2.00000000"),
            new BigDecimal("0.00000000"),
            CurrencyPair.BTC_USD,
            "156395743911600001",
            date3,
            new BigDecimal("8000.0000"),
            Order.OrderStatus.PENDING_NEW);
    LimitOrder order3Open =
        createOrder(
            Order.OrderType.BID,
            new BigDecimal("2.00000000"),
            new BigDecimal("0.00000000"),
            CurrencyPair.BTC_USD,
            "156395743911600001",
            date3,
            new BigDecimal("8000.0000"),
            Order.OrderStatus.NEW);

    ArrayList<Order> orderChanges = Lists.newArrayList(observable.blockingIterable());
    assertThat(orderChanges).hasSize(7);
    assertThat(orderChanges.get(0)).usingRecursiveComparison().isEqualTo(order1);
    assertThat(orderChanges.get(1)).usingRecursiveComparison().isEqualTo(order2);
    assertThat(orderChanges.get(2)).usingRecursiveComparison().isEqualTo(order3Pending);
    assertThat(orderChanges.get(3)).usingRecursiveComparison().isEqualTo(order3Open);
    assertThat(orderChanges.get(4)).usingRecursiveComparison().isEqualTo(order2Matched);
    assertThat(orderChanges.get(5)).usingRecursiveComparison().isEqualTo(order2Matched2);
    assertThat(orderChanges.get(6)).usingRecursiveComparison().isEqualTo(order2Filled);
  }

  private LimitOrder createOrder(
      Order.OrderType type,
      BigDecimal originalAmount,
      BigDecimal cumulativeAmount,
      CurrencyPair pair,
      String id,
      Date timestamp,
      BigDecimal limitPrice,
      Order.OrderStatus status) {
    return new LimitOrder.Builder(type, pair)
        .id(id)
        .userReference(null)
        .originalAmount(originalAmount)
        .cumulativeAmount(cumulativeAmount)
        .limitPrice(limitPrice)
        .orderStatus(status)
        .averagePrice(null)
        .timestamp(timestamp)
        .fee(null)
        .build();
  }

  @Test
  public void it_handles_trades() throws IOException, ParseException {
    JsonNode snapshot = TestUtils.getJsonContent("/trade/user-trades-snapshot.json");
    JsonNode update = TestUtils.getJsonContent("/trade/user-trades-update.json");
    when(streamingService.subscribeChannel(anyString()))
        .thenReturn(Observable.just(snapshot, update));

    Observable<UserTrade> userTrades = service.getUserTrades(CurrencyPair.BTC_USD);

    verify(streamingService).subscribeChannel("user-BTC-USD");
    Date date = dateFormat.parse("2019-08-06T10:00:05.658Z");
    ArrayList<UserTrade> trades = Lists.newArrayList(userTrades.blockingIterable());
    assertThat(trades).hasSize(1);
    assertThat(trades.get(0))
        .usingRecursiveComparison()
        .isEqualTo(
            new UserTrade.Builder()
                .type(Order.OrderType.ASK)
                .originalAmount(new BigDecimal("0.50000000"))
                .currencyPair(CurrencyPair.BTC_USD)
                .price(new BigDecimal("955.3000"))
                .timestamp(date)
                .id("4441691")
                .orderId("156508560418400001")
                .feeAmount(new BigDecimal("0.2388"))
                .feeCurrency(Currency.USD)
                .build());
  }

  @Test
  public void it_handles_orders_events() throws IOException, ParseException {
    JsonNode snapshot = TestUtils.getJsonContent("/trade/order-events-snapshot.json");
    JsonNode update = TestUtils.getJsonContent("/trade/order-events-update.json");
    when(streamingService.subscribeChannel(anyString()))
        .thenReturn(Observable.just(snapshot, update));

    Observable<LgoOrderEvent> events = service.getRawBatchOrderEvents(CurrencyPair.BTC_USD);

    verify(streamingService).subscribeChannel("user-BTC-USD");
    Date date1 = dateFormat.parse("2019-07-24T08:37:19.116Z");
    Date date2 = dateFormat.parse("2019-07-24T08:37:19.849Z");
    Date date3 = dateFormat.parse("2019-07-24T08:37:19.922Z");
    ArrayList<LgoOrderEvent> lgoOrderEvents = Lists.newArrayList(events.blockingIterable());
    assertThat(lgoOrderEvents.get(0))
        .isEqualToComparingFieldByField(
            new LgoPendingOrderEvent(
                6317543L,
                "pending",
                "156395743911600001",
                date1,
                "L",
                new BigDecimal("8000.0000"),
                Order.OrderType.BID,
                new BigDecimal("2.00000000")));
    assertThat(lgoOrderEvents.get(1))
        .isEqualToComparingFieldByField(
            new LgoOpenOrderEvent(6317543L, "open", "156395743911600001", date2));
    assertThat(lgoOrderEvents.get(2))
        .isEqualToComparingFieldByField(
            new LgoPendingOrderEvent(
                6317543L,
                "pending",
                "156395743912700001",
                date2,
                "L",
                new BigDecimal("8000.0001"),
                Order.OrderType.BID,
                new BigDecimal("2.00000000")));
    assertThat(lgoOrderEvents.get(3))
        .isEqualToComparingFieldByField(
            new LgoInvalidOrderEvent(
                6317543L, "invalid", "156395743912700001", date2, "InvalidAmount"));
    assertThat(lgoOrderEvents.get(4))
        .isEqualToComparingFieldByField(
            new LgoDoneOrderEvent(6317543L, "done", "156345266189100001", date3, "canceled", null));
  }

  @Test
  public void it_handles_orders_ack() throws IOException, ParseException {
    JsonNode snapshot = TestUtils.getJsonContent("/trade/afr-snapshot.json");
    JsonNode update1 = TestUtils.getJsonContent("/trade/afr-update1.json");
    JsonNode update2 = TestUtils.getJsonContent("/trade/afr-update2.json");
    when(streamingService.subscribeChannel(anyString()))
        .thenReturn(Observable.just(snapshot, update1, update2));

    Observable<LgoOrderEvent> events = service.getRawReceivedOrderEvents();

    verify(streamingService).subscribeChannel("afr");
    Date date1 = dateFormat.parse("2019-07-24T13:42:34.970Z");
    Date date2 = dateFormat.parse("2019-07-24T13:42:35.698Z");
    ArrayList<LgoOrderEvent> lgoOrderEvents = Lists.newArrayList(events.blockingIterable());
    assertThat(lgoOrderEvents.get(0))
        .isEqualToComparingFieldByField(
            new LgoReceivedOrderEvent("156397575497000001", "plop", "received", date1));
    assertThat(lgoOrderEvents.get(1))
        .isEqualToComparingFieldByField(
            new LgoFailedOrderEvent(
                "156397575497000001", "plop", "failed", date2, "INVALID_PAYLOAD"));
  }

  @Test
  public void it_handles_all_order_events() throws IOException, ParseException {
    JsonNode snapshotOrders = TestUtils.getJsonContent("/trade/all-orders-snapshot.json");
    JsonNode snapshotAFR = TestUtils.getJsonContent("/trade/all-afr-snapshot.json");
    JsonNode update1 = TestUtils.getJsonContent("/trade/all-afr-update.json");
    JsonNode update2 = TestUtils.getJsonContent("/trade/all-orders-update.json");
    when(streamingService.subscribeChannel("afr"))
        .thenReturn(Observable.just(snapshotAFR, update1));
    when(streamingService.subscribeChannel("user-BTC-USD"))
        .thenReturn(Observable.just(snapshotOrders, update2));

    Observable<LgoOrderEvent> events =
        service.getRawAllOrderEvents(Collections.singletonList(CurrencyPair.BTC_USD));

    verify(streamingService).subscribeChannel("afr");
    verify(streamingService).subscribeChannel("user-BTC-USD");
    Date date1 = dateFormat.parse("2019-07-25T07:16:21.600Z");
    Date date2 = dateFormat.parse("2019-07-25T07:16:22.959Z");
    ArrayList<LgoOrderEvent> lgoOrderEvents = Lists.newArrayList(events.blockingIterable());
    assertThat(lgoOrderEvents.get(0))
        .isEqualToComparingFieldByField(
            new LgoReceivedOrderEvent("156403898160000001", "0", "received", date1));
    assertThat(lgoOrderEvents.get(1))
        .isEqualToComparingFieldByField(
            new LgoPendingOrderEvent(
                6393996L,
                "pending",
                "156403898160000001",
                date1,
                "L",
                new BigDecimal("7000.0000"),
                Order.OrderType.ASK,
                new BigDecimal("3.00000000")));
    assertThat(lgoOrderEvents.get(2))
        .isEqualToComparingFieldByField(
            new LgoOpenOrderEvent(6393996L, "open", "156403898160000001", date2));
  }

  @Test
  public void it_places_a_limit_order() throws IOException, ParseException {
    Date date = dateFormat.parse("2019-07-25T07:16:21.600Z");
    LimitOrder limitOrder =
        new LimitOrder(
            Order.OrderType.ASK,
            new BigDecimal("0.5"),
            CurrencyPair.BTC_USD,
            null,
            date,
            new BigDecimal("12000"));
    when(nonceFactory.createValue()).thenReturn(22L);
    LgoKey key =
        new LgoKey(
            "abcdefg",
            new Date().toInstant().minus(1, ChronoUnit.HOURS),
            new Date().toInstant().plus(1, ChronoUnit.HOURS));
    InputStream stream = LgoStreamingExchangeExample.class.getResourceAsStream("/public.pem");
    String utf8 = IOUtils.toString(stream, StandardCharsets.UTF_8);
    key.setValue(parsePublicKey(utf8));
    when(keyService.selectKey()).thenReturn(key);
    when(signatureService.signOrder(anyString())).thenReturn(new LgoOrderSignature("signed"));
    when(streamingService.sendMessage(anyString())).then(inv -> null);

    String ref = service.placeLimitOrder(limitOrder);

    verify(nonceFactory).createValue();
    verify(keyService).selectKey();
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(streamingService).sendMessage(captor.capture());
    assertThat(captor.getValue().contains("\"reference\":22"));
    assertThat(
        captor
            .getValue()
            .contains(
                "\"signature\":{\"value\":\"signed\",\"source\":\"RSA\"},\"key_id\":\"abcdefg\"},\"type\":\"placeorder\""));
    assertThat(ref).isEqualTo("22");
  }

  private static String parsePublicKey(String key) {
    return key.replaceAll("-----END PUBLIC KEY-----", "")
        .replaceAll("-----BEGIN PUBLIC KEY-----", "")
        .replaceAll("\n", "")
        .replaceAll("\r", "");
  }
}
