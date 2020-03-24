package org.knowm.xchange.currency;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import org.knowm.xchange.instrument.Instrument;

/**
 * Value object to provide the following to API:
 *
 * <ul>
 *   <li>Provision of major currency symbol pairs (EUR/USD, GBP/USD etc)
 *   <li>Provision of arbitrary symbol pairs for exchange index trading, notional currencies etc
 * </ul>
 *
 * <p>Symbol pairs are quoted, for example, as EUR/USD 1.25 such that 1 EUR can be purchased with
 * 1.25 USD
 */
public class CurrencyPair extends Instrument implements Comparable<CurrencyPair>, Serializable {

  private static final long serialVersionUID = 414711266389792746L;

  // Provide some standard major symbols
  public static final CurrencyPair EUR_USD = new CurrencyPair(Currency.EUR, Currency.USD);
  public static final CurrencyPair GBP_USD = new CurrencyPair(Currency.GBP, Currency.USD);
  public static final CurrencyPair USD_JPY = new CurrencyPair(Currency.USD, Currency.JPY);
  public static final CurrencyPair JPY_USD = new CurrencyPair(Currency.JPY, Currency.USD);
  public static final CurrencyPair USD_CHF = new CurrencyPair(Currency.USD, Currency.CHF);
  public static final CurrencyPair USD_AUD = new CurrencyPair(Currency.USD, Currency.AUD);
  public static final CurrencyPair USD_CAD = new CurrencyPair(Currency.USD, Currency.CAD);
  public static final CurrencyPair USD_RUR = new CurrencyPair(Currency.USD, Currency.RUR);
  public static final CurrencyPair EUR_RUR = new CurrencyPair(Currency.EUR, Currency.RUR);
  public static final CurrencyPair USD_XRP = new CurrencyPair(Currency.USD, Currency.XRP);
  public static final CurrencyPair EUR_XRP = new CurrencyPair(Currency.EUR, Currency.XRP);
  public static final CurrencyPair USD_XVN = new CurrencyPair(Currency.USD, Currency.XVN);
  public static final CurrencyPair EUR_XVN = new CurrencyPair(Currency.EUR, Currency.XVN);
  public static final CurrencyPair KRW_XRP = new CurrencyPair(Currency.KRW, Currency.XRP);

