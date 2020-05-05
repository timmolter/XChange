package org.knowm.xchange.poloniex.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.poloniex.PoloniexAuthenticated;
import org.knowm.xchange.poloniex.PoloniexUtils;
import org.knowm.xchange.poloniex.dto.PoloniexException;
import org.knowm.xchange.poloniex.dto.trade.PoloniexAccountBalance;
import org.knowm.xchange.poloniex.dto.trade.PoloniexMarginAccountResponse;
import org.knowm.xchange.poloniex.dto.trade.PoloniexMarginPostionResponse;
import org.knowm.xchange.poloniex.dto.trade.PoloniexMoveResponse;
import org.knowm.xchange.poloniex.dto.trade.PoloniexOpenOrder;
import org.knowm.xchange.poloniex.dto.trade.PoloniexOrderFlags;
import org.knowm.xchange.poloniex.dto.trade.PoloniexTrade;
import org.knowm.xchange.poloniex.dto.trade.PoloniexTradeResponse;

/** @author Zach Holmes */
public class PoloniexTradeServiceRaw extends PoloniexBaseService {

  public PoloniexTradeServiceRaw(Exchange exchange) {

    super(exchange);
  }

  public Map<String, PoloniexOpenOrder[]> returnOpenOrders() throws IOException {
    return poloniexAuthenticated.returnOpenOrders(
        apiKey, signatureCreator, exchange.getNonceFactory(), PoloniexAuthenticated.AllPairs.all);
  }

  PoloniexTrade[] returnOrderTrades(String orderId) throws IOException {
    return poloniexAuthenticated.returnOrderTrades(
        apiKey, signatureCreator, exchange.getNonceFactory(), orderId);
  }

  public PoloniexOpenOrder[] returnOpenOrders(CurrencyPair currencyPair) throws IOException {
    return poloniexAuthenticated.returnOpenOrders(
        apiKey,
        signatureCreator,
        exchange.getNonceFactory(),
        PoloniexUtils.toPairString(currencyPair));
  }

  public PoloniexTrade[] returnTradeHistory(
      CurrencyPair currencyPair, Long startTime, Long endTime, Integer limit) throws IOException {

    return poloniexAuthenticated.returnTradeHistory(
        apiKey,
        signatureCreator,
        exchange.getNonceFactory(),
        PoloniexUtils.toPairString(currencyPair),
        startTime,
        endTime,
        limit);
  }

  public HashMap<String, PoloniexTrade[]> returnTradeHistory(
      Long startTime, Long endTime, Integer limit) throws IOException {

    String ignore = null; // only used so PoloniexAuthenticated.returnTradeHistory can be overloaded
    return poloniexAuthenticated.returnTradeHistory(
        apiKey,
        signatureCreator,
        exchange.getNonceFactory(),
        "all",
        startTime,
        endTime,
        limit,
        ignore);
  }

  public PoloniexMarginAccountResponse returnMarginAccountSummary() throws IOException {
    return poloniexAuthenticated.returnMarginAccountSummary(
        apiKey, signatureCreator, exchange.getNonceFactory());
  }

  public PoloniexMarginPostionResponse returnMarginPosition(CurrencyPair currencyPair)
      throws IOException {
    return poloniexAuthenticated.getMarginPosition(
        apiKey,
        signatureCreator,
        exchange.getNonceFactory(),
        PoloniexUtils.toPairString(currencyPair));
  }

  public Map<String, PoloniexMarginPostionResponse> returnAllMarginPositions() throws IOException {
    return poloniexAuthenticated.getMarginPosition(
        apiKey, signatureCreator, exchange.getNonceFactory(), PoloniexAuthenticated.AllPairs.all);
  }

  public PoloniexAccountBalance returnAvailableAccountBalances(String account) throws IOException {
    return poloniexAuthenticated.returnAvailableAccountBalances(
        apiKey, signatureCreator, exchange.getNonceFactory(), account);
  }

  public Map<String, Map<String, BigDecimal>> returnTradableBalances() throws IOException {
    return poloniexAuthenticated.returnTradableBalances(
        apiKey, signatureCreator, exchange.getNonceFactory());
  }

  public PoloniexAccountBalance[] returnAllAvailableAccountBalances() throws IOException {
    return poloniexAuthenticated.returnAvailableAccountBalances(
        apiKey, signatureCreator, exchange.getNonceFactory());
  }

