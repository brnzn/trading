package com.trade.cache.servivce;

import com.trade.cache.datasource.CacheDatasource;
import com.trade.cache.domain.PublishedInstrument;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InstrumentsSweeperTest {
    @Mock
    private CacheDatasource cacheDatasource;

    @InjectMocks
    private InstrumentsSweeper sweeper;

    private Integer daysToKeep = 30;

    @Before
    public void init() {
        ReflectionTestUtils.setField(sweeper, "daysToKeep", daysToKeep);
    }

    @Test
    public void shouldDeleteLOldRecords() {
        List<PublishedInstrument> instruments = asList(new PublishedInstrument("V1", "S1", new BigDecimal("1")),
                new PublishedInstrument("V1", "S2", new BigDecimal("1")));

        ArgumentCaptor<Date> dateArgumentCaptor = ArgumentCaptor.forClass(Date.class);

        when(cacheDatasource.findAllWithLastUpdateDateBefore(dateArgumentCaptor.capture())).thenReturn(instruments);

        sweeper.sweep();

        verify(cacheDatasource).deleteAll(instruments);

        Calendar today = Calendar.getInstance();

        Calendar capturedCalendar = Calendar.getInstance();
        capturedCalendar.setTime(dateArgumentCaptor.getValue());

        assertThat(today.get(Calendar.DAY_OF_YEAR) - capturedCalendar.get(Calendar.DAY_OF_YEAR)).isEqualTo(daysToKeep);
    }

    @Test
    public void shouldNotDeleteLIfNoRecordsFound() {
        when(cacheDatasource.findAllWithLastUpdateDateBefore(any(Date.class))).thenReturn(emptyList());

        sweeper.sweep();

        verify(cacheDatasource, never()).deleteAll(any());
    }
}