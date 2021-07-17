package org.knowm.xchange.examples.okex.v5.trade;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.instrument.FuturesContract;
import org.knowm.xchange.okex.v5.OkexExchange;
import org.knowm.xchange.okex.v5.dto.trade.OkexTradeParams;
import org.knowm.xchange.okex.v5.dto.trade.OkexTradeParams.OkexCancelOrderParams;
import org.knowm.xchange.service.trade.TradeService;

public class OkexOrdersDemo {

  public static void main(String[] args) throws IOException {

    ExchangeSpecification exSpec = new ExchangeSpecification(OkexExchange.class);
    exSpec.setSecretKey("");
    exSpec.setApiKey("");
    exSpec.setExchangeSpecificParametersItem("passphrase", "");
    Exchange okexExchange = ExchangeFactory.INSTANCE.createExchange(exSpec);

    generic(okexExchange);
  }

  private static void generic(Exchange okexExchange) throws IOException {

    TradeService tradeService = okexExchange.getTradeService();
    FuturesContract contract = new FuturesContract(CurrencyPair.BTC_USDT, "210924");

    OpenOrders openOrders = tradeService.getOpenOrders();
    System.out.println(openOrders);

    /*    OpenPositions futuresPosition = tradeService.getOpenPositions();

    List<OpenPosition> positions = futuresPosition.getOpenPositions();

    for (OpenPosition position : positions) {
      System.out.println(position);
    }*/
    // Place 1 lot buy limit order at price 200 for the BTC_UST Sept 24th Contact
    try {
      String placeLimitOrder =
          tradeService.placeLimitOrder(
              new LimitOrder(
                  OrderType.BID,
                  new BigDecimal("1"),
                  contract,
                  "0",
                  new Date(),
                  new BigDecimal("200")));
      System.out.println(placeLimitOrder);

      OkexCancelOrderParams req =
          new OkexTradeParams.OkexCancelOrderParams(contract, placeLimitOrder);

      boolean cancelOrder = tradeService.cancelOrder(req);
      System.out.println("Cancelled " + cancelOrder);
    } catch (Exception | Error ex) {
      System.out.println("Unable to place order due to  " + ex);
    }
  }
}
