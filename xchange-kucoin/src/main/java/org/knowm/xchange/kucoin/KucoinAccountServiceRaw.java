package org.knowm.xchange.kucoin;

import static org.knowm.xchange.kucoin.KucoinExceptionClassifier.classifyingExceptions;

import java.io.IOException;
import java.util.List;
import org.knowm.xchange.kucoin.dto.request.ApplyWithdrawApiRequest;
import org.knowm.xchange.kucoin.dto.request.CreateAccountRequest;
import org.knowm.xchange.kucoin.dto.request.InnerTransferRequest;
import org.knowm.xchange.kucoin.dto.response.AccountBalancesResponse;
import org.knowm.xchange.kucoin.dto.response.AccountLedgersResponse;
import org.knowm.xchange.kucoin.dto.response.ApplyWithdrawResponse;
import org.knowm.xchange.kucoin.dto.response.DepositAddressResponse;
import org.knowm.xchange.kucoin.dto.response.DepositResponse;
import org.knowm.xchange.kucoin.dto.response.InternalTransferResponse;
import org.knowm.xchange.kucoin.dto.response.Pagination;
import org.knowm.xchange.kucoin.dto.response.WithdrawalResponse;

public class KucoinAccountServiceRaw extends KucoinBaseService {

  protected KucoinAccountServiceRaw(KucoinExchange exchange) {
    super(exchange);
  }

  public List<AccountBalancesResponse> getKucoinAccounts() throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () -> accountApi.getAccountList(apiKey, digest, nonceFactory, passphrase, null, null));
  }

  public Void createKucoinAccount(String currency, String type) throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () ->
            accountApi.createAccount(
                apiKey,
                digest,
                nonceFactory,
                passphrase,
                CreateAccountRequest.builder().currency(currency).type(type).build()));
  }

  public ApplyWithdrawResponse applyWithdraw(ApplyWithdrawApiRequest req) throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () -> withdrawalAPI.applyWithdraw(apiKey, digest, nonceFactory, passphrase, req));
  }

  public InternalTransferResponse innerTransfer(InnerTransferRequest req) throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () -> accountApi.innerTransfer(apiKey, digest, nonceFactory, passphrase, req));
  }

  public Pagination<AccountLedgersResponse> getAccountLedgers(
      String accountId, Long startAt, Long endAt, Integer pageSize, Integer currentPage)
      throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () ->
            accountApi.getAccountLedgers(
                apiKey,
                digest,
                nonceFactory,
                passphrase,
                accountId,
                startAt,
                endAt,
                pageSize,
                currentPage));
  }

  public Pagination<WithdrawalResponse> getWithdrawalsList(
      String currency,
      String status,
      Long startAt,
      Long endAt,
      Integer pageSize,
      Integer currentPage)
      throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () ->
            withdrawalAPI.getWithdrawalsList(
                apiKey,
                digest,
                nonceFactory,
                passphrase,
                currency,
                status,
                startAt,
                endAt,
                pageSize,
                currentPage));
  }

  public Pagination<DepositResponse> getDepositList(
      String currency,
      String status,
      Long startAt,
      Long endAt,
      Integer pageSize,
      Integer currentPage)
      throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
        () ->
            depositAPI.getDepositList(
                apiKey,
                digest,
                nonceFactory,
                passphrase,
                currency,
                status,
                startAt,
                endAt,
                pageSize,
                currentPage));
  }

  public DepositAddressResponse createDepositAddress(
          String currency, String chain)
          throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
            () ->
                    depositAPI.createDepositAddress(
                            apiKey,
                            digest,
                            nonceFactory,
                            passphrase,
                            currency,
                            chain));
  }

  public DepositAddressResponse getDepositAddress(
          String currency, String chain)
          throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
            () ->
                    depositAPI.getDepositAddress(
                            apiKey,
                            digest,
                            nonceFactory,
                            passphrase,
                            currency,
                            chain));
  }

  public List<DepositAddressResponse> getDepositAddresses(
          String currency)
          throws IOException {
    checkAuthenticated();
    return classifyingExceptions(
            () ->
                    depositAPI.getDepositAddresses(
                            apiKey,
                            digest,
                            nonceFactory,
                            passphrase,
                            currency));
  }

}
