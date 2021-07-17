package org.knowm.xchange.dto.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.derivative.FuturesContract;
import org.knowm.xchange.derivative.OptionsContract;
import org.knowm.xchange.utils.ObjectMapperHelper;

/**
 * This class is loaded during creation of the Exchange and is intended to hold both data that is
 * readily available from an HTTP API request at an exchange extended by semi-static data that is
 * not available from an HTTP API, but is still important information to have. Examples include
 * currency pairs, max polling rates, scaling factors, etc. For more info see:
 * https://github.com/timmolter/XChange/wiki/Design-Notes
 *
 * <p>This class is used only in the API by the classes that merge metadata stored in custom JSON
 * file and online info from the remote exchange.
 */
public class ExchangeMetaData implements Serializable {

  private static final long serialVersionUID = -1495610469981534977L;

  @JsonProperty("currency_pairs")
  private Map<CurrencyPair, CurrencyPairMetaData> currencyPairs;

  @JsonProperty("currencies")
  private Map<Currency, CurrencyMetaData> currencies;

  @JsonProperty("futures")
  private Map<FuturesContract, DerivativeMetaData> futures;

  @JsonProperty("options")
  private Map<OptionsContract, DerivativeMetaData> options;

  @JsonProperty("public_rate_limits")
  private RateLimit[] publicRateLimits;

  @JsonProperty("private_rate_limits")
  private RateLimit[] privateRateLimits;

  /**
   * If true, both public and private calls use single rate limit policy, which is described in
   * {@link #privateRateLimits}.
   */
  @JsonProperty("share_rate_limits")
  private boolean shareRateLimits = true;

  /**
   * Constructor
   *
   * @param currencyPairs Map of {@link CurrencyPair} -> {@link CurrencyPairMetaData}
   * @param currency Map of currency -> {@link CurrencyMetaData}
   */
  public ExchangeMetaData(
          Map<CurrencyPair, CurrencyPairMetaData> currencyPairs,
          Map<Currency, CurrencyMetaData> currency,
          RateLimit[] publicRateLimits,
          RateLimit[] privateRateLimits,
          Boolean shareRateLimits) {
    this(currencyPairs, currency, null, null, publicRateLimits, privateRateLimits, shareRateLimits);
  }

  public ExchangeMetaData(
      @JsonProperty("currency_pairs") Map<CurrencyPair, CurrencyPairMetaData> currencyPairs,
      @JsonProperty("currencies") Map<Currency, CurrencyMetaData> currency,
      @JsonProperty("futures") Map<FuturesContract, DerivativeMetaData> futures,
      @JsonProperty("options") Map<OptionsContract, DerivativeMetaData> options,
      @JsonProperty("public_rate_limits") RateLimit[] publicRateLimits,
      @JsonProperty("private_rate_limits") RateLimit[] privateRateLimits,
      @JsonProperty("share_rate_limits") Boolean shareRateLimits) {

    this.currencyPairs = currencyPairs;
    this.currencies = currency;
    this.futures = futures;
    this.options = options;

    this.publicRateLimits = publicRateLimits;
    this.privateRateLimits = privateRateLimits;

    this.shareRateLimits = shareRateLimits != null ? shareRateLimits : false;
  }

  /**
   * @return minimum number of milliseconds required between any two remote calls, assuming the
   *     client makes consecutive calls without any bursts or breaks for an infinite period of time.
   *     Returns null if the rateLimits collection is null or empty
   */
  @JsonIgnore
  public static Long getPollDelayMillis(RateLimit[] rateLimits) {
    if (rateLimits == null || rateLimits.length == 0) {
      return null;
    }
    long result = 0;
    for (RateLimit rateLimit : rateLimits) {
      // this is the delay between calls, we want max, any smaller number is for burst calls
      result = Math.max(result, rateLimit.getPollDelayMillis());
    }
    return result;
  }

  public Map<CurrencyPair, CurrencyPairMetaData> getCurrencyPairs() {
    return currencyPairs;
  }

  public Map<Currency, CurrencyMetaData> getCurrencies() {
    return currencies;
  }

  public Map<FuturesContract, DerivativeMetaData> getFutures() {
    return futures;
  }

  public Map<OptionsContract, DerivativeMetaData> getOptions() {
    return options;
  }

  public RateLimit[] getPublicRateLimits() {
    return publicRateLimits;
  }

  public RateLimit[] getPrivateRateLimits() {
    return privateRateLimits;
  }

  public boolean isShareRateLimits() {
    return shareRateLimits;
  }

  @JsonIgnore
  public String toJSONString() {
    return ObjectMapperHelper.toJSON(this);
  }

  @Override
  public String toString() {
    return "ExchangeMetaData [currencyPairs="
        + currencyPairs
        + ", currencies="
        + currencies
        + ", futures="
        + futures
        + ", options="
        + options    
        + ", publicRateLimits="
        + Arrays.toString(publicRateLimits)
        + ", privateRateLimits="
        + Arrays.toString(privateRateLimits)
        + ", shareRateLimits="
        + shareRateLimits
        + "]";
  }
}
