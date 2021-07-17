package org.knowm.xchangestream.vega;

import info.bitrich.xchangestream.service.ConnectableService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.reactivex.Completable;
import io.vegaprotocol.vega.api.TradingDataServiceGrpc;
import io.vegaprotocol.vega.api.TradingServiceGrpc;
import org.knowm.xchange.ExchangeSpecification;

public class VegaStreamingService extends ConnectableService {
    protected TradingServiceGrpc.TradingServiceBlockingStub tradingService;
    protected TradingDataServiceGrpc.TradingDataServiceBlockingStub tradingDataService;
    protected TradingDataServiceGrpc.TradingDataServiceStub asyncTradingDataService;

    private final ManagedChannel channel;

    protected VegaStreamingService(ExchangeSpecification exchangeSpec) {
        channel = ManagedChannelBuilder.forAddress(
                exchangeSpec.getHost(),
                exchangeSpec.getPort()
        )
                .usePlaintext()
                .build();

    }

    @Override
    protected Completable openConnection() {
        return Completable.create(
                completable -> {
                    tradingService = TradingServiceGrpc.newBlockingStub(channel).withWaitForReady();
                    tradingDataService = TradingDataServiceGrpc.newBlockingStub(channel).withWaitForReady();
                    asyncTradingDataService = TradingDataServiceGrpc.newStub(channel).withWaitForReady();

                    completable.onComplete();
                });
    }
}
