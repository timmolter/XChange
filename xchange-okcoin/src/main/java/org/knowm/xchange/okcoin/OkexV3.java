package org.knowm.xchange.okcoin;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.knowm.xchange.okcoin.v3.dto.account.FuturesLeverageResponse;
import org.knowm.xchange.okcoin.v3.dto.account.MarginAccountResponse;
import org.knowm.xchange.okcoin.v3.dto.account.MarginAccountSettingsRecord;
import org.knowm.xchange.okcoin.v3.dto.account.OkexDepositRecord;
import org.knowm.xchange.okcoin.v3.dto.account.OkexFundingAccountRecord;
import org.knowm.xchange.okcoin.v3.dto.account.OkexSpotAccountRecord;
import org.knowm.xchange.okcoin.v3.dto.account.OkexWithdrawalRecord;
import org.knowm.xchange.okcoin.v3.dto.account.OkexWithdrawalRequest;
import org.knowm.xchange.okcoin.v3.dto.account.OkexWithdrawalResponse;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexDepth;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexFutureInstrument;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexFutureTicker;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexFuturesTrade;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexSpotInstrument;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexSpotTicker;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexSwapInstrument;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexSwapTicker;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexTrade;
import org.knowm.xchange.okcoin.v3.dto.trade.FundsTransferRequest;
import org.knowm.xchange.okcoin.v3.dto.trade.FundsTransferResponse;
import org.knowm.xchange.okcoin.v3.dto.trade.FuturesAccountsByCurrencyResponse;
import org.knowm.xchange.okcoin.v3.dto.trade.FuturesAccountsResponse;
import org.knowm.xchange.okcoin.v3.dto.trade.FuturesMultipleOrderCancellationResponse;
import org.knowm.xchange.okcoin.v3.dto.trade.FuturesMultipleOrderPlacementRequest;
import org.knowm.xchange.okcoin.v3.dto.trade.FuturesOpenOrdersResponse;
import org.knowm.xchange.okcoin.v3.dto.trade.FuturesOrderPlacementRequest;
import org.knowm.xchange.okcoin.v3.dto.trade.FuturesPositionsResponse;
import org.knowm.xchange.okcoin.v3.dto.trade.MarginBorrowRequest;
import org.knowm.xchange.okcoin.v3.dto.trade.MarginBorrowResponse;
import org.knowm.xchange.okcoin.v3.dto.trade.MarginRepaymentRequest;
import org.knowm.xchange.okcoin.v3.dto.trade.MarginRepaymentResponse;
import org.knowm.xchange.okcoin.v3.dto.trade.MarginSetLeverageRequest;
import org.knowm.xchange.okcoin.v3.dto.trade.OkexFuturesTransaction;
import org.knowm.xchange.okcoin.v3.dto.trade.OkexOpenOrder;
import org.knowm.xchange.okcoin.v3.dto.trade.OkexResponse;
import org.knowm.xchange.okcoin.v3.dto.trade.OkexSwapTransaction;
import org.knowm.xchange.okcoin.v3.dto.trade.OkexTransaction;
import org.knowm.xchange.okcoin.v3.dto.trade.OrderBatchCancellationRequest;
import org.knowm.xchange.okcoin.v3.dto.trade.OrderCancellationRequest;
import org.knowm.xchange.okcoin.v3.dto.trade.OrderCancellationResponse;
import org.knowm.xchange.okcoin.v3.dto.trade.OrderPlacementResponse;
import org.knowm.xchange.okcoin.v3.dto.trade.SpotOrderPlacementRequest;
import org.knowm.xchange.okcoin.v3.dto.trade.SwapAccountsResponse;
import org.knowm.xchange.okcoin.v3.dto.trade.SwapFuturesMultipleOrderPlacementResponse;
import org.knowm.xchange.okcoin.v3.dto.trade.SwapMultipleOrderCancellationResponse;
import org.knowm.xchange.okcoin.v3.dto.trade.SwapMultipleOrderPlacementRequest;
import org.knowm.xchange.okcoin.v3.dto.trade.SwapOpenOrdersResponse;
import org.knowm.xchange.okcoin.v3.dto.trade.SwapOrderBatchCancellationRequest;
import org.knowm.xchange.okcoin.v3.dto.trade.SwapOrderPlacementRequest;
import org.knowm.xchange.okcoin.v3.dto.trade.SwapPositionsEntry;
import org.knowm.xchange.okcoin.v3.service.OkexException;

