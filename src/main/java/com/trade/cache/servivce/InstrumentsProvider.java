package com.trade.cache.servivce;

import com.trade.cache.domain.PublishedInstrument;

import java.util.Collection;

public interface InstrumentsProvider {
    Collection<PublishedInstrument> getInstrumentsByVendor(String vendor);
    Collection<PublishedInstrument> getInstrumentsBySymbol(String symbol);
}
