package org.knowm.xchange.poloniex.dto.trade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class PoloniexTradeTest {

  @Test
  public void testTradeHistoryMultiPair()
      throws JsonParseException, JsonMappingException, IOException {

    final InputStream is =
        PoloniexTrade.class.getResourceAsStream(
            "/org/knowm/xchange/poloniex/dto/trade/trade-history-multi-pair.json");

    final ObjectMapper mapper = new ObjectMapper();
    final JavaType stringType = mapper.getTypeFactory().constructType(String.class, String.class);
    final JavaType tradeArray = mapper.getTypeFactory().constructArrayType(PoloniexTrade.class);
    final JavaType multiPairTradeType =
        mapper.getTypeFactory().constructMapType(HashMap.class, stringType, tradeArray);

    final Map<String, PoloniexTrade[]> tradeHistory = mapper.readValue(is, multiPairTradeType);
    assertThat(tradeHistory).hasSize(2);

    assertThat(tradeHistory).containsKey("BTC_LTC");
    assertThat(tradeHistory.get("BTC_LTC")).hasSize(2);

    assertThat(tradeHistory).containsKey("BTC_DRK");
    assertThat(tradeHistory.get("BTC_DRK")).hasSize(1);

    PoloniexTrade trade = tradeHistory.get("BTC_DRK")[0];
    assertThat(trade.getTradeID()).isEqualTo("296610");
    assertThat(trade.getDate()).isEqualTo("2014-09-14 04:54:57");
    assertThat(trade.getRate()).isEqualTo("0.00583818");
    assertThat(trade.getAmount()).isEqualTo("0.03510854");
    assertThat(trade.getTotal()).isEqualTo("0.00020497");
    assertTrue(trade.getFee().compareTo(BigDecimal.valueOf(0.002)) == 0);
    assertThat(trade.getOrderNumber()).isEqualTo("19961972");
    assertThat(trade.getType()).isEqualTo("buy");
  }

  @Test
  public void testTradeHistorySinglePair()
      throws JsonParseException, JsonMappingException, IOException {

    final InputStream is =
        PoloniexTrade.class.getResourceAsStream(
            "/org/knowm/xchange/poloniex/dto/trade/trade-history-single-pair.json");

    final ObjectMapper mapper = new ObjectMapper();
    final JavaType tradeArray = mapper.getTypeFactory().constructArrayType(PoloniexTrade.class);

    PoloniexTrade[] tradeHistory = mapper.readValue(is, tradeArray);
    assertThat(tradeHistory).hasSize(3);

    PoloniexTrade trade = tradeHistory[0];
    assertThat(trade.getTradeID()).isEqualTo("267356");
    assertThat(trade.getDate()).isEqualTo("2014-09-12 00:22:32");
    assertThat(trade.getRate()).isEqualTo("0.01026896");
    assertThat(trade.getAmount()).isEqualTo("0.01000000");
    assertThat(trade.getTotal()).isEqualTo("0.00010269");
    assertTrue(trade.getFee().compareTo(BigDecimal.valueOf(0.002)) == 0);
    assertThat(trade.getOrderNumber()).isEqualTo("17730787");
    assertThat(trade.getType()).isEqualTo("sell");
  }
}
