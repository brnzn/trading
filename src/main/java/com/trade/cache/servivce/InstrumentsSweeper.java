package com.trade.cache.servivce;

import com.trade.cache.datasource.CacheDatasource;
import com.trade.cache.domain.PublishedInstrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

@Component
public class InstrumentsSweeper {
    private static final Logger LOG = LoggerFactory.getLogger(InstrumentsSweeper.class);

    private final CacheDatasource cacheDatasource;

    @Value("${app.maxInstrumentAge}")
    private Integer daysToKeep;

    @Autowired
    public InstrumentsSweeper(CacheDatasource cacheDatasource) {
        this.cacheDatasource = cacheDatasource;
    }

    @Scheduled(cron = "${cron.sweeper}")
    public void sweep() {
        LOG.info("Sweeping...");
        Date date = getCutoffDate();
        Collection<PublishedInstrument> instrumentsToDelete = cacheDatasource.findAllWithLastUpdateDateBefore(date);

        if(!CollectionUtils.isEmpty(instrumentsToDelete)) {
            cacheDatasource.deleteAll(instrumentsToDelete);
        }
    }

    private Date getCutoffDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.roll(Calendar.DAY_OF_YEAR, -daysToKeep);

        return calendar.getTime();
    }

}
