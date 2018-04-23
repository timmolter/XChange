package org.knowm.xchange.wex.v3.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.FundingRecord;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.trade.params.DefaultWithdrawFundsParams;
import org.knowm.xchange.service.trade.params.TradeHistoryParamOffset;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.WithdrawFundsParams;
import org.knowm.xchange.wex.v3.WexAdapters;
import org.knowm.xchange.wex.v3.dto.account.WexAccountInfo;
import org.knowm.xchange.wex.v3.dto.trade.WexTransHistoryResult;

/** @author Matija Mazi */
public class WexAccountService extends WexAccountServiceRaw implements AccountService {

  /**
   * Constructor
   *
   * @param exchange
   */
  public WexAccountService(Exchange exchange) {

    super(exchange);
  }

  @Override
  public AccountInfo getAccountInfo() throws IOException {

    WexAccountInfo info = getBTCEAccountInfo();
    return AccountInfo.build(WexAdapters.adaptWallet(info));
  }

  @Override
  public String withdrawFunds(Currency currency, BigDecimal amount, String address)
      throws IOException {
    return withdraw(currency.toString(), amount, address);
  }

  @Override
  public String withdrawFunds(WithdrawFundsParams params) throws IOException {
    if (params instanceof DefaultWithdrawFundsParams) {
      DefaultWithdrawFundsParams defaultParams = (DefaultWithdrawFundsParams) params;
      return withdrawFunds(
          defaultParams.getCurrency(), defaultParams.getAmount(), defaultParams.getAddress());
    }
    throw new IllegalStateException("Don't know how to withdraw: " + params);
  }

  @Override
  public String requestDepositAddress(Currency currency, String... args) throws IOException {

    throw new NotAvailableFromExchangeException();
  }

  @Override
  public TradeHistoryParams createFundingHistoryParams() {
    throw new NotAvailableFromExchangeException();
  }

  @Override
  public List<FundingRecord> getFundingHistory(TradeHistoryParams params) throws IOException {
    Long offset = null;
    if (params instanceof TradeHistoryParamOffset) {
      offset = ((TradeHistoryParamOffset) params).getOffset();
    }

    Map<Long, WexTransHistoryResult> map = transferHistory(offset);

    return WexAdapters.adaptFundingRecords(map);
  }
}
