package info.bitrich.xchangestream.poloniex2.dto;

import java.math.BigDecimal;

/** Created by Marcin Rabiej 22.05.2019 */
public class BalanceUpdatedEvent {

  private String currencyId;
  private String walletId;
  private BigDecimal amount;

  public BalanceUpdatedEvent(String currencyId, String walletId, BigDecimal amount) {
    this.currencyId = currencyId;
    this.walletId = walletId;
    this.amount = amount;
  }

  public String getCurrencyId() {
    return currencyId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getWalletId() {
    return walletId;
  }

  @Override
  public String toString() {
    return "BalanceUpdatedEvent{"
        + "currencyId='"
        + currencyId
        + '\''
        + ", walletId='"
        + walletId
        + '\''
        + ", amount="
        + amount
        + '}';
  }
}
