package com.trade.cache.web;

import com.trade.cache.domain.PublishedInstrument;
import com.trade.cache.servivce.InstrumentsProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = InstrumentsCacheController.class)
public class InstrumentsCacheControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InstrumentsProvider instrumentsProvider;

    @Test
    public void shouldReturnInstrumentsByVendor() throws Exception {
        String vendor = "v1";
        String symbol1 = "symbol";
        BigDecimal price1 = new BigDecimal("1.1");
        String symbol2 = "s2";
        BigDecimal price2 = new BigDecimal("2.2");

        when(instrumentsProvider.getInstrumentsByVendor(vendor)).thenReturn(asList(anInstrument(vendor, symbol1, price1), anInstrument(vendor, symbol2, price2)));

        perform(
                get("/api/instruments")
                        .param("vendor", vendor)
                        .contentType("application/json")
                        .accept("application/json"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpect(jsonPath("$[0]vendor", is(vendor)))
                        .andExpect(jsonPath("$[0].symbol", is(symbol1)))
                        .andExpect(jsonPath("$[0].price", is(price1.doubleValue())))
                        .andExpect(jsonPath("$[1]vendor", is(vendor)))
                        .andExpect(jsonPath("$[1].symbol", is(symbol2)))
                        .andExpect(jsonPath("$[1].price", is(price2.doubleValue())));
    }

    @Test
    public void shouldReturnInstrumentsBySymbol() throws Exception {
        String vendor1 = "v1";
        String symbol = "symbol";
        BigDecimal price1 = new BigDecimal("1.1");
        String vendor2 = "v2";
        BigDecimal price2 = new BigDecimal("2.2");

        when(instrumentsProvider.getInstrumentsBySymbol(symbol)).thenReturn(asList(anInstrument(vendor1, symbol, price1), anInstrument(vendor2, symbol, price2)));

        perform(
                get("/api/instruments")
                        .param("symbol", symbol)
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]vendor", is(vendor1)))
                .andExpect(jsonPath("$[0].symbol", is(symbol)))
                .andExpect(jsonPath("$[0].price", is(price1.doubleValue())))
                .andExpect(jsonPath("$[1]vendor", is(vendor2)))
                .andExpect(jsonPath("$[1].symbol", is(symbol)))
                .andExpect(jsonPath("$[1].price", is(price2.doubleValue())));
    }

    @Test
    public void shouldReturn404IfVendorNotFound() throws Exception {
        String vendor = "v1";

        when(instrumentsProvider.getInstrumentsByVendor(vendor)).thenReturn(Collections.emptyList());

        perform(
                get("/api/instruments")
                        .param("vendor", vendor)
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturn400IfVendorAndSymbolParamsMissing() throws Exception {
        perform(
                get("/api/instruments")
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Missing request parameter. Supported parameters are vendor or symbol")));
    }

    @Test
    public void shouldReturn400IfVendorAndSymbolParamsExist() throws Exception {
        perform(
                get("/api/instruments")
                        .param("vendor", "v")
                        .param("symbol", "s")
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Requesting prices by Vendor and Symbol is not supported")));
    }

    @Test
    public void shouldReturn500WhenExceptionOccurred() throws Exception {
        String vendor = "v1";

        when(instrumentsProvider.getInstrumentsByVendor(vendor)).thenThrow(new RuntimeException("test exception"));

        perform(
                get("/api/instruments")
                        .param("vendor", vendor)
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", is("Unexpected Error Occurred")));
    }

    private PublishedInstrument anInstrument(String vendor, String symbol, BigDecimal price) {
        return new PublishedInstrument(vendor, symbol, price);
    }

    private ResultActions perform(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        return mockMvc.perform(requestBuilder);
    }
}