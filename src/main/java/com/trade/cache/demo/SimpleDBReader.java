package com.trade.cache.demo;

import com.trade.cache.datasource.CacheDatasource;
import com.trade.cache.domain.PublishedInstrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Collection;

public class SimpleDBReader {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleDBReader.class);

    private CacheDatasource cacheDatasource;

    public SimpleDBReader(CacheDatasource cacheDatasource) {
        this.cacheDatasource = cacheDatasource;
    }

    @Scheduled(cron = "*/5 * * * * *")
    public void createMessages() {
        LOG.debug("Reading from db...");

        Collection<PublishedInstrument> all = cacheDatasource.findAll();

        StringBuilder sb = new StringBuilder();
        sb.append("\n==================================================================");

        all.forEach(instrument -> {
            sb.append("\n").append(instrument.toString());
        });

        sb.append("\n==================================================================");

        LOG.info(sb.toString());
    }

}
