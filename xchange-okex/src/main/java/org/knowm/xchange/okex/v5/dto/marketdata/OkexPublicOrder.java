package org.knowm.xchange.okex.v5.dto.marketdata;

import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = OkexPublicOrder.okexOrderDeserializer.class)
public class OkexPublicOrder {

  private final BigDecimal price;
  private final BigDecimal volume;
  private final Integer liquidatedOrders;
  private final Integer activeOrders;

  public OkexPublicOrder(
      BigDecimal price, BigDecimal volume, Integer liquidatedOrders, Integer activeOrders) {

    this.price = price;
    this.volume = volume;
    this.liquidatedOrders = liquidatedOrders;
    this.activeOrders = activeOrders;
  }

  public BigDecimal getPrice() {

    return price;
  }

  public BigDecimal getVolume() {

    return volume;
  }

  @Override
  public String toString() {
    return "OkexPublicOrder{"
        + "price="
        + price
        + ", volume="
        + volume
        + ", liquidatedOrders="
        + liquidatedOrders
        + ", activeOrders="
        + activeOrders
        + '}';
  }

  static class okexOrderDeserializer extends JsonDeserializer<OkexPublicOrder> {

    @Override
    public OkexPublicOrder deserialize(JsonParser jsonParser, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {

      ObjectCodec oc = jsonParser.getCodec();
      JsonNode node = oc.readTree(jsonParser);
      if (node.isArray()) {
        BigDecimal price = new BigDecimal(node.path(0).asText());
        BigDecimal volume = new BigDecimal(node.path(1).asText());
        Integer liquidatedOrders = new Integer(node.path(2).asText());
        Integer activeOrders = new Integer(node.path(3).asText());

        return new OkexPublicOrder(price, volume, liquidatedOrders, activeOrders);
      }

      return null;
    }
  }
}
