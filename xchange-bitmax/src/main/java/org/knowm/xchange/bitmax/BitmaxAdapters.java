package org.knowm.xchange.bitmax;

import org.knowm.xchange.bitmax.dto.account.BitmaxCashAccountBalanceDto;
import org.knowm.xchange.bitmax.dto.marketdata.BitmaxAssetDto;
import org.knowm.xchange.bitmax.dto.marketdata.BitmaxMarketTradesDto;
import org.knowm.xchange.bitmax.dto.marketdata.BitmaxOrderbookDto;
import org.knowm.xchange.bitmax.dto.marketdata.BitmaxProductDto;
import org.knowm.xchange.bitmax.dto.trade.BitmaxFlags;
import org.knowm.xchange.bitmax.dto.trade.BitmaxOpenOrdersResponse;
import org.knowm.xchange.bitmax.dto.trade.BitmaxPlaceOrderRequestPayload;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.meta.CurrencyMetaData;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.utils.jackson.CurrencyPairDeserializer;

import java.time.Instant;
import java.util.*;

public class BitmaxAdapters {

  public static OrderBook adaptOrderBook(BitmaxOrderbookDto bitmaxOrderbookDto) {
    List<LimitOrder> asks = new ArrayList<>();
    List<LimitOrder> bids = new ArrayList<>();

    CurrencyPair currencyPair =
        CurrencyPairDeserializer.getCurrencyPairFromString(bitmaxOrderbookDto.getSymbol());

    bitmaxOrderbookDto
        .getData()
        .getAsks()
        .forEach(
            askOrder ->
                asks.add(
                    new LimitOrder.Builder(Order.OrderType.ASK, currencyPair)
                        .limitPrice(askOrder.getPrice())
                        .originalAmount(askOrder.getVolume())
                        .build()));

    bitmaxOrderbookDto
        .getData()
        .getBids()
        .forEach(
            bidOrder ->
                bids.add(
                    new LimitOrder.Builder(Order.OrderType.BID, currencyPair)
                        .limitPrice(bidOrder.getPrice())
                        .originalAmount(bidOrder.getVolume())
                        .build()));

    return new OrderBook(bitmaxOrderbookDto.getData().getTimestamp(), asks, bids);
  }

  public static AccountInfo adaptAccountInfo(
      List<BitmaxCashAccountBalanceDto> bitmaxCashAccountBalanceDtoList) {
    List<Balance> balances = new ArrayList<>();

    bitmaxCashAccountBalanceDtoList.forEach(
        bitmaxCashAccountBalanceDto ->
            balances.add(
                new Balance.Builder()
                    .currency(new Currency(bitmaxCashAccountBalanceDto.getAsset()))
                    .available(bitmaxCashAccountBalanceDto.getAvailableBalance())
                    .total(bitmaxCashAccountBalanceDto.getTotalBalance())
                    .frozen(
                        bitmaxCashAccountBalanceDto
                            .getTotalBalance()
                            .subtract(bitmaxCashAccountBalanceDto.getAvailableBalance()))
                    .build()));

    return new AccountInfo(
        Wallet.Builder.from(balances)
            .id("spot")
            .features(new HashSet<>(Collections.singletonList(Wallet.WalletFeature.TRADING)))
            .build());
  }

  public static BitmaxPlaceOrderRequestPayload adaptLimitOrderToBitmaxPlaceOrderRequestPayload(
      LimitOrder limitOrder) {
    return new BitmaxPlaceOrderRequestPayload(
        limitOrder.getInstrument().toString(),
        Date.from(Instant.now()).toInstant().toEpochMilli(),
        limitOrder.getOriginalAmount().toString(),
        BitmaxPlaceOrderRequestPayload.BitmaxOrderType.limit,
        limitOrder.getType().equals(Order.OrderType.ASK)
            ? BitmaxPlaceOrderRequestPayload.BitmaxSide.sell
            : BitmaxPlaceOrderRequestPayload.BitmaxSide.buy,
        null,
        limitOrder.getLimitPrice().toString(),
        null,
        limitOrder.hasFlag(BitmaxFlags.POST_ONLY),
        null,
        null);
  }

  public static UserTrades adaptUserTrades(
      List<BitmaxOpenOrdersResponse> bitmaxOrderHistoryResponse) {
    List<UserTrade> userTrades = new ArrayList<>();

    bitmaxOrderHistoryResponse.forEach(
        order ->
            userTrades.add(
                new UserTrade.Builder()
                    .feeAmount(order.getCumFee())
                    .orderId(order.getOrderId())
                    .price(order.getPrice())
                    .type(adaptBitmaxSideToOrderType(order.getSide()))
                    .originalAmount(order.getOrderQty())
                    .id(order.getOrderId())
                    .timestamp(order.getLastExecTime())
                    .currencyPair(
                        CurrencyPairDeserializer.getCurrencyPairFromString(order.getSymbol()))
                    .feeCurrency(new Currency(order.getFeeAsset()))
                    .instrument(
                        CurrencyPairDeserializer.getCurrencyPairFromString(order.getSymbol()))
                    .build()));

    return new UserTrades(userTrades, Trades.TradeSortType.SortByTimestamp);
  }

