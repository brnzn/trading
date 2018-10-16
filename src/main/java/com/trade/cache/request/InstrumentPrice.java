package com.trade.cache.request;

import java.math.BigDecimal;

public class InstrumentPrice {
    private String symbol;
    private BigDecimal price;

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
