package org.knowm.xchange.kucoin.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeFeeResponse {

    private String symbol;

    private BigDecimal takerFeeRate;

    private BigDecimal makerFeeRate;

}
