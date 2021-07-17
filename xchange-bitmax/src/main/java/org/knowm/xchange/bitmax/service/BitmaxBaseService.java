package org.knowm.xchange.bitmax.service;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.bitmax.BitmaxException;
import org.knowm.xchange.bitmax.IBitmax;
import org.knowm.xchange.bitmax.IBitmaxAuthenticated;
import org.knowm.xchange.bitmax.dto.BitmaxResponse;
import org.knowm.xchange.client.ExchangeRestProxyBuilder;
import org.knowm.xchange.service.BaseExchangeService;
import org.knowm.xchange.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.mazi.rescu.ParamsDigest;

import java.io.IOException;

/**
 * In order for Bitmax exchange authenticated endpoints to work you must add a specificParameterItem
 * named 'account-group' on the getExchangeSpecification.getExchangeSpecificParameters Map
 */
public class BitmaxBaseService extends BaseExchangeService implements BaseService {

  protected IBitmax bitmax;
  protected IBitmaxAuthenticated bitmaxAuthenticated;
  protected ParamsDigest signatureCreator;

  private static final Logger LOG = LoggerFactory.getLogger(BitmaxBaseService.class);

  public BitmaxBaseService(Exchange exchange) {
    super(exchange);
    bitmax =
        ExchangeRestProxyBuilder.forInterface(IBitmax.class, exchange.getExchangeSpecification())
            .build();
    if (exchange
        .getExchangeSpecification()
        .getExchangeSpecificParameters()
        .containsKey("account-group")) {
      ExchangeSpecification specWithAccountGroup = exchange.getDefaultExchangeSpecification();
      specWithAccountGroup.setSslUri(
          exchange.getExchangeSpecification().getSslUri()
              + exchange
                  .getExchangeSpecification()
                  .getExchangeSpecificParametersItem("account-group")
              + "/");
      bitmaxAuthenticated =
          ExchangeRestProxyBuilder.forInterface(IBitmaxAuthenticated.class, specWithAccountGroup)
              .build();
    } else {
      LOG.warn(
          "Authenticated endpoints will not work because no 'account-group' specificParameter has been found.");
    }
    signatureCreator =
        BitmaxDigest.createInstance(exchange.getExchangeSpecification().getSecretKey());
  }

  public <R> R checkResult(BitmaxResponse<R> response) throws BitmaxException {
    if (response.getCode() == 0) {
      return response.getData();
    } else {
      throw new BitmaxException(response.getCode(), response.getMessage());
    }
  }
}
