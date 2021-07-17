package org.knowm.xchange.bitmax.service;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.bitmax.BitmaxAdapters;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.service.account.AccountService;

import java.io.IOException;

public class BitmaxAccountService extends BitmaxAccountServiceRaw implements AccountService {

  public BitmaxAccountService(Exchange exchange) {
    super(exchange);
  }

  @Override
  public AccountInfo getAccountInfo() throws IOException {
    return BitmaxAdapters.adaptAccountInfo(getBitmaxCashAccountBalance());
  }
}
