package com.trade.cache.camel;

import com.trade.cache.domain.PublishedInstrument;
import com.trade.cache.request.VendorPublication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component("instrumentsSplitter")
public class InstrumentsSplitter {
    private static final Logger LOG = LoggerFactory.getLogger(InstrumentsSplitter.class);

    public List<PublishedInstrument> split(VendorPublication publication) {
        LOG.info("==> Splitting instruments {}", publication.getInstruments());

        return publication.getInstruments().stream()
                .map(ti -> new PublishedInstrument(publication.getVendor(), ti.getSymbol(), ti.getPrice()))
                .collect(Collectors.toList());
    }

}
