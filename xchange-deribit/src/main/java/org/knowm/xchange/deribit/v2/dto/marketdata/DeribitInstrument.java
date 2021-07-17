package org.knowm.xchange.deribit.v2.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import org.knowm.xchange.deribit.v2.dto.Kind;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class DeribitInstrument {

  /**
   * specifies minimal price change and, as follows, the number of decimal places for instrument
   * prices
   */
  @JsonProperty("tick_size")
  private BigDecimal tickSize;

  /** The settlement period */
  @JsonProperty("settlement_period")
  private String settlementPeriod;

  /** The currency in which the instrument prices are quoted */
  @JsonProperty("quote_currency")
  private String quoteCurrency;

  /**
   * Minimum amount for trading. For perpetual and futures - in USD units, for options it is amount
   * of corresponding cryptocurrency contracts, e.g., BTC or ETH.
   */
  @JsonProperty("min_trade_amount")
  private BigDecimal minTradeAmount;

  /** Instrument kind, "future" or "option" */
  @JsonProperty("kind")
  private Kind kind;

  /** Indicates if the instrument can currently be traded */
  @JsonProperty("is_active")
  private boolean isActive;

  /** Unique instrument identifier */
  @JsonProperty("instrument_name")
  private String instrumentName;

  /** The time when the instrument will expire (milliseconds) */
  @JsonProperty("expiration_timestamp")
  private long expirationTimestamp;

  /** The time when the instrument was first created (milliseconds) */
  @JsonProperty("creation_timestamp")
  private long creationTimestamp;

  /** Contract size for instrument */
  @JsonProperty("contract_size")
  private int contractSize;

  /** The underlying currency being traded */
  @JsonProperty("base_currency")
  private String baseCurrency;

  /** The strike value. (only for options) */
  @JsonProperty("strike")
  private BigDecimal strike;

  /** The option type (only for options) */
  @JsonProperty("option_type")
  private String optionType;

  /** Maker commission for instrument */
  @JsonProperty("maker_commission")
  private BigDecimal makerCommission;

  /** Taker commission for instrument */
  @JsonProperty("taker_commission")
  private BigDecimal takerCommission;

  public Date getExpirationTimestamp() {
    return new Date(expirationTimestamp);
  }

  public Date getCreationTimestamp() {
    return new Date(creationTimestamp);
  }
}
