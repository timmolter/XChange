package org.knowm.xchange.bitmax;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.knowm.xchange.bitmax.dto.BitmaxResponse;
import org.knowm.xchange.bitmax.dto.trade.BitmaxOpenOrdersResponse;
import org.knowm.xchange.bitmax.dto.trade.BitmaxOrderResponse;
import org.knowm.xchange.utils.jackson.CurrencyPairDeserializer;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BitmaxTradeDataJSONTest {

      @Test
      public void bitmaxPlaceOrderResponseTest() throws IOException {

            // Read in the JSON from the example resources
            InputStream is =
            BitmaxTradeDataJSONTest.class.getResourceAsStream(
                "/org/knowm/xchange/bitmax/bitmaxPlaceOrderResponseExample.json");

            // Use Jackson to parse it
            ObjectMapper mapper = new ObjectMapper();
            BitmaxResponse<BitmaxOrderResponse> bitmaxAssets =
                mapper.readValue(is, new TypeReference<BitmaxResponse<BitmaxOrderResponse>>() {});

            // Verify that the example data was unmarshalled correctly
            assertThat(bitmaxAssets.getData().getAc()).isEqualTo("MARGIN");
            assertThat(bitmaxAssets.getData().getInfo().getOrderId()).isEqualTo("16e85b4d9b9a8bXHbAwwoqDoc3d66830");
      }

      @Test
      public void bitmaxOpenOrdersResponseTest() throws IOException {

            // Read in the JSON from the example resources
            InputStream is =
                    BitmaxTradeDataJSONTest.class.getResourceAsStream(
                            "/org/knowm/xchange/bitmax/bitmaxOpenOrdersResponseExample.json");

            // Use Jackson to parse it
            ObjectMapper mapper = new ObjectMapper();
            BitmaxResponse<List<BitmaxOpenOrdersResponse>> bitmaxAssets =
                    mapper.readValue(is, new TypeReference<BitmaxResponse<List<BitmaxOpenOrdersResponse>>>() {});

            // Verify that the example data was unmarshalled correctly
            assertThat(bitmaxAssets.getData().size()).isEqualTo(1);
            assertThat(bitmaxAssets.getData().get(0).getAvgPx()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(bitmaxAssets.getData().get(0).getSymbol()).isEqualTo(CurrencyPairDeserializer.getCurrencyPairFromString("BTC/USDT").toString());
      }

      @Test
      public void bitmaxOrderHistoryResponseTest() throws IOException {

            // Read in the JSON from the example resources
            InputStream is =
                    BitmaxTradeDataJSONTest.class.getResourceAsStream(
                            "/org/knowm/xchange/bitmax/bitmaxOrderHistoryResponseExample.json");

            // Use Jackson to parse it
            ObjectMapper mapper = new ObjectMapper();
            BitmaxResponse<List<BitmaxOpenOrdersResponse>> bitmaxAssets =
                    mapper.readValue(is, new TypeReference<BitmaxResponse<List<BitmaxOpenOrdersResponse>>>() {});

            // Verify that the example data was unmarshalled correctly
            assertThat(bitmaxAssets.getData().size()).isEqualTo(1);
            assertThat(bitmaxAssets.getData().get(0).getAvgPx()).isEqualByComparingTo(BigDecimal.valueOf(7243.34));
            assertThat(bitmaxAssets.getData().get(0).getSymbol()).isEqualTo(CurrencyPairDeserializer.getCurrencyPairFromString("BTC/USDT").toString());
      }
}
