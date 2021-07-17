package org.knowm.xchange.okex.v5;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.knowm.xchange.okex.v5.dto.OkexException;
import org.knowm.xchange.okex.v5.dto.OkexResponse;
import org.knowm.xchange.okex.v5.dto.marketdata.OkexInstrument;
import org.knowm.xchange.okex.v5.dto.marketdata.OkexOrderbook;
import org.knowm.xchange.okex.v5.dto.marketdata.OkexTrade;

@Path("/api/v5")
@Produces(APPLICATION_JSON)
public interface Okex {
  String instrumentsPath = "/public/instruments"; // Stated as 20 req/2 sec

  // To avoid 429s, actual req/second may need to be lowered!
  Map<String, List<Integer>> publicPathRateLimits =
      new HashMap<String, List<Integer>>() {
        {
          put(instrumentsPath, Arrays.asList(8, 1));
        }
      };

  @GET
  @Path(instrumentsPath)
  OkexResponse<List<OkexInstrument>> getInstruments(
      @QueryParam("instType") String instrumentType,
      @QueryParam("uly") String underlying,
      @QueryParam("instId") String instrumentId)
      throws OkexException, IOException;

  @GET
  @Path("/market/trades")
  @Consumes(MediaType.APPLICATION_JSON)
  OkexResponse<List<OkexTrade>> getTrades(
      @QueryParam("instId") String instrument, @QueryParam("limit") int l)
      throws IOException, OkexException;

  @GET
  @Path("/market/books")
  OkexResponse<List<OkexOrderbook>> getOrderbook(
      @QueryParam("instId") String instrument, @QueryParam("sz") int depth)
      throws IOException, OkexException;
}
