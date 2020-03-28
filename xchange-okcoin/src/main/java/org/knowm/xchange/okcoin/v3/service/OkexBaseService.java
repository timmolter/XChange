package org.knowm.xchange.okcoin.v3.service;

import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.okcoin.OkexDigestV3;
import org.knowm.xchange.okcoin.OkexExchangeV3;
import org.knowm.xchange.okcoin.OkexV3;
import org.knowm.xchange.service.BaseExchangeService;
import org.knowm.xchange.service.BaseService;
import si.mazi.rescu.RestProxyFactory;

public class OkexBaseService extends BaseExchangeService<OkexExchangeV3> implements BaseService {

  protected final OkexV3 okex;
  protected final String apikey;
  protected final String passphrase;
  protected final OkexDigestV3 digest;

  protected final String tradepwd;

  public OkexBaseService(OkexExchangeV3 exchange) {
    super(exchange);
    final ExchangeSpecification spec = exchange.getExchangeSpecification();
    okex = RestProxyFactory.createProxy(OkexV3.class, spec.getSslUri(), getClientConfig());
    apikey = spec.getApiKey();
    passphrase = (String) spec.getExchangeSpecificParametersItem("passphrase");

    String secretKey = spec.getSecretKey();
    digest = secretKey == null ? null : new OkexDigestV3(secretKey);
    tradepwd = (String) spec.getExchangeSpecificParametersItem("tradepwd");
  }

  protected static String timestamp() {
    return System.currentTimeMillis() / 1000 + ".000"; //          <-- works as well
    // return Instant.now().toString();
  }

  protected String createDelimitedString(String[] items) {

    StringBuilder commaDelimitedString = null;
    if (items != null) {
      for (String item : items) {
        if (commaDelimitedString == null) {
          commaDelimitedString = new StringBuilder(item);
        } else {
          commaDelimitedString.append(",").append(item);
        }
      }
    }

    return (commaDelimitedString == null) ? null : commaDelimitedString.toString();
  }
}
