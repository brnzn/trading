package com.trade.cache.config;

import com.trade.cache.datasource.CacheDatasource;
import com.trade.cache.demo.SimpleDBReader;
import com.trade.cache.demo.SimpleProducer;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ConditionalOnProperty(
        name = "demo",
        havingValue = "true")
@EnableScheduling
public class DemoConfig {
    @Bean
    public ProducerTemplate producerTemplate(CamelContext camelContext) {
        return camelContext.createProducerTemplate();
    }

    @Bean
    public SimpleDBReader simpleDBReader(CacheDatasource cacheDatasource) {
        return new SimpleDBReader(cacheDatasource);
    }

    @Bean
    public SimpleProducer simpleProducer() {
        return new SimpleProducer();
    }
}
