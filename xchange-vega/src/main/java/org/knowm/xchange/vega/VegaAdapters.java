package org.knowm.xchange.vega;

import io.vegaprotocol.vega.Markets;
import io.vegaprotocol.vega.Vega;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.meta.CurrencyMetaData;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.utils.DateUtils;

import java.math.BigDecimal;
import java.util.*;

public class VegaAdapters {

    public static ExchangeMetaData adaptMetaData(List<CurrencyPair> currencyPairs) {
        Map<CurrencyPair, CurrencyPairMetaData> currencyPairMap = new HashMap<>(currencyPairs.size());

        for (CurrencyPair pair : currencyPairs) {
            currencyPairMap.put(pair, null);
        }

        return new ExchangeMetaData(
                currencyPairMap,
                Collections.<Currency, CurrencyMetaData>emptyMap(),
                null,
                null,
                null
        );
    }

    public static CurrencyPair adaptMarketToCurrencyPair(Markets.Market market) {
        String base = null;
        String quote = null;

        for (String tag : market.getTradableInstrument().getInstrument().getMetadata().getTagsList()) {
            if (tag.startsWith("base:")) {
                base = tag.replace("base:", "");
            } else if (tag.startsWith("ticker:")) {
                base = tag.replace("ticker:", "");
            } else if (tag.startsWith("quote:")) {
                quote = tag.replace("quote:", "");
            }
        }

        return new CurrencyPair(base, quote);
    }

    public static Trade adaptTrade(Vega.Trade trade, CurrencyPair currencyPair) {
        Order.OrderType orderType = null;

        if (trade.getAggressor() == Vega.Side.SIDE_BUY) {
            orderType = Order.OrderType.BID;
        } else if (trade.getAggressor() == Vega.Side.SIDE_SELL) {
            orderType = Order.OrderType.ASK;
        }

        return new Trade(
                orderType,
                BigDecimal.valueOf(trade.getSize()),
                currencyPair,
                BigDecimal.valueOf(trade.getPrice()),
                DateUtils.fromUnixTime(trade.getTimestamp()),
                trade.getId(),
                trade.getSellOrder(),
                trade.getBuyOrder()
        );
    }

    public static OrderBook adaptOrderBook(List<Vega.Order> orders, CurrencyPair currencyPair) {
        List<LimitOrder> bidOrders = new ArrayList<>();
        List<LimitOrder> askOrders = new ArrayList<>();

        for (Vega.Order order : orders) {
            if (order.getSide() == Vega.Side.SIDE_BUY) {
                bidOrders.add(
                        new LimitOrder.Builder(Order.OrderType.BID, currencyPair)
                                .originalAmount(BigDecimal.valueOf(order.getSize()))
                                .id(order.getId())
                                .timestamp(DateUtils.fromUnixTime(order.getCreatedAt()))
                                .averagePrice(BigDecimal.valueOf(order.getPrice()))
                                .build()
                );
            } else if (order.getSide() == Vega.Side.SIDE_SELL) {
                askOrders.add(
                        new LimitOrder.Builder(Order.OrderType.ASK, currencyPair)
                                .originalAmount(BigDecimal.valueOf(order.getSize()))
                                .id(order.getId())
                                .timestamp(DateUtils.fromUnixTime(order.getCreatedAt()))
                                .averagePrice(BigDecimal.valueOf(order.getPrice()))
                                .build()
                );
            }
        }

        return new OrderBook(null, askOrders, bidOrders);
    }
}
