package org.knowm.xchange.okex.v5.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Author: Max Gao (gaamox@tutanota.com) Created: 08-06-2021 */
public class OkexResponse<V> {

  private final String code;
  private final String msg;
  private final V data;

  public OkexResponse(
      @JsonProperty("code") String code,
      @JsonProperty("msg") String msg,
      @JsonProperty("data") V data) {
    this.code = code;
    this.msg = msg;
    this.data = data;
  }

  public boolean isSuccess() {
    return "0".equals(code);
  }

  public V getData() {
    return data;
  }

  public String getCode() {
    return code;
  }

  public String getMsg() {
    return msg;
  }

  @Override
  public String toString() {
    return "OkexResponse{" + "code=" + code + ", msg=" + msg + '}';
  }
}
