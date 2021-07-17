package org.knowm.xchange.cryptofacilities.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.QueryParam;
import org.knowm.xchange.service.BaseParamsDigest;
import si.mazi.rescu.RestInvocation;

/** @author Jean-Christophe Laruelle */
public class CryptoFacilitiesDigest extends BaseParamsDigest {

  /**
   * Constructor
   *
   * @param secretKeyBase64
   * @throws IllegalArgumentException if key is invalid (cannot be base-64-decoded or the decoded
   *     key is invalid).
   */
  private CryptoFacilitiesDigest(byte[] secretKeyBase64) {

    super(secretKeyBase64, HMAC_SHA_512);
  }

  public static CryptoFacilitiesDigest createInstance(String secretKeyBase64) {

    if (secretKeyBase64 != null) {
      return new CryptoFacilitiesDigest(Base64.getDecoder().decode(secretKeyBase64.getBytes()));
    } else {
      return null;
    }
  }

  @Override
  public String digestParams(RestInvocation restInvocation) {

    MessageDigest sha256;
    try {
      sha256 = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(
          "Illegal algorithm (SHA-256) for post body digest. Check the implementation.");
    }
    try {
      String decodedQuery =
          URLDecoder.decode(
              restInvocation.getParamsMap().get(QueryParam.class).asQueryString(),
              StandardCharsets.UTF_8.name());
      sha256.update(decodedQuery.getBytes());
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(
          "Bad encoding found on request query while hashing (SHA256) the POST data.", e);
    }
    try {
      sha256.update(
          URLDecoder.decode(restInvocation.getRequestBody(), StandardCharsets.UTF_8.name())
              .getBytes());
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(
          "Bad encoding found on request body while hashing (SHA256) the POST data.", e);
    }

    sha256.update(restInvocation.getParamValue(HeaderParam.class, "Nonce").toString().getBytes());
    sha256.update(restInvocation.getPath().getBytes());

    Mac mac512 = getMac();
    mac512.update(sha256.digest());
    return Base64.getEncoder().encodeToString(mac512.doFinal()).trim();
  }
}
