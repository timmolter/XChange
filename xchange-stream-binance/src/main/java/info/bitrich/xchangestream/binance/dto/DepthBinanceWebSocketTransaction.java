package info.bitrich.xchangestream.binance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.knowm.xchange.binance.dto.marketdata.BinanceOrderbook;

public class DepthBinanceWebSocketTransaction extends ProductBinanceWebSocketTransaction {

  private final BinanceOrderbook orderBook;
  private final BinanceRawOrderBookUpdate rawDepthUpdate;
  private final long lastUpdateId;
  private final long firstUpdateId;

  public DepthBinanceWebSocketTransaction(
      @JsonProperty("e") String eventType,
      @JsonProperty("E") String eventTime,
      @JsonProperty("s") String symbol,
      @JsonProperty("U") long firstUpdateId,
      @JsonProperty("u") long lastUpdateId,
      @JsonProperty("b") List<Object[]> _bids,
      @JsonProperty("a") List<Object[]> _asks) {
    super(eventType, eventTime, symbol);
    this.firstUpdateId = firstUpdateId;
    this.lastUpdateId = lastUpdateId;
    orderBook = new BinanceOrderbook(lastUpdateId, _bids, _asks);
    rawDepthUpdate = new BinanceRawOrderBookUpdate(eventType, eventTime, symbol,
            firstUpdateId, lastUpdateId, _bids, _asks);
  }

  public BinanceOrderbook getOrderBook() {
    return orderBook;
  }

  public long getFirstUpdateId() {
    return firstUpdateId;
  }

  public long getLastUpdateId() {
    return lastUpdateId;
  }

  public BinanceRawOrderBookUpdate getRawDepthUpdate() { return rawDepthUpdate; }
}
