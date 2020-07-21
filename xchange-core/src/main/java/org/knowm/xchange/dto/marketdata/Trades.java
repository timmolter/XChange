package org.knowm.xchange.dto.marketdata;

import static org.knowm.xchange.dto.marketdata.Trades.TradeSortType.SortByID;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** DTO representing a collection of trades */
public class Trades implements Serializable {

  private static final long serialVersionUID = 5790082783307641329L;

  private static final TradeIDComparator TRADE_ID_COMPARATOR = new TradeIDComparator();
  private static final TradeTimestampComparator TRADE_TIMESTAMP_COMPARATOR =
      new TradeTimestampComparator();

  private final List<Trade> trades;
  private final String lastID;
  private final String nextPageCursor;
  private final TradeSortType tradeSortType;

  /**
   * Constructor Default sort is SortByID
   *
   * @param trades List of trades
   */
  public Trades(List<Trade> trades) {

    this(trades, 0L, SortByID);
  }

  /**
   * Constructor
   *
   * @param trades List of trades
   * @param tradeSortType Trade sort type
   */
  public Trades(List<Trade> trades, TradeSortType tradeSortType) {

    this(trades, 0L, tradeSortType);
  }

  /**
   * Constructor
   *
   * @param trades A list of trades
   * @param lastID Last Unique ID
   * @param tradeSortType Trade sort type
   */
  public Trades(List<Trade> trades, String lastID, TradeSortType tradeSortType) {
    this(trades, lastID, tradeSortType, null);
  }

  /**
   * Constructor
   *
   * @param trades A list of trades
   * @param lastID Last Unique ID
   * @param tradeSortType Trade sort type
   */
  public Trades(List<Trade> trades, Long lastID, TradeSortType tradeSortType) {
    this(trades, String.valueOf(lastID), tradeSortType, null);
  }

  /**
   * Constructor
   *
   * @param trades A list of trades
   * @param lastID Last Unique ID
   * @param tradeSortType Trade sort type
   * @param nextPageCursor a marker that lets you receive the next page of trades using
   *     TradeHistoryParamNextPageCursor
   */
  public Trades(
      List<Trade> trades, String lastID, TradeSortType tradeSortType, String nextPageCursor) {

    this.trades = new ArrayList<>(trades);
    this.lastID = lastID;
    this.tradeSortType = tradeSortType;
    this.nextPageCursor = nextPageCursor;

    switch (tradeSortType) {
      case SortByTimestamp:
        Collections.sort(this.trades, TRADE_TIMESTAMP_COMPARATOR);
        break;
      case SortByID:
        Collections.sort(this.trades, TRADE_ID_COMPARATOR);
        break;

      default:
        break;
    }
  }

  /** @return A list of trades ordered by id */
  public List<Trade> getTrades() {

    return trades;
  }

  /** @return a Unique ID for the fetched trades */
  public String getlastID() {

    return lastID;
  }

  public TradeSortType getTradeSortType() {

    return tradeSortType;
  }

  public String getNextPageCursor() {
    return nextPageCursor;
  }

  @Override
  public String toString() {

    StringBuilder sb = new StringBuilder("Trades\n");
    sb.append("lastID= ").append(lastID).append("\n");

    for (Trade trade : getTrades()) {
      sb.append("[trade=").append(trade.toString()).append("]\n");
    }
    sb.append("nextPageCursor= ").append(nextPageCursor).append("\n");
    return sb.toString();
  }

  public enum TradeSortType {
    SortByTimestamp,
    SortByID
  }

  public static class TradeTimestampComparator implements Comparator<Trade> {

    @Override
    public int compare(Trade trade1, Trade trade2) {

      return trade1.getTimestamp().compareTo(trade2.getTimestamp());
    }
  }

  public static class TradeIDComparator implements Comparator<Trade> {

    private static final int[] ALLOWED_RADIXES = {10, 16};

    @Override
    public int compare(Trade trade1, Trade trade2) {
      for (int radix : ALLOWED_RADIXES) {
        try {
          BigInteger id1 = new BigInteger(trade1.getId(), radix);
          BigInteger id2 = new BigInteger(trade2.getId(), radix);
          return id1.compareTo(id2);
        } catch (NumberFormatException ignored) {
        }
      }
      return trade1.getId().compareTo(trade2.getId());
    }
  }
}
