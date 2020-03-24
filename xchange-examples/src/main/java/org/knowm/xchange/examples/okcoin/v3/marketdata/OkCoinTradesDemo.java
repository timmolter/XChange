package org.knowm.xchange.examples.okcoin.v3.marketdata;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.okcoin.OkexExchangeV3;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexDepth;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexFutureInstrument;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexFuturesTrade;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexSpotInstrument;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexTrade;
import org.knowm.xchange.okcoin.v3.service.OkexMarketDataServiceRaw;
import org.knowm.xchange.okcoin.v3.service.OkexTradeService;
import org.knowm.xchange.service.marketdata.MarketDataService;

public class OkCoinTradesDemo {

  public static void main(String[] args) throws IOException {

    ExchangeSpecification exSpec = new ExchangeSpecification(OkexExchangeV3.class);
    exSpec.setSecretKey("D281C21275D62C1CAAFD4C59B5D60F57");
    exSpec.setApiKey("197f2ab8-d0fb-464b-84fd-cfd971c112ac");

    Exchange okcoinExchange = ExchangeFactory.INSTANCE.createExchange(exSpec);

    futures(okcoinExchange);

    // generic(okcoinExchange);
    // raw(okcoinExchange);
  }

  private static void futures(Exchange okcoinExchange) throws IOException {

    OkexTradeService tradeService = (OkexTradeService) okcoinExchange.getTradeService();

    OpenOrders openOrders = tradeService.getOpenOrders();
    System.out.println(openOrders);

    String placeLimitOrder =
        tradeService.placeLimitOrder(
            new LimitOrder(
                OrderType.BID,
                new BigDecimal("1"),
                CurrencyPair.BTC_USD,
                "0",
                new Date(),
                new BigDecimal("200")));
    System.out.println(placeLimitOrder);

    boolean cancelOrder = tradeService.cancelOrder(placeLimitOrder);
    System.out.println("Cancelled " + cancelOrder);
  }

  private static void generic(Exchange okcoinExchange) throws IOException {

    // Interested in the public market data feed (no authentication)
    MarketDataService marketDataService = okcoinExchange.getMarketDataService();

    // Get the latest trade data for BTC_USD
    Trades trades = marketDataService.getTrades(CurrencyPair.BTC_USDT);
    System.out.println(trades);
    System.out.println("Trades(0): " + trades.getTrades().get(0).toString());
    System.out.println("Trades size: " + trades.getTrades().size());

    OrderBook orderBook = marketDataService.getOrderBook(CurrencyPair.BTC_USDT);
    System.out.println(orderBook);
    System.out.println("Asks(0): " + orderBook.getAsks().get(0).toString());
    System.out.println("Bid(0): " + orderBook.getBids().get(0).toString());
    System.out.println("Depth: " + orderBook.getBids().size());

    Trades futuresTrades = marketDataService.getTrades(CurrencyPair.BTC_USD, "200925");
    System.out.println(futuresTrades);
    System.out.println("Trades(0): " + futuresTrades.getTrades().get(0).toString());
    System.out.println("Trades size: " + futuresTrades.getTrades().size());

    OrderBook futuresOrderBook = marketDataService.getOrderBook(CurrencyPair.BTC_USD, "200925");
    System.out.println(futuresOrderBook);
    System.out.println("Asks(0): " + futuresOrderBook.getAsks().get(0).toString());
    System.out.println("Bid(0): " + futuresOrderBook.getBids().get(0).toString());
    System.out.println("Depth: " + futuresOrderBook.getBids().size());

    // Get the latest trades data for BTC_USD for the past couple of trades
    trades = marketDataService.getTrades(CurrencyPair.BTC_USDT, trades.getlastID() - 10);
    System.out.println(trades);
    System.out.println("Trades size: " + trades.getTrades().size());
  }

  private static void raw(Exchange okcoinExchange) throws IOException {

    // Interested in the public market data feed (no authentication)
    OkexMarketDataServiceRaw okexMarketDataServiceRaw =
        (OkexMarketDataServiceRaw) okcoinExchange.getMarketDataService();

    // Get the latest trade data for BTC_USD
    List<OkexFutureInstrument> instruments = okexMarketDataServiceRaw.getAllFutureInstruments();
    List<OkexSpotInstrument> spotInstruments = okexMarketDataServiceRaw.getAllSpotInstruments();
    System.out.println("Trades size: " + instruments.size());
    OkexTrade[] trades = okexMarketDataServiceRaw.getTrades("BTC-USDT");
    OkexDepth depth = okexMarketDataServiceRaw.getDepth("BTC-USDT");
    OkexFuturesTrade[] futuresTrades = okexMarketDataServiceRaw.getFuturesTrades("BTC-USD-200327");
    OkexDepth futuresDepth = okexMarketDataServiceRaw.getFuturesDepth("BTC-USD-200327");

    for (int i = 0; i < trades.length; i++) {
      OkexTrade okCoinTrade = trades[i];
      System.out.println(okCoinTrade.toString());
    }
    System.out.println("Trades size: " + trades.length);
  }
}
