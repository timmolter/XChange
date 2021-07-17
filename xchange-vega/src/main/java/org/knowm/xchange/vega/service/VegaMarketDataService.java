package org.knowm.xchange.vega.service;

import io.vegaprotocol.vega.Markets;
import io.vegaprotocol.vega.Vega;
import io.vegaprotocol.vega.api.Trading;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.utils.DateUtils;
import org.knowm.xchange.vega.VegaAdapters;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class VegaMarketDataService extends VegaMarketDataServiceRaw implements MarketDataService {

    private Map<CurrencyPair, String> currencyPairs;

    public VegaMarketDataService(Exchange exchange) {
        super(exchange);
    }

    public Map<CurrencyPair, String> getCurrencyPairs() {
        if (currencyPairs == null) {
            currencyPairs = new HashMap<>();

            for (Markets.Market market : this.getAllMarkets()) {
                currencyPairs.put(VegaAdapters.adaptMarketToCurrencyPair(market), market.getId());
            }
        }

        return currencyPairs;
    }

    public List<CurrencyPair> getExchangeSymbols() {
        return new ArrayList<>(getCurrencyPairs().keySet());
    }

    @Override
    public Ticker getTicker(CurrencyPair currencyPair, Object... args) throws IOException {
        String marketId = getCurrencyPairs().get(currencyPair);

        Trading.MarketDepthResponse marketDepth = this.getMarketDepth(marketId, 1);
        Vega.MarketData marketData = this.getMarketData(marketId);
        List<Vega.Candle> dayCandles = this.getCandles(marketId, Vega.Interval.INTERVAL_I1D, org.apache.commons.lang3.time.DateUtils.addDays(new Date(),-1));

        Vega.Trade lastTrade = marketDepth.getLastTrade();

        return new Ticker.Builder()
                .instrument(currencyPair)
                .last(BigDecimal.valueOf(lastTrade.getPrice()))
//                .open(stats.getOpen())
                .high(BigDecimal.valueOf(marketData.getBestBidPrice()))
                .low(BigDecimal.valueOf(marketData.getBestOfferPrice()))
                .bid(BigDecimal.valueOf(marketDepth.getBuy(0).getPrice()))
                .ask(BigDecimal.valueOf(marketDepth.getSell(0).getPrice()))
                .volume(BigDecimal.valueOf(marketData.getIndicativeVolume()))
                .timestamp(parseDate(lastTrade.getTimestamp()))
                .percentageChange(BigDecimal.valueOf((dayCandles.get(0).getClose() - dayCandles.get(1).getClose()) / dayCandles.get(1).getClose() * 100))
                .build();
    }

    @Override
    public OrderBook getOrderBook(CurrencyPair currencyPair, Object... args) throws IOException {
        List<LimitOrder> bidOrders = new ArrayList<>();
        List<LimitOrder> askOrders = new ArrayList<>();

        List<Vega.Order> orders = this.getOrders(getCurrencyPairs().get(currencyPair));

        for (Vega.Order order : orders) {
            if (order.getSide() == Vega.Side.SIDE_BUY) {
                bidOrders.add(
                        new LimitOrder.Builder(Order.OrderType.BID, currencyPair)
                            .originalAmount(BigDecimal.valueOf(order.getSize()))
                            .id(order.getId())
                            .timestamp(parseDate(order.getCreatedAt()))
                            .averagePrice(BigDecimal.valueOf(order.getPrice()))
                            .build()
                );
            } else if (order.getSide() == Vega.Side.SIDE_SELL) {
                askOrders.add(
                        new LimitOrder.Builder(Order.OrderType.ASK, currencyPair)
                                .originalAmount(BigDecimal.valueOf(order.getSize()))
                                .id(order.getId())
                                .timestamp(parseDate(order.getCreatedAt()))
                                .averagePrice(BigDecimal.valueOf(order.getPrice()))
                                .build()
                );
            }
        }

        return new OrderBook(null, askOrders, bidOrders);
    }

    @Override
    public Trades getTrades(CurrencyPair currencyPair, Object... args) throws IOException {
        List<Vega.Trade> vegaTrades = this.getTrades(getCurrencyPairs().get(currencyPair));
        List<Trade> trades = new ArrayList<>(vegaTrades.size());

        for (Vega.Trade trade : vegaTrades) {
            trades.add(VegaAdapters.adaptTrade(trade, currencyPair));
        }

        return new Trades(trades);
    }

    private Date parseDate(long timestamp) {
        return DateUtils.fromUnixTime(timestamp);
    }
}
