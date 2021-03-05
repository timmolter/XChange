package info.bitrich.xchangestream.btcmarkets.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Date;

public class BTCMarketsWebSocketTickerMessage {
    public final String marketId;

    public final Date timestamp;

    public final BigDecimal bestBid;

    public final BigDecimal bestAsk;

    public final BigDecimal lastPrice;

    public final BigDecimal highPrice;

    public final BigDecimal lowPrice;

    public final BigDecimal volume24h;

    public final String messageType;

    public BTCMarketsWebSocketTickerMessage(
            @JsonProperty("marketId") String marketId,
            @JsonProperty("timestamp") Date timestamp,
            @JsonProperty("bestBid") BigDecimal bestBid,
            @JsonProperty("bestAsk") BigDecimal bestAsk,
            @JsonProperty("lastPrice") BigDecimal lastPrice,
            @JsonProperty("low24h") BigDecimal lowPrice,
            @JsonProperty("high24h") BigDecimal highPrice,
            @JsonProperty("volume24h") BigDecimal volume24h,
            @JsonProperty("messageType") String messageType) {
        this.marketId = marketId;
        this.timestamp = timestamp;
        this.bestBid = bestBid;
        this.bestAsk = bestAsk;
        this.lastPrice = lastPrice;
        this.volume24h = volume24h;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.messageType = messageType;
    }
}
