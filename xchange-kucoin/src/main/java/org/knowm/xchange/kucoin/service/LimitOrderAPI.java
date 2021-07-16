package org.knowm.xchange.kucoin.service;

import org.knowm.xchange.kucoin.dto.response.KucoinResponse;
import org.knowm.xchange.kucoin.dto.response.OrderResponse;
import si.mazi.rescu.ParamsDigest;
import si.mazi.rescu.SynchronizedValueFactory;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

@Path("/api/v1/limit/orders")
@Produces(MediaType.APPLICATION_JSON)
public interface LimitOrderAPI {

    @GET
    KucoinResponse<List<OrderResponse>> getRecentOrders(
            @HeaderParam(APIConstants.API_HEADER_KEY) String apiKey,
            @HeaderParam(APIConstants.API_HEADER_SIGN) ParamsDigest signature,
            @HeaderParam(APIConstants.API_HEADER_TIMESTAMP) SynchronizedValueFactory<Long> nonce,
            @HeaderParam(APIConstants.API_HEADER_PASSPHRASE) String apiPassphrase
    ) throws IOException;

}
