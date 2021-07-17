package org.knowm.xchange.vega.service;

import io.vegaprotocol.vega.Markets;
import io.vegaprotocol.vega.Vega;
import io.vegaprotocol.vega.api.Trading;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.utils.DateUtils;

import java.util.Date;
import java.util.List;

public class VegaMarketDataServiceRaw extends VegaBaseService {

    protected VegaMarketDataServiceRaw(Exchange exchange) {
        super(exchange);
    }

    public List<Vega.Trade> getTrades(String marketId) {
        return this.tradingDataService.tradesByMarket(
                Trading.TradesByMarketRequest.newBuilder()
                        .setMarketId(marketId)
                        .build()
        ).getTradesList();
    }

    public List<Vega.Order> getOrders(String marketId) {
        return this.tradingDataService.ordersByMarket(
                Trading.OrdersByMarketRequest.newBuilder()
                        .setMarketId(marketId)
                        .build()
        ).getOrdersList();
    }

    public Trading.MarketDepthResponse getMarketDepth(String marketId, long maxDepth) {
        return this.tradingDataService.marketDepth(
                Trading.MarketDepthRequest.newBuilder()
                        .setMarketId(marketId)
                        .setMaxDepth(maxDepth)
                        .build()
        );
    }

    public List<Markets.Market> getAllMarkets() {
        return this.tradingDataService.markets(
                Trading.MarketsRequest.newBuilder().build()
        ).getMarketsList();
    }

    public Vega.MarketData getMarketData(String marketId) {
        return this.tradingDataService.marketDataByID(
                Trading.MarketDataByIDRequest.newBuilder()
                        .setMarketId(marketId)
                        .build()
        ).getMarketData();
    }

    public List<Vega.Candle> getCandles(String marketId, Vega.Interval interval, Date since) {
        return this.tradingDataService.candles(
                Trading.CandlesRequest.newBuilder()
                        .setMarketId(marketId)
                        .setInterval(interval)
                        .setSinceTimestamp(DateUtils.toUnixTime(since) * 100000000)
                        .build()
        ).getCandlesList();
    }
}
