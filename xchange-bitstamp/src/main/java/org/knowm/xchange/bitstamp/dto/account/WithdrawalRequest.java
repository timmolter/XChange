package org.knowm.xchange.bitstamp.dto.account;

import java.math.BigDecimal;
import java.util.Date;

import org.knowm.xchange.bitstamp.BitstampUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WithdrawalRequest {

  private Long id;

  private final Date datetime;

  private Type type;
  private BigDecimal amount;
  @JsonProperty("status")
  private String statusOriginal;         // keep the original status, if it comes to "unknown"
  
  private String data;                   // additional withdrawal request data
  private String address;                // Bitcoin withdrawal address (bitcoin withdrawals only).
  @JsonProperty("transaction_id")
  private String transactionId;         // Transaction id (bitcoin withdrawals only).

  public Long getId() { return id; }
  public WithdrawalRequest(@JsonProperty("datetime") String datetime) {
    super();
    this.datetime = BitstampUtils.parseDate(datetime);
  }
  
  public Date getDatetime() { return datetime; }
  public Type getType() { return type; }
  public BigDecimal getAmount() { return amount; }
  public Status getStatus() {
      return Status.fromString(statusOriginal); 
  }
  public String getStatusOriginal() { return statusOriginal; }
  public String getData() { return data; }
  public String getAddress() { return address; }
  public String getTransactionId() { return transactionId; }

  @Override
  public String toString() {
    return "WithdrawalRequest [id=" + id + ", datetime=" + datetime + ", type=" + type + ", amount=" + amount + ", status="
            + getStatus() + ", statusOriginal=" + statusOriginal + ", data=" + data + ", address=" + address + ", transactionId=" + transactionId + "]";
  }

  public enum Type {
    SEPA, bitcoin, wire, rippleUSD, rippleBTC, XRP, litecoin, unknown;
    
    // 0 (SEPA), 1 (bitcoin) or 2(WIRE transfer).
    @JsonCreator
    public static Type fromString(String string) {
        switch (string) {
        case "0": return SEPA;
        case "1": return bitcoin;
        case "2": return wire;
        case "6": return rippleUSD;
        case "7": return rippleBTC;
        case "14": return XRP;
        case "15": return litecoin;
        default: return unknown;
      }
    }
  }

  public enum Status {
    open, in_process, finished, canceled, failed, unknown;
    
    // 0 (open), 1 (in process), 2 (finished), 3 (canceled) or 4 (failed).
    @JsonCreator
    public static Status fromString(String string) {
        switch (string) {
        case "0": return open;
        case "1": return in_process;
        case "2": return finished;
        case "3": return canceled;
        case "4": return failed;
        default: return unknown;
      }
    }
  }
}
