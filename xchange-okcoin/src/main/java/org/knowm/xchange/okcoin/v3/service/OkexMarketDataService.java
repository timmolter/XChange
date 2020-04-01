package org.knowm.xchange.okcoin.v3.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.instrument.FuturesContract;
import org.knowm.xchange.instrument.SwapContract;
import org.knowm.xchange.okcoin.OkexAdaptersV3;
import org.knowm.xchange.okcoin.OkexExchangeV3;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexDepth;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexFuturesTrade;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexSpotTicker;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexSwapTrade;
import org.knowm.xchange.okcoin.v3.dto.marketdata.OkexTrade;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.marketdata.params.Params;

public class OkexMarketDataService extends OkexMarketDataServiceRaw implements MarketDataService {

  public OkexMarketDataService(OkexExchangeV3 exchange) {
    super(exchange);
  }

  @Override
  public Ticker getTicker(CurrencyPair currencyPair, Object... args) throws IOException {
    OkexSpotTicker tokenPairInformation =
        okex.getSpotTicker(OkexAdaptersV3.toSpotInstrument(currencyPair));
    return OkexAdaptersV3.convert(tokenPairInformation);
  }

  @Override
  public List<Ticker> getTickers(Params params) throws IOException {
    return okex.getAllSpotTickers()
        .stream()
        .map(OkexAdaptersV3::convert)
        .collect(Collectors.toList());
  }

  @Override
  public Trades getTrades(CurrencyPair currencyPair, Object... args) throws IOException {
    final OkexTrade[] trades;

    if (args == null || args.length == 0) {
      trades = getTrades(OkexAdaptersV3.toSpotInstrument(currencyPair));
      return OkexAdaptersV3.adaptTrades(trades, currencyPair);
    } else {

      if (args[0] instanceof Long) {
        trades = getTrades(OkexAdaptersV3.toSpotInstrument(currencyPair), (Long) args[0]);
        return OkexAdaptersV3.adaptTrades(trades, currencyPair);
      } else {
        String arg = (String) args[0];
        if (arg.toUpperCase().equals("SWAP")) {
          SwapContract swapContract = new SwapContract(currencyPair, (String) args[0]);
          Object[] newArgs = new Object[args.length - 1];
          System.arraycopy(args, 1, newArgs, 0, args.length - 1);
          OkexSwapTrade[] swapTrades = getSwapTrades(OkexAdaptersV3.toSwapInstrument(swapContract));
          return OkexAdaptersV3.adaptSwapTrades(swapTrades, swapContract);
        }

        FuturesContract futuresContract = new FuturesContract(currencyPair, (String) args[0]);
        Object[] newArgs = new Object[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
        OkexFuturesTrade[] futuresTrades =
            getFuturesTrades(OkexAdaptersV3.toFuturesInstrument(futuresContract));
        return OkexAdaptersV3.adaptFuturesTrades(futuresTrades, futuresContract);
      }
    }
  }

  @Override
  public OrderBook getOrderBook(CurrencyPair currencyPair, Object... args) throws IOException {

    final OkexDepth okexDepth;

    if (args == null || args.length == 0) {
      okexDepth = getDepth(OkexAdaptersV3.toSpotInstrument(currencyPair));

    } else {
      if (args[0] instanceof Long) {
        okexDepth = getDepth(OkexAdaptersV3.toSpotInstrument(currencyPair), (Integer) args[0]);

      } else {
        String arg = (String) args[0];
        if (arg.toUpperCase().equals("SWAP")) {
          SwapContract swapContract = new SwapContract(currencyPair, (String) args[0]);
          Object[] newArgs = new Object[args.length - 1];
          System.arraycopy(args, 1, newArgs, 0, args.length - 1);
          okexDepth = getSwapDepth(OkexAdaptersV3.toSwapInstrument(swapContract));

        } else {
          FuturesContract futuresContract = new FuturesContract(currencyPair, (String) args[0]);
          Object[] newArgs = new Object[args.length - 1];
          System.arraycopy(args, 1, newArgs, 0, args.length - 1);
          okexDepth = getFuturesDepth(OkexAdaptersV3.toFuturesInstrument(futuresContract));
        }
      }
    }

    return convertOrderBook(okexDepth, currencyPair);
  }

  public static OrderBook convertOrderBook(OkexDepth ob, CurrencyPair pair) {
    List<LimitOrder> bids =
        ob.bids
            .entrySet()
            .stream()
            .map(e -> new LimitOrder(OrderType.BID, e.getValue(), pair, null, null, e.getKey()))
            .collect(Collectors.toList());
    List<LimitOrder> asks =
        ob.asks
            .entrySet()
            .stream()
            .map(e -> new LimitOrder(OrderType.ASK, e.getValue(), pair, null, null, e.getKey()))
            .collect(Collectors.toList());
    return new OrderBook(ob.getTimestamp(), asks, bids);
  }
}
