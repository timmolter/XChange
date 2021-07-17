package info.bitrich.xchangestream.binance.dto;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiConsumer;

public class BinanceRawOrderBookUpdate {
    private final String eventType;
    private final String eventTime;
    private final String symbol;
    private final SortedMap<BigDecimal, BigDecimal> bids;
    private final SortedMap<BigDecimal, BigDecimal> asks;
    private final long firstUpdateId;
    private final long lastUpdateId;

    public BinanceRawOrderBookUpdate(String eventType,
                                     String eventTime,
                                     String symbol,
                                     long firstUpdateId,
                                     long lastUpdateId,
                                     List<Object[]> _bids,
                                     List<Object[]> _asks) {
        this.eventType = eventType;
        this.eventTime = eventTime;
        this.symbol = symbol;
        this.firstUpdateId = firstUpdateId;
        this.lastUpdateId = lastUpdateId;

        BiConsumer<Object[], Map<BigDecimal, BigDecimal>> entryProcessor =
        (obj, col) -> {
          BigDecimal price = new BigDecimal(obj[0].toString());
          BigDecimal qty = new BigDecimal(obj[1].toString());
          col.put(price, qty);
        };

        TreeMap<BigDecimal, BigDecimal> bids = new TreeMap<>((k1, k2) -> -k1.compareTo(k2));
        TreeMap<BigDecimal, BigDecimal> asks = new TreeMap<>();

        _bids.forEach(e -> entryProcessor.accept(e, bids));
        _asks.forEach(e -> entryProcessor.accept(e, asks));

        this.bids = Collections.unmodifiableSortedMap(bids);
        this.asks = Collections.unmodifiableSortedMap(asks);
    }

    public String getEventType() { return eventType; }

    public String getEventTime() { return eventTime; }

    public String getSymbol() { return symbol; }

    public long getFirstUpdateId() { return firstUpdateId; }

    public long getLastUpdateId() { return lastUpdateId; }

    public SortedMap<BigDecimal, BigDecimal> getBids() { return bids; }

    public SortedMap<BigDecimal, BigDecimal> getAsks() { return asks; }
}