  // Provide some courtesy BTC major symbols
  public static final CurrencyPair BTC_USD = new CurrencyPair(Currency.BTC, Currency.USD);
  public static final CurrencyPair BTC_GBP = new CurrencyPair(Currency.BTC, Currency.GBP);
  public static final CurrencyPair BTC_EUR = new CurrencyPair(Currency.BTC, Currency.EUR);
  public static final CurrencyPair BTC_JPY = new CurrencyPair(Currency.BTC, Currency.JPY);
  public static final CurrencyPair BTC_CHF = new CurrencyPair(Currency.BTC, Currency.CHF);
  public static final CurrencyPair BTC_AUD = new CurrencyPair(Currency.BTC, Currency.AUD);
  public static final CurrencyPair BTC_CAD = new CurrencyPair(Currency.BTC, Currency.CAD);
  public static final CurrencyPair BTC_CNY = new CurrencyPair(Currency.BTC, Currency.CNY);
  public static final CurrencyPair BTC_DKK = new CurrencyPair(Currency.BTC, Currency.DKK);
  public static final CurrencyPair BTC_HKD = new CurrencyPair(Currency.BTC, Currency.HKD);
  public static final CurrencyPair BTC_MXN = new CurrencyPair(Currency.BTC, Currency.MXN);
  public static final CurrencyPair BTC_NZD = new CurrencyPair(Currency.BTC, Currency.NZD);
  public static final CurrencyPair BTC_PLN = new CurrencyPair(Currency.BTC, Currency.PLN);
  public static final CurrencyPair BTC_RUB = new CurrencyPair(Currency.BTC, Currency.RUB);
  public static final CurrencyPair BTC_SEK = new CurrencyPair(Currency.BTC, Currency.SEK);
  public static final CurrencyPair BTC_SGD = new CurrencyPair(Currency.BTC, Currency.SGD);
  public static final CurrencyPair BTC_NOK = new CurrencyPair(Currency.BTC, Currency.NOK);
  public static final CurrencyPair BTC_THB = new CurrencyPair(Currency.BTC, Currency.THB);
  public static final CurrencyPair BTC_RUR = new CurrencyPair(Currency.BTC, Currency.RUR);
  public static final CurrencyPair BTC_ZAR = new CurrencyPair(Currency.BTC, Currency.ZAR);
  public static final CurrencyPair BTC_BRL = new CurrencyPair(Currency.BTC, Currency.BRL);
  public static final CurrencyPair BTC_CZK = new CurrencyPair(Currency.BTC, Currency.CZK);
  public static final CurrencyPair BTC_ILS = new CurrencyPair(Currency.BTC, Currency.ILS);
  public static final CurrencyPair BTC_KRW = new CurrencyPair(Currency.BTC, Currency.KRW);
  public static final CurrencyPair BTC_LTC = new CurrencyPair(Currency.BTC, Currency.LTC);
  public static final CurrencyPair BTC_XRP = new CurrencyPair(Currency.BTC, Currency.XRP);
  public static final CurrencyPair BTC_NMC = new CurrencyPair(Currency.BTC, Currency.NMC);
  public static final CurrencyPair BTC_XVN = new CurrencyPair(Currency.BTC, Currency.XVN);
  public static final CurrencyPair BTC_IDR = new CurrencyPair(Currency.BTC, Currency.IDR);
  public static final CurrencyPair BTC_PHP = new CurrencyPair(Currency.BTC, Currency.PHP);
  public static final CurrencyPair BTC_STR = new CurrencyPair(Currency.BTC, Currency.STR);
  public static final CurrencyPair BTC_INR = new CurrencyPair(Currency.BTC, Currency.INR);
  public static final CurrencyPair BTC_XAUR = new CurrencyPair(Currency.BTC, Currency.XAUR);
  public static final CurrencyPair BTC_IOC = new CurrencyPair(Currency.BTC, Currency.IOC);
  public static final CurrencyPair BTC_TRY = new CurrencyPair(Currency.BTC, Currency.TRY);
  public static final CurrencyPair ETH_TRY = new CurrencyPair(Currency.ETH, Currency.TRY);

  public static final CurrencyPair BCH_USD = new CurrencyPair(Currency.BCH, Currency.USD);
  public static final CurrencyPair BCH_AUD = new CurrencyPair(Currency.BCH, Currency.AUD);
  public static final CurrencyPair BCH_CAD = new CurrencyPair(Currency.BCH, Currency.CAD);
  public static final CurrencyPair BCH_GBP = new CurrencyPair(Currency.BCH, Currency.GBP);
  public static final CurrencyPair BCH_EUR = new CurrencyPair(Currency.BCH, Currency.EUR);
  public static final CurrencyPair BCH_BTC = new CurrencyPair(Currency.BCH, Currency.BTC);
  public static final CurrencyPair BCH_ETH = new CurrencyPair(Currency.BCH, Currency.ETH);

  public static final CurrencyPair BCA_USD = new CurrencyPair(Currency.BCA, Currency.USD);
  public static final CurrencyPair BCA_EUR = new CurrencyPair(Currency.BCA, Currency.EUR);
  public static final CurrencyPair BCA_CNY = new CurrencyPair(Currency.BCA, Currency.CNY);
  public static final CurrencyPair BCA_JPY = new CurrencyPair(Currency.BCA, Currency.JPY);
  public static final CurrencyPair BCA_BTC = new CurrencyPair(Currency.BCA, Currency.BTC);
  public static final CurrencyPair BCA_ETH = new CurrencyPair(Currency.BCA, Currency.ETH);

  public static final CurrencyPair ETH_BTC = new CurrencyPair(Currency.ETH, Currency.BTC);
  public static final CurrencyPair ETH_GBP = new CurrencyPair(Currency.ETH, Currency.GBP);
  public static final CurrencyPair ETH_USD = new CurrencyPair(Currency.ETH, Currency.USD);
  public static final CurrencyPair ETH_USDT = new CurrencyPair(Currency.ETH, Currency.USDT);
  public static final CurrencyPair ETH_EUR = new CurrencyPair(Currency.ETH, Currency.EUR);
  public static final CurrencyPair ETH_JPY = new CurrencyPair(Currency.ETH, Currency.JPY);
  public static final CurrencyPair ETH_CNY = new CurrencyPair(Currency.ETH, Currency.CNY);
  public static final CurrencyPair ETH_AUD = new CurrencyPair(Currency.ETH, Currency.AUD);
  public static final CurrencyPair ETH_NZD = new CurrencyPair(Currency.ETH, Currency.NZD);

