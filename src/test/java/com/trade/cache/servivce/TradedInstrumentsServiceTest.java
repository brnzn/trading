package com.trade.cache.servivce;

import com.trade.cache.datasource.CacheDatasource;
import com.trade.cache.domain.PublishedInstrument;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TradedInstrumentsServiceTest {
    private BigDecimal price = new BigDecimal("1");
    private String vendor = "vendor";
    private String symbol = "symbol";

    @Mock
    private CacheDatasource cacheDatasource;

    @InjectMocks
    private TradedInstrumentsService service;

    @Test
    public void shouldSavePublishedTradedInstrument() {

        PublishedInstrument publishedInstrument = new PublishedInstrument(vendor, symbol, price);

        ArgumentCaptor<PublishedInstrument> instrumentArgumentCaptor = ArgumentCaptor.forClass(PublishedInstrument.class);

        service.upsertPrice(publishedInstrument);

        verify(cacheDatasource).upsertPrice(instrumentArgumentCaptor.capture());

        assertThat(publishedInstrument).isEqualTo(instrumentArgumentCaptor.getValue());
    }

    @Test
    public void shouldGetInstrumentsByVendor() {
        List<PublishedInstrument> publishedInstruments = asList(new PublishedInstrument(vendor, symbol, price),
                new PublishedInstrument(vendor, symbol, price));

        when(cacheDatasource.getInstrumentsByVendor(vendor)).thenReturn(publishedInstruments);

        Collection<PublishedInstrument> result = service.getInstrumentsByVendor(vendor);

        assertThat(result).hasSize(2);

        assertThat(result).containsAll(publishedInstruments);
    }

    @Test
    public void shouldGetInstrumentsBySymbol() {
        List<PublishedInstrument> publishedInstruments = asList(new PublishedInstrument(vendor, symbol, price),
                new PublishedInstrument(vendor, symbol, price));

        when(cacheDatasource.getInstrumentsBySymbol(symbol)).thenReturn(publishedInstruments);

        Collection<PublishedInstrument> result = service.getInstrumentsBySymbol(symbol);

        assertThat(result).hasSize(2);

        assertThat(result).containsAll(publishedInstruments);
    }
}