package org.knowm.xchange.btc38.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @author nxtpool
 */
public class Btc38Trade {

  private final long date;
  private final BigDecimal price;
  private final BigDecimal amount;
  private final String tid;

  /**
   * Constructor
   *
   * @param date
   * @param price
   * @param amount
   * @param tid
   */
  public Btc38Trade(@JsonProperty("date") long date, @JsonProperty("price") BigDecimal price, @JsonProperty("amount") BigDecimal amount,
                    @JsonProperty("tid") String tid) {

    this.date = date;
    this.price = price;
    this.amount = amount;
    this.tid = tid;
  }

  public long getDate() {

    return date;
  }

  public BigDecimal getPrice() {

    return price;
  }

  public BigDecimal getAmount() {

    return amount;
  }

  public String getTid() {

    return tid;
  }

  @Override
  public String toString() {

    return "Btc38Trade{" + "date=" + date + ", price=" + price + ", amount=" + amount + ", tid='" + tid + '\'' + '}';
  }
}