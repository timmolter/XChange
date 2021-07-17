package org.knowm.xchange.bitmax.service;

import org.knowm.xchange.service.BaseParamsDigest;
import si.mazi.rescu.RestInvocation;

import javax.crypto.Mac;
import java.util.Base64;

public class BitmaxDigest extends BaseParamsDigest {

  public BitmaxDigest(String secretKeyBase64) throws IllegalArgumentException {
    super(secretKeyBase64, HMAC_SHA_256);
  }

  public static BitmaxDigest createInstance(String secretKeyBase64) {
    if (secretKeyBase64 != null) {
      return new BitmaxDigest(secretKeyBase64);
    } else return null;
  }

  @Override
  public String digestParams(RestInvocation restInvocation) {

    String message;

    if (restInvocation.getPath().contains("cash")) {
      message =
          restInvocation.getHttpHeadersFromParams().get("x-auth-timestamp")
              + restInvocation
                  .getPath()
                  .substring(restInvocation.getPath().lastIndexOf("cash") + 5);
    } else if (restInvocation.getPath().contains("margin")) {
      message =
          restInvocation.getHttpHeadersFromParams().get("x-auth-timestamp")
              + restInvocation
                  .getPath()
                  .substring(restInvocation.getPath().lastIndexOf("margin") + 7);
    } else {
      message =
          restInvocation.getHttpHeadersFromParams().get("x-auth-timestamp")
              + restInvocation.getPath().substring(restInvocation.getPath().lastIndexOf("/") + 1);
    }

    Mac mac256 = getMac();
    mac256.update(message.getBytes());

    return Base64.getEncoder().encodeToString(mac256.doFinal()).trim();
  }
}
