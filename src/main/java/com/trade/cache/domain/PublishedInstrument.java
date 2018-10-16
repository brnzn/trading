package com.trade.cache.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class PublishedInstrument implements Serializable {
    private String vendor;
    private String symbol;
    private BigDecimal price;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date lastUpdate;

    public PublishedInstrument(String vendor, String symbol, BigDecimal price) {
        this(vendor, symbol, price, null);
    }

    public PublishedInstrument(String vendor, String symbol, BigDecimal price, Date lastUpdate) {
        this.vendor = vendor;
        this.symbol = symbol;
        this.price = price;
        this.lastUpdate = lastUpdate;
    }

    public String getVendor() {
        return vendor;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public String toString() {
        return "PublishedInstrument{" +
                "vendor='" + vendor + '\'' +
                ", symbol='" + symbol + '\'' +
                ", price=" + price +
                ", lastUpdate=" + lastUpdate +
                '}';
    }
}
