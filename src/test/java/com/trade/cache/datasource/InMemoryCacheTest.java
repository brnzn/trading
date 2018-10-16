package com.trade.cache.datasource;

import com.trade.cache.datasource.repository.InstrumentsRepository;
import com.trade.cache.datasource.repository.model.InstrumentEntity;
import com.trade.cache.datasource.repository.model.TradedInstrumentId;
import com.trade.cache.domain.PublishedInstrument;
import com.trade.cache.exception.RecoverableCacheAccessException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.RecoverableDataAccessException;

import java.math.BigDecimal;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InMemoryCacheTest {
    @Rule
    public ExpectedException thrown= ExpectedException.none();

    private String vendor = "vendor";
    private String symbol = "symbol";
    private BigDecimal price = new BigDecimal("1");

    @Mock
    private InstrumentsRepository instrumentsRepository;

    @InjectMocks
    private InMemoryCache inMemoryCache;

    @Captor
    private ArgumentCaptor<List<InstrumentEntity>> entityArgumentCaptor;

    @Test
    public void shouldDeleteGivenInstruments() {
        String vendor1 = "vendor1";
        PublishedInstrument instrument = new PublishedInstrument(vendor, symbol, price);
        PublishedInstrument instrument1 = new PublishedInstrument(vendor1, symbol, price);

        List<PublishedInstrument> publishedInstruments = asList(instrument, instrument1);

        inMemoryCache.deleteAll(publishedInstruments);

        verify(instrumentsRepository).deleteAll(entityArgumentCaptor.capture());

        List<InstrumentEntity> entities = entityArgumentCaptor.getValue();

        assertThat(entities).hasSize(2);

        InstrumentEntity expectedEntity = new InstrumentEntity(instrument.getVendor(), instrument.getSymbol(), instrument.getPrice());
        InstrumentEntity expectedEntity1 = new InstrumentEntity(instrument1.getVendor(), instrument1.getSymbol(), instrument1.getPrice());

        assertThat(entities).containsExactlyInAnyOrder(expectedEntity, expectedEntity1);
    }

    @Test
    public void shouldFindInstrumentsWithLastUpdateDateBeforeGivenDate() {
        InstrumentEntity expectedEntity = new InstrumentEntity(vendor, symbol, price);
        Date date = new Date();

        when(instrumentsRepository.findAllWithLastUpdateDateTimeBefore(date)).thenReturn(asList(expectedEntity));

        Collection<PublishedInstrument> instruments = inMemoryCache.findAllWithLastUpdateDateBefore(date);

        assertThat(instruments).hasSize(1);

        PublishedInstrument instrument = instruments.iterator().next();

        assertThat(instrument.getVendor()).isEqualTo(vendor);
        assertThat(instrument.getSymbol()).isEqualTo(symbol);
        assertThat(instrument.getPrice()).isEqualTo(price);
    }

    @Test
    public void shouldFindAll() {
        String vendor1 = "vendor1";
        InstrumentEntity expectedEntity = new InstrumentEntity(vendor, symbol, price);
        InstrumentEntity expectedEntity1 = new InstrumentEntity(vendor1, symbol, price);

        when(instrumentsRepository.findAll()).thenReturn(asList(expectedEntity, expectedEntity1));

        Collection<PublishedInstrument> all = inMemoryCache.findAll();

        assertThat(all).hasSize(2);

        List<PublishedInstrument> publishedInstruments = new ArrayList<>(all);

        assertTrue(compare(expectedEntity, publishedInstruments.get(0)));
        assertTrue(compare(expectedEntity1, publishedInstruments.get(1)));
    }

    @Test
    public void shouldSaveNewInstrument() {
        PublishedInstrument publishedInstrument = new PublishedInstrument(vendor, symbol, price);

        when(instrumentsRepository.findById(new TradedInstrumentId(vendor, symbol))).thenReturn(Optional.empty());

        ArgumentCaptor<InstrumentEntity> captor = ArgumentCaptor.forClass(InstrumentEntity.class);

        inMemoryCache.upsertPrice(publishedInstrument);

        verify(instrumentsRepository).save(captor.capture());

        InstrumentEntity capturedInstrument = captor.getValue();

        assertThat(capturedInstrument.getPrice()).isEqualTo(price);
        assertThat(capturedInstrument.getTradedInstrumentId().getVendorId()).isEqualTo(vendor);
        assertThat(capturedInstrument.getTradedInstrumentId().getSymbol()).isEqualTo(symbol);

    }

    @Test
    public void shouldUpdateExistingInstrument() {
        BigDecimal newPrice = new BigDecimal("2");

        PublishedInstrument publishedInstrument = new PublishedInstrument(vendor, symbol, newPrice);
        InstrumentEntity tradedInstrument = new InstrumentEntity(vendor, symbol, price);

        when(instrumentsRepository.findById(new TradedInstrumentId(vendor, symbol))).thenReturn(Optional.of(tradedInstrument));

        ArgumentCaptor<InstrumentEntity> captor = ArgumentCaptor.forClass(InstrumentEntity.class);

        inMemoryCache.upsertPrice(publishedInstrument);

        verify(instrumentsRepository).save(captor.capture());

        InstrumentEntity capturedInstrument = captor.getValue();

        assertThat(capturedInstrument).isEqualTo(tradedInstrument);
        assertThat(tradedInstrument.getPrice()).isEqualTo(newPrice);

    }

    @Test
    public void shouldGetInstrumentsByVendor() {
        InstrumentEntity entity = new InstrumentEntity(vendor, symbol, price);

        List<InstrumentEntity> instruments = asList(entity);

        when(instrumentsRepository.findByTradedInstrumentIdVendorId(vendor)).thenReturn(instruments);

        Collection<PublishedInstrument> result = inMemoryCache.getInstrumentsByVendor(vendor);

        assertThat(result).hasSize(1);

        PublishedInstrument publishedInstrument = result.iterator().next();

        assertThat(publishedInstrument.getVendor()).isEqualTo(vendor);
        assertThat(publishedInstrument.getSymbol()).isEqualTo(symbol);
        assertThat(publishedInstrument.getPrice()).isEqualTo(price);
        assertThat(publishedInstrument.getLastUpdate()).isEqualTo(entity.getLastUpdate());

    }

    @Test
    public void shouldGetInstrumentsBySymbol() {
        InstrumentEntity entity = new InstrumentEntity(vendor, symbol, price);
        List<InstrumentEntity> instruments = asList(entity);

        when(instrumentsRepository.findByTradedInstrumentIdSymbol(symbol)).thenReturn(instruments);

        Collection<PublishedInstrument> result = inMemoryCache.getInstrumentsBySymbol(symbol);

        assertThat(result).hasSize(1);

        PublishedInstrument publishedInstrument = result.iterator().next();
        assertThat(publishedInstrument.getVendor()).isEqualTo(vendor);
        assertThat(publishedInstrument.getSymbol()).isEqualTo(symbol);
        assertThat(publishedInstrument.getPrice()).isEqualTo(price);
        assertThat(publishedInstrument.getLastUpdate()).isEqualTo(entity.getLastUpdate());

    }

    @Test
    public void shouldReturnEmptyListIfInstrumentsByVendorNotFound() {
        when(instrumentsRepository.findByTradedInstrumentIdVendorId(vendor)).thenReturn(emptyList());

        Collection<PublishedInstrument> result = inMemoryCache.getInstrumentsByVendor(vendor);

        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnEmptyListIfInstrumentsBySymbolNotFound() {
        when(instrumentsRepository.findByTradedInstrumentIdSymbol(symbol)).thenReturn(emptyList());

        Collection<PublishedInstrument> result = inMemoryCache.getInstrumentsBySymbol(symbol);

        assertThat(result).isEmpty();
    }

    @Test
    public void findAllShouldConvertTransientDataAccessExceptionToRecoverableException() {
        thrown.expect(RecoverableCacheAccessException.class);
        thrown.expectMessage("test exception");

        when(instrumentsRepository.findAll()).thenThrow(new QueryTimeoutException("test exception"));

        inMemoryCache.findAll();
    }

    @Test
    public void findAlShouldConvertRecoverableDataAccessExceptionToRecoverableException() {
        thrown.expect(RecoverableCacheAccessException.class);
        thrown.expectMessage("test exception");

        when(instrumentsRepository.findAll()).thenThrow(new RecoverableDataAccessException("test exception"));

        inMemoryCache.findAll();
    }

    @Test
    public void deleteAllShouldConvertTransientDataAccessExceptionToRecoverableException() {
        thrown.expect(RecoverableCacheAccessException.class);
        thrown.expectMessage("test exception");

        doThrow(new QueryTimeoutException("test exception")).when(instrumentsRepository).deleteAll(Collections.emptyList());

        inMemoryCache.deleteAll(Collections.emptyList());
    }

    @Test
    public void deleteAllShouldConvertRecoverableDataAccessExceptionToRecoverableException() {
        thrown.expect(RecoverableCacheAccessException.class);
        thrown.expectMessage("test exception");

        doThrow(new RecoverableDataAccessException("test exception")).when(instrumentsRepository).deleteAll(Collections.emptyList());

        inMemoryCache.deleteAll(Collections.emptyList());
    }

    @Test
    public void findByDateShouldConvertTransientDataAccessExceptionToRecoverableException() {
        thrown.expect(RecoverableCacheAccessException.class);
        thrown.expectMessage("test exception");

        Date date = new Date();
        when(instrumentsRepository.findAllWithLastUpdateDateTimeBefore(date)).thenThrow(new QueryTimeoutException("test exception"));

        inMemoryCache.findAllWithLastUpdateDateBefore(date);
    }

    @Test
    public void findByDateShouldConvertRecoverableDataAccessExceptionToRecoverableException() {
        thrown.expect(RecoverableCacheAccessException.class);
        thrown.expectMessage("test exception");
        Date date = new Date();

        when(instrumentsRepository.findAllWithLastUpdateDateTimeBefore(date)).thenThrow(new RecoverableDataAccessException("test exception"));

        inMemoryCache.findAllWithLastUpdateDateBefore(date);
    }

    @Test
    public void upsertPriceShouldConvertTransientDataAccessExceptionToRecoverableException() {
        thrown.expect(RecoverableCacheAccessException.class);
        thrown.expectMessage("test exception");

        PublishedInstrument instrument = new PublishedInstrument(vendor, symbol, price);
        doThrow(new QueryTimeoutException("test exception")).when(instrumentsRepository).findById(new TradedInstrumentId(vendor, symbol));

        inMemoryCache.upsertPrice(instrument);
    }

    @Test
    public void upsertPriceShouldConvertRecoverableDataAccessExceptionToRecoverableException() {
        thrown.expect(RecoverableCacheAccessException.class);
        thrown.expectMessage("test exception");

        PublishedInstrument instrument = new PublishedInstrument(vendor, symbol, price);
        doThrow(new RecoverableDataAccessException("test exception")).when(instrumentsRepository).findById(new TradedInstrumentId(vendor, symbol));

        inMemoryCache.upsertPrice(instrument);
    }

    @Test
    public void findByVendorShouldConvertTransientDataAccessExceptionToRecoverableException() {
        thrown.expect(RecoverableCacheAccessException.class);
        thrown.expectMessage("test exception");

        when(instrumentsRepository.findByTradedInstrumentIdVendorId(vendor)).thenThrow(new QueryTimeoutException("test exception"));

        inMemoryCache.getInstrumentsByVendor(vendor);
    }

    @Test
    public void findByVendorShouldConvertRecoverableDataAccessExceptionToRecoverableException() {
        thrown.expect(RecoverableCacheAccessException.class);
        thrown.expectMessage("test exception");

        when(instrumentsRepository.findByTradedInstrumentIdVendorId(vendor)).thenThrow(new RecoverableDataAccessException("test exception"));

        inMemoryCache.getInstrumentsByVendor(vendor);
    }

    @Test
    public void findBySymbolShouldConvertTransientDataAccessExceptionToRecoverableException() {
        thrown.expect(RecoverableCacheAccessException.class);
        thrown.expectMessage("test exception");

        when(instrumentsRepository.findByTradedInstrumentIdSymbol(symbol)).thenThrow(new QueryTimeoutException("test exception"));

        inMemoryCache.getInstrumentsBySymbol(symbol);
    }

    @Test
    public void findBySymbolShouldConvertRecoverableDataAccessExceptionToRecoverableException() {
        thrown.expect(RecoverableCacheAccessException.class);
        thrown.expectMessage("test exception");

        when(instrumentsRepository.findByTradedInstrumentIdSymbol(symbol)).thenThrow(new RecoverableDataAccessException("test exception"));

        inMemoryCache.getInstrumentsBySymbol(symbol);
    }

    private boolean compare(InstrumentEntity entity, PublishedInstrument publishedInstrument) {
        return entity.getTradedInstrumentId().getSymbol().equals(publishedInstrument.getSymbol()) &&
                entity.getTradedInstrumentId().getVendorId().equals(publishedInstrument.getVendor()) &&
                entity.getPrice().equals(publishedInstrument.getPrice());
    }


}