package com.trade.cache.datasource;

import com.trade.cache.datasource.repository.InstrumentsRepository;
import com.trade.cache.datasource.repository.model.InstrumentEntity;
import com.trade.cache.datasource.repository.model.TradedInstrumentId;
import com.trade.cache.domain.PublishedInstrument;
import com.trade.cache.exception.RecoverableCacheAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
public class InMemoryCache implements CacheDatasource {
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryCache.class);

    private final InstrumentsRepository instrumentsRepository;

    @Autowired
    public InMemoryCache(InstrumentsRepository instrumentsRepository) {
        this.instrumentsRepository = instrumentsRepository;
    }

    @Override
    public void deleteAll(Collection<PublishedInstrument> instruments) {
        try {
            List<InstrumentEntity> entities = instruments.stream()
                    .map(this::toInstrumentEntity)
                    .collect(Collectors.toList());

            LOG.debug("Deleting instruments {}", entities);


            instrumentsRepository.deleteAll(entities);
        } catch (TransientDataAccessException | RecoverableDataAccessException e) {
            throw new RecoverableCacheAccessException(e);
        }

    }

    @Override
    public Collection<PublishedInstrument> findAllWithLastUpdateDateBefore(Date date) {
        LOG.debug("Getting instruments before {}", date);

        try {
            List<InstrumentEntity> entities = instrumentsRepository.findAllWithLastUpdateDateTimeBefore(date);

            LOG.debug("Instruments before date {} [{}]", date, entities);

            return entities.stream()
                    .map(this::toPublishedInstrument)
                    .collect(Collectors.toList());
        } catch (TransientDataAccessException | RecoverableDataAccessException e) {
            throw new RecoverableCacheAccessException(e);
        }

    }

    @Override
    public Collection<PublishedInstrument> findAll() {
        try {
            Iterable<InstrumentEntity> entities = instrumentsRepository.findAll();

            return StreamSupport.stream(entities.spliterator(), false)
                    .map(this::toPublishedInstrument)
                    .collect(Collectors.toList());
        } catch (TransientDataAccessException | RecoverableDataAccessException e) {
            throw new RecoverableCacheAccessException(e);
        }
    }

    @Override
    public void upsertPrice(PublishedInstrument instrument) {
        LOG.debug("Saving {}", instrument);

        try {
            TradedInstrumentId instrumentId = new TradedInstrumentId(instrument.getVendor(), instrument.getSymbol());

            Optional<InstrumentEntity> existingInstrument = instrumentsRepository.findById(instrumentId);

            InstrumentEntity tradedInstrument = existingInstrument.map(ti -> ti.withPrice(instrument.getPrice()))
                    .orElseGet(() -> toInstrumentEntity(instrument));

            instrumentsRepository.save(tradedInstrument);
        } catch (TransientDataAccessException | RecoverableDataAccessException e) {
            throw new RecoverableCacheAccessException(e);
        }

    }

    @Override
    public Collection<PublishedInstrument> getInstrumentsByVendor(String vendor) {
        LOG.debug("Getting instruments by vendor [{}]", vendor);

        try {
            List<InstrumentEntity> instruments = instrumentsRepository.findByTradedInstrumentIdVendorId(vendor);

            return toPublishedInstruments(instruments);
        } catch (TransientDataAccessException | RecoverableDataAccessException e) {
            throw new RecoverableCacheAccessException(e);
        }

    }

    @Override
    public Collection<PublishedInstrument> getInstrumentsBySymbol(String symbol) {
        LOG.debug("Getting instruments by symbol [{}]", symbol);

        try {
            List<InstrumentEntity> instruments = instrumentsRepository.findByTradedInstrumentIdSymbol(symbol);

            return toPublishedInstruments(instruments);
        } catch (TransientDataAccessException | RecoverableDataAccessException e) {
            throw new RecoverableCacheAccessException(e);
        }
    }

    private Collection<PublishedInstrument> toPublishedInstruments(List<InstrumentEntity> instruments) {
        return instruments.stream()
                .map(this::toPublishedInstrument).collect(Collectors.toList());
    }

    private PublishedInstrument toPublishedInstrument(InstrumentEntity instrument) {
        return new PublishedInstrument(instrument.getTradedInstrumentId().getVendorId(),
                instrument.getTradedInstrumentId().getSymbol(),
                instrument.getPrice(),
                instrument.getLastUpdate());
    }

    private InstrumentEntity toInstrumentEntity(PublishedInstrument instrument) {
        return new InstrumentEntity(instrument.getVendor(), instrument.getSymbol(), instrument.getPrice());
    }

}
