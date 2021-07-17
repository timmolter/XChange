package org.knowm.xchange.dto.meta;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;

public class DerivativeMetaData implements Serializable { 
  private static final long serialVersionUID = 1L;

  @JsonProperty("trading_fee")
  private final BigDecimal tradingFee;

  @JsonProperty("fee_tiers")
  private final FeeTier[] feeTiers;

  @JsonProperty("tick_size")
  private final BigDecimal tickSize;
    
  @JsonProperty("min_trade_amount")
  private final BigDecimal minTradeAmount;

  @JsonProperty("contract_size")
  private final BigDecimal contractSize;

  public DerivativeMetaData(
          @JsonProperty("trading_fee") BigDecimal tradingFee,
          @JsonProperty("fee_tiers") FeeTier[] feeTiers,
          @JsonProperty("tick_size") BigDecimal tickSize,
          @JsonProperty("min_trade_amount") BigDecimal minTradeAmount,
          @JsonProperty("contract_size") BigDecimal contractSize) {
    this.tradingFee = tradingFee;
    this.feeTiers = feeTiers;
    this.tickSize = tickSize;
    this.minTradeAmount = minTradeAmount;
    this.contractSize = contractSize;
  }

  public BigDecimal getTradingFee() { 
    return tradingFee;
  }

  public FeeTier[] getFeeTiers() {
    return feeTiers;
  }

  public BigDecimal getTickSize() {
    return tickSize;
  }

  public BigDecimal getMinTradeAmount() {
    return minTradeAmount;
  }

  public BigDecimal getContractSize() {
    return contractSize;
  }

  @Override
  public String toString() {
    return "DerivativeMetaData [" +
                "tradingFee=" + tradingFee +
                ", tickSize=" + tickSize +
                ", minTradeAmount=" + minTradeAmount +
                ", contractSize=" + contractSize +
                ']';
  }
}
