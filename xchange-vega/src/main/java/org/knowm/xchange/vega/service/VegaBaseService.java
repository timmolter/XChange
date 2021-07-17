package org.knowm.xchange.vega.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.vegaprotocol.vega.api.TradingDataServiceGrpc;
import io.vegaprotocol.vega.api.TradingServiceGrpc;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.service.BaseExchangeService;
import org.knowm.xchange.service.BaseService;

public class VegaBaseService extends BaseExchangeService implements BaseService {
    protected final TradingServiceGrpc.TradingServiceBlockingStub tradingService;
    protected final TradingDataServiceGrpc.TradingDataServiceBlockingStub tradingDataService;

    protected VegaBaseService(Exchange exchange) {
        super(exchange);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(
                    exchange.getExchangeSpecification().getHost(),
                    exchange.getExchangeSpecification().getPort()
                )
                .usePlaintext()
                .build();

        tradingService = TradingServiceGrpc.newBlockingStub(channel);
        tradingDataService = TradingDataServiceGrpc.newBlockingStub(channel);
    }
}
