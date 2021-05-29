package info.bitrich.xchangestream.ftx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Streams;
import info.bitrich.xchangestream.ftx.dto.FtxOrderbookResponse;
import info.bitrich.xchangestream.ftx.dto.FtxTickerResponse;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.instrument.Instrument;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.instrument.Instrument;

import java.util.List;
import java.util.zip.CRC32;

public class FtxStreamingAdapters {

  private static final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();

  public static OrderBook adaptOrderbookMessage(
      OrderBook orderBook, Instrument instrument, JsonNode jsonNode) {

    Streams.stream(jsonNode)
        .filter(JsonNode::isObject)
        .map(
            res -> {
              try {
                return mapper.readValue(res.toString(), FtxOrderbookResponse.class);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
        .forEach(
            message -> {
              if ("partial".equals(message.getAction())) {
                message
                    .getAsks()
                    .forEach(
                        ask ->
                            orderBook
                                .getAsks()
                                .add(
                                    new LimitOrder.Builder(Order.OrderType.ASK, instrument)
                                        .limitPrice(ask.get(0))
                                        .originalAmount(ask.get(1))
                                        .build()));

                message
                    .getBids()
                    .forEach(
                        bid ->
                            orderBook
                                .getBids()
                                .add(
                                    new LimitOrder.Builder(Order.OrderType.BID, instrument)
                                        .limitPrice(bid.get(0))
                                        .originalAmount(bid.get(1))
                                        .build()));
              } else {
                message
                    .getAsks()
                    .forEach(
                        ask ->
                            orderBook.update(
                                new LimitOrder.Builder(Order.OrderType.ASK, instrument)
                                    .limitPrice(ask.get(0))
                                    .originalAmount(ask.get(1))
                                    .build()));
                message
                    .getBids()
                    .forEach(
                        bid ->
                            orderBook.update(
                                new LimitOrder.Builder(Order.OrderType.BID, instrument)
                                    .limitPrice(bid.get(0))
                                    .originalAmount(bid.get(1))
                                    .build()));
              }
            });

    return new OrderBook(
        Date.from(Instant.now()),
        new ArrayList<>(orderBook.getAsks()),
        new ArrayList<>(orderBook.getBids()),
        true);
  }

  public static UserTrade adaptUserTrade(JsonNode jsonNode) {
    return new UserTrade.Builder()
        .currencyPair(new CurrencyPair(jsonNode.get("data").get("market").asText()))
        .type(
            jsonNode.get("data").get("side").asText().equals("buy")
                ? Order.OrderType.BID
                : Order.OrderType.ASK)
        .instrument(new CurrencyPair(jsonNode.get("data").get("market").asText()))
        .originalAmount(BigDecimal.valueOf(jsonNode.get("data").get("size").asDouble()))
        .price(BigDecimal.valueOf(jsonNode.get("data").get("price").asDouble()))
        .timestamp(Date.from(Instant.ofEpochMilli(jsonNode.get("data").get("time").asLong())))
        .id(jsonNode.get("data").get("id").asText())
        .orderId(jsonNode.get("data").get("orderId").asText())
        .feeAmount(BigDecimal.valueOf(jsonNode.get("data").get("fee").asDouble()))
        .feeCurrency(new Currency(jsonNode.get("data").get("feeCurrency").asText()))
        .build();
  }

  public static Ticker adaptTickerMessage(Instrument instrument, JsonNode jsonNode) {
    return Streams.stream(jsonNode)
        .filter(JsonNode::isObject)
        .map(
            res -> {
              try {
                return mapper.readValue(res.toString(), FtxTickerResponse.class);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
        .map(ftxTickerResponse -> ftxTickerResponse.toTicker(instrument))
        .findFirst()
        .orElse(new Ticker.Builder().build());
  }
}
