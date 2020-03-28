package org.knowm.xchange.okcoin.v3.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import org.knowm.xchange.okcoin.OkexExchangeV3;

public class OkexDepth {

  public final SortedMap<BigDecimal, BigDecimal> bids;
  public final SortedMap<BigDecimal, BigDecimal> asks;
  private Date timestamp;

  public OkexDepth(
      @JsonProperty("bids") List<Object[]> bidsJson,
      @JsonProperty("asks") List<Object[]> asksJson,
      @JsonProperty(required = false, value = "timestamp") String timestamp) {

    BiConsumer<Object[], Map<BigDecimal, BigDecimal>> entryProcessor =
        (obj, col) -> {
          BigDecimal price = new BigDecimal(obj[0].toString());
          BigDecimal qty = new BigDecimal(obj[1].toString());
          col.put(price, qty);
        };

    TreeMap<BigDecimal, BigDecimal> bids = new TreeMap<>((k1, k2) -> -k1.compareTo(k2));
    TreeMap<BigDecimal, BigDecimal> asks = new TreeMap<>();

    bidsJson.stream().forEach(e -> entryProcessor.accept(e, bids));
    asksJson.stream().forEach(e -> entryProcessor.accept(e, asks));

    this.bids = Collections.unmodifiableSortedMap(bids);
    this.asks = Collections.unmodifiableSortedMap(asks);

    try {
      this.timestamp = OkexExchangeV3.timestampFormatter.parse(timestamp);
    } catch (ParseException e) {
      this.timestamp = null;
    }
  }

  public Date getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {

    return "OkCoinDepth [asks=" + asks + ", bids=" + bids + "]";
  }
}
