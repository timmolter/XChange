package org.knowm.xchange.binance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.knowm.xchange.binance.dto.account.AssetDetail;
import org.knowm.xchange.binance.dto.marketdata.BinancePriceQuantity;
import org.knowm.xchange.binance.dto.trade.BinanceOrder;
import org.knowm.xchange.binance.dto.trade.OrderSide;
import org.knowm.xchange.binance.dto.trade.OrderStatus;
import org.knowm.xchange.binance.service.BinanceTradeService.BinanceOrderFlags;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.meta.CurrencyMetaData;
import org.knowm.xchange.dto.meta.WalletHealth;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.StopOrder;

public class BinanceAdapters {

  private BinanceAdapters() {}

  public static String toSymbol(CurrencyPair pair) {
    if (pair.equals(CurrencyPair.IOTA_BTC)) {
      return "IOTABTC";
    }
    return pair.base.getCurrencyCode() + pair.counter.getCurrencyCode();
  }

  public static String toSymbol(Currency currency) {
    if (Currency.IOT.equals(currency)) {
      return "IOTA";
    }
    return currency.getSymbol();
  }

  public static OrderType convert(OrderSide side) {
    switch (side) {
      case BUY:
        return OrderType.BID;
      case SELL:
        return OrderType.ASK;
      default:
        throw new RuntimeException("Not supported order side: " + side);
    }
  }

  public static OrderSide convert(OrderType type) {
    switch (type) {
      case ASK:
        return OrderSide.SELL;
      case BID:
        return OrderSide.BUY;
      default:
        throw new RuntimeException("Not supported order type: " + type);
    }
  }

  public static CurrencyPair convert(String symbol) {
    // Iterate by base currency priority at binance.
    for (Currency base : Arrays.asList(Currency.BTC, Currency.ETH, Currency.BNB, Currency.USDT)) {
      if (symbol.contains(base.toString())) {
        String counter = symbol.replace(base.toString(), "");
        return new CurrencyPair(base, new Currency(counter));
      }
    }
    throw new IllegalArgumentException("Could not parse currency pair from '" + symbol + "'");
  }

  public static long id(String id) {
    try {
      return Long.valueOf(id);
    } catch (Throwable e) {
      throw new IllegalArgumentException("Binance id must be a valid long number.", e);
    }
  }

  public static Order.OrderStatus adaptOrderStatus(OrderStatus orderStatus) {
    switch (orderStatus) {
      case NEW:
        return Order.OrderStatus.NEW;
      case FILLED:
        return Order.OrderStatus.FILLED;
      case EXPIRED:
        return Order.OrderStatus.EXPIRED;
      case CANCELED:
        return Order.OrderStatus.CANCELED;
      case REJECTED:
        return Order.OrderStatus.REJECTED;
      case PENDING_CANCEL:
        return Order.OrderStatus.PENDING_CANCEL;
      case PARTIALLY_FILLED:
        return Order.OrderStatus.PARTIALLY_FILLED;
      default:
        return Order.OrderStatus.UNKNOWN;
    }
  }

  public static OrderType convertType(boolean isBuyer) {
    return isBuyer ? OrderType.BID : OrderType.ASK;
  }

  public static CurrencyPair adaptSymbol(String symbol) {
    int pairLength = symbol.length();
    if (symbol.endsWith("USDT")) {
      return new CurrencyPair(symbol.substring(0, pairLength - 4), "USDT");
    } else if (symbol.endsWith("USDC")) {
      return new CurrencyPair(symbol.substring(0, pairLength - 4), "USDC");
    } else if (symbol.endsWith("TUSD")) {
      return new CurrencyPair(symbol.substring(0, pairLength - 4), "TUSD");
    } else if (symbol.endsWith("USDS")) {
      return new CurrencyPair(symbol.substring(0, pairLength - 4), "USDS");
    } else if (symbol.endsWith("BUSD")) {
      return new CurrencyPair(symbol.substring(0, pairLength - 4), "BUSD");
    } else {
      return new CurrencyPair(
          symbol.substring(0, pairLength - 3), symbol.substring(pairLength - 3));
    }
  }


