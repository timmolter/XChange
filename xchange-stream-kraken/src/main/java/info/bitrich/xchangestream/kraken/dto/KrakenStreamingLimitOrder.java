package info.bitrich.xchangestream.kraken.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import info.bitrich.xchangestream.kraken.dto.enums.KrakenEventType;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.kraken.dto.trade.KrakenOrderFlags;
import org.knowm.xchange.kraken.dto.trade.KrakenType;
/** @author marcinrabiej */
public class KrakenStreamingLimitOrder extends KrakenEvent {
  @JsonProperty
  private String token;
  @JsonProperty
  private String pair;
  @JsonProperty
  private String type;
  @JsonProperty
  private String ordertype;
  @JsonProperty
  private String price;
  @JsonProperty
  private String volume;
  @JsonProperty
  private String leverage;
  @JsonProperty
  private String userref;
  @JsonProperty
  private String oflags;
  @JsonProperty
  private Integer reqid;

  public KrakenStreamingLimitOrder(LimitOrder order) {
    super(KrakenEventType.addOrder);
    this.pair = order.getCurrencyPair().base + "/" + order.getCurrencyPair().counter;
    this.type = KrakenType.fromOrderType(order.getType()).toString();
    this.ordertype = "limit";
    this.price = order.getLimitPrice().stripTrailingZeros().toPlainString();
    this.volume = order.getOriginalAmount().stripTrailingZeros().toPlainString();
    this.leverage = order.getLeverage();
    if (order.getOrderFlags().contains(KrakenOrderFlags.POST)) {
      this.oflags = "post";
    }
    this.userref = order.getUserReference();
    this.reqid = Integer.parseInt(order.getUserReference());
  }

  public Integer getReqid() {
    return reqid;
  }

  public void setToken(String token) {
    this.token = token;
  }
}
