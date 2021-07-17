package org.knowm.xchange.bitmax;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import si.mazi.rescu.HttpStatusExceptionSupport;

public class BitmaxException extends HttpStatusExceptionSupport {

  private final int code;

  public BitmaxException(@JsonProperty("code") int code, @JsonProperty("message") String message) {
    super(message);
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