import si.mazi.rescu.ParamsDigest;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public interface OkexV3 {

  public static final String OK_ACCESS_PASSPHRASE = "OK-ACCESS-PASSPHRASE";
  public static final String OK_ACCESS_KEY = "OK-ACCESS-KEY";
  public static final String OK_ACCESS_SIGN = "OK-ACCESS-SIGN";
  public static final String OK_ACCESS_TIMESTAMP = "OK-ACCESS-TIMESTAMP";

  /** ******************************* Funding Account API ********************************* */
  @GET
  @Path("/account/v3/wallet")
  List<OkexFundingAccountRecord> fundingAccountInformation(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase)
      throws IOException, OkexException;

  /**
   * @param currency required, Token
   * @param amount required, Transfer amount
   * @param from required, the remitting account (0: sub account 1: spot 3: futures 4:C2C 5: margin
   *     6: Funding Account 8:PiggyBank 9：swap)
   * @param to required, the beneficiary account(0: sub account 1:spot 3: futures 4:C2C 5: margin 6:
   *     Funding Account 8:PiggyBank 9 :swap)
   * @param subAccount optional, sub account name
   * @param instrumentid optional, margin token pair ID, for supported pairs only
   * @return
   * @throws IOException
   * @throws OkexException
   */
  @POST
  @Path("/account/v3/transfer")
  @Consumes(MediaType.APPLICATION_JSON)
  FundsTransferResponse fundsTransfer(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      FundsTransferRequest req)
      throws IOException, OkexException;

  @POST
  @Path("/account/v3/withdrawal")
  @Consumes(MediaType.APPLICATION_JSON)
  OkexWithdrawalResponse withdrawal(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      OkexWithdrawalRequest req)
      throws IOException, OkexException;

  @GET
  @Path("/account/v3/withdrawal/history")
  List<OkexWithdrawalRecord> recentWithdrawalHistory(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase)
      throws IOException, OkexException;

  @GET
  @Path("/account/v3/deposit/history")
  List<OkexDepositRecord> recentDepositHistory(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase)
      throws IOException, OkexException;

  /** ******************************* Spot Token Trading API ********************************* */
  @GET
  @Path("/spot/v3/accounts")
  List<OkexSpotAccountRecord> spotTradingAccount(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase)
      throws IOException, OkexException;

  @POST
  @Path("/spot/v3/orders")
  @Consumes(MediaType.APPLICATION_JSON)
  OrderPlacementResponse spotPlaceOrder(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      SpotOrderPlacementRequest req)
      throws IOException, OkexException;

  @POST
  @Path("/spot/v3/batch_orders")
  @Consumes(MediaType.APPLICATION_JSON)
  Map<String, List<OrderPlacementResponse>> spotPlaceMultipleOrders(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      List<SpotOrderPlacementRequest> req)
      throws IOException, OkexException;

  @POST
  @Path("/spot/v3/cancel_orders/{order_id}")
  @Consumes(MediaType.APPLICATION_JSON)
  OrderCancellationResponse spotCancelOrder(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      @PathParam("order_id") String orderId,
      OrderCancellationRequest req)
      throws IOException, OkexException;

  @POST
  @Path("/spot/v3/cancel_batch_orders")
  @Consumes(MediaType.APPLICATION_JSON)
  /**
   * @param req
   * @return map with instrument_id -> List<OrderCancellationResponse>
   * @throws IOException
   * @throws OkexException
   */
  Map<String, List<OrderCancellationResponse>> spotCancelMultipleOrders(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      List<OrderBatchCancellationRequest> req)
      throws IOException, OkexException;

  /**
   * @param instrumentId [required] list the orders of specific trading pairs
   * @param from [optional] request page after this id (latest information) (eg. 1, 2, 3, 4, 5.
   *     There is only a 5 "from 4", while there are 1, 2, 3 "to 4")
   * @param to [optional] request page after (newer) this id.
   * @param limit [optional] number of results per request. Maximum 100. (default 100)
   * @param state [required ] Order Status("-2":Failed,"-1":Cancelled,"0":Open ,"1":Partially
   *     Filled, "2":Fully Filled,"3":Submitting,"4":Cancelling,"6": Incomplete（open+partially
   *     filled），"7":Complete（cancelled+fully filled））
   * @return
   * @throws IOException
   * @throws OkexException
   */
  @GET
  @Path("/spot/v3/orders")
  List<OkexOpenOrder> getSpotOrderList(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      @QueryParam("instrument_id") String instrumentId,
      @QueryParam("from") String from,
      @QueryParam("to") String to,
      @QueryParam("limit") Integer limit,
      @QueryParam("state") String state)
      throws IOException, OkexException;

  /**
   * @param orderId required, order ID
   * @param instrumentId required, trading pair
   * @param from optional, Request page before (older) this pagination id,the parameter are
   *     order_id, ledger_id or trade_id of the endpoint, etc.
   * @param to optional, Request page after (newer) this pagination id,the parameter are order_id,
   *     ledger_id or trade_id of the endpoint, etc.
   * @param limit optional, Number of results per request. Maximum 100. (default 100)
   * @return
   * @throws IOException
   * @throws OkexException
   */
  @GET
  @Path("/spot/v3/fills")
  List<OkexTransaction> getSpotTransactionDetails(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      @QueryParam("order_id") String orderId,
      @QueryParam("instrument_id") String instrumentId,
      @QueryParam("from") String from,
      @QueryParam("to") String to,
      @QueryParam("limit") Integer limit)
      throws IOException, OkexException;

  @GET
  @Path("/spot/v3/instruments")
  List<OkexSpotInstrument> getAllSpotInstruments() throws IOException, OkexException;

  @GET
  @Path("/spot/v3/instruments/ticker")
  List<OkexSpotTicker> getAllSpotTickers() throws IOException, OkexException;

  @GET
  @Path("/spot/v3/instruments/{instrument_id}/trades")
  OkexTrade[] getTrades(
      @PathParam("instrument_id") String instrument, @QueryParam("limit") Long since)
      throws IOException;

  @GET
  @Path("/spot/v3/instruments/{instrument_id}/book")
  OkexDepth getDepth(
      @PathParam("instrument_id") String instrumentId, @QueryParam("size") Integer size)
      throws IOException;

  @GET
  @Path("/spot/v3/instruments/{instrument_id}/ticker")
  OkexSpotTicker getSpotTicker(@PathParam("instrument_id") String instrumentId)
      throws IOException, OkexException;

  /** ******************************* Futures Trading API ********************************* */
  @GET
  @Path("/futures/v3/instruments")
  List<OkexFutureInstrument> getAllFutureInstruments() throws IOException, OkexException;

  @GET
  @Path("/futures/v3/instruments/ticker")
  List<OkexFutureTicker> getAllFutureTickers() throws IOException, OkexException;

  @GET
  @Path("/futures/v3/instruments/{instrument_id}/trades")
  OkexFuturesTrade[] getFuturesTrades(
      @PathParam("instrument_id") String instrument, @QueryParam("limit") Long since)
      throws IOException;

  @GET
  @Path("/futures/v3/instruments/{instrument_id}/book")
  OkexDepth getFuturesDepth(
      @PathParam("instrument_id") String instrumentId, @QueryParam("size") Integer size)
      throws IOException;

  @GET
  @Path("/futures/v3/position")
  FuturesPositionsResponse getFuturesPositions(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase)
      throws IOException, OkexException;

  @GET
  @Path("/futures/v3/accounts")
  FuturesAccountsResponse getFuturesAccounts(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase)
      throws IOException, OkexException;

  @GET
  @Path("/futures/v3/accounts/{currency}")
  FuturesAccountsByCurrencyResponse getFuturesAccounts(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      @PathParam("currency") String currency)
      throws IOException, OkexException;

  @GET
  @Path("/futures/v3/accounts/{currency}/leverage")
  FuturesLeverageResponse getFuturesLeverage(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      @PathParam("currency") String currency)
      throws IOException, OkexException;

  /**
   * @param instrumentId
   * @param from
   * @param to
   * @param limit
   * @param state Order Status("-2":Failed,"-1":Cancelled,"0":Open ,"1":Partially Filled, "2":Fully
   *     Filled,"3":Submitting,"4":Cancelling,"6": Incomplete（open+partially
   *     filled），"7":Complete（cancelled+fully filled））
   * @return
   * @throws IOException
   * @throws OkexException
   */
  @GET
  @Path("/futures/v3/orders/{instrument_id}")
  FuturesOpenOrdersResponse getFuturesOrderList(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      @PathParam("instrument_id") String instrumentId,
      @QueryParam("from") String from,
      @QueryParam("to") String to,
      @QueryParam("limit") Integer limit,
      @QueryParam("state") String state)
      throws IOException, OkexException;

  @POST
  @Path("/futures/v3/order")
  @Consumes(MediaType.APPLICATION_JSON)
  OrderPlacementResponse futuresPlaceOrder(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      FuturesOrderPlacementRequest req)
      throws IOException, OkexException;

  @POST
  @Path("/futures/v3/orders")
  @Consumes(MediaType.APPLICATION_JSON)
  SwapFuturesMultipleOrderPlacementResponse futuresPlaceMultipleOrders(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      FuturesMultipleOrderPlacementRequest req)
      throws IOException, OkexException;

  @POST
  @Path("/futures/v3/cancel_order/{instrument_id}/{order_id}")
  @Consumes(MediaType.APPLICATION_JSON)
  OrderCancellationResponse futuresCancelOrder(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      @PathParam("instrument_id") String instrumentId,
      @PathParam("order_id") String orderId)
      throws IOException, OkexException;

  @POST
  @Path("/futures/v3/cancel_batch_orders/{instrument_id}")
  @Consumes(MediaType.APPLICATION_JSON)
  FuturesMultipleOrderCancellationResponse futuresCancelMultipleOrders(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      @PathParam("instrument_id") String instrumentId,
      OrderBatchCancellationRequest req)
      throws IOException, OkexException;

  @GET
  @Path("/futures/v3/fills")
  List<OkexFuturesTransaction> getFuturesTransactionDetails(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      @QueryParam("order_id") String orderId,
      @QueryParam("instrument_id") String instrumentId,
      @QueryParam("from") String from,
      @QueryParam("to") String to,
      @QueryParam("limit") Integer limit)
      throws IOException, OkexException;

  /**
   * ******************************** Perpetual SWAP Trading API *********************************
   */
  @GET
  @Path("/swap/v3/instruments")
  List<OkexSwapInstrument> getAllSwapInstruments() throws IOException, OkexException;

  @GET
  @Path("/swap/v3/instruments/ticker")
  List<OkexSwapTicker> getAllSwapTickers() throws IOException, OkexException;

  /**
   * @param instrumentId
   * @param from
   * @param to
   * @param limit
   * @param state Yes Order Status("-2":Failed,"-1":Cancelled,"0":Open ,"1":Partially Filled,
   *     "2":Fully Filled,"3":Submitting,"4":Cancelling,"6": Incomplete（open+partially
   *     filled），"7":Complete（cancelled+fully filled））
   * @return
   * @throws IOException
   * @throws OkexException
   */
  @GET
  @Path("/swap/v3/orders/{instrument_id}")
  SwapOpenOrdersResponse getSwapOrderList(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      @PathParam("instrument_id") String instrumentId,
      @QueryParam("from") String from,
      @QueryParam("to") String to,
      @QueryParam("limit") Integer limit,
      @QueryParam("state") String state)
      throws IOException, OkexException;

  @GET
  @Path("/swap/v3/position")
  List<SwapPositionsEntry> getSwapPositions(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase)
      throws IOException, OkexException;

  @GET
  @Path("/swap/v3/accounts")
  SwapAccountsResponse getSwapAccounts(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase)
      throws IOException, OkexException;

  @POST
  @Path("/swap/v3/order")
  @Consumes(MediaType.APPLICATION_JSON)
  OrderPlacementResponse swapPlaceOrder(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      SwapOrderPlacementRequest req)
      throws IOException, OkexException;

  @POST
  @Path("/swap/v3/orders")
  @Consumes(MediaType.APPLICATION_JSON)
  SwapFuturesMultipleOrderPlacementResponse swapPlaceMultipleOrders(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      SwapMultipleOrderPlacementRequest req)
      throws IOException, OkexException;

  @POST
  @Path("/swap/v3/cancel_order/{instrument_id}/{order_id}")
  @Consumes(MediaType.APPLICATION_JSON)
  OrderCancellationResponse swapCancelOrder(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      @PathParam("instrument_id") String instrumentId,
      @PathParam("order_id") String orderId)
      throws IOException, OkexException;

  @POST
  @Path("/swap/v3/cancel_batch_orders/{instrument_id}")
  @Consumes(MediaType.APPLICATION_JSON)
  SwapMultipleOrderCancellationResponse swapCancelMultipleOrders(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      @PathParam("instrument_id") String instrumentId,
      SwapOrderBatchCancellationRequest req)
      throws IOException, OkexException;

  @GET
  @Path("/swap/v3/fills")
  List<OkexSwapTransaction> getSwapTransactionDetails(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      @QueryParam("order_id") String orderId,
      @QueryParam("instrument_id") String instrumentId,
      @QueryParam("from") String from,
      @QueryParam("to") String to,
      @QueryParam("limit") Integer limit)
      throws IOException, OkexException;
  /** ******************************** Margin Trading API ********************************* */
  @GET
  @Path("/margin/v3/accounts")
  MarginAccountResponse[] marginAccounts(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase)
      throws IOException, OkexException;

  @GET
  @Path("/margin/v3/accounts/availability")
  List<MarginAccountSettingsRecord> marginAccountsSettings(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase)
      throws IOException, OkexException;

  @POST
  @Path("/margin/v3/orders")
  @Consumes(MediaType.APPLICATION_JSON)
  OrderPlacementResponse marginPlaceOrder(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      SpotOrderPlacementRequest req)
      throws IOException, OkexException;

  @POST
  @Path("/margin/v3/accounts/{instrument_id}/leverage")
  @Consumes(MediaType.APPLICATION_JSON)
  OkexResponse setLeverage(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      @PathParam("instrument_id") String instrumentId,
      MarginSetLeverageRequest req)
      throws IOException, OkexException;

  @POST
  @Path("/margin/v3/accounts/borrow")
  @Consumes(MediaType.APPLICATION_JSON)
  MarginBorrowResponse marginBorrow(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      MarginBorrowRequest req)
      throws IOException, OkexException;

  @POST
  @Path("/margin/v3/accounts/repayment")
  @Consumes(MediaType.APPLICATION_JSON)
  MarginRepaymentResponse marginRepayment(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      MarginRepaymentRequest req)
      throws IOException, OkexException;

  @POST
  @Path("/margin/v3/cancel_order/{instrument_id}/{order_id}")
  @Consumes(MediaType.APPLICATION_JSON)
  OrderCancellationResponse marginCancelOrder(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      @PathParam("instrument_id") String instrumentId,
      @PathParam("order_id") String orderId)
      throws IOException, OkexException;

  /**
   * @param instrumentId [required] list the orders of specific trading pairs
   * @param from [optional] request page after this id (latest information) (eg. 1, 2, 3, 4, 5.
   *     There is only a 5 "from 4", while there are 1, 2, 3 "to 4")
   * @param to [optional] request page after (newer) this id.
   * @param limit [optional] number of results per request. Maximum 100. (default 100)
   * @param state [required ] Order Status("-2":Failed,"-1":Cancelled,"0":Open ,"1":Partially
   *     Filled, "2":Fully Filled,"3":Submitting,"4":Cancelling,"6": Incomplete（open+partially
   *     filled），"7":Complete（cancelled+fully filled））
   * @return
   * @throws IOException
   * @throws OkexException
   */
  @GET
  @Path("/margin/v3/orders")
  List<OkexOpenOrder> getMarginOrderList(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      @QueryParam("instrument_id") String instrumentId,
      @QueryParam("from") String from,
      @QueryParam("to") String to,
      @QueryParam("limit") Integer limit,
      @QueryParam("state") String state)
      throws IOException, OkexException;

  /**
   * @param orderId required, order ID
   * @param instrumentId required, trading pair
   * @param from optional, Request page before (older) this pagination id,the parameter are
   *     order_id, ledger_id or trade_id of the endpoint, etc.
   * @param to optional, Request page after (newer) this pagination id,the parameter are order_id,
   *     ledger_id or trade_id of the endpoint, etc.
   * @param limit optional, Number of results per request. Maximum 100. (default 100)
   * @return
   * @throws IOException
   * @throws OkexException
   */
  @GET
  @Path("/margin/v3/fills")
  List<OkexTransaction> getMarginTransactionDetails(
      @HeaderParam(OK_ACCESS_KEY) String apiKey,
      @HeaderParam(OK_ACCESS_SIGN) ParamsDigest signature,
      @HeaderParam(OK_ACCESS_TIMESTAMP) String timestamp,
      @HeaderParam(OK_ACCESS_PASSPHRASE) String passphrase,
      @QueryParam("order_id") String orderId,
      @QueryParam("instrument_id") String instrumentId,
      @QueryParam("from") String from,
      @QueryParam("to") String to,
      @QueryParam("limit") Integer limit)
      throws IOException, OkexException;
}
