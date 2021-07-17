package org.knowm.xchange.bitmax.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class BitmaxCashAccountBalanceDto {

  private final String asset;

  private final BigDecimal totalBalance;

  private final BigDecimal availableBalance;

  public BitmaxCashAccountBalanceDto(
      @JsonProperty("asset") String asset,
      @JsonProperty("totalBalance") BigDecimal totalBalance,
      @JsonProperty("availableBalance") BigDecimal availableBalance) {
    this.asset = asset;
    this.totalBalance = totalBalance;
    this.availableBalance = availableBalance;
  }

  public String getAsset() {
    return asset;
  }

  public BigDecimal getTotalBalance() {
    return totalBalance;
  }

  public BigDecimal getAvailableBalance() {
    return availableBalance;
  }

  @Override
  public String toString() {
    return "BitmaxCashAccountBalanceDto{"
        + "asset='"
        + asset
        + '\''
        + ", totalBalance="
        + totalBalance
        + ", availableBalance="
        + availableBalance
        + '}';
  }
}
