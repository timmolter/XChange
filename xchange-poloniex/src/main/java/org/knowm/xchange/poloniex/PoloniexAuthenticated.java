package org.knowm.xchange.poloniex;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.knowm.xchange.poloniex.dto.PoloniexException;
import org.knowm.xchange.poloniex.dto.account.PoloniexBalance;
import org.knowm.xchange.poloniex.dto.account.PoloniexLoan;
import org.knowm.xchange.poloniex.dto.account.TransferResponse;
import org.knowm.xchange.poloniex.dto.account.WithdrawalResponse;
import org.knowm.xchange.poloniex.dto.trade.PoloniexAccountBalance;
import org.knowm.xchange.poloniex.dto.trade.PoloniexDepositsWithdrawalsResponse;
import org.knowm.xchange.poloniex.dto.trade.PoloniexGenerateNewAddressResponse;
import org.knowm.xchange.poloniex.dto.trade.PoloniexMarginAccountResponse;
import org.knowm.xchange.poloniex.dto.trade.PoloniexMarginPostionResponse;
import org.knowm.xchange.poloniex.dto.trade.PoloniexMoveResponse;
import org.knowm.xchange.poloniex.dto.trade.PoloniexOpenOrder;
import org.knowm.xchange.poloniex.dto.trade.PoloniexTrade;
import org.knowm.xchange.poloniex.dto.trade.PoloniexTradeResponse;
import si.mazi.rescu.ParamsDigest;
import si.mazi.rescu.SynchronizedValueFactory;

/** @author Zach Holmes */
@Path("tradingApi")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public interface PoloniexAuthenticated {

  @POST
  @FormParam("command")
  HashMap<String, PoloniexBalance> returnCompleteBalances(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce,
      @FormParam("account") String account)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  HashMap<String, PoloniexLoan[]> returnActiveLoans(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  HashMap<String, String> returnDepositAddresses(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  PoloniexGenerateNewAddressResponse generateNewAddress(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce,
      @FormParam("currency") String currency)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  Map<String, PoloniexOpenOrder[]> returnOpenOrders(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce,
      @FormParam("currencyPair") AllPairs all)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  PoloniexTrade[] returnOrderTrades(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce,
      @FormParam("orderNumber") String orderID)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  PoloniexOpenOrder[] returnOpenOrders(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce,
      @FormParam("currencyPair") String currencyPair)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  PoloniexTrade[] returnTradeHistory(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce,
      @FormParam("currencyPair") String currencyPair,
      @FormParam("start") Long startTime,
      @FormParam("end") Long endTime,
      @FormParam("limit") Integer limit)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  PoloniexMarginAccountResponse returnMarginAccountSummary(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  PoloniexMarginPostionResponse getMarginPosition(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce,
      @FormParam("currencyPair") String currencyPair)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  Map<String, PoloniexMarginPostionResponse> getMarginPosition(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce,
      @FormParam("currencyPair") AllPairs all)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  HashMap<String, PoloniexTrade[]> returnTradeHistory(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce,
      @FormParam("currencyPair") String currencyPair,
      @FormParam("start") Long startTime,
      @FormParam("end") Long endTime,
      @FormParam("limit") Integer limit,
      @FormParam("ignore") String overload)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  PoloniexTradeResponse buy(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce,
      @FormParam("amount") String amount,
      @FormParam("rate") String rate,
      @FormParam("currencyPair") String currencyPair,
      @FormParam("fillOrKill") Integer fillOrKill,
      @FormParam("immediateOrCancel") Integer immediateOrCancel,
      @FormParam("postOnly") Integer postOnly)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  PoloniexTradeResponse sell(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce,
      @FormParam("amount") String amount,
      @FormParam("rate") String rate,
      @FormParam("currencyPair") String currencyPair,
      @FormParam("fillOrKill") Integer fillOrKill,
      @FormParam("immediateOrCancel") Integer immediateOrCancel,
      @FormParam("postOnly") Integer postOnly)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  PoloniexTradeResponse marginBuy(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce,
      @FormParam("amount") String amount,
      @FormParam("rate") String rate,
      @FormParam("currencyPair") String currencyPair,
      @FormParam("lendingRate") Double lendingRate)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  PoloniexTradeResponse marginSell(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce,
      @FormParam("amount") String amount,
      @FormParam("rate") String rate,
      @FormParam("currencyPair") String currencyPair,
      @FormParam("lendingRate") Double lendingRate)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  PoloniexMoveResponse moveOrder(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce,
      @FormParam("orderNumber") String orderNumber,
      @FormParam("amount") String amount,
      @FormParam("rate") String rate,
      @FormParam("immediateOrCancel") Integer immediateOrCancel,
      @FormParam("postOnly") Integer postOnly)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  HashMap<String, String> cancelOrder(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce,
      @FormParam("orderNumber") String orderNumber)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  WithdrawalResponse withdraw(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce,
      @FormParam("currency") String currency,
      @FormParam("amount") BigDecimal amount,
      @FormParam("address") String address,
      @FormParam("paymentId") @Nullable String paymentId)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  TransferResponse transferBalance(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce,
      @FormParam("currency") String currency,
      @FormParam("amount") BigDecimal amount,
      @FormParam("fromAccount") String fromAccount,
      @FormParam("toAccount") String toAccount)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  HashMap<String, String> returnFeeInfo(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  PoloniexAccountBalance returnAvailableAccountBalances(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce,
      @Nullable @FormParam("account") String account)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  Map<String, Map<String, BigDecimal>> returnTradableBalances(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  PoloniexAccountBalance[] returnAvailableAccountBalances(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce)
      throws PoloniexException, IOException;

  @POST
  @FormParam("command")
  PoloniexDepositsWithdrawalsResponse returnDepositsWithdrawals(
      @HeaderParam("Key") String apiKey,
      @HeaderParam("Sign") ParamsDigest signature,
      @FormParam("nonce") SynchronizedValueFactory<Long> nonce,
      @FormParam("start") Long startTime,
      @FormParam("end") Long endTime)
      throws PoloniexException, IOException;

  enum AllPairs {
    all
  }
}
