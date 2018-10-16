package com.trade.cache.camel;

import com.trade.cache.config.CamelConfig;
import com.trade.cache.config.properties.AppProperties;
import com.trade.cache.datasource.repository.InstrumentsRepository;
import com.trade.cache.domain.PublishedInstrument;
import com.trade.cache.exception.RecoverableException;
import com.trade.cache.servivce.InstrumentsSweeper;
import com.trade.cache.servivce.TradedInstrumentsService;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest
@Import(CamelConfig.class)
@MockEndpoints
public class CamelRoutesTest {
    private static final String PUBLICATION_QUEUE = "activemq:prices";
    private static final String VENDOR = "V1";
    private static final String SYMBOL = "b1";
    private static final String PRICE = "1.01";

    private String singlePriceMessage = String.format("{\"vendor\":\"%s\", \"instruments\": [{\"symbol\": \"%s\",\"price\": %s}]}", VENDOR, SYMBOL, PRICE);

    @Autowired
    private AppProperties properties;

    @Autowired
    private CamelContext camelContext;

    @EndpointInject(uri = "mock:bean:tradedInstrumentsService")
    private MockEndpoint tradedInstrumentsServiceEndpoint;

    @EndpointInject(uri = "mock:activemq:topic:instruments")
    private MockEndpoint instrumentsPublicationTopicEndpoint;

    @EndpointInject(uri = "mock:activemq:prices:dlq")
    private MockEndpoint pricesDlq;

    @MockBean
    private InstrumentsRepository instrumentsRepository;

    @MockBean
    private TradedInstrumentsService tradedInstrumentsService;

    @Before
    public void init() {
        tradedInstrumentsServiceEndpoint.reset();
        instrumentsPublicationTopicEndpoint.reset();
        pricesDlq.reset();
    }

    @Test
    public void shouldRouteInMessageSuccessfully() throws Exception {
        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();

        String expectedPublished = "{\"vendor\":\"V1\",\"symbol\":\"b1\",\"price\":1.01}";

        tradedInstrumentsServiceEndpoint.setExpectedCount(1);

        instrumentsPublicationTopicEndpoint.setExpectedCount(1);
        instrumentsPublicationTopicEndpoint.expectedBodiesReceived(expectedPublished);

        NotifyBuilder notify = new NotifyBuilder(camelContext).whenDone(1).create();

        producerTemplate.sendBody(PUBLICATION_QUEUE, singlePriceMessage);

        assertTrue(notify.matches(10, TimeUnit.SECONDS));

        tradedInstrumentsServiceEndpoint.assertIsSatisfied();
        instrumentsPublicationTopicEndpoint.assertIsSatisfied();
    }

    @Test
    public void exceptionWhileSavingInstrumentShouldGoToDLQ() throws Exception {
        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();

        doThrow(new RuntimeException("test exception")).when(tradedInstrumentsService).upsertPrice(any(PublishedInstrument.class));

        pricesDlq.setExpectedCount(1);
        instrumentsPublicationTopicEndpoint.setExpectedCount(0);

        NotifyBuilder notify = new NotifyBuilder(camelContext).whenDone(1).create();


        producerTemplate.sendBody(PUBLICATION_QUEUE, singlePriceMessage);

        assertTrue(notify.matches(5, TimeUnit.SECONDS));

        instrumentsPublicationTopicEndpoint.assertIsSatisfied();
        pricesDlq.assertIsSatisfied();
    }

    @Test
    public void shouldRetryOnRecoverableExceptionAndThenRouteToDLQ() throws Exception {
        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();

        doThrow(new RecoverableException(null)).when(tradedInstrumentsService).upsertPrice(any(PublishedInstrument.class));

        tradedInstrumentsServiceEndpoint.setExpectedMessageCount(1 + properties.getMaxRedeliveries());
        pricesDlq.setExpectedCount(1);
        instrumentsPublicationTopicEndpoint.setExpectedCount(0);

        NotifyBuilder notify = new NotifyBuilder(camelContext).whenDone(1).create();

        producerTemplate.sendBody(PUBLICATION_QUEUE, singlePriceMessage);

        assertTrue(notify.matches(10, TimeUnit.SECONDS));

        instrumentsPublicationTopicEndpoint.assertIsSatisfied();
        pricesDlq.assertIsSatisfied();
        tradedInstrumentsServiceEndpoint.assertIsSatisfied();
    }

    @Test
    public void failuresAfterSplitterShouldHappenInIsolation () throws Exception {
        camelContext.getRouteDefinition("dataPersist").adviceWith(camelContext, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveAddLast().throwException(new RuntimeException("!!!!"));
            }
        });

        camelContext.start();
        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();

        String symbol1 = "s1";
        String symbol2 = "s2";
        String multiPriceMessage = String.format("{\"vendor\":\"v1\", \"instruments\": [{\"symbol\": \"%s\",\"price\": 1}, {\"symbol\": \"%s\",\"price\": 2}]}", symbol1, symbol2);

        tradedInstrumentsServiceEndpoint.setExpectedCount(2);

        instrumentsPublicationTopicEndpoint.setExpectedCount(0);

        NotifyBuilder notify = new NotifyBuilder(camelContext).whenDone(1).create();

        producerTemplate.sendBody(PUBLICATION_QUEUE, multiPriceMessage);

        assertTrue(notify.matches(10, TimeUnit.SECONDS));

        tradedInstrumentsServiceEndpoint.assertIsSatisfied();
        instrumentsPublicationTopicEndpoint.assertIsSatisfied();

        List<Exchange> exchanges = tradedInstrumentsServiceEndpoint.getExchanges();
        assertThat(exchanges.get(0).getIn().getBody(PublishedInstrument.class).getSymbol()).isEqualTo(symbol1);
        assertThat(exchanges.get(1).getIn().getBody(PublishedInstrument.class).getSymbol()).isEqualTo(symbol2);
    }

    @TestConfiguration
    static class TestContextConfiguration {
        @Bean
        public InstrumentsSweeper instrumentsSweeper() {
            return mock(InstrumentsSweeper.class);
        }
    }
}