  public static final CurrencyPair XAUR_BTC = new CurrencyPair(Currency.XAUR, Currency.BTC);

  public static final CurrencyPair XDC_BTC = new CurrencyPair(Currency.XDC, Currency.BTC);

  public static final CurrencyPair SC_BTC = new CurrencyPair(Currency.SC, Currency.BTC);

  public static final CurrencyPair DCR_BTC = new CurrencyPair(Currency.DCR, Currency.BTC);

  public static final CurrencyPair XRP_BTC = new CurrencyPair(Currency.XRP, Currency.BTC);
  public static final CurrencyPair XRP_ETH = new CurrencyPair(Currency.XRP, Currency.ETH);
  public static final CurrencyPair XRP_EUR = new CurrencyPair(Currency.XRP, Currency.EUR);
  public static final CurrencyPair XRP_USD = new CurrencyPair(Currency.XRP, Currency.USD);
  public static final CurrencyPair XRP_USDT = new CurrencyPair(Currency.XRP, Currency.USDT);
  public static final CurrencyPair XRP_GBP = new CurrencyPair(Currency.XRP, Currency.GBP);

  public static final CurrencyPair LTC_AUD = new CurrencyPair(Currency.LTC, Currency.AUD);
  public static final CurrencyPair LTC_GBP = new CurrencyPair(Currency.LTC, Currency.GBP);
  public static final CurrencyPair LTC_USD = new CurrencyPair(Currency.LTC, Currency.USD);
  public static final CurrencyPair LTC_KRW = new CurrencyPair(Currency.LTC, Currency.KRW);
  public static final CurrencyPair LTC_CNY = new CurrencyPair(Currency.LTC, Currency.CNY);
  public static final CurrencyPair LTC_RUR = new CurrencyPair(Currency.LTC, Currency.RUR);
  public static final CurrencyPair LTC_EUR = new CurrencyPair(Currency.LTC, Currency.EUR);
  public static final CurrencyPair LTC_BTC = new CurrencyPair(Currency.LTC, Currency.BTC);
  public static final CurrencyPair LTC_XRP = new CurrencyPair(Currency.LTC, Currency.XRP);
  public static final CurrencyPair LTC_TRY = new CurrencyPair(Currency.LTC, Currency.TRY);

  public static final CurrencyPair NMC_USD = new CurrencyPair(Currency.NMC, Currency.USD);
  public static final CurrencyPair NMC_CNY = new CurrencyPair(Currency.NMC, Currency.CNY);
  public static final CurrencyPair NMC_EUR = new CurrencyPair(Currency.NMC, Currency.EUR);
  public static final CurrencyPair NMC_KRW = new CurrencyPair(Currency.NMC, Currency.KRW);
  public static final CurrencyPair NMC_BTC = new CurrencyPair(Currency.NMC, Currency.BTC);
  public static final CurrencyPair NMC_LTC = new CurrencyPair(Currency.NMC, Currency.LTC);
  public static final CurrencyPair NMC_XRP = new CurrencyPair(Currency.NMC, Currency.XRP);

  public static final CurrencyPair NVC_USD = new CurrencyPair(Currency.NVC, Currency.USD);
  public static final CurrencyPair NVC_BTC = new CurrencyPair(Currency.NVC, Currency.BTC);

  public static final CurrencyPair TRC_BTC = new CurrencyPair(Currency.TRC, Currency.BTC);

  public static final CurrencyPair PPC_USD = new CurrencyPair(Currency.PPC, Currency.USD);
  public static final CurrencyPair PPC_BTC = new CurrencyPair(Currency.PPC, Currency.BTC);
  public static final CurrencyPair PPC_LTC = new CurrencyPair(Currency.PPC, Currency.LTC);

