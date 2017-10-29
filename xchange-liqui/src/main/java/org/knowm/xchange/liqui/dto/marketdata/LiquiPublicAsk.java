package org.knowm.xchange.liqui.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.math.BigDecimal;
import java.util.List;

public class LiquiPublicAsk {

    private final BigDecimal price;
    private final BigDecimal volume;

    @JsonCreator
    public LiquiPublicAsk(final List<Double> ask) {
        price = new BigDecimal(ask.get(0).toString());
        volume = new BigDecimal(ask.get(1).toString());
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    @Override
    public String toString() {
        return "LiquiPublicAsk{" +
                "price=" + price +
                ", volume=" + volume +
                '}';
    }
}
