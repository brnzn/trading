package com.trade.cache.request;

import java.util.List;

public class VendorPublication {
    private String vendor;
    private List<InstrumentPrice> instruments;

    public VendorPublication() {}

    public String getVendor() {
        return vendor;
    }

    public List<InstrumentPrice> getInstruments() {
        return instruments;
    }

}