  public static final CurrencyPair FTC_USD = new CurrencyPair(Currency.FTC, Currency.USD);
  public static final CurrencyPair FTC_CNY = new CurrencyPair(Currency.FTC, Currency.CNY);
  public static final CurrencyPair FTC_BTC = new CurrencyPair(Currency.FTC, Currency.BTC);
  public static final CurrencyPair FTC_LTC = new CurrencyPair(Currency.FTC, Currency.LTC);

  public static final CurrencyPair XEM_USD = new CurrencyPair(Currency.XEM, Currency.USD);
  public static final CurrencyPair XEM_USDT = new CurrencyPair(Currency.XEM, Currency.USDT);
  public static final CurrencyPair XEM_KRW = new CurrencyPair(Currency.XEM, Currency.KRW);
  public static final CurrencyPair XEM_JPY = new CurrencyPair(Currency.XEM, Currency.JPY);
  public static final CurrencyPair XEM_BTC = new CurrencyPair(Currency.XEM, Currency.BTC);
  public static final CurrencyPair XEM_ETH = new CurrencyPair(Currency.XEM, Currency.ETH);
  public static final CurrencyPair XEM_EUR = new CurrencyPair(Currency.XEM, Currency.EUR);

  public static final CurrencyPair XMR_BTC = new CurrencyPair(Currency.XMR, Currency.BTC);
  public static final CurrencyPair XMR_ETH = new CurrencyPair(Currency.XMR, Currency.ETH);
  public static final CurrencyPair XMR_USD = new CurrencyPair(Currency.XMR, Currency.USD);
  public static final CurrencyPair XMR_USDT = new CurrencyPair(Currency.XMR, Currency.USDT);

  public static final CurrencyPair XPM_USD = new CurrencyPair(Currency.XPM, Currency.USD);
  public static final CurrencyPair XPM_CNY = new CurrencyPair(Currency.XPM, Currency.CNY);
  public static final CurrencyPair XPM_BTC = new CurrencyPair(Currency.XPM, Currency.BTC);
  public static final CurrencyPair XPM_LTC = new CurrencyPair(Currency.XPM, Currency.LTC);
  public static final CurrencyPair XPM_PPC = new CurrencyPair(Currency.XPM, Currency.PPC);

  public static final CurrencyPair XVN_XRP = new CurrencyPair(Currency.XVN, Currency.XRP);

  public static final CurrencyPair STEEM_BTC = new CurrencyPair(Currency.STEEM, Currency.BTC);
  public static final CurrencyPair STEEM_USD = new CurrencyPair(Currency.STEEM, Currency.USD);
  public static final CurrencyPair STEEM_USDT = new CurrencyPair(Currency.STEEM, Currency.USDT);
  public static final CurrencyPair STEEM_ETH = new CurrencyPair(Currency.STEEM, Currency.ETH);
  public static final CurrencyPair STEEM_BNB = new CurrencyPair(Currency.STEEM, Currency.BNB);
  public static final CurrencyPair STEEM_KRW = new CurrencyPair(Currency.STEEM, Currency.KRW);

  public static final CurrencyPair VET_BTC = new CurrencyPair(Currency.VET, Currency.BTC);
  public static final CurrencyPair VET_USDT = new CurrencyPair(Currency.VET, Currency.USDT);
  public static final CurrencyPair VET_ETH = new CurrencyPair(Currency.VET, Currency.ETH);
  public static final CurrencyPair VET_BNB = new CurrencyPair(Currency.VET, Currency.BNB);

  public static final CurrencyPair ADA_BTC = new CurrencyPair(Currency.ADA, Currency.BTC);
  public static final CurrencyPair ADA_USDT = new CurrencyPair(Currency.ADA, Currency.USDT);
  public static final CurrencyPair ADA_ETH = new CurrencyPair(Currency.ADA, Currency.ETH);
  public static final CurrencyPair ADA_BNB = new CurrencyPair(Currency.ADA, Currency.BNB);

  public static final CurrencyPair TRX_BTC = new CurrencyPair(Currency.TRX, Currency.BTC);
  public static final CurrencyPair TRX_USDT = new CurrencyPair(Currency.TRX, Currency.USDT);
  public static final CurrencyPair TRX_ETH = new CurrencyPair(Currency.TRX, Currency.ETH);
  public static final CurrencyPair TRX_BNB = new CurrencyPair(Currency.TRX, Currency.BNB);

