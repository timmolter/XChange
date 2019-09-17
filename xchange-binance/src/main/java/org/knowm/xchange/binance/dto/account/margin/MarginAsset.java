package org.knowm.xchange.binance.dto.account.margin;

import java.math.BigDecimal;

public class MarginAsset {

  private String assetFullName;
  private String assetName;
  private Boolean isBorrowable;
  private Boolean isMortgageable;
  private BigDecimal userMinBorrow;
  private BigDecimal userMinRepay;

  public MarginAsset(
      String assetFullName,
      String assetName,
      Boolean isBorrowable,
      Boolean isMortgageable,
      BigDecimal userMinBorrow,
      BigDecimal userMinRepay) {
    this.assetFullName = assetFullName;
    this.assetName = assetName;
    this.isBorrowable = isBorrowable;
    this.isMortgageable = isMortgageable;
    this.userMinBorrow = userMinBorrow;
    this.userMinRepay = userMinRepay;
  }

  public String getAssetFullName() {
    return assetFullName;
  }

  public void setAssetFullName(String assetFullName) {
    this.assetFullName = assetFullName;
  }

  public String getAssetName() {
    return assetName;
  }

  public void setAssetName(String assetName) {
    this.assetName = assetName;
  }

  public Boolean getBorrowable() {
    return isBorrowable;
  }

  public void setBorrowable(Boolean borrowable) {
    isBorrowable = borrowable;
  }

  public Boolean getMortgageable() {
    return isMortgageable;
  }

  public void setMortgageable(Boolean mortgageable) {
    isMortgageable = mortgageable;
  }

  public BigDecimal getUserMinBorrow() {
    return userMinBorrow;
  }

  public void setUserMinBorrow(BigDecimal userMinBorrow) {
    this.userMinBorrow = userMinBorrow;
  }

  public BigDecimal getUserMinRepay() {
    return userMinRepay;
  }

  public void setUserMinRepay(BigDecimal userMinRepay) {
    this.userMinRepay = userMinRepay;
  }
}
