package com.trade.cache.web;

import com.trade.cache.domain.PublishedInstrument;
import com.trade.cache.servivce.InstrumentsProvider;
import com.trade.cache.web.error.MissingRequestParameterException;
import com.trade.cache.web.error.MultipleRequestParametersException;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class InstrumentsCacheController {

    private InstrumentsProvider instrumentsProvider;

    @Autowired
    public InstrumentsCacheController(InstrumentsProvider instrumentsProvider) {
        this.instrumentsProvider = instrumentsProvider;
    }

    @GetMapping(value = "/instruments", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request (either no params provided, or vendor and symbol provided)"),
            @ApiResponse(code = 404, message = "Instruments Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<Collection<PublishedInstrument>> getInstruments(@RequestParam("vendor") Optional<String> vendor,
                                                                          @RequestParam("symbol") Optional<String> symbol) {
        validateParams(vendor, symbol);

        Collection<PublishedInstrument> publishedInstruments = vendor.map(v -> instrumentsProvider.getInstrumentsByVendor(v))
                .orElseGet(() -> instrumentsProvider.getInstrumentsBySymbol(symbol.get()));

        if(CollectionUtils.isEmpty(publishedInstruments)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().body(publishedInstruments);
    }

    private void validateParams(@RequestParam("vendor") Optional<String> vendor, @RequestParam("symbol") Optional<String> symbol) {
        if(!vendor.isPresent() && !symbol.isPresent()) {
            throw new MissingRequestParameterException();
        }

        if(vendor.isPresent() && symbol.isPresent()) {
            throw new MultipleRequestParametersException();
        }
    }

}
