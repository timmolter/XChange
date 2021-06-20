package org.knowm.xchange.gateio.dto;

import com.google.common.io.CharStreams;
import org.knowm.xchange.gateio.dto.response.GateioTradesResponse;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class GateioTradesResponseTest {
    @Test
    public void V4_TradeResponse_ParsingTest() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("TradeResponses.json");
        String body = null;
        try (final Reader reader = new InputStreamReader(is)) {
            body = CharStreams.toString(reader);
        }

        GateioTradesResponse message =
                StreamingObjectMapperHelper.getObjectMapper()
                        .readValue(body, GateioTradesResponse.class);

        // Message Metadata
        Assert.assertEquals(1606292218, message.getTime());
        Assert.assertEquals("spot.trades", message.getChannel());
        Assert.assertEquals("update", message.getEvent());

        // Result data
        Assert.assertEquals(309143071, message.getResult().getId());
        Assert.assertEquals(1606292218, message.getResult().getCreateTimeSeconds());
        Assert.assertEquals("1606292218213.4578", message.getResult().getCreateTimeMilliseconds());
        Assert.assertEquals("GT_USDT", message.getResult().getCurrencyPair());
        Assert.assertEquals("sell", message.getResult().getSide());
        Assert.assertEquals("16.4700000000", message.getResult().getAmount());
        Assert.assertEquals("0.4705000000", message.getResult().getPrice());
    }
}