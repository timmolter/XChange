package info.bitrich.xchangestream.ftx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Streams;
import info.bitrich.xchangestream.ftx.dto.FtxOrderbookResponse;
import info.bitrich.xchangestream.ftx.dto.FtxTickerResponse;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.ftx.FtxAdapters;
import org.knowm.xchange.ftx.dto.marketdata.FtxTradeDto;
import org.knowm.xchange.instrument.Instrument;

public class FtxStreamingAdapters {

  private static final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();
  /** Incoming values always has 1 trailing 0 after the decimal, and start with 1 zero */
  private static final DecimalFormat df = new DecimalFormat("0.0########");  // 10 decimal places

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

              if (orderBook.getAsks().size() > 0 && orderBook.getBids().size() > 0) {
                Long calculatedChecksum =
                    getOrderbookChecksum(orderBook.getAsks(), orderBook.getBids());

                if (!calculatedChecksum.equals(message.getChecksum())) {
                  throw new RuntimeException("Checksum is not correct!");
                }
              }
            });

    return new OrderBook(
        Date.from(Instant.now()),
        new ArrayList<>(orderBook.getAsks()),
        new ArrayList<>(orderBook.getBids()),
        true);
  }

  public static Long getOrderbookChecksum(List<LimitOrder> asks, List<LimitOrder> bids) {
    StringBuilder data = new StringBuilder(3072);
    for (int i = 0; i < 100; i++) {
      if (bids.size() > i) {
        data.append(df.format(bids.get(i).getLimitPrice()))
            .append(":")
            .append(df.format(bids.get(i).getOriginalAmount()))
            .append(":");
      }

      if (asks.size() > i) {
        data.append(df.format(asks.get(i).getLimitPrice()))
            .append(":")
            .append(df.format(asks.get(i).getOriginalAmount()))
            .append(":");
      }
    }
    
    String s = data.length() > 0 ? data.substring(0, data.length() - 1) : data.toString(); // strip last :
    
    CRC32 crc32 = new CRC32();
    byte[] toBytes = s.getBytes(StandardCharsets.UTF_8);
    crc32.update(toBytes, 0, toBytes.length);

    return crc32.getValue();
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

  public static Iterable<Trade> adaptTradesMessage(Instrument instrument, JsonNode jsonNode) {
    return Streams.stream(jsonNode)
        .filter(JsonNode::isArray)
        .map(res -> (ArrayNode) res)
        .flatMap(Streams::stream)
        .map(
            tradeNode -> {
              try {
                return mapper.readValue(tradeNode.toString(), FtxTradeDto.class);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
        .map(
            ftxTradeDto ->
                new Trade.Builder()
                    .timestamp(ftxTradeDto.getTime())
                    .instrument(instrument)
                    .id(ftxTradeDto.getId())
                    .price(ftxTradeDto.getPrice())
                    .type(FtxAdapters.adaptFtxOrderSideToOrderType(ftxTradeDto.getSide()))
                    .originalAmount(ftxTradeDto.getSize())
                    .build())
        .collect(Collectors.toList());
  }

  public static UserTrade adaptUserTrade(JsonNode jsonNode) {
    return new UserTrade.Builder()
            .currencyPair(new CurrencyPair(jsonNode.get("data").get("market").asText()))
            .type(
                    "buy".equals(jsonNode.get("data").get("side").asText())
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
}
