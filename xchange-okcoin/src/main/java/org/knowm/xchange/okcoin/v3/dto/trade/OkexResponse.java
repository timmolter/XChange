package org.knowm.xchange.okcoin.v3.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.knowm.xchange.okcoin.v3.service.OkexException;
import si.mazi.rescu.HttpResponseAware;

@Setter
public abstract class OkexResponse implements HttpResponseAware {

  @Getter private Map<String, List<String>> responseHeaders;

  @Setter private String httpBody;

  @Getter private Boolean result;

  @JsonProperty("error_code")
  private String errorCode;

  @JsonProperty("error_message")
  private String errorMessage;

  public void checkResult() {
    if ((errorMessage != null && !errorMessage.isEmpty())
        || (errorCode != null && !errorCode.isEmpty() && !errorCode.equals("0"))) {
      OkexException e = new OkexException();
      e.setCode(errorCode);
      e.setMessage(errorMessage + "\nbody: " + httpBody + "\nheaders: " + responseHeaders);
      throw e;
    }
  }
}
