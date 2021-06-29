package info.bitrich.xchangestream.poloniex2;

import org.knowm.xchange.service.BaseParamsDigest;
import si.mazi.rescu.RestInvocation;

import javax.crypto.Mac;
import java.math.BigInteger;

public class PoloniexSigner extends BaseParamsDigest {

  public PoloniexSigner(String secretKeyBase64) throws IllegalArgumentException {
    super(secretKeyBase64, "HmacSHA512");
  }

  public String digestParams(RestInvocation restInvocation) {
    return getSignature(restInvocation.getRequestBody().getBytes());
  }

  public String getSignature(byte[] bytes) {
    Mac mac = this.getMac();
    mac.update(bytes);
    return String.format("%0128x", new Object[] {new BigInteger(1, mac.doFinal())});
  }
}
