package com.trade.cache.servivce;

import com.trade.cache.domain.PublishedInstrument;

public interface InstrumentsConsumer {
    void upsertPrice(PublishedInstrument instrument);
}
