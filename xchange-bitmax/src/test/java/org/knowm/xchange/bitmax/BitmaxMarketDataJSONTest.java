package org.knowm.xchange.bitmax;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.knowm.xchange.bitmax.dto.BitmaxResponse;
import org.knowm.xchange.bitmax.dto.marketdata.BitmaxAssetDto;
import org.knowm.xchange.bitmax.dto.marketdata.BitmaxOrderbookDto;
import org.knowm.xchange.bitmax.dto.marketdata.BitmaxProductDto;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BitmaxMarketDataJSONTest {

    @Test
    public void bitmaxAssetDtoTest() throws IOException {

        // Read in the JSON from the example resources
        InputStream is =
                BitmaxMarketDataJSONTest.class.getResourceAsStream(
                        "/org/knowm/xchange/bitmax/bitmaxAssetsResponseExample.json");

        // Use Jackson to parse it
        ObjectMapper mapper = new ObjectMapper();
        BitmaxResponse<List<BitmaxAssetDto>> bitmaxAssets = mapper.readValue(is, new TypeReference<BitmaxResponse<List<BitmaxAssetDto>>>(){});

        // Verify that the example data was unmarshalled correctly
        assertThat(bitmaxAssets.getData().size()).isEqualTo(1);
        assertThat(bitmaxAssets.getData().get(0).getStatus()).isEqualTo(BitmaxAssetDto.BitmaxAssetStatus.Normal);

    }

    @Test
    public void bitmaxProductDtoTest() throws IOException {

        // Read in the JSON from the example resources
        InputStream is =
                BitmaxMarketDataJSONTest.class.getResourceAsStream(
                        "/org/knowm/xchange/bitmax/bitmaxProductsResponseExample.json");

        // Use Jackson to parse it
        ObjectMapper mapper = new ObjectMapper();
        BitmaxResponse<List<BitmaxProductDto>> bitmaxAssets = mapper.readValue(is, new TypeReference<BitmaxResponse<List<BitmaxProductDto>>>(){});

        // Verify that the example data was unmarshalled correctly
        assertThat(bitmaxAssets.getData().size()).isEqualTo(1);
        assertThat(bitmaxAssets.getData().get(0).getStatus()).isEqualTo(BitmaxAssetDto.BitmaxAssetStatus.Normal);

    }

    @Test
    public void bitmaxOrderbookDtoTest() throws IOException {

        // Read in the JSON from the example resources
        InputStream is =
                BitmaxMarketDataJSONTest.class.getResourceAsStream(
                        "/org/knowm/xchange/bitmax/bitmaxOrderbookResponseExample.json");

        // Use Jackson to parse it
        ObjectMapper mapper = new ObjectMapper();
        BitmaxResponse<BitmaxOrderbookDto> bitmaxAssets = mapper.readValue(is, new TypeReference<BitmaxResponse<BitmaxOrderbookDto>>(){});

        // Verify that the example data was unmarshalled correctly
        assertThat(bitmaxAssets.getData().getData().getSeqnum()).isEqualTo(5068757);
        assertThat(bitmaxAssets.getData().getM()).isEqualTo("depth-snapshot");
        assertThat(bitmaxAssets.getData().getData().getAsks().get(0).getPrice()).isEqualByComparingTo(BigDecimal.valueOf(0.06848));
        assertThat(bitmaxAssets.getData().getData().getAsks().get(0).getVolume()).isEqualByComparingTo(BigDecimal.valueOf(4084.2));

    }
}
