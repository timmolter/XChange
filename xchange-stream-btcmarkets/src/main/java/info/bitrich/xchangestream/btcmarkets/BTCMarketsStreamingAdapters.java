package info.bitrich.xchangestream.btcmarkets;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import info.bitrich.xchangestream.btcmarkets.dto.BTCMarketsWebSocketOrderbookMessage;
import info.bitrich.xchangestream.btcmarkets.dto.BTCMarketsWebSocketTickerMessage;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.utils.DateUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

class BTCMarketsStreamingAdapters {
    public static String adaptCurrencyPairToMarketId(CurrencyPair currencyPair) {
        return currencyPair.base.toString() + "-" + currencyPair.counter.toString();
    }

    public static CurrencyPair adaptMarketIdToCurrencyPair(String marketId) {
        String[] parts = marketId.split("-");
        return new CurrencyPair(parts[0], parts[1]);
    }

    public static Ticker adaptTickerMessageToTicker(BTCMarketsWebSocketTickerMessage t) {
        return new Ticker.Builder()
                .last(t.lastPrice)
                .bid(t.bestBid)
                .ask(t.bestAsk)
                .timestamp(t.timestamp)
                .high(t.highPrice)
                .low(t.lowPrice)
                .build();
    }

    public static OrderBook adaptOrderbookMessageToOrderbook(
            BTCMarketsWebSocketOrderbookMessage message) throws InvalidFormatException {
        CurrencyPair currencyPair = adaptMarketIdToCurrencyPair(message.marketId);
        BiFunction<List<BigDecimal>, Order.OrderType, LimitOrder> toLimitOrder =
                (strings, ot) ->
                        new LimitOrder.Builder(ot, currencyPair)
                                .originalAmount(strings.get(1))
                                .limitPrice(strings.get(0))
                                .build();

        return new OrderBook(
                DateUtils.fromISODateString(message.timestamp),
                message.asks.stream()
                        .map((o) -> toLimitOrder.apply(o, Order.OrderType.ASK))
                        .collect(Collectors.toList()),
                message.bids.stream()
                        .map((o) -> toLimitOrder.apply(o, Order.OrderType.BID))
                        .collect(Collectors.toList()));
    }
}
