package com.trade.cache.camel;

import com.trade.cache.config.properties.AppProperties;
import com.trade.cache.exception.RecoverableException;
import com.trade.cache.request.VendorPublication;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.apache.camel.LoggingLevel.ERROR;

@Component
public class CamelRoutes extends RouteBuilder {
    @Autowired
    private AppProperties properties;

    @Override
    public void configure() {
        onException(RecoverableException.class)
                .maximumRedeliveries(properties.getMaxRedeliveries())
                .maximumRedeliveryDelay(properties.getMaxRedeliveryDelay())
                .redeliveryDelay(properties.getRedeliveryDelay())
                .backOffMultiplier(properties.getBackOffMultiplier())
                .useOriginalMessage()
                .useExponentialBackOff()
                .log(ERROR, "${exception}")
                .to(properties.getDLQ())
                .handled(true)
                .end();

        onException(Throwable.class)
                .id("IrrecoverableExceptionHandler")
                .handled(true)
                .useOriginalMessage()
                .log(ERROR, "${exception}")
                .to(properties.getDLQ())
                .handled(true)
                .end();

        from(properties.getPublicationQueue())
                .routeId("pricesPublicationHandler")
                .log(LoggingLevel.DEBUG, "pricesPublicationHandler", "Received publication [${body}]")
                .unmarshal().json(JsonLibrary.Jackson, VendorPublication.class)
                .split().method(InstrumentsSplitter.class, "split")
                .to("direct:persistTradedInstrument")
                .to("direct:publishTradedInstrument")
                .end();

        from("direct:persistTradedInstrument")
                .routeId("dataPersist")
                .log(LoggingLevel.DEBUG, "direct:persistTradedInstrument", "[${body}]")
                .to("bean:tradedInstrumentsService?method=upsertPrice")
                .end();

        from("direct:publishTradedInstrument")
                .routeId("pricePublisher")
                .log(LoggingLevel.DEBUG, "direct:publishTradedInstrument", "Publishing message ==> [${body}]")
                .marshal().json(JsonLibrary.Jackson)
                .to(properties.getPublicationTopic())
                .end();
    }
}
