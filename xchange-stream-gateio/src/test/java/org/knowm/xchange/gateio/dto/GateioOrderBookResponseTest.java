package org.knowm.xchange.gateio.dto;

import com.google.common.io.CharStreams;
import org.knowm.xchange.gateio.dto.response.GateioOrderBookResponse;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class GateioOrderBookResponseTest {
    @Test
    public void V4_OrderBookResponse_ParsingTest() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("OrderBookResponse.json");
        String body = null;
        try (final Reader reader = new InputStreamReader(is)) {
            body = CharStreams.toString(reader);
        }

        GateioOrderBookResponse message =
                StreamingObjectMapperHelper.getObjectMapper()
                        .readValue(body, GateioOrderBookResponse.class);

        // Message Metadata
        Assert.assertEquals(1606295412, message.getTime());
        Assert.assertEquals("spot.order_book", message.getChannel());
        Assert.assertEquals("update", message.getEvent());

        // Result data
        Assert.assertEquals(1606295412123L, message.getResult().getUpdateTimeMilliseconds());
        Assert.assertEquals(48791820, message.getResult().getLastUpdateId());
        Assert.assertEquals("BTC_USDT", message.getResult().getCurrencyPair());

        // Bids
        Assert.assertEquals("19079.55", message.getResult().getBids()[0][0]);
        Assert.assertEquals("0.0195", message.getResult().getBids()[0][1]);

        // Asks
        Assert.assertEquals("19080.24", message.getResult().getAsks()[0][0]);
        Assert.assertEquals("0.1638", message.getResult().getAsks()[0][1]);
    }
}