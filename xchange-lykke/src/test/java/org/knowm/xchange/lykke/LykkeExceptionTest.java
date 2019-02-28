package org.knowm.xchange.lykke;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LykkeExceptionTest {
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testUnmarshalLykkeAssetPairs() throws IOException {
        // Read in the JSON from the example resources
        InputStream is =
                LykkeAssetsTest.class.getResourceAsStream(
                        "/org/knowm/xchange/lykke/example-lykkeException.json");
        LykkeException lykkeEx = mapper.readValue(is, LykkeException.class);

        assertThat(lykkeEx.getError().getCode()).isEqualTo("NotFound");
        assertThat(lykkeEx.getError().getField()).isNull();
        assertThat(lykkeEx.getError().getMessage()).isEqualTo("Not found");


    }
}