  // start of extra ANX supported pair
  // BTC
  public static final CurrencyPair BTC_XDC = new CurrencyPair(Currency.BTC, Currency.XDC);
  public static final CurrencyPair BTC_PPC = new CurrencyPair(Currency.BTC, Currency.PPC);
  public static final CurrencyPair STR_BTC = new CurrencyPair(Currency.STR, Currency.BTC);

  // LTC
  public static final CurrencyPair LTC_HKD = new CurrencyPair(Currency.LTC, Currency.HKD);
  public static final CurrencyPair LTC_XDC = new CurrencyPair(Currency.LTC, Currency.XDC);
  public static final CurrencyPair LTC_NMC = new CurrencyPair(Currency.LTC, Currency.NMC);
  public static final CurrencyPair LTC_PPC = new CurrencyPair(Currency.LTC, Currency.PPC);

  // DOGE
  public static final CurrencyPair DOGE_HKD = new CurrencyPair(Currency.DOGE, Currency.HKD);
  public static final CurrencyPair DOGE_BTC = new CurrencyPair(Currency.DOGE, Currency.BTC);
  public static final CurrencyPair DOGE_LTC = new CurrencyPair(Currency.DOGE, Currency.LTC);
  public static final CurrencyPair DOGE_NMC = new CurrencyPair(Currency.DOGE, Currency.NMC);
  public static final CurrencyPair DOGE_PPC = new CurrencyPair(Currency.DOGE, Currency.PPC);
  public static final CurrencyPair DOGE_USD = new CurrencyPair(Currency.DOGE, Currency.USD);
  public static final CurrencyPair DOGE_TRY = new CurrencyPair(Currency.DOGE, Currency.TRY);

  public static final CurrencyPair XDC_HKD = new CurrencyPair(Currency.XDC, Currency.HKD);
  public static final CurrencyPair XDC_LTC = new CurrencyPair(Currency.XDC, Currency.LTC);
  public static final CurrencyPair XDC_NMC = new CurrencyPair(Currency.XDC, Currency.NMC);
  public static final CurrencyPair XDC_PPC = new CurrencyPair(Currency.XDC, Currency.PPC);
  public static final CurrencyPair XDC_USD = new CurrencyPair(Currency.XDC, Currency.USD);

  // NMC
  public static final CurrencyPair NMC_HKD = new CurrencyPair(Currency.NMC, Currency.HKD);
  public static final CurrencyPair NMC_XDC = new CurrencyPair(Currency.NMC, Currency.XDC);
  public static final CurrencyPair NMC_PPC = new CurrencyPair(Currency.NMC, Currency.PPC);

  // PPC
  public static final CurrencyPair PPC_HKD = new CurrencyPair(Currency.PPC, Currency.HKD);
  public static final CurrencyPair PPC_XDC = new CurrencyPair(Currency.PPC, Currency.XDC);
  public static final CurrencyPair PPC_NMC = new CurrencyPair(Currency.PPC, Currency.NMC);
  // end

  // IOTA
  public static final CurrencyPair IOTA_USD = new CurrencyPair(Currency.IOT, Currency.USD);
  public static final CurrencyPair IOTA_BTC = new CurrencyPair(Currency.IOT, Currency.BTC);
  public static final CurrencyPair IOTA_ETH = new CurrencyPair(Currency.IOT, Currency.ETH);
  // end

  // OMG
  public static final CurrencyPair OMG_USD = new CurrencyPair(Currency.OMG, Currency.USD);
  public static final CurrencyPair OMG_BTC = new CurrencyPair(Currency.OMG, Currency.BTC);
  public static final CurrencyPair OMG_ETH = new CurrencyPair(Currency.OMG, Currency.ETH);
  // end

  // NEO
  public static final CurrencyPair NEO_USD = new CurrencyPair(Currency.NEO, Currency.USD);
  public static final CurrencyPair NEO_USDT = new CurrencyPair(Currency.NEO, Currency.USDT);
  public static final CurrencyPair NEO_BTC = new CurrencyPair(Currency.NEO, Currency.BTC);
  public static final CurrencyPair NEO_ETH = new CurrencyPair(Currency.NEO, Currency.ETH);
  // end

