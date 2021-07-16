package org.knowm.xchange.kucoin.dto.response;

import lombok.Data;

@Data
public class DepositAddressResponse {
    private String address;
    private String memo;
    private String chain;
    private String contractAddress;

}
