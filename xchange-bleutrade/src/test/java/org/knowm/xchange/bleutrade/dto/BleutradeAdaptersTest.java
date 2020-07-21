package org.knowm.xchange.bleutrade.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.knowm.xchange.bleutrade.BleutradeAdapters;
import org.knowm.xchange.bleutrade.BleutradeAssert;
import org.knowm.xchange.bleutrade.dto.account.BleutradeBalancesReturn;
import org.knowm.xchange.bleutrade.dto.marketdata.BleutradeCurrenciesReturn;
import org.knowm.xchange.bleutrade.dto.marketdata.BleutradeMarketHistoryReturn;
import org.knowm.xchange.bleutrade.dto.marketdata.BleutradeMarketsReturn;
import org.knowm.xchange.bleutrade.dto.marketdata.BleutradeOrderBookReturn;
import org.knowm.xchange.bleutrade.dto.marketdata.BleutradeTickerReturn;
import org.knowm.xchange.bleutrade.dto.trade.BleutradeOpenOrdersReturn;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.meta.CurrencyMetaData;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;

public class BleutradeAdaptersTest extends BleutradeDtoTestSupport {

  @Test
  public void shouldAdaptBalances() throws IOException {
    // given
    final BleutradeBalancesReturn response = parse(BleutradeBalancesReturn.class);
    final Balance[] expectedBalances = expectedBalances();

    // when
    Wallet wallet = BleutradeAdapters.adaptBleutradeBalances(response.getResult());

    // then
    assertThat(wallet.getBalances()).hasSize(2);

    BleutradeAssert.assertEquals(wallet.getBalance(Currency.DOGE), expectedBalances[0]);
    BleutradeAssert.assertEquals(wallet.getBalance(Currency.BTC), expectedBalances[1]);
  }

  @Test
  public void shouldAdaptMarkets() throws IOException {
    // given
    final BleutradeMarketsReturn response = parse(BleutradeMarketsReturn.class);

    // when
    Set<CurrencyPair> currencyPairs = BleutradeAdapters.adaptBleutradeCurrencyPairs(response);

    // then
    assertThat(currencyPairs).hasSize(2);
    assertThat(currencyPairs).contains(CurrencyPair.DOGE_BTC, BLEU_BTC_CP);
  }

  @Test
  public void shouldAdaptMarketHistory() throws IOException {
    // given
    final BleutradeMarketHistoryReturn response = parse(BleutradeMarketHistoryReturn.class);
    final Trade[] expectedTrades = expectedTrades();

    // when
    Trades trades =
        BleutradeAdapters.adaptBleutradeMarketHistory(response.getResult(), CurrencyPair.BTC_AUD);

    // then
    assertThat(trades.getlastID()).isEqualTo("0");

    List<Trade> tradeList = trades.getTrades();
    assertThat(tradeList).hasSize(2);

    for (int i = 0; i < tradeList.size(); i++) {
      BleutradeAssert.assertEquals(tradeList.get(i), expectedTrades[i]);
    }
  }

  @Test
  public void shouldAdaptOpenOrders() throws IOException {
    // given
    final BleutradeOpenOrdersReturn response = parse(BleutradeOpenOrdersReturn.class);
    final LimitOrder[] expectedOrders = expectedOrders();

    // when
    OpenOrders openOrders = BleutradeAdapters.adaptBleutradeOpenOrders(response.getResult());

    // then
    List<LimitOrder> orderList = openOrders.getOpenOrders();
    assertThat(orderList).hasSize(2);

    for (int i = 0; i < orderList.size(); i++) {
      BleutradeAssert.assertEquals(orderList.get(i), expectedOrders[i]);
    }
  }

  @Test
  public void shouldAdaptOrderBook() throws IOException {
    // given
    final BleutradeOrderBookReturn response = parse(BleutradeOrderBookReturn.class);
    final LimitOrder[] expectedBids = expectedBids();
    final LimitOrder[] expectedAsks = expectedAsks();

    // when
    OrderBook orderBook =
        BleutradeAdapters.adaptBleutradeOrderBook(response.getResult(), CurrencyPair.BTC_AUD);

    // then
    List<LimitOrder> bids = orderBook.getBids();
    assertThat(bids).hasSize(2);

    for (int i = 0; i < bids.size(); i++) {
      BleutradeAssert.assertEquals(bids.get(i), expectedBids[i]);
    }

    List<LimitOrder> asks = orderBook.getAsks();
    assertThat(asks).hasSize(4);

    for (int i = 0; i < asks.size(); i++) {
      BleutradeAssert.assertEquals(asks.get(i), expectedAsks[i]);
    }
  }

  @Test
  public void shouldAdaptTicker() throws IOException {
    // given
    final BleutradeTickerReturn response = parse(BleutradeTickerReturn.class);
    final Ticker expectedTicker = expectedTicker();

    // when
    Ticker ticker = BleutradeAdapters.adaptBleutradeTicker(response.getResult().get(0));

    // then
    BleutradeAssert.assertEquals(ticker, expectedTicker);
  }

  @Test
  public void shouldAdaptExchangeMetaData() throws IOException {
    // given
    final BleutradeCurrenciesReturn currenciesResponse = parse(BleutradeCurrenciesReturn.class);
    final BleutradeMarketsReturn marketsResponse = parse(BleutradeMarketsReturn.class);
    final CurrencyPairMetaData[] expectedMetaDataList = expectedMetaDataList();
    final String[] expectedMetaDataStr = expectedMetaDataStr();

    // when
    ExchangeMetaData exchangeMetaData =
        BleutradeAdapters.adaptToExchangeMetaData(
            currenciesResponse.getResult(), marketsResponse.getResult());

    // then
    Map<Currency, CurrencyMetaData> currencyMetaDataMap = exchangeMetaData.getCurrencies();
    assertThat(currencyMetaDataMap).hasSize(2);
    assertThat(currencyMetaDataMap.get(Currency.BTC).getScale()).isEqualTo(8);
    assertThat(currencyMetaDataMap.get(Currency.LTC).getScale()).isEqualTo(8);

    Map<CurrencyPair, CurrencyPairMetaData> marketMetaDataMap = exchangeMetaData.getCurrencyPairs();
    assertThat(marketMetaDataMap).hasSize(2);

    // there is no reliable information about valid tradingFee calculation formula
    BleutradeAssert.assertEquals(
        marketMetaDataMap.get(CurrencyPair.DOGE_BTC), expectedMetaDataList[0]);
    assertThat(marketMetaDataMap.get(CurrencyPair.DOGE_BTC).toString())
        .isEqualTo(expectedMetaDataStr[0]);

    BleutradeAssert.assertEquals(marketMetaDataMap.get(BLEU_BTC_CP), expectedMetaDataList[1]);
    assertThat(marketMetaDataMap.get(BLEU_BTC_CP).toString()).isEqualTo(expectedMetaDataStr[1]);
  }
}
