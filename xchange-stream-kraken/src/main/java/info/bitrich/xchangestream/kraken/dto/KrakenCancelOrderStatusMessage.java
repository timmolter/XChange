package info.bitrich.xchangestream.kraken.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import info.bitrich.xchangestream.kraken.dto.enums.KrakenCancelOrderStatus;
import info.bitrich.xchangestream.kraken.dto.enums.KrakenEventType;
/** @author marcinrabiej */
public class KrakenCancelOrderStatusMessage extends KrakenEvent {

  private final KrakenCancelOrderStatus status;
  private final String errorMessage;
  private final Integer reqid;

  @JsonCreator
  public KrakenCancelOrderStatusMessage(
      @JsonProperty("event") KrakenEventType event,
      @JsonProperty("status") KrakenCancelOrderStatus status,
      @JsonProperty("reqid") Integer reqid,
      @JsonProperty("errorMessage") String errorMessage) {
    super(event);
    this.status = status;
    this.reqid = reqid;
    this.errorMessage = errorMessage;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public KrakenCancelOrderStatus getStatus() {
    return status;
  }

  public Integer getReqid() {
    return reqid;
  }
}
