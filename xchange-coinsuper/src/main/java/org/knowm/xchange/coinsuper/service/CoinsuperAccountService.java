package org.knowm.xchange.coinsuper.service;

import java.io.IOException;
import java.util.List;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.coinsuper.CoinsuperAdapters;
import org.knowm.xchange.coinsuper.dto.CoinsuperResponse;
import org.knowm.xchange.coinsuper.dto.marketdata.CoinsuperOrderbook;
import org.knowm.xchange.coinsuper.dto.marketdata.CoinsuperTicker;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.marketdata.params.Params;

public class CoinsuperAccountService extends CoinsuperAccountServiceRaw implements AccountService  {
	  public CoinsuperAccountService(Exchange exchange) {
		    super(exchange);
	  }
	  
	  @Override
	  public AccountInfo getAccountInfo() throws IOException {
	    return CoinsuperAdapters.convertBalance(super.getUserAssetInfo());
	  }
	  
	  /**
	   * 
	   * @param args
	   * @return
	   */
	  private int getLimit(Object... args) {
		    int limitDepth = 0;
		    if (args != null && args.length == 1) {
		      Object arg0 = args[0];
		      if (!(arg0 instanceof Integer)) {
		        throw new ExchangeException("Argument 0 must be an Integer!");
		      } else {
		        limitDepth = (Integer) arg0;
		      }
		    }
		    return limitDepth;
	}	  
}
