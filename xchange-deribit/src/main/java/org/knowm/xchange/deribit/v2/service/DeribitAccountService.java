package org.knowm.xchange.deribit.v2.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.knowm.xchange.deribit.v2.DeribitAdapters;
import org.knowm.xchange.deribit.v2.DeribitExchange;
import org.knowm.xchange.deribit.v2.dto.account.AccountSummary;
import org.knowm.xchange.dto.account.*;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.service.account.AccountService;

public class DeribitAccountService extends DeribitAccountServiceRaw implements AccountService {

  /** currently there are only two (base) currencies supported by deribit */
  private final Supplier<Stream<String>> currencies = () -> Stream.of("BTC", "ETH");

  public DeribitAccountService(DeribitExchange exchange) {
    super(exchange);
  }

  @Override
  public AccountInfo getAccountInfo() {
    List<Balance> balances =
        currencies
            .get()
            .parallel()
            .map(
                c -> {
                  try {
                    AccountSummary accountSummary = super.getAccountSummary(c, false);
                    return DeribitAdapters.adapt(accountSummary);
                  } catch (IOException e) {
                    throw new ExchangeException(e);
                  }
                })
            .collect(Collectors.toList());
    Wallet wallet = Wallet.Builder.from(balances).build();

    List<OpenPosition> openPositions =
        currencies
            .get()
            .parallel()
            .flatMap(
                c -> {
                  try {
                    return super.getPositions(c, null).stream().map(DeribitAdapters::adapt);
                  } catch (IOException e) {
                    throw new ExchangeException(e);
                  }
                })
            .collect(Collectors.toList());    
    
    return new AccountInfo(null, null, Collections.singleton(wallet), openPositions, null);
  }
}
