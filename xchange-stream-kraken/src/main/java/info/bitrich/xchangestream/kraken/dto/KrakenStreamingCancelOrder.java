package info.bitrich.xchangestream.kraken.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import info.bitrich.xchangestream.kraken.dto.enums.KrakenEventType;

import java.util.Collections;
import java.util.List;
/** @author marcinrabiej */
public class KrakenStreamingCancelOrder extends KrakenEvent {
  @JsonProperty
  private String token;
  @JsonProperty
  private List<String> txid;
  @JsonProperty
  private Integer reqid;

  public KrakenStreamingCancelOrder(String orderId, Integer reqid) {
    super(KrakenEventType.cancelOrder);
    txid = Collections.singletonList(orderId);
    this.reqid = reqid;
  }

  public Integer getReqid() {
    return reqid;
  }

  public void setToken(String token) {
    this.token = token;
  }
}
