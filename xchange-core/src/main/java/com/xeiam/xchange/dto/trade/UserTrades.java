package com.xeiam.xchange.dto.trade;

import java.util.List;

import com.xeiam.xchange.dto.marketdata.Trades;

public class UserTrades extends Trades {

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public UserTrades(List<UserTrade> trades, TradeSortType tradeSortType) {

    super((List) trades, tradeSortType);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public UserTrades(List<UserTrade> trades, long lastID, TradeSortType tradeSortType) {

    super((List) trades, lastID, tradeSortType);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List<UserTrade> getUserTrades() {

    return (List) getTrades();
  }
}
