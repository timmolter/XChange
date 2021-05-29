package info.bitrich.xchangestream.ftx;

import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import info.bitrich.xchangestream.core.StreamingTradeService;
import info.bitrich.xchangestream.service.netty.ConnectionStateModel;
import io.reactivex.Completable;
import io.reactivex.Observable;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.ftx.FtxExchange;

public class FtxStreamingExchange extends FtxExchange implements StreamingExchange {

  private final String API_URI = "wss://ftx.com/ws/";

  private FtxStreamingService ftxStreamingService;
  private FtxStreamingMarketDataService ftxStreamingMarketDataService;
  private FtxStreamingTradeService ftxStreamingTradeService;

  @Override
  protected void initServices() {
    super.initServices();

    if (exchangeSpecification.getApiKey() != null) {
      this.ftxStreamingService = new FtxStreamingService(API_URI, exchangeSpecification);
      this.ftxStreamingTradeService = new FtxStreamingTradeService(ftxStreamingService);
    } else {
      this.ftxStreamingService = new FtxStreamingService(API_URI);
    }

    this.ftxStreamingMarketDataService = new FtxStreamingMarketDataService(ftxStreamingService);
  }

  @Override
  public Completable connect(ProductSubscription... args) {
    return ftxStreamingService.connect();
  }

  @Override
  public Completable disconnect() {
    return ftxStreamingService.disconnect();
  }

  @Override
  public boolean isAlive() {
    return ftxStreamingService.isSocketOpen();
  }

  @Override
  public Observable<Object> connectionSuccess() {
    return ftxStreamingService.subscribeConnectionSuccess();
  }

  @Override
  public Observable<Throwable> reconnectFailure() {
    return ftxStreamingService.subscribeReconnectFailure();
  }

  @Override
  public Observable<ConnectionStateModel.State> connectionStateObservable() {
    return ftxStreamingService.subscribeConnectionState();
  }

  @Override
  public ExchangeSpecification getDefaultExchangeSpecification() {
    ExchangeSpecification spec = super.getDefaultExchangeSpecification();
    spec.setShouldLoadRemoteMetaData(false);
    return spec;
  }

  @Override
  public StreamingMarketDataService getStreamingMarketDataService() {
    return ftxStreamingMarketDataService;
  }

  @Override
  public StreamingTradeService getStreamingTradeService() {
    return ftxStreamingTradeService;
  }

  @Override
  public void useCompressedMessages(boolean compressedMessages) {
    ftxStreamingService.useCompressedMessages(compressedMessages);
  }
}
