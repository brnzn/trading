package com.trade.cache.datasource;

import com.trade.cache.domain.PublishedInstrument;

import java.util.Collection;
import java.util.Date;

public interface CacheDatasource {
    void deleteAll(Collection<PublishedInstrument> instruments);
    Collection<PublishedInstrument> findAllWithLastUpdateDateBefore(Date date);
    Collection<PublishedInstrument> findAll();
    void upsertPrice(PublishedInstrument instrument);
    Collection<PublishedInstrument> getInstrumentsByVendor(String vendor);
    Collection<PublishedInstrument> getInstrumentsBySymbol(String symbol);
}
