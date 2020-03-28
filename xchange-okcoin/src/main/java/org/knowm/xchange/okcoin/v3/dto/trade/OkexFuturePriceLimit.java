package org.knowm.xchange.okcoin.v3.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class OkexFuturePriceLimit {

  @JsonProperty("instrument_id")
  private String instrumentId;

  private String timestamp;

  private BigDecimal highest;

  private BigDecimal lowest;
}
