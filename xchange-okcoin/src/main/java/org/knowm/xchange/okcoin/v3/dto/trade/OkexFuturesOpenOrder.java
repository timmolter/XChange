package org.knowm.xchange.okcoin.v3.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import lombok.Data;

@Data
public class OkexFuturesOpenOrder {

  @JsonProperty("instrument_id")
  /** Contract ID, e.g. “BTC-USD-180213” */
  private String instrumentId;

  @JsonProperty("client_oid")
  /** String the order ID customised by yourself */
  private String clientOid;
  /** Quantity */
  private BigDecimal size;
  /** Order creation date */
  private String timestamp;

  @JsonProperty("filled_qty")
  /** Filled quantity */
  private BigDecimal filledQty;
  /** Fees */
  private BigDecimal fee;

  @JsonProperty("order_id")
  /** order ID */
  private String orderId;
  /** Order Price */
  private BigDecimal price;

  @JsonProperty("price_avg")
  /** Average price */
  private BigDecimal priceAvg;
  /** Type (1: open long 2: open short 3: close long 4: close short) */
  private FuturesSwapType type;
  /** Contract Value */
  @JsonProperty("contract_val")
  private BigDecimal contractVal;
  /** Leverage , 1-100x */
  private BigDecimal leverage;

  @JsonProperty("order_type")
  /** 0: Normal limit order 1: Post only 2: Fill Or Kill 3: Immediatel Or Cancel */
  private OrderPlacementType orderType;
  /** profit */
  private BigDecimal pnl;
  /**
   * Order Status("-2":Failed,"-1":Cancelled,"0":Open ,"1":Partially Filled, "2":Fully
   * Filled,"3":Submitting,"4":Cancelling,）
   */
  private String state;

  public Date getTimestamp() {
    return Date.from(Instant.parse(timestamp));
  }
}