  // not real currencies, but tradable commodities (GH/s)
  public static final CurrencyPair GHs_BTC = new CurrencyPair(Currency.GHs, Currency.BTC);
  public static final CurrencyPair GHs_NMC = new CurrencyPair(Currency.GHs, Currency.NMC);

  public static final CurrencyPair CNC_BTC = new CurrencyPair(Currency.CNC, Currency.BTC);

  public static final CurrencyPair WDC_USD = new CurrencyPair(Currency.WDC, Currency.USD);
  public static final CurrencyPair WDC_BTC = new CurrencyPair(Currency.WDC, Currency.BTC);
  public static final CurrencyPair DVC_BTC = new CurrencyPair(Currency.DVC, Currency.BTC);

  public static final CurrencyPair DGC_BTC = new CurrencyPair(Currency.DGC, Currency.BTC);

  public static final CurrencyPair UTC_USD = new CurrencyPair(Currency.UTC, Currency.USD);
  public static final CurrencyPair UTC_EUR = new CurrencyPair(Currency.UTC, Currency.EUR);
  public static final CurrencyPair UTC_BTC = new CurrencyPair(Currency.UTC, Currency.BTC);
  public static final CurrencyPair UTC_LTC = new CurrencyPair(Currency.UTC, Currency.LTC);

  // Kraken additional pairs
  public static final CurrencyPair ADA_USD = new CurrencyPair(Currency.ADA, Currency.USD);
  public static final CurrencyPair ADA_EUR = new CurrencyPair(Currency.ADA, Currency.EUR);
  public static final CurrencyPair ADA_CAD = new CurrencyPair(Currency.ADA, Currency.CAD);

  public static final CurrencyPair ATOM_BTC = new CurrencyPair(Currency.ATOM, Currency.BTC);
  public static final CurrencyPair ATOM_ETH = new CurrencyPair(Currency.ATOM, Currency.ETH);
  public static final CurrencyPair ATOM_USD = new CurrencyPair(Currency.ATOM, Currency.USD);
  public static final CurrencyPair ATOM_EUR = new CurrencyPair(Currency.ATOM, Currency.EUR);
  public static final CurrencyPair ATOM_CAD = new CurrencyPair(Currency.ATOM, Currency.CAD);

  public static final CurrencyPair ETC_BTC = new CurrencyPair(Currency.ETC, Currency.BTC);
  public static final CurrencyPair ETC_EUR = new CurrencyPair(Currency.ETC, Currency.EUR);
  public static final CurrencyPair ETC_ETH = new CurrencyPair(Currency.ETC, Currency.ETH);
  public static final CurrencyPair ETC_USD = new CurrencyPair(Currency.ETC, Currency.USD);

  public static final CurrencyPair ICN_BTC = new CurrencyPair(Currency.ICN, Currency.BTC);
  public static final CurrencyPair ICN_ETH = new CurrencyPair(Currency.ICN, Currency.ETH);

  public static final CurrencyPair DASH_USD = new CurrencyPair(Currency.DASH, Currency.USD);
  public static final CurrencyPair DASH_EUR = new CurrencyPair(Currency.DASH, Currency.EUR);
  public static final CurrencyPair DASH_BTC = new CurrencyPair(Currency.DASH, Currency.BTC);
  public static final CurrencyPair DASH_TRY = new CurrencyPair(Currency.DASH, Currency.TRY);

  public static final CurrencyPair MLN_ETH = new CurrencyPair(Currency.MLN, Currency.ETH);
  public static final CurrencyPair MLN_BTC = new CurrencyPair(Currency.MLN, Currency.BTC);

  public static final CurrencyPair ZEC_EUR = new CurrencyPair(Currency.ZEC, Currency.EUR);
  public static final CurrencyPair ZEC_USD = new CurrencyPair(Currency.ZEC, Currency.USD);
  public static final CurrencyPair ZEC_BTC = new CurrencyPair(Currency.ZEC, Currency.BTC);

  public static final CurrencyPair ZEN_USD = new CurrencyPair(Currency.ZEN, Currency.USD);
  public static final CurrencyPair ZEN_BTC = new CurrencyPair(Currency.ZEN, Currency.BTC);

  public static final CurrencyPair GNO_ETH = new CurrencyPair(Currency.GNO, Currency.ETH);
  public static final CurrencyPair GNO_BTC = new CurrencyPair(Currency.GNO, Currency.BTC);

