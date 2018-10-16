package com.trade.cache.config.properties;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

public class CamelProperties {
    private String publicationQueue;
    private String dlq;
    private String publicationTopic;

    @NestedConfigurationProperty
    private RedeliveryProperties redelivery = new RedeliveryProperties();

    public RedeliveryProperties getRedelivery() {
        return redelivery;
    }

    public String getPublicationQueue() {
        return publicationQueue;
    }

    public void setPublicationQueue(String publicationQueue) {
        this.publicationQueue = publicationQueue;
    }

    public String getDlq() {
        return dlq;
    }

    public void setDlq(String dlq) {
        this.dlq = dlq;
    }

    public String getPublicationTopic() {
        return publicationTopic;
    }

    public void setPublicationTopic(String publicationTopic) {
        this.publicationTopic = publicationTopic;
    }

    public int getMaxRedeliveries() {
        return redelivery.getMax();
    }

    public long getMaxRedeliveryDelay() {
        return redelivery.getMaxDelay();
    }

    public long getRedeliveryDelay() {
        return redelivery.getRedeliveryDelay();
    }

    public double getBackOffMultiplier() {
        return redelivery.getBackOffMultiplier();
    }
}
