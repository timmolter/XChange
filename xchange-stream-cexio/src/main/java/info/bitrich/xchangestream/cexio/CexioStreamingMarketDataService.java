package info.bitrich.xchangestream.cexio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.cexio.dto.CexioWebSocketOrderBookSubscribeResponse;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import io.reactivex.Observable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;

import io.reactivex.annotations.NonNull;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;

public class CexioStreamingMarketDataService implements StreamingMarketDataService {

  private final CexioStreamingRawService streamingOrderDataService;

  public CexioStreamingMarketDataService(CexioStreamingRawService streamingOrderDataService) {
    this.streamingOrderDataService = streamingOrderDataService;
  }

  static class OrderBookUpdateConsumer
      implements io.reactivex.functions.Function<
          CexioWebSocketOrderBookSubscribeResponse, OrderBook> {
    BigInteger prevID = null;
    OrderBook orderBookSoFar =
        new OrderBook(new Date(), new ArrayList<>(), new ArrayList<>());
    final CexioStreamingRawService streamingOrderDataService;

    public OrderBookUpdateConsumer(CexioStreamingRawService streamingOrderDataService) {
      this.streamingOrderDataService = streamingOrderDataService;
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public OrderBook apply(@NonNull CexioWebSocketOrderBookSubscribeResponse t) throws Exception {
      OrderBook retVal;
      if (prevID != null && prevID.add(BigInteger.ONE).compareTo(t.id) != 0) {
        prevID = null;
        orderBookSoFar = new OrderBook(new Date(), new ArrayList<>(), new ArrayList<>());

        throw new IllegalStateException(
            "Received an update message with id ["
                + t.id
                + "] not sequential to last id ["
                + prevID
                + "]. "
                + "Orderbook out of order!");
      }

      prevID = t.id;
      orderBookSoFar = CexioAdapters.adaptOrderBookIncremental(orderBookSoFar, t);
      retVal = orderBookSoFar;

      return retVal;
    }
  }

  @Override
  public Observable<OrderBook> getOrderBook(CurrencyPair currencyPair, Object... args) {
    String channelNameForPair =
        CexioStreamingRawService.GetOrderBookChannelForCurrencyPair(currencyPair);

    final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();
    // check depth parameter
    int depth = 0;
    if (args != null && args[0] instanceof Integer) {
      depth = (Integer) args[0];
    }
    Observable<JsonNode> jsonNodeObservable =
        streamingOrderDataService.subscribeChannel(channelNameForPair, currencyPair, depth);
    OrderBookUpdateConsumer orderBookConsumer =
        new OrderBookUpdateConsumer(streamingOrderDataService);
    return jsonNodeObservable
        .map(
            s -> {
              JsonNode dataNode = s.get("data");
              return mapper.readValue(
                  dataNode.toString(), CexioWebSocketOrderBookSubscribeResponse.class);
            })
        .map(orderBookConsumer);
  }

  @Override
  public Observable<Ticker> getTicker(CurrencyPair currencyPair, Object... args) {
    String channelNameForPair =
        CexioStreamingRawService.GetOrderBookChannelForCurrencyPair(currencyPair);

    final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();
    int depth = 1;
    Observable<JsonNode> jsonNodeObservable =
        streamingOrderDataService.subscribeChannel(channelNameForPair, currencyPair, depth);
    OrderBookUpdateConsumer orderBookConsumer =
        new OrderBookUpdateConsumer(streamingOrderDataService);

    return jsonNodeObservable
        .map(
            s -> {
              JsonNode dataNode = s.get("data");
              return mapper.readValue(
                  dataNode.toString(), CexioWebSocketOrderBookSubscribeResponse.class);
            })
        .map(orderBookConsumer)
        .filter(orderBook -> !orderBook.getBids().isEmpty() && !orderBook.getAsks().isEmpty())
        .map(
            orderBook ->
                new Ticker.Builder()
                    .timestamp(orderBook.getTimeStamp())
                    .instrument(currencyPair)
                    .bid(orderBook.getBids().get(0).getLimitPrice())
                    .ask(orderBook.getAsks().get(0).getLimitPrice())
                    .build())
        .distinctUntilChanged(
            (prev, next) ->
                prev.getBid().compareTo(next.getBid()) == 0
                    && prev.getAsk().compareTo(next.getAsk()) == 0);
  }

  @Override
  public Observable<Trade> getTrades(CurrencyPair currencyPair, Object... args) {
    throw new NotYetImplementedForExchangeException();
  }
}
