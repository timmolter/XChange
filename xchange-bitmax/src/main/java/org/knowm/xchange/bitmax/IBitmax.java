package org.knowm.xchange.bitmax;

import org.knowm.xchange.bitmax.dto.BitmaxResponse;
import org.knowm.xchange.bitmax.dto.marketdata.BitmaxAssetDto;
import org.knowm.xchange.bitmax.dto.marketdata.BitmaxMarketTradesDto;
import org.knowm.xchange.bitmax.dto.marketdata.BitmaxOrderbookDto;
import org.knowm.xchange.bitmax.dto.marketdata.BitmaxProductDto;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

@Path("api/pro/v1")
@Produces(MediaType.APPLICATION_JSON)
public interface IBitmax {

  @GET
  @Path("/assets")
  BitmaxResponse<List<BitmaxAssetDto>> getAllAssets() throws IOException;

  @GET
  @Path("/products")
  BitmaxResponse<List<BitmaxProductDto>> getAllProducts() throws IOException;

  @GET
  @Path("/depth")
  BitmaxResponse<BitmaxOrderbookDto> getOrderbookDepth(@QueryParam("symbol") String symbol)
      throws IOException;

  @GET
  @Path("/trades")
  BitmaxResponse<BitmaxMarketTradesDto> getTrades(@QueryParam("symbol") String symbol)
      throws IOException;
}