  public static OpenOrders adaptOpenOrders(
      List<BitmaxOpenOrdersResponse> bitmaxOpenOrdersResponses) {
    List<LimitOrder> openOrders = new ArrayList<>();

    bitmaxOpenOrdersResponses.forEach(
        bitmaxOpenOrdersResponse ->
            openOrders.add(
                new LimitOrder.Builder(
                        adaptBitmaxSideToOrderType(bitmaxOpenOrdersResponse.getSide()),
                        CurrencyPairDeserializer.getCurrencyPairFromString(
                            bitmaxOpenOrdersResponse.getSymbol()))
                    .originalAmount(bitmaxOpenOrdersResponse.getOrderQty())
                    .limitPrice(bitmaxOpenOrdersResponse.getPrice())
                    .fee(bitmaxOpenOrdersResponse.getCumFee())
                    .averagePrice(bitmaxOpenOrdersResponse.getAvgPx())
                    .id(bitmaxOpenOrdersResponse.getOrderId())
                    .timestamp(bitmaxOpenOrdersResponse.getLastExecTime())
                    .orderStatus(
                        Order.OrderStatus.valueOf(
                            bitmaxOpenOrdersResponse.getStatus().toUpperCase()))
                    .remainingAmount(
                        bitmaxOpenOrdersResponse
                            .getOrderQty()
                            .subtract(bitmaxOpenOrdersResponse.getCumFilledQty()))
                    .flag(
                        (bitmaxOpenOrdersResponse.getExecInst().equals("POST"))
                            ? BitmaxFlags.POST_ONLY
                            : null)
                    .build()));

    return new OpenOrders(openOrders);
  }

  public static List<Order> adaptOpenOrderById(BitmaxOpenOrdersResponse bitmaxOpenOrdersResponse) {
    List<Order> openOrders = new ArrayList<>();

    openOrders.add(
        new LimitOrder.Builder(
                adaptBitmaxSideToOrderType(bitmaxOpenOrdersResponse.getSide()),
                CurrencyPairDeserializer.getCurrencyPairFromString(
                    bitmaxOpenOrdersResponse.getSymbol()))
            .originalAmount(bitmaxOpenOrdersResponse.getOrderQty())
            .limitPrice(bitmaxOpenOrdersResponse.getPrice())
            .fee(bitmaxOpenOrdersResponse.getCumFee())
            .averagePrice(bitmaxOpenOrdersResponse.getAvgPx())
            .id(bitmaxOpenOrdersResponse.getOrderId())
            .timestamp(bitmaxOpenOrdersResponse.getLastExecTime())
            .orderStatus(
                Order.OrderStatus.valueOf(bitmaxOpenOrdersResponse.getStatus().toUpperCase()))
            .remainingAmount(
                bitmaxOpenOrdersResponse
                    .getOrderQty()
                    .subtract(bitmaxOpenOrdersResponse.getCumFilledQty()))
            .flag(
                (bitmaxOpenOrdersResponse.getExecInst().equals("POST"))
                    ? BitmaxFlags.POST_ONLY
                    : null)
            .build());

    return openOrders;
  }

  public static ExchangeMetaData adaptExchangeMetaData(
      List<BitmaxAssetDto> bitmaxAssetDtos, List<BitmaxProductDto> bitmaxProductDtos) {
    Map<Currency, CurrencyMetaData> currencyMetaDataMap = new HashMap<>();
    Map<CurrencyPair, CurrencyPairMetaData> currencyPairMetaDataMap = new HashMap<>();

    bitmaxAssetDtos.forEach(
        bitmaxAssetDto ->
            currencyMetaDataMap.put(
                new Currency(bitmaxAssetDto.getAssetCode()),
                new CurrencyMetaData(
                    bitmaxAssetDto.getPrecisionScale(),
                    bitmaxAssetDto.getWithdrawFee(),
                    bitmaxAssetDto.getMinWithdrawalAmt())));

    bitmaxProductDtos.forEach(
        bitmaxProductDto ->
            currencyPairMetaDataMap.put(
                CurrencyPairDeserializer.getCurrencyPairFromString(bitmaxProductDto.getSymbol()),
                new CurrencyPairMetaData.Builder()
                    .tradingFee(bitmaxProductDto.getCommissionReserveRate())
                    .priceScale(bitmaxProductDto.getTickSize().scale())
                    .baseScale(bitmaxProductDto.getLotSize().scale())
                    .counterMaximumAmount(bitmaxProductDto.getMinNotional())
                    .counterMaximumAmount(bitmaxProductDto.getMaxNotional())
                    .amountStepSize(bitmaxProductDto.getTickSize())
                    .build()));

    return new ExchangeMetaData(currencyPairMetaDataMap, currencyMetaDataMap, null, null, null);
  }

  public static Order.OrderType adaptBitmaxSideToOrderType(
      BitmaxPlaceOrderRequestPayload.BitmaxSide bitmaxSide) {
    if (bitmaxSide.equals(BitmaxPlaceOrderRequestPayload.BitmaxSide.buy)) {
      return Order.OrderType.BID;
    } else {
      return Order.OrderType.ASK;
    }
  }

  public static Trades adaptTrades(BitmaxMarketTradesDto marketTradesDto) {
    List<Trade> trades = new ArrayList<>();

    marketTradesDto
        .getData()
        .forEach(
            bitmaxMarketTradesData ->
                trades.add(
                    new Trade.Builder()
                        .price(bitmaxMarketTradesData.getPrice())
                        .originalAmount(bitmaxMarketTradesData.getQuantity())
                        .timestamp(bitmaxMarketTradesData.getTimestamp())
                        .instrument(new CurrencyPair(marketTradesDto.getSymbol()))
                        .type(
                            bitmaxMarketTradesData.isBuyerMaker()
                                ? Order.OrderType.ASK
                                : Order.OrderType.BID)
                        .build()));

    return new Trades(trades, Trades.TradeSortType.SortByTimestamp);
  }
}
