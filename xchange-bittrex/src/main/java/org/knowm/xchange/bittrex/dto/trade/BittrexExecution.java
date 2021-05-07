package org.knowm.xchange.bittrex.dto.trade;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BittrexExecution {
  private String id;
  private String marketSymbol;
  private Date executedAt;
  private BigDecimal quantity;
  private BigDecimal rate;
  private String orderId;
  private BigDecimal commission;
  private Boolean isTaker;
}
