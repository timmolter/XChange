package org.knowm.xchange.ftx;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.meta.CurrencyMetaData;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.dto.meta.RateLimit;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.ftx.dto.FtxResponse;
import org.knowm.xchange.ftx.dto.account.FtxAccountDto;
import org.knowm.xchange.ftx.dto.account.FtxWalletBalanceDto;
import org.knowm.xchange.ftx.dto.marketdata.FtxMarketsDto;
import org.knowm.xchange.ftx.dto.marketdata.FtxOrderbookDto;
import org.knowm.xchange.ftx.dto.trade.*;
import org.knowm.xchange.utils.jackson.CurrencyPairDeserializer;

public class FtxAdapters {

  public static OrderBook adaptOrderBook(
      FtxResponse<FtxOrderbookDto> ftxOrderbookDto, CurrencyPair currencyPair) {

    List<LimitOrder> asks = new ArrayList<>();
    List<LimitOrder> bids = new ArrayList<>();

    ftxOrderbookDto
        .getResult()
        .getAsks()
        .forEach(
            ftxAsk -> {
              asks.add(
                  adaptOrderbookOrder(
                      ftxAsk.getVolume(), ftxAsk.getPrice(), currencyPair, Order.OrderType.ASK));
            });

    ftxOrderbookDto
        .getResult()
        .getBids()
        .forEach(
            ftxBid -> {
              bids.add(
                  adaptOrderbookOrder(
                      ftxBid.getVolume(), ftxBid.getPrice(), currencyPair, Order.OrderType.BID));
            });

    return new OrderBook(Date.from(Instant.now()), asks, bids);
  }

  public static LimitOrder adaptOrderbookOrder(
      BigDecimal amount, BigDecimal price, CurrencyPair currencyPair, Order.OrderType orderType) {

    return new LimitOrder(orderType, amount, currencyPair, "", null, price);
  }

  public static AccountInfo adaptAccountInfo(
      FtxResponse<FtxAccountDto> ftxAccountDto,
      FtxResponse<List<FtxWalletBalanceDto>> ftxBalancesDto) {

    List<Balance> ftxAccountInfo = new ArrayList<>();
    List<Balance> ftxSpotBalances = new ArrayList<>();

    ftxAccountInfo.add(
        new Balance(
            Currency.USD,
            ftxAccountDto.getResult().getCollateral(),
            ftxAccountDto.getResult().getFreeCollateral()));

    ftxBalancesDto
        .getResult()
        .forEach(
            ftxWalletBalanceDto -> {
              ftxSpotBalances.add(
                  new Balance(
                      ftxWalletBalanceDto.getCoin(),
                      ftxWalletBalanceDto.getTotal(),
                      ftxWalletBalanceDto.getFree()));
            });

    Wallet accountWallet =
        Wallet.Builder.from(ftxAccountInfo)
            .features(
                Collections.unmodifiableSet(
                    new HashSet<>(
                        Arrays.asList(
                            Wallet.WalletFeature.MARGIN_TRADING,
                            Wallet.WalletFeature.MARGIN_FUNDING))))
            .maxLeverage(ftxAccountDto.getResult().getLeverage())
            .currentLeverage(
                ftxAccountDto
                    .getResult()
                    .getTotalPositionSize()
                    .divide(
                        ftxAccountDto.getResult().getTotalAccountValue(),
                        3,
                        RoundingMode.HALF_EVEN))
            .id("margin")
            .build();

    Wallet spotWallet =
        Wallet.Builder.from(ftxSpotBalances)
            .features(
                Collections.unmodifiableSet(
                    new HashSet<>(
                        Arrays.asList(Wallet.WalletFeature.FUNDING, Wallet.WalletFeature.TRADING))))
            .id("spot")
            .build();

    return new AccountInfo(
        ftxAccountDto.getResult().getUsername(),
        ftxAccountDto.getResult().getTakerFee(),
        Collections.unmodifiableList(Arrays.asList(accountWallet, spotWallet)),
        Date.from(Instant.now()));
  }

  public static ExchangeMetaData adaptExchangeMetaData(FtxMarketsDto marketsDto) {

    Map<CurrencyPair, CurrencyPairMetaData> currencyPairs = new HashMap<>();
    Map<Currency, CurrencyMetaData> currency = new HashMap<>();

    marketsDto
        .getMarketList()
        .forEach(
            ftxMarketDto -> {
              CurrencyPairMetaData currencyPairMetaData =
                  new CurrencyPairMetaData.Builder()
                      .amountStepSize(ftxMarketDto.getPriceIncrement())
                      .minimumAmount(ftxMarketDto.getSizeIncrement())
                      .priceScale(ftxMarketDto.getPriceIncrement().scale())
                      .baseScale(ftxMarketDto.getSizeIncrement().scale())
                      .build();

              if ("spot".equals(ftxMarketDto.getType())) {
                CurrencyPair currencyPair =
                    new CurrencyPair(
                        ftxMarketDto.getBaseCurrency(), ftxMarketDto.getQuoteCurrency());
                currencyPairs.put(currencyPair, currencyPairMetaData);
                if (!currency.containsKey(currencyPair.base)) {
                  currency.put(
                      currencyPair.base,
                      new CurrencyMetaData(
                          ftxMarketDto.getSizeIncrement().scale(), BigDecimal.ZERO));
                }
                if (!currency.containsKey(currencyPair.counter)) {
                  currency.put(
                      currencyPair.counter,
                      new CurrencyMetaData(
                          ftxMarketDto.getPriceIncrement().scale(), BigDecimal.ZERO));
                }
              } else if ("future".equals(ftxMarketDto.getType())
                  && ftxMarketDto.getName().contains("/")) {
                CurrencyPair futuresContract = new CurrencyPair(ftxMarketDto.getName());
                currencyPairs.put(futuresContract, currencyPairMetaData);
              }
            });

    RateLimit[] rateLimits = {new RateLimit(30, 1, TimeUnit.SECONDS)};

    return new ExchangeMetaData(currencyPairs, currency, rateLimits, rateLimits, true);
  }

