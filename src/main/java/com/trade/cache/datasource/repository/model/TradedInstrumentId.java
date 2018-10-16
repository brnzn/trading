package com.trade.cache.datasource.repository.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TradedInstrumentId implements Serializable {
    @Column(name = "VENDOR_ID")
    private String vendorId;

    private String symbol;

    public TradedInstrumentId() {
    }

    public TradedInstrumentId(String vendorId, String symbol) {
        this.vendorId = vendorId;
        this.symbol = symbol;
    }

    public String getVendorId() {
        return vendorId;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradedInstrumentId that = (TradedInstrumentId) o;
        return Objects.equals(vendorId, that.vendorId) &&
                Objects.equals(symbol, that.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vendorId, symbol);
    }

    @Override
    public String toString() {
        return "TradedInstrumentPK{" +
                "vendorId='" + vendorId + '\'' +
                ", symbol='" + symbol + '\'' +
                '}';
    }
}
