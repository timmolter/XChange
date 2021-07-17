package org.knowm.xchange.bitmax.dto.marketdata;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.math.BigDecimal;

@JsonDeserialize(using = BitmaxPublicOrder.BitmaxOrderDeserializer.class)
public class BitmaxPublicOrder {

  private final BigDecimal price;
  private final BigDecimal volume;

  public BitmaxPublicOrder(BigDecimal price, BigDecimal volume) {

    this.price = price;
    this.volume = volume;
  }

  public BigDecimal getPrice() {

    return price;
  }

  public BigDecimal getVolume() {

    return volume;
  }

  @Override
  public String toString() {
    return "BitmaxPublicOrder{" + "price=" + price + ", volume=" + volume + '}';
  }

  static class BitmaxOrderDeserializer extends JsonDeserializer<BitmaxPublicOrder> {

    @Override
    public BitmaxPublicOrder deserialize(JsonParser jsonParser, DeserializationContext ctxt)
        throws IOException {

      ObjectCodec oc = jsonParser.getCodec();
      JsonNode node = oc.readTree(jsonParser);
      if (node.isArray()) {
        BigDecimal price = new BigDecimal(node.path(0).asText());
        BigDecimal volume = new BigDecimal(node.path(1).asText());

        return new BitmaxPublicOrder(price, volume);
      }

      return null;
    }
  }
}
