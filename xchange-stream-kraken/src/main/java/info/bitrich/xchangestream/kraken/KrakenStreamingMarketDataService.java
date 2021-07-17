package info.bitrich.xchangestream.kraken;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import info.bitrich.xchangestream.kraken.dto.enums.KrakenSubscriptionName;
import io.reactivex.Observable;
import org.apache.commons.lang3.ObjectUtils;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeSet;

/** @author makarid, pchertalev */
public class KrakenStreamingMarketDataService implements StreamingMarketDataService {

  private static final Logger LOG = LoggerFactory.getLogger(KrakenStreamingMarketDataService.class);

  private static final int MIN_DATA_ARRAY_SIZE = 4;

  public static final String KRAKEN_CHANNEL_DELIMITER = "-";

  private final KrakenStreamingService service;

  public KrakenStreamingMarketDataService(KrakenStreamingService service) {
    this.service = service;
  }

    @Override
    public Observable<OrderBook> getOrderBook(CurrencyPair currencyPair, Object... args) {
      String channelName = getChannelName(KrakenSubscriptionName.book, currencyPair);
      TreeSet<LimitOrder> bids = Sets.newTreeSet();
      TreeSet<LimitOrder> asks = Sets.newTreeSet();
      int depth = ObjectUtils.defaultIfNull(KrakenStreamingService.parseOrderBookSize(args), KrakenStreamingService.ORDER_BOOK_SIZE_DEFAULT);
      return subscribe(channelName, MIN_DATA_ARRAY_SIZE, args).map(arrayNode -> {
                            try {
                                    return KrakenStreamingAdapters.adaptOrderbookMessage(depth, bids, asks, currencyPair, arrayNode);
                            } catch (IllegalStateException e) {
                                LOG.warn("Resubscribing {} channel after adapter error {}", currencyPair, e.getMessage());
                                bids.clear();
                                asks.clear();
                                // Resubscribe to the channel, triggering a new snapshot
                                this.service.sendMessage(service.getUnsubscribeMessage(channelName, args));
                                this.service.sendMessage(service.getSubscribeMessage(channelName, args));
                                return new OrderBook(null, Lists.newArrayList(), Lists.newArrayList(), false);
                            }
                        })
              .filter(ob -> ob.getBids().size() > 0 && ob.getAsks().size() > 0);
    }

  @Override
  public Observable<Ticker> getTicker(CurrencyPair currencyPair, Object... args) {
    String channelName = getChannelName(KrakenSubscriptionName.ticker, currencyPair);
    return subscribe(channelName, MIN_DATA_ARRAY_SIZE, null)
        .map(arrayNode -> KrakenStreamingAdapters.adaptTickerMessage(currencyPair, arrayNode));
  }

  @Override
  public Observable<Trade> getTrades(CurrencyPair currencyPair, Object... args) {
    String channelName = getChannelName(KrakenSubscriptionName.trade, currencyPair);
    return subscribe(channelName, MIN_DATA_ARRAY_SIZE, null)
        .flatMap(
            arrayNode ->
                Observable.fromIterable(
                    KrakenStreamingAdapters.adaptTrades(currencyPair, arrayNode)));
  }

  public Observable<ArrayNode> subscribe(String channelName, int maxItems, Object... args) {
    return service
        .subscribeChannel(channelName, args)
        .filter(node -> node instanceof ArrayNode)
        .map(node -> (ArrayNode) node)
        .filter(
            list -> {
              if (list.size() < maxItems) {
                LOG.warn(
                    "Invalid message in channel {}. It contains {} array items but expected at least {}",
                    channelName,
                    list.size(),
                    maxItems);
                return false;
              }
              return true;
            });
  }

  public String getChannelName(KrakenSubscriptionName subscriptionName, CurrencyPair currencyPair) {
    String pair = currencyPair.base.toString() + "/" + currencyPair.counter.toString();
    return subscriptionName + KRAKEN_CHANNEL_DELIMITER + pair;
  }
}