  public static FtxOrderRequestPayload adaptLimitOrderToFtxOrderPayload(LimitOrder limitOrder) {
    return new FtxOrderRequestPayload(
        limitOrder.getInstrument().toString(),
        adaptOrderTypeToFtxOrderSide(limitOrder.getType()),
        limitOrder.getLimitPrice(),
        FtxOrderType.limit,
        limitOrder.getOriginalAmount(),
        limitOrder.hasFlag(FtxOrderFlags.REDUCE_ONLY),
        limitOrder.hasFlag(FtxOrderFlags.IOC),
        limitOrder.hasFlag(FtxOrderFlags.POST_ONLY),
        limitOrder.getUserReference());
  }

  public static UserTrades adaptUserTrades(List<FtxOrderDto> ftxUserTrades) {
    List<UserTrade> userTrades = new ArrayList<>();

    ftxUserTrades.forEach(
        ftxOrderDto -> {
          userTrades.add(
              new UserTrade.Builder()
                  .instrument(
                      CurrencyPairDeserializer.getCurrencyPairFromString(ftxOrderDto.getMarket()))
                  .currencyPair(
                      CurrencyPairDeserializer.getCurrencyPairFromString(ftxOrderDto.getMarket()))
                  .timestamp(ftxOrderDto.getCreatedAt())
                  .id(ftxOrderDto.getId())
                  .orderUserReference(ftxOrderDto.getClientId())
                  .originalAmount(ftxOrderDto.getSize())
                  .type(adaptFtxOrderSideToOrderSide(ftxOrderDto.getSide()))
                  .price(
                      ftxOrderDto.getAvgFillPrice() == null
                          ? ftxOrderDto.getPrice()
                          : ftxOrderDto.getAvgFillPrice())
                  .build());
        });

    return new UserTrades(userTrades, Trades.TradeSortType.SortByTimestamp);
  }

  public static LimitOrder adaptLimitOrder(FtxOrderDto ftxOrderDto) {

    return new LimitOrder.Builder(
            adaptFtxOrderSideToOrderSide(ftxOrderDto.getSide()),
            CurrencyPairDeserializer.getCurrencyPairFromString(ftxOrderDto.getMarket()))
        .originalAmount(ftxOrderDto.getSize())
        .limitPrice(ftxOrderDto.getPrice())
        .averagePrice(ftxOrderDto.getAvgFillPrice())
        .userReference(ftxOrderDto.getClientId())
        .timestamp(ftxOrderDto.getCreatedAt())
        .flags(
            Collections.unmodifiableSet(
                new HashSet<>(
                    Arrays.asList(
                        (ftxOrderDto.isIoc() ? FtxOrderFlags.IOC : null),
                        (ftxOrderDto.isPostOnly() ? FtxOrderFlags.POST_ONLY : null),
                        (ftxOrderDto.isReduceOnly() ? FtxOrderFlags.REDUCE_ONLY : null)))))
        .remainingAmount(ftxOrderDto.getRemainingSize())
        .orderStatus(ftxOrderDto.getStatus())
        .id(ftxOrderDto.getId())
        .build();
  }

  public static OpenOrders adaptOpenOrders(FtxResponse<List<FtxOrderDto>> ftxOpenOrdersResponse) {
    List<LimitOrder> openOrders = new ArrayList<>();

    ftxOpenOrdersResponse
        .getResult()
        .forEach(
            ftxOrderDto -> {
              openOrders.add(adaptLimitOrder(ftxOrderDto));
            });

    return new OpenOrders(openOrders);
  }

  public static FtxOrderSide adaptOrderTypeToFtxOrderSide(Order.OrderType orderType) {

    switch (orderType) {
      case ASK:
        return FtxOrderSide.sell;
      case BID:
        return FtxOrderSide.buy;
      case EXIT_ASK:
        return null;
      case EXIT_BID:
        return null;
      default:
        return null;
    }
  }

  public static Order.OrderType adaptFtxOrderSideToOrderSide(FtxOrderSide ftxOrderSide) {

    return ftxOrderSide == FtxOrderSide.buy ? Order.OrderType.BID : Order.OrderType.ASK;
  }

  public static String adaptCurrencyPair(CurrencyPair currencyPair) {
    if (currencyPair.counter.getCurrencyCode().contains("PERP")
        || currencyPair.counter.getCurrencyCode().contains("[0-9]+")) {
      return currencyPair.base + "-" + currencyPair.counter;
    } else {
      return currencyPair.toString();
    }
  }
}
