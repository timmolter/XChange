package org.knowm.xchange.gateio.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
/** Author: Max Gao (gaamox@tutanota.com) Created: 05-05-2021 */
public class GateioOrderBookResponse extends GateioWebSocketTransaction {
  @JsonProperty("result")
  private Result result;

  @Getter
  @Setter
  public class Result {
    @JsonProperty("t")
    private long updateTimeMilliseconds;

    @JsonProperty("lastUpdateId")
    private long lastUpdateId;

    @JsonProperty("s")
    private String currencyPair;

    @JsonProperty("bids")
    private String[][] bids; // Pre-sorted, ascending - [price, amount]

    @JsonProperty("asks")
    private String[][] asks; // Pre-sorted, descending - [price, amount]
  }

  @Override
  public CurrencyPair getCurrencyPair() {
    if (result.getCurrencyPair() == null) return null; // Expect to hit this on the success
    return new CurrencyPair(result.getCurrencyPair().replace('_', '/'));
  }

  public OrderBook toOrderBook(CurrencyPair currencyPair) {
    List<LimitOrder> bids =
        Arrays.stream(result.bids)
            .map(
                rawBid ->
                    new LimitOrder(
                        Order.OrderType.BID,
                        new BigDecimal(rawBid[1]),
                        currencyPair,
                        Long.toString(result.lastUpdateId),
                        null,
                        new BigDecimal(rawBid[0])))
            .collect(Collectors.toList());

    List<LimitOrder> asks =
        Arrays.stream(result.asks)
            .map(
                rawAsk ->
                    new LimitOrder(
                        Order.OrderType.ASK,
                        new BigDecimal(rawAsk[1]),
                        currencyPair,
                        Long.toString(result.lastUpdateId),
                        null,
                        new BigDecimal(rawAsk[0])))
            .collect(Collectors.toList());

    return new OrderBook(
        Date.from(Instant.ofEpochMilli(result.updateTimeMilliseconds)), asks, bids, false);
  }
}
