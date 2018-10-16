package com.trade.cache.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app")
public class AppProperties {
    @NestedConfigurationProperty
    private BrokerProperties broker = new BrokerProperties();

    @NestedConfigurationProperty
    private CamelProperties camel = new CamelProperties();

    public String getBrokerUrl() {
        return broker.getUrl();
    }

    public BrokerProperties getBroker() {
        return broker;
    }

    public CamelProperties getCamel() {
        return camel;
    }

    public String getDLQ() {
        return camel.getDlq();
    }

    public String getPublicationQueue() {
        return camel.getPublicationQueue();
    }

    public String getPublicationTopic() {
        return camel.getPublicationTopic();
    }

    public int getMaxRedeliveries() {
        return camel.getMaxRedeliveries();
    }

    public long getMaxRedeliveryDelay() {
        return camel.getMaxRedeliveryDelay();
    }

    public long getRedeliveryDelay() {
        return camel.getRedeliveryDelay();
    }

    public double getBackOffMultiplier() {
        return camel.getBackOffMultiplier();
    }
}