  public static final CurrencyPair EOS_ETH = new CurrencyPair(Currency.EOS, Currency.ETH);
  public static final CurrencyPair EOS_BTC = new CurrencyPair(Currency.EOS, Currency.BTC);

  public static final CurrencyPair BCC_USD = new CurrencyPair(Currency.BCC, Currency.USD);
  public static final CurrencyPair BCC_BTC = new CurrencyPair(Currency.BCC, Currency.BTC);

  public static final CurrencyPair BAT_USD = new CurrencyPair(Currency.BAT, Currency.USD);
  public static final CurrencyPair BAT_BTC = new CurrencyPair(Currency.BAT, Currency.BTC);

  // Tether Pairs
  public static final CurrencyPair BTC_USDT = new CurrencyPair(Currency.BTC, Currency.USDT);
  public static final CurrencyPair DASH_USDT = new CurrencyPair(Currency.DASH, Currency.USDT);

  // UAH pairs
  public static final CurrencyPair BTC_UAH = new CurrencyPair(Currency.BTC, Currency.UAH);
  public static final CurrencyPair ETH_UAH = new CurrencyPair(Currency.ETH, Currency.UAH);
  public static final CurrencyPair BCH_UAH = new CurrencyPair(Currency.BCH, Currency.UAH);

  // Bitmex futures contracts
  public static final CurrencyPair XBT_USD = new CurrencyPair(Currency.XBT, Currency.USD);
  public static final CurrencyPair XBT_H18 = new CurrencyPair(Currency.XBT, Currency.H18);
  public static final CurrencyPair XBT_M18 = new CurrencyPair(Currency.XBT, Currency.M18);
  public static final CurrencyPair XBT_U18 = new CurrencyPair(Currency.XBT, Currency.U18);
  public static final CurrencyPair XBT_Z18 = new CurrencyPair(Currency.XBT, Currency.Z18);

  public static final CurrencyPair ADA_H18 = new CurrencyPair(Currency.ADA, Currency.H18);
  public static final CurrencyPair ADA_M18 = new CurrencyPair(Currency.ADA, Currency.M18);
  public static final CurrencyPair ADA_H19 = new CurrencyPair(Currency.ADA, Currency.H19);

  public static final CurrencyPair BCH_H18 = new CurrencyPair(Currency.BCH, Currency.H18);
  public static final CurrencyPair BCH_M18 = new CurrencyPair(Currency.BCH, Currency.M18);
  public static final CurrencyPair BCH_H19 = new CurrencyPair(Currency.BCH, Currency.H19);

  public static final CurrencyPair EOS_H19 = new CurrencyPair(Currency.EOS, Currency.H19);

  public static final CurrencyPair ETH_H18 = new CurrencyPair(Currency.ETH, Currency.H18);
  public static final CurrencyPair ETH_M18 = new CurrencyPair(Currency.ETH, Currency.M18);

  public static final CurrencyPair LTC_H18 = new CurrencyPair(Currency.LTC, Currency.H18);
  public static final CurrencyPair LTC_M18 = new CurrencyPair(Currency.LTC, Currency.M18);
  public static final CurrencyPair LTC_H19 = new CurrencyPair(Currency.LTC, Currency.H19);

  public static final CurrencyPair TRX_H19 = new CurrencyPair(Currency.TRX, Currency.H19);

  public static final CurrencyPair XRP_H18 = new CurrencyPair(Currency.XRP, Currency.H18);
  public static final CurrencyPair XRP_M18 = new CurrencyPair(Currency.XRP, Currency.M18);
  public static final CurrencyPair XRP_H19 = new CurrencyPair(Currency.XRP, Currency.H19);

  public static final CurrencyPair DASH_H18 = new CurrencyPair(Currency.DASH, Currency.H18);
  public static final CurrencyPair NEO_H18 = new CurrencyPair(Currency.NEO, Currency.H18);
  public static final CurrencyPair XMR_H18 = new CurrencyPair(Currency.XMR, Currency.H18);
  public static final CurrencyPair XLM_H18 = new CurrencyPair(Currency.XLM, Currency.H18);
  public static final CurrencyPair ZEC_H18 = new CurrencyPair(Currency.ZEC, Currency.H18);
  public static final CurrencyPair ETC_7D =
      new CurrencyPair(Currency.ETC, Currency.getInstance("7D"));

