package org.knowm.xchange.gateio.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Trade;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
/** Author: Max Gao (gaamox@tutanota.com) Created: 05-05-2021 */
public class GateioTradesResponse extends GateioWebSocketTransaction {
  @JsonProperty("result")
  private Result result;

  @Getter
  @Setter
  public class Result {
    @JsonProperty("id")
    private long id;

    @JsonProperty("create_time")
    private long createTimeSeconds;

    @JsonProperty("create_time_ms")
    private String createTimeMilliseconds;

    @JsonProperty("side")
    private String side;

    @JsonProperty("currency_pair")
    private String currencyPair;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("price")
    private String price;
  }

  @Override
  public CurrencyPair getCurrencyPair() {
    return new CurrencyPair(result.getCurrencyPair().replace('_', '/'));
  }

  public Trade toTrade() {
    return new Trade(
        "sell".equals(this.result.side) ? Order.OrderType.ASK : Order.OrderType.BID,
        new BigDecimal(result.amount),
        getCurrencyPair(),
        new BigDecimal(result.price),
        new Date(new BigDecimal(result.createTimeMilliseconds).longValue()),
        Long.toString(result.id),
        null,
        null);
  }
}
