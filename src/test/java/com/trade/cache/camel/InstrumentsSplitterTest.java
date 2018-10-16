package com.trade.cache.camel;

import com.trade.cache.domain.PublishedInstrument;
import com.trade.cache.request.InstrumentPrice;
import com.trade.cache.request.VendorPublication;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class InstrumentsSplitterTest {
    private String vendor = "vendor";
    private String symbol = "symbol";
    private BigDecimal price = new BigDecimal("1");

    @Test
    public void shouldSplitInstrumentPrice() {
        VendorPublication publication = vendorPublication(asList(instrumentPrice(symbol, price)));

        InstrumentsSplitter splitter = new InstrumentsSplitter();

        List<PublishedInstrument> instruments = splitter.split(publication);

        assertThat(instruments).hasSize(1);

        PublishedInstrument instrument = instruments.get(0);

        assertThat(instrument.getVendor()).isEqualTo(vendor);
        assertThat(instrument.getSymbol()).isEqualTo(symbol);
        assertThat(instrument.getPrice()).isEqualTo(price);
    }

    @Test
    public void numberOfInstrumentsShouldMatchNumberOfPrices() {
        VendorPublication publication = vendorPublication(asList(mock(InstrumentPrice.class), mock(InstrumentPrice.class)));

        InstrumentsSplitter splitter = new InstrumentsSplitter();

        List<PublishedInstrument> instruments = splitter.split(publication);

        assertThat(instruments).hasSize(2);
    }

    private VendorPublication vendorPublication(List<InstrumentPrice> instruments) {
        VendorPublication publication = new VendorPublication();

        ReflectionTestUtils.setField(publication, "vendor", vendor);
        ReflectionTestUtils.setField(publication, "instruments", instruments);

        return publication;
    }

    private InstrumentPrice instrumentPrice(String symbol, BigDecimal price) {
        InstrumentPrice instrumentPrice = new InstrumentPrice();
        ReflectionTestUtils.setField(instrumentPrice, "symbol", symbol);
        ReflectionTestUtils.setField(instrumentPrice, "price", price);

        return instrumentPrice;
    }
}