  // Bankera Exchange pairs
  public static final CurrencyPair BNK_BTC = new CurrencyPair(Currency.BNK, Currency.BTC);
  public static final CurrencyPair BNK_ETH = new CurrencyPair(Currency.BNK, Currency.ETH);
  public static final CurrencyPair BNK_USDT = new CurrencyPair(Currency.BNK, Currency.USDT);
  public static final CurrencyPair XRP_BNK = new CurrencyPair(Currency.XRP, Currency.BNK);
  public static final CurrencyPair XLM_BNK = new CurrencyPair(Currency.XLM, Currency.BNK);
  public static final CurrencyPair LTC_BNK = new CurrencyPair(Currency.LTC, Currency.BNK);
  public static final CurrencyPair ZEC_BNK = new CurrencyPair(Currency.ZEC, Currency.BNK);
  public static final CurrencyPair XLM_BTC = new CurrencyPair(Currency.XLM, Currency.BTC);
  public static final CurrencyPair XLM_ETH = new CurrencyPair(Currency.XLM, Currency.ETH);
  public static final CurrencyPair LTC_ETH = new CurrencyPair(Currency.LTC, Currency.ETH);
  public static final CurrencyPair ZEC_ETH = new CurrencyPair(Currency.ZEC, Currency.ETH);
  public static final CurrencyPair XLM_USDT = new CurrencyPair(Currency.XLM, Currency.USDT);
  public static final CurrencyPair LTC_USDT = new CurrencyPair(Currency.LTC, Currency.USDT);
  public static final CurrencyPair ZEC_USDT = new CurrencyPair(Currency.ZEC, Currency.USDT);

  public final Currency base;

  public final Currency counter;

  /**
   * Full constructor In general the CurrencyPair.base is what you're wanting to buy/sell. The
   * CurrencyPair.counter is what currency you want to use to pay/receive for your purchase/sale.
   *
   * @param base The base currency is what you're wanting to buy/sell
   * @param counter The counter currency is what currency you want to use to pay/receive for your
   *     purchase/sale.
   */
  public CurrencyPair(Currency base, Currency counter) {

    this.base = base;
    this.counter = counter;
  }

  /**
   * String constructor In general the CurrencyPair.base is what you're wanting to buy/sell. The
   * CurrencyPair.counter is what currency you want to use to pay/receive for your purchase/sale.
   *
   * @param baseSymbol The base symbol is what you're wanting to buy/sell
   * @param counterSymbol The counter symbol is what currency you want to use to pay/receive for
   *     your purchase/sale.
   */
  public CurrencyPair(String baseSymbol, String counterSymbol) {

    this(Currency.getInstance(baseSymbol), Currency.getInstance(counterSymbol));
  }

  /**
   * Parse currency pair from a string in the same format as returned by toString() method - ABC/XYZ
   */
  @JsonCreator
  public CurrencyPair(String currencyPair) {

    int split = currencyPair.indexOf('/');
    if (split < 1) {
      throw new IllegalArgumentException(
          "Could not parse currency pair from '" + currencyPair + "'");
    }
    String base = currencyPair.substring(0, split);
    String counter = currencyPair.substring(split + 1);

    this.base = Currency.getInstance(base);
    this.counter = Currency.getInstance(counter);
  }

  @JsonValue
  @Override
  public String toString() {

    return base + "/" + counter;
  }

  public boolean contains(Currency currency) {
    return base.equals(currency) || counter.equals(currency);
  }

  @Override
  public int hashCode() {

    final int prime = 31;
    int result = 1;
    result = prime * result + ((base == null) ? 0 : base.hashCode());
    result = prime * result + ((counter == null) ? 0 : counter.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CurrencyPair other = (CurrencyPair) obj;
    if (base == null) {
      if (other.base != null) {
        return false;
      }
    } else if (!base.equals(other.base)) {
      return false;
    }
    if (counter == null) {
      if (other.counter != null) {
        return false;
      }
    } else if (!counter.equals(other.counter)) {
      return false;
    }
    return true;
  }

  @Override
  public int compareTo(CurrencyPair o) {

    return (base.compareTo(o.base) << 16) + counter.compareTo(o.counter);
  }
}
