package org.knowm.xchange.bitmax.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BitmaxResponse<V> {

  @JsonIgnore private final String ac;

  @JsonIgnore private final String accountId;

  @JsonIgnore private final String message;

  private final Integer code;

  private final V data;

  public BitmaxResponse(
      @JsonProperty("ac") String ac,
      @JsonProperty("accountId") String accountId,
      @JsonProperty("message") String message,
      @JsonProperty("code") Integer code,
      @JsonProperty("data") V data) {
    this.ac = ac;
    this.accountId = accountId;
    this.message = message;
    this.code = code;
    this.data = data;
  }

  public String getAc() {
    return ac;
  }

  public String getAccountId() {
    return accountId;
  }

  public String getMessage() {
    return message;
  }

  public Integer getCode() {
    return code;
  }

  public V getData() {
    return data;
  }

  @Override
  public String toString() {
    return "BitmaxResponse{"
        + "ac='"
        + ac
        + '\''
        + ", accountId='"
        + accountId
        + '\''
        + ", message='"
        + message
        + '\''
        + ", code="
        + code
        + ", data="
        + data
        + '}';
  }
}
