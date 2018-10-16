package com.trade.cache.servivce;

import com.trade.cache.datasource.CacheDatasource;
import com.trade.cache.domain.PublishedInstrument;
import org.apache.camel.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component("tradedInstrumentsService")
public class TradedInstrumentsService implements InstrumentsConsumer, InstrumentsProvider {
    private final CacheDatasource cacheDatasource;

    @Autowired
    public TradedInstrumentsService(CacheDatasource cacheDatasource) {
        this.cacheDatasource = cacheDatasource;
    }

    @Override
    @Handler
    public void upsertPrice(PublishedInstrument instrument) {
        cacheDatasource.upsertPrice(instrument);
    }

    @Override
    public Collection<PublishedInstrument> getInstrumentsByVendor(String vendor) {
        return cacheDatasource.getInstrumentsByVendor(vendor);
    }

    @Override
    public Collection<PublishedInstrument> getInstrumentsBySymbol(String symbol) {
        return cacheDatasource.getInstrumentsBySymbol(symbol);
    }
}
