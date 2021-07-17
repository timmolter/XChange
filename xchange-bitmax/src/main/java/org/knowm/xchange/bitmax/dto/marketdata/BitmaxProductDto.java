package org.knowm.xchange.bitmax.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class BitmaxProductDto {

  private final String symbol;

  private final String baseAsset;

  private final String quoteAsset;

  private final BitmaxAssetDto.BitmaxAssetStatus status;

  private final BigDecimal minNotional;

  private final BigDecimal maxNotional;

  private final boolean marginTradable;

  private final BitmaxProductCommissionType commissionType;

  private final BigDecimal commissionReserveRate;

  private final BigDecimal tickSize;

  private final BigDecimal lotSize;

  public BitmaxProductDto(
      @JsonProperty("symbol") String symbol,
      @JsonProperty("baseAsset") String baseAsset,
      @JsonProperty("quoteAsset") String quoteAsset,
      @JsonProperty("status") BitmaxAssetDto.BitmaxAssetStatus status,
      @JsonProperty("minNotional") BigDecimal minNotional,
      @JsonProperty("maxNotional") BigDecimal maxNotional,
      @JsonProperty("marginTradable") boolean marginTradable,
      @JsonProperty("commissionType") BitmaxProductCommissionType commissionType,
      @JsonProperty("commissionReserveRate") BigDecimal commissionReserveRate,
      @JsonProperty("tickSize") BigDecimal tickSize,
      @JsonProperty("lotSize") BigDecimal lotSize) {
    this.symbol = symbol;
    this.baseAsset = baseAsset;
    this.quoteAsset = quoteAsset;
    this.status = status;
    this.minNotional = minNotional;
    this.maxNotional = maxNotional;
    this.marginTradable = marginTradable;
    this.commissionType = commissionType;
    this.commissionReserveRate = commissionReserveRate;
    this.tickSize = tickSize;
    this.lotSize = lotSize;
  }

  public String getSymbol() {
    return symbol;
  }

  public String getBaseAsset() {
    return baseAsset;
  }

  public String getQuoteAsset() {
    return quoteAsset;
  }

  public BitmaxAssetDto.BitmaxAssetStatus getStatus() {
    return status;
  }

  public BigDecimal getMinNotional() {
    return minNotional;
  }

  public BigDecimal getMaxNotional() {
    return maxNotional;
  }

  public boolean isMarginTradeable() {
    return marginTradable;
  }

  public BitmaxProductCommissionType getCommissionType() {
    return commissionType;
  }

  public BigDecimal getCommissionReserveRate() {
    return commissionReserveRate;
  }

  public BigDecimal getTickSize() {
    return tickSize;
  }

  public BigDecimal getLotSize() {
    return lotSize;
  }

  @Override
  public String toString() {
    return "BitmaxProductDto{"
        + "symbol='"
        + symbol
        + '\''
        + ", baseAsset='"
        + baseAsset
        + '\''
        + ", quoteAsset='"
        + quoteAsset
        + '\''
        + ", status="
        + status
        + ", minNotional="
        + minNotional
        + ", maxNotional="
        + maxNotional
        + ", marginTradeable="
        + marginTradable
        + ", commissionType="
        + commissionType
        + ", commissionReserveRate="
        + commissionReserveRate
        + ", tickSize="
        + tickSize
        + ", lotSize="
        + lotSize
        + '}';
  }

  public enum BitmaxProductCommissionType {
    Base,
    Quote,
    Received
  }
}
