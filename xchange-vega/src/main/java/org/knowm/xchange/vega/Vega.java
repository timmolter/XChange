package org.knowm.xchange.vega;

import org.knowm.xchange.vega.dto.marketdata.VegaMarketData;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public interface Vega {

    @GET
    @Path("markets-data")
    List<VegaMarketData> getMarketData();

}
