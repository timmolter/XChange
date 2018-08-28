package org.knowm.xchange.upbit.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import javax.ws.rs.QueryParam;
import si.mazi.rescu.ParamsDigest;
import si.mazi.rescu.RestInvocation;

public class UpbitJWTDigest implements ParamsDigest {

  private static final String HMAC_SHA_512 = "HmacSHA512";
  private String accessKey;
  private String secretKey;

  private UpbitJWTDigest(String accessKey, String secretKey) throws IllegalArgumentException {
    this.accessKey = accessKey;
    this.secretKey = secretKey;
  }

  public static UpbitJWTDigest createInstance(String accessKey, String secretKey) {
    return new UpbitJWTDigest(accessKey, secretKey);
  }

  @Override
  public String digestParams(RestInvocation restInvocation) {
    String queryString = "";
    Iterator it;
    if (restInvocation.getParamsMap().get(QueryParam.class) != null
        && !restInvocation.getParamsMap().get(QueryParam.class).isEmpty()) {
      queryString = String.valueOf(restInvocation.getParamsMap().get(QueryParam.class));
    } else if (restInvocation.getRequestBody() != null
        && !restInvocation.getRequestBody().isEmpty()) {
      ObjectMapper mapper = new ObjectMapper();
      try {
        Map<String, String> map = mapper.readValue(restInvocation.getRequestBody(), Map.class);
        it = map.keySet().iterator();
        while (it.hasNext()) {
          String key = (String) it.next();
          String value = map.get(key);
          queryString += "&" + key + "=" + value;
        }
        queryString = queryString.substring(1);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    Algorithm algorithm = null;
    String jwtToken = null;
    algorithm = Algorithm.HMAC256(secretKey);
    JWTCreator.Builder builder = JWT.create();
    if (queryString.length() > 0) {
      builder
          .withClaim("access_key", accessKey)
          .withClaim("nonce", String.valueOf(new Date().getTime()))
          .withClaim("query", queryString);
    } else {
      builder
          .withClaim("access_key", accessKey)
          .withClaim("nonce", String.valueOf(new Date().getTime()));
    }
    jwtToken = builder.sign(algorithm);

    return "Bearer " + jwtToken;
  }
}