  public PoloniexTradeResponse buy(LimitOrder limitOrder) throws IOException {
    return orderEntry(limitOrder, false);
  }

  public PoloniexTradeResponse sell(LimitOrder limitOrder) throws IOException {
    return orderEntry(limitOrder, true);
  }

  private PoloniexTradeResponse orderEntry(LimitOrder limitOrder, boolean sell) throws IOException {
    final Integer fillOrKill = limitOrder.hasFlag(PoloniexOrderFlags.FILL_OR_KILL) ? 1 : null;
    final Integer immediateOrCancel =
        limitOrder.hasFlag(PoloniexOrderFlags.IMMEDIATE_OR_CANCEL) ? 1 : null;
    final Integer postOnly = limitOrder.hasFlag(PoloniexOrderFlags.POST_ONLY) ? 1 : null;

    Double lendingRate = null;
    // TODO need to be able to specify the rate at which you would like to borrow margin.

    if (limitOrder.hasFlag(PoloniexOrderFlags.MARGIN)) {

      return sell
          ? poloniexAuthenticated.marginSell(
              apiKey,
              signatureCreator,
              exchange.getNonceFactory(),
              limitOrder.getOriginalAmount().toPlainString(),
              limitOrder.getLimitPrice().toPlainString(),
              PoloniexUtils.toPairString(limitOrder.getCurrencyPair()),
              lendingRate)
          : poloniexAuthenticated.marginBuy(
              apiKey,
              signatureCreator,
              exchange.getNonceFactory(),
              limitOrder.getOriginalAmount().toPlainString(),
              limitOrder.getLimitPrice().toPlainString(),
              PoloniexUtils.toPairString(limitOrder.getCurrencyPair()),
              lendingRate);

    } else {

      return sell
          ? poloniexAuthenticated.sell(
              apiKey,
              signatureCreator,
              exchange.getNonceFactory(),
              limitOrder.getOriginalAmount().toPlainString(),
              limitOrder.getLimitPrice().toPlainString(),
              PoloniexUtils.toPairString(limitOrder.getCurrencyPair()),
              fillOrKill,
              immediateOrCancel,
              postOnly)
          : poloniexAuthenticated.buy(
              apiKey,
              signatureCreator,
              exchange.getNonceFactory(),
              limitOrder.getOriginalAmount().toPlainString(),
              limitOrder.getLimitPrice().toPlainString(),
              PoloniexUtils.toPairString(limitOrder.getCurrencyPair()),
              fillOrKill,
              immediateOrCancel,
              postOnly);
    }
  }

  public PoloniexMoveResponse move(
      String orderId, BigDecimal originalAmount, BigDecimal limitPrice, PoloniexOrderFlags flag)
      throws IOException {

    Integer immediateOrCancel;
    if (flag == PoloniexOrderFlags.IMMEDIATE_OR_CANCEL) {
      immediateOrCancel = 1;
    } else {
      immediateOrCancel = null;
    }

    Integer postOnly;
    if (flag == PoloniexOrderFlags.POST_ONLY) {
      postOnly = 1;
    } else {
      postOnly = null;
    }

    return poloniexAuthenticated.moveOrder(
        apiKey,
        signatureCreator,
        exchange.getNonceFactory(),
        orderId,
        originalAmount.toPlainString(),
        limitPrice.toPlainString(),
        immediateOrCancel,
        postOnly);
  }

  public PoloniexMoveResponse move(String orderId, BigDecimal originalAmount, BigDecimal limitPrice)
      throws IOException {

    return move(orderId, originalAmount, limitPrice, null);
  }

  public boolean cancel(String orderId) throws IOException {
    HashMap<String, String> response =
        poloniexAuthenticated.cancelOrder(
            apiKey, signatureCreator, exchange.getNonceFactory(), orderId);
    if (response.containsKey("error")) {
      throw new PoloniexException(response.get("error"));
    } else {
      return response.get("success").equals("1");
    }
  }

  public HashMap<String, String> getFeeInfo() throws IOException {
    HashMap<String, String> response =
        poloniexAuthenticated.returnFeeInfo(apiKey, signatureCreator, exchange.getNonceFactory());
    if (response.containsKey("error")) {
      throw new PoloniexException(response.get("error"));
    }
    return response;
  }
}