  public static Order adaptOrder(BinanceOrder order) {
    OrderType type = convert(order.side);
    CurrencyPair currencyPair = adaptSymbol(order.symbol);
    Order.Builder builder;
    if (order.type.equals(org.knowm.xchange.binance.dto.trade.OrderType.MARKET)) {
      builder = new MarketOrder.Builder(type, currencyPair);
    } else if (order.type.equals(org.knowm.xchange.binance.dto.trade.OrderType.LIMIT)
        || order.type.equals(org.knowm.xchange.binance.dto.trade.OrderType.LIMIT_MAKER)) {
      builder = new LimitOrder.Builder(type, currencyPair).limitPrice(order.price);
    } else {
      builder = new StopOrder.Builder(type, currencyPair).stopPrice(order.stopPrice);
    }
    builder
        .orderStatus(adaptOrderStatus(order.status))
        .originalAmount(order.origQty)
        .id(Long.toString(order.orderId))
        .timestamp(order.getTime())
        .cumulativeAmount(order.executedQty);
    if (order.executedQty.signum() != 0 && order.cummulativeQuoteQty.signum() != 0) {
      builder.averagePrice(
          order.cummulativeQuoteQty.divide(order.executedQty, MathContext.DECIMAL32));
    }
    if (order.clientOrderId != null) {
      builder.flag(BinanceOrderFlags.withClientId(order.clientOrderId));
    }
    return builder.build();
  }

  private static Ticker adaptPriceQuantity(BinancePriceQuantity priceQuantity) {
    return new Ticker.Builder()
        .currencyPair(adaptSymbol(priceQuantity.symbol))
        .ask(priceQuantity.askPrice)
        .askSize(priceQuantity.askQty)
        .bid(priceQuantity.bidPrice)
        .bidSize(priceQuantity.bidQty)
        .build();
  }

  public static List<Ticker> adaptPriceQuantities(List<BinancePriceQuantity> priceQuantities) {
    return priceQuantities.stream()
        .map(BinanceAdapters::adaptPriceQuantity)
        .collect(Collectors.toList());
  }

  static CurrencyMetaData adaptCurrencyMetaData(
      Map<Currency, CurrencyMetaData> currencies,
      Currency currency,
      Map<String, AssetDetail> assetDetailMap,
      int precision) {
    if (assetDetailMap != null) {
      AssetDetail asset = assetDetailMap.get(currency.getCurrencyCode());
      if (asset != null) {
        BigDecimal withdrawalFee = asset.getWithdrawFee().stripTrailingZeros();
        BigDecimal minWithdrawalAmount =
            new BigDecimal(asset.getMinWithdrawAmount()).stripTrailingZeros();
        WalletHealth walletHealth =
            getWalletHealth(asset.isDepositStatus(), asset.isWithdrawStatus());
        return new CurrencyMetaData(precision, withdrawalFee, minWithdrawalAmount, walletHealth);
      }
    }

    BigDecimal withdrawalFee = null;
    BigDecimal minWithdrawalAmount = null;
    if (currencies.containsKey(currency)) {
      CurrencyMetaData currencyMetaData = currencies.get(currency);
      withdrawalFee = currencyMetaData.getWithdrawalFee();
      minWithdrawalAmount = currencyMetaData.getMinWithdrawalAmount();
    }
    return new CurrencyMetaData(precision, withdrawalFee, minWithdrawalAmount);
  }

  private static WalletHealth getWalletHealth(boolean depositEnabled, boolean withdrawEnabled) {
    if (depositEnabled && withdrawEnabled) {
      return WalletHealth.ONLINE;
    }
    if (!depositEnabled && withdrawEnabled) {
      return WalletHealth.DEPOSITS_DISABLED;
    }
    if (depositEnabled) {
      return WalletHealth.WITHDRAWALS_DISABLED;
    }
    return WalletHealth.OFFLINE;
  }

  public static org.knowm.xchange.binance.dto.trade.OrderType adaptOrderType(StopOrder order) {

    if (order.getIntention() == null) {
      throw new IllegalArgumentException("Missing intention");
    }

    switch (order.getIntention()) {
      case STOP_LOSS:
        return order.getLimitPrice() == null
            ? org.knowm.xchange.binance.dto.trade.OrderType.STOP_LOSS
            : org.knowm.xchange.binance.dto.trade.OrderType.STOP_LOSS_LIMIT;
      case TAKE_PROFIT:
        return order.getLimitPrice() == null
            ? org.knowm.xchange.binance.dto.trade.OrderType.TAKE_PROFIT
            : org.knowm.xchange.binance.dto.trade.OrderType.TAKE_PROFIT_LIMIT;
      default:
        throw new IllegalStateException("Unexpected value: " + order.getIntention());
    }
  }
}
