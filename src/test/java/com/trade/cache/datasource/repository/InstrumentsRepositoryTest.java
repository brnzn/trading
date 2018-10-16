package com.trade.cache.datasource.repository;

import com.trade.cache.datasource.repository.model.InstrumentEntity;
import com.trade.cache.datasource.repository.model.TradedInstrumentId;
import com.trade.cache.servivce.InstrumentsSweeper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@SpringBootTest
//@AutoConfigureTestDatabase
public class InstrumentsRepositoryTest {
    private String vendor = "vendor";
    private String symbol = "symbol";
    private BigDecimal price = new BigDecimal("1.1");

    @Autowired
    private InstrumentsRepository instrumentsRepository;

    @Before
    public void init() {
        instrumentsRepository.deleteAll();
    }

    @Test
    public void shouldFindInstrumentsBeforeGivenDateTime() {
        InstrumentEntity tradedInstrument = aTradedInstrument(vendor, symbol);

        instrumentsRepository.save(tradedInstrument);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        List<InstrumentEntity> oldInstruments = instrumentsRepository.findAllWithLastUpdateDateTimeBefore(calendar.getTime());

        assertThat(oldInstruments).hasSize(1);
    }

    @Test
    public void shouldNotFindInstrumentsBeforeGivenDateTime() {

        InstrumentEntity tradedInstrument = aTradedInstrument(vendor, symbol);

        instrumentsRepository.save(tradedInstrument);

        Calendar calendar = Calendar.getInstance();
        calendar.roll(Calendar.DAY_OF_MONTH, false);

        List<InstrumentEntity> oldInstruments = instrumentsRepository.findAllWithLastUpdateDateTimeBefore(calendar.getTime());

        assertThat(oldInstruments).isEmpty();
    }

    @Test
    public void shouldSaveTradedInstrument() {
        InstrumentEntity tradedInstrument = aTradedInstrument(vendor, symbol);

        instrumentsRepository.save(tradedInstrument);

        Optional<InstrumentEntity> result = instrumentsRepository.findById(new TradedInstrumentId(vendor, symbol));

        assertThat(result).isNotEmpty();

        InstrumentEntity cachedItem = result.get();

        assertThat(cachedItem.getPrice()).isEqualTo(price);
        assertThat(cachedItem.getTradedInstrumentId()).isEqualTo(tradedInstrument.getTradedInstrumentId());
    }

    @Test
    public void shouldGetAllPricesByVendor() {
        String symbol1 = "S1";
        String symbol2 = "S2";

        List<InstrumentEntity> tradedInstruments = asList(aTradedInstrument(vendor, symbol1), aTradedInstrument(vendor, symbol2));

        tradedInstruments.forEach(instrumentsRepository::save);

        List<InstrumentEntity> result = instrumentsRepository.findByTradedInstrumentIdVendorId(vendor);

        assertThat(result).hasSize(2);

        List<String> symbols = result.stream().map(trading -> trading.getTradedInstrumentId().getSymbol()).collect(Collectors.toList());

        assertThat(symbols).containsExactly(symbol1, symbol2);
    }

    @Test
    public void shouldGetAllPricesForAGivenTradedInstrument() {
        String symbol = "TI";
        String vendor1 = "V1";
        String vendor2 = "V2";

        List<InstrumentEntity> tradedInstruments = asList(aTradedInstrument(vendor1, symbol), aTradedInstrument(vendor2, symbol));

        tradedInstruments.forEach(instrumentsRepository::save);

        List<InstrumentEntity> result = instrumentsRepository.findByTradedInstrumentIdSymbol("TI");

        assertThat(result).hasSize(2);

        List<String> vendors = result.stream().map(trading -> trading.getTradedInstrumentId().getVendorId()).collect(Collectors.toList());

        assertThat(vendors).containsExactly(vendor1, vendor2);
    }

    @Test
    public void shouldUpdateExistingTradedInstrument() {
        BigDecimal newPrice = new BigDecimal("99.99");

        InstrumentEntity tradedInstrument = aTradedInstrument(vendor, symbol);

        instrumentsRepository.save(tradedInstrument);

        tradedInstrument.setPrice(newPrice);

        instrumentsRepository.save(tradedInstrument);

        Optional<InstrumentEntity> updasted = instrumentsRepository.findById(tradedInstrument.getTradedInstrumentId());

        assertThat(updasted).isNotEmpty();
        assertThat(updasted.get().getPrice()).isEqualTo(newPrice);
    }

    @Test
    public void shouldDeleteInstruments() {
        InstrumentEntity tradedInstrument = aTradedInstrument(vendor, symbol);

        instrumentsRepository.save(tradedInstrument);

        instrumentsRepository.deleteAll(asList(tradedInstrument));

        Iterable<InstrumentEntity> all = instrumentsRepository.findAll();

        assertThat(all).isEmpty();
    }

    private InstrumentEntity aTradedInstrument(String vendor, String symbol) {
        return new InstrumentEntity(vendor, symbol, price);
    }

    @TestConfiguration
    static class TestContextConfiguration {

        @Bean
        public InstrumentsSweeper instrumentsSweeper() {
            return mock(InstrumentsSweeper.class);
        }
    }

}