package org.knowm.xchange.deribit.v2.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class DeribitOrderBook {

  private final TreeMap<BigDecimal, BigDecimal> bids = new TreeMap<>((k1, k2) -> -k1.compareTo(k2));
  private final TreeMap<BigDecimal, BigDecimal> asks = new TreeMap<>();

  /** Underlying price for implied volatility calculations (options only) */
  @JsonProperty("underlying_price")
  private BigDecimal underlyingPrice;

  /** Name of the underlying future, or index_price (options only) */
  @JsonProperty("underlying_index")
  private String underlyingIndex;

  /** The timestamp (seconds since the Unix epoch, with millisecond precision) */
  @JsonProperty("timestamp")
  private long timestamp;

  @JsonProperty("stats")
  private DeribitStats stats;

  /** The state of the order book. Possible values are open and closed. */
  @JsonProperty("state")
  private String state;

  /** The settlement price for the instrument. Only when state = open */
  @JsonProperty("settlement_price")
  private BigDecimal settlementPrice;

  /**
   * The total amount of outstanding contracts in the corresponding amount units. For perpetual and
   * futures the amount is in USD units, for options it is amount of corresponding cryptocurrency
   * contracts, e.g., BTC or ETH.
   */
  @JsonProperty("open_interest")
  private BigDecimal openInterest;

  /**
   * The minimum price for the future. Any sell orders you submit lower than this price will be
   * clamped to this minimum.
   */
  @JsonProperty("min_price")
  private BigDecimal minPrice;

  /**
   * The maximum price for the future. Any buy orders you submit higher than this price, will be
   * clamped to this maximum.
   */
  @JsonProperty("max_price")
  private BigDecimal maxPrice;

  /** The mark price for the instrument */
  @JsonProperty("mark_price")
  private BigDecimal markPrice;

  /** (Only for option) implied volatility for mark price */
  @JsonProperty("mark_iv")
  private BigDecimal markIv;

  /** The price for the last trade */
  @JsonProperty("last_price")
  private BigDecimal lastPrice;

  /** Interest rate used in implied volatility calculations (options only) */
  @JsonProperty("interest_rate")
  private BigDecimal interestRate;

  /** Unique instrument identifier */
  @JsonProperty("instrument_name")
  private String instrumentName;

  /** Current index price */
  @JsonProperty("index_price")
  private BigDecimal indexPrice;

  /** (Only for option) */
  @JsonProperty("greeks")
  private DeribitGreeks greeks;

  /** The settlement price for the instrument. Only when state = closed */
  @JsonProperty("delivery_price")
  private BigDecimal deliveryPrice;

  /** Funding 8h (perpetual only) */
  @JsonProperty("funding_8h")
  private BigDecimal funding8h;

  /** Current funding (perpetual only) */
  @JsonProperty("current_funding")
  private BigDecimal currentFunding;

  /** id of the notification */
  @JsonProperty("change_id")
  private long changeId;

  /** The current best bid price, null if there aren't any bids */
  @JsonProperty("best_bid_price")
  private BigDecimal bestBidPrice;

  /** It represents the requested order size of all best bids */
  @JsonProperty("best_bid_amount")
  private BigDecimal bestBidAmount;

  /** The current best ask price, null if there aren't any asks */
  @JsonProperty("best_ask_price")
  private BigDecimal bestAskPrice;

  /** It represents the requested order size of all best asks */
  @JsonProperty("best_ask_amount")
  private BigDecimal bestAskAmount;

  /** (Only for option) implied volatility for best ask */
  @JsonProperty("ask_iv")
  private BigDecimal askIv;

  /** (Only for option) implied volatility for best bid */
  @JsonProperty("bid_iv")
  private BigDecimal bidIv;

  /** array of [price, amount] List of bids */
  @JsonProperty("bids")
  public void setBids(List<List<BigDecimal>> bids) {
    convertOrders(bids, this.bids);
  }

  /** array of [price, amount], List of asks */
  @JsonProperty("asks")
  public void setAsks(List<List<BigDecimal>> asks) {
    convertOrders(asks, this.asks);
  }

  public Date getTimestamp() {
    return new Date(timestamp);
  }

  private static void convertOrders(
      List<List<BigDecimal>> from, TreeMap<BigDecimal, BigDecimal> to) {
    from.forEach(l -> to.put(l.get(0), l.get(1)));
  }
}
