package org.knowm.xchange.bitmax.service;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.bitmax.BitmaxException;
import org.knowm.xchange.bitmax.dto.account.BitmaxCashAccountBalanceDto;

import java.io.IOException;
import java.util.List;

public class BitmaxAccountServiceRaw extends BitmaxBaseService {

  public BitmaxAccountServiceRaw(Exchange exchange) {
    super(exchange);
  }

  public List<BitmaxCashAccountBalanceDto> getBitmaxCashAccountBalance()
      throws BitmaxException, IOException {
    return checkResult(
        bitmaxAuthenticated.getCashAccountBalance(
            exchange.getExchangeSpecification().getApiKey(),
            exchange.getNonceFactory().createValue(),
            signatureCreator));
  }
}
