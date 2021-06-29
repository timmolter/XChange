package info.bitrich.xchangestream.kraken.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import info.bitrich.xchangestream.kraken.dto.enums.KrakenAddOrderStatus;
import info.bitrich.xchangestream.kraken.dto.enums.KrakenEventType;
/** @author marcinrabiej */
public class KrakenAddOrderStatusMessage extends KrakenEvent {

  private final String descr;
  private final KrakenAddOrderStatus status;
  private final String txid;
  private final String errorMessage;
  private final Integer reqid;

  @JsonCreator
  public KrakenAddOrderStatusMessage(
      @JsonProperty("event") KrakenEventType event,
      @JsonProperty("descr") String descr,
      @JsonProperty("status") KrakenAddOrderStatus status,
      @JsonProperty("txid") String txid,
      @JsonProperty("reqid") Integer reqid,
      @JsonProperty("errorMessage") String errorMessage) {
    super(event);
    this.descr = descr;
    this.status = status;
    this.txid = txid;
    this.reqid = reqid;
    this.errorMessage = errorMessage;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public KrakenAddOrderStatus getStatus() {
    return status;
  }

  public String getTxid() {
    return txid;
  }

  public Integer getReqid() {
    return reqid;
  }
}
