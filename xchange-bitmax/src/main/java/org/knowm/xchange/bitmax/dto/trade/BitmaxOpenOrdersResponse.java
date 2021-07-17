package org.knowm.xchange.bitmax.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

public class BitmaxOpenOrdersResponse {

  private final BigDecimal avgPx;

  private final BigDecimal cumFee;

  private final BigDecimal cumFilledQty;

  private final String errorCode;

  private final String feeAsset;

  private final Date lastExecTime;

  private final String orderId;

  private final BigDecimal orderQty;

  private final BitmaxPlaceOrderRequestPayload.BitmaxOrderType orderType;

  private final BigDecimal price;

  private final Long seqNum;

  private final BitmaxPlaceOrderRequestPayload.BitmaxSide side;

  private final String status;

  private final BigDecimal stopPrice;

  private final String symbol;

  private final String execInst;

  public BitmaxOpenOrdersResponse(
      @JsonProperty("avgPx") BigDecimal avgPx,
      @JsonProperty("cumFee") BigDecimal cumFee,
      @JsonProperty("cumFilledQty") BigDecimal cumFilledQty,
      @JsonProperty("errorCode") String errorCode,
      @JsonProperty("feeAsset") String feeAsset,
      @JsonProperty("lastExecTime") Long lastExecTime,
      @JsonProperty("orderId") String orderId,
      @JsonProperty("orderQty") BigDecimal orderQty,
      @JsonProperty("orderType") String orderType,
      @JsonProperty("price") BigDecimal price,
      @JsonProperty("seqNum") Long seqNum,
      @JsonProperty("side") String side,
      @JsonProperty("status") String status,
      @JsonProperty("stopPrice") BigDecimal stopPrice,
      @JsonProperty("symbol") String symbol,
      @JsonProperty("execInst") String execInst) {
    this.avgPx = avgPx;
    this.cumFee = cumFee;
    this.cumFilledQty = cumFilledQty;
    this.errorCode = errorCode;
    this.feeAsset = feeAsset;
    this.lastExecTime = Date.from(Instant.ofEpochMilli(lastExecTime));
    this.orderId = orderId;
    this.orderQty = orderQty;
    this.orderType =
        BitmaxPlaceOrderRequestPayload.BitmaxOrderType.valueOf(orderType.toLowerCase());
    this.price = price;
    this.seqNum = seqNum;
    this.side = BitmaxPlaceOrderRequestPayload.BitmaxSide.valueOf(side.toLowerCase());
    this.status = status;
    this.stopPrice = stopPrice;
    this.symbol = symbol;
    this.execInst = execInst;
  }

  public BigDecimal getAvgPx() {
    return avgPx;
  }

  public BigDecimal getCumFee() {
    return cumFee;
  }

  public BigDecimal getCumFilledQty() {
    return cumFilledQty;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public String getFeeAsset() {
    return feeAsset;
  }

  public Date getLastExecTime() {
    return lastExecTime;
  }

  public String getOrderId() {
    return orderId;
  }

  public BigDecimal getOrderQty() {
    return orderQty;
  }

  public BitmaxPlaceOrderRequestPayload.BitmaxOrderType getOrderType() {
    return orderType;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public Long getSeqNum() {
    return seqNum;
  }

  public BitmaxPlaceOrderRequestPayload.BitmaxSide getSide() {
    return side;
  }

  public String getStatus() {
    return status;
  }

  public BigDecimal getStopPrice() {
    return stopPrice;
  }

  public String getSymbol() {
    return symbol;
  }

  public String getExecInst() {
    return execInst;
  }

  @Override
  public String toString() {
    return "BitmaxOpenOrdersResponse{"
        + "avgPx="
        + avgPx
        + ", cumFee="
        + cumFee
        + ", cumFilledQty="
        + cumFilledQty
        + ", errorCode='"
        + errorCode
        + '\''
        + ", feeAsset='"
        + feeAsset
        + '\''
        + ", lastExecTime="
        + lastExecTime
        + ", orderId='"
        + orderId
        + '\''
        + ", orderQty="
        + orderQty
        + ", orderType='"
        + orderType
        + '\''
        + ", price="
        + price
        + ", seqNum="
        + seqNum
        + ", side='"
        + side
        + '\''
        + ", status='"
        + status
        + '\''
        + ", stopPrice="
        + stopPrice
        + ", symbol='"
        + symbol
        + '\''
        + ", execInst='"
        + execInst
        + '\''
        + '}';
  }
}
