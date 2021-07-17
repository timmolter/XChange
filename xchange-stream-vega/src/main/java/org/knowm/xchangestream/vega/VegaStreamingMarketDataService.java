package org.knowm.xchangestream.vega;

import info.bitrich.xchangestream.core.StreamingMarketDataService;
import io.grpc.stub.StreamObserver;
import io.reactivex.Observable;
import io.vegaprotocol.vega.Markets;
import io.vegaprotocol.vega.Vega;
import io.vegaprotocol.vega.api.Trading;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.utils.DateUtils;
import org.knowm.xchange.vega.VegaAdapters;

import java.math.BigDecimal;
import java.util.*;

public class VegaStreamingMarketDataService implements StreamingMarketDataService {
    private final VegaStreamingService service;

    private Map<CurrencyPair, String> currencyPairs;

    public Map<CurrencyPair, String> getCurrencyPairs() {
        if (currencyPairs == null) {
            currencyPairs = new HashMap<>();

            List<Markets.Market> markets = service.tradingDataService.markets(
                    Trading.MarketsRequest.newBuilder().build()
            ).getMarketsList();

            for (Markets.Market market : markets) {
                currencyPairs.put(VegaAdapters.adaptMarketToCurrencyPair(market), market.getId());
            }
        }

        return currencyPairs;
    }

    public VegaStreamingMarketDataService(VegaStreamingService service) {
        this.service = service;
    }

    @Override
    public Observable<OrderBook> getOrderBook(CurrencyPair currencyPair, Object... args) {
        String marketId = getCurrencyPairs().get(currencyPair);

        return Observable.create(emitter -> {
            service.asyncTradingDataService.ordersSubscribe(
                    Trading.OrdersSubscribeRequest.newBuilder().setMarketId(marketId).build(),
                    new StreamObserver<Trading.OrdersSubscribeResponse>() {
                        @Override
                        public void onNext(Trading.OrdersSubscribeResponse response) {
                            emitter.onNext(VegaAdapters.adaptOrderBook(response.getOrdersList(), currencyPair));
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            emitter.onError(throwable);
                        }

                        @Override
                        public void onCompleted() {
                            emitter.onComplete();
                        }
                    });
        });
    }

    @Override
    public Observable<Ticker> getTicker(CurrencyPair currencyPair, Object... args) {
        String marketId = getCurrencyPairs().get(currencyPair);

        Observable<Vega.MarketDepth> marketDepthObservable = Observable.create(emitter -> {
            service.asyncTradingDataService.marketDepthSubscribe(
                    Trading.MarketDepthSubscribeRequest.newBuilder()
                            .setMarketId(marketId)
                            .build(),
                    new StreamObserver<Trading.MarketDepthSubscribeResponse>() {
                        @Override
                        public void onNext(Trading.MarketDepthSubscribeResponse response) {
                            emitter.onNext(response.getMarketDepth());
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            emitter.onError(throwable);
                        }

                        @Override
                        public void onCompleted() {
                            emitter.onComplete();
                        }
                    });
        });

        Observable<Vega.MarketData> marketDataObservable = Observable.create(emitter -> {
            service.asyncTradingDataService.marketsDataSubscribe(
                    Trading.MarketsDataSubscribeRequest.newBuilder()
                            .setMarketId(marketId)
                            .build(),
                    new StreamObserver<Trading.MarketsDataSubscribeResponse>() {
                        @Override
                        public void onNext(Trading.MarketsDataSubscribeResponse response) {
                            emitter.onNext(response.getMarketData());
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            emitter.onError(throwable);
                        }

                        @Override
                        public void onCompleted() {
                            emitter.onComplete();
                        }
                    });
        });

        Observable<List<Vega.Trade>> tradesObservable = Observable.create(emitter -> {
            service.asyncTradingDataService.tradesSubscribe(
                    Trading.TradesSubscribeRequest.newBuilder()
                            .setMarketId(marketId)
                            .build(),
                    new StreamObserver<Trading.TradesSubscribeResponse>() {
                        @Override
                        public void onNext(Trading.TradesSubscribeResponse response) {
                            emitter.onNext(response.getTradesList());
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            emitter.onError(throwable);
                        }

                        @Override
                        public void onCompleted() {
                            emitter.onComplete();
                        }
                    });
        });

        return Observable.combineLatest(marketDepthObservable, marketDataObservable, tradesObservable, (marketDepth, marketData, trades) -> {
            Vega.Trade lastTrade = trades.get(0);

            return new Ticker.Builder()
                    .instrument(currencyPair)
                    .last(BigDecimal.valueOf(lastTrade.getPrice()))
                    .high(BigDecimal.valueOf(marketData.getBestBidPrice()))
                    .low(BigDecimal.valueOf(marketData.getBestOfferPrice()))
                    .bid(BigDecimal.valueOf(marketDepth.getBuy(0).getPrice()))
                    .ask(BigDecimal.valueOf(marketDepth.getSell(0).getPrice()))
                    .volume(BigDecimal.valueOf(marketData.getIndicativeVolume()))
                    .timestamp(DateUtils.fromUnixTime(lastTrade.getTimestamp()))
                    .build();
        });
    }

    @Override
    public Observable<Trade> getTrades(CurrencyPair currencyPair, Object... args) {
        String marketId = getCurrencyPairs().get(currencyPair);

        return Observable.create(emitter -> {
            service.asyncTradingDataService.tradesSubscribe(
                    Trading.TradesSubscribeRequest.newBuilder()
                            .setMarketId(marketId)
                            .build(),
                    new StreamObserver<Trading.TradesSubscribeResponse>() {
                        @Override
                        public void onNext(Trading.TradesSubscribeResponse response) {
                            for (Vega.Trade trade : response.getTradesList()) {
                                emitter.onNext(VegaAdapters.adaptTrade(trade, currencyPair));
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            emitter.onError(throwable);
                        }

                        @Override
                        public void onCompleted() {
                            emitter.onComplete();
                        }
                    });
        });
    }
}
