package info.bitrich.xchangestream.coinbasepro;

import static org.knowm.xchange.coinbasepro.CoinbaseProAdapters.adaptOrderBook;
import static org.knowm.xchange.coinbasepro.CoinbaseProAdapters.adaptTicker;
import static org.knowm.xchange.coinbasepro.CoinbaseProAdapters.adaptTrades;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.knowm.xchange.coinbasepro.dto.marketdata.CoinbaseProProductBook;
import org.knowm.xchange.coinbasepro.dto.marketdata.CoinbaseProProductTicker;
import org.knowm.xchange.coinbasepro.dto.marketdata.CoinbaseProTrade;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.marketdata.Trades;

import com.fasterxml.jackson.databind.ObjectMapper;

import info.bitrich.xchangestream.coinbasepro.dto.CoinbaseProWebSocketTransaction;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import io.reactivex.Observable;

/**
 * Created by luca on 4/3/17.
 */
public class CoinbaseProStreamingMarketDataService implements StreamingMarketDataService {

    private static final String SNAPSHOT = "snapshot";
    private static final String L2UPDATE = "l2update";
    private static final String TICKER = "ticker";
    private static final String MATCH = "match";

    private final CoinbaseProStreamingService service;

    private final Map<CurrencyPair, SortedMap<BigDecimal, String>> bids = new HashMap<>();
    private final Map<CurrencyPair, SortedMap<BigDecimal, String>> asks = new HashMap<>();

    CoinbaseProStreamingMarketDataService(CoinbaseProStreamingService service) {
        this.service = service;
    }

    private boolean containsPair(List<CurrencyPair> pairs, CurrencyPair pair) {
        for (CurrencyPair item : pairs) {
            if (item.compareTo(pair) == 0) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Observable<OrderBook> getOrderBook(CurrencyPair currencyPair, Object... args) {
        if (!containsPair(service.getProduct().getOrderBook(), currencyPair))
            throw new UnsupportedOperationException(String.format("The currency pair %s is not subscribed for orderbook", currencyPair));
        final int maxDepth = (args.length > 0 && args[0] instanceof Integer) ? (int) args[0] : 100;
        return getRawWebSocketTransactions(currencyPair, false)
                .filter(message -> message.getType().equals(SNAPSHOT) || message.getType().equals(L2UPDATE))
                .map(s -> {
                    if (s.getType().equals(SNAPSHOT)) {
                        bids.put(currencyPair, new TreeMap<>(java.util.Collections.reverseOrder()));
                        asks.put(currencyPair, new TreeMap<>());
                    }
                    CoinbaseProProductBook productBook = s.toCoinbaseProProductBook(bids.get(currencyPair), asks.get(currencyPair), maxDepth);
                    return adaptOrderBook(productBook, currencyPair);
                });
    }

    /**
     * Returns an Observable of {@link CoinbaseProProductTicker}, not converted to {@link Ticker}
     *
     * @param currencyPair the currency pair.
     * @param args         optional arguments.
     * @return an Observable of {@link CoinbaseProProductTicker}.
     */
    public Observable<CoinbaseProProductTicker> getRawTicker(CurrencyPair currencyPair, Object... args) {
        if (!containsPair(service.getProduct().getTicker(), currencyPair))
            throw new UnsupportedOperationException(String.format("The currency pair %s is not subscribed for ticker", currencyPair));
        return getRawWebSocketTransactions(currencyPair, true)
                .filter(message -> message.getType().equals(TICKER))
                .map(CoinbaseProWebSocketTransaction::toCoinbaseProProductTicker);
    }

    /**
     * Returns the CoinbasePro ticker converted to the normalized XChange object.
     * CoinbasePro does not directly provide ticker data via web service.
     * As stated by: https://docs.coinbasepro.com/#get-product-ticker, we can just listen for 'match' messages.
     *
     * @param currencyPair Currency pair of the ticker
     * @param args         optional parameters.
     * @return an Observable of normalized Ticker objects.
     */
    @Override
    public Observable<Ticker> getTicker(CurrencyPair currencyPair, Object... args) {
        if (!containsPair(service.getProduct().getTicker(), currencyPair))
            throw new UnsupportedOperationException(String.format("The currency pair %s is not subscribed for ticker", currencyPair));
        return getRawWebSocketTransactions(currencyPair, true)
                .filter(message -> message.getType().equals(TICKER))
                .map(s -> adaptTicker(s.toCoinbaseProProductTicker(), s.toCoinbaseProProductStats(), currencyPair));
    }

    @Override
    public Observable<Trade> getTrades(CurrencyPair currencyPair, Object... args) {
        if (!containsPair(service.getProduct().getTrades(), currencyPair))
            throw new UnsupportedOperationException(String.format("The currency pair %s is not subscribed for trades", currencyPair));
        return getRawWebSocketTransactions(currencyPair, true)
                .filter(message -> message.getType().equals(MATCH))
                .filter((CoinbaseProWebSocketTransaction s) -> s.getUserId() == null)
                .map((CoinbaseProWebSocketTransaction s) -> s.toCoinbaseProTrade())
                .map((CoinbaseProTrade t) -> adaptTrades(new CoinbaseProTrade[]{t}, currencyPair))
                .map((Trades h) -> h.getTrades().get(0));
    }

    /**
     * Web socket transactions related to the specified currency, in their raw format.
     *
     * @param currencyPair The currency pair.
     * @return The stream.
     */
    public Observable<CoinbaseProWebSocketTransaction> getRawWebSocketTransactions(CurrencyPair currencyPair, boolean filterChannelName) {
        return service.getRawWebSocketTransactions(currencyPair, filterChannelName);
    }
}