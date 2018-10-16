package com.trade.cache.demo;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;


public class SimpleProducer {
    private String instrumentSeperator = ",";
    private String instrumentTemplate = "{\"symbol\": \"%s\",\"price\": %2.2f}";

    private static final String PUBLICATION = "{\"vendor\":\"%s\", \"instruments\": [%s]}";

    private static final Logger LOG = LoggerFactory.getLogger(SimpleProducer.class);
    @Autowired
    private ProducerTemplate producerTemplate;

    @Scheduled(cron = "*/5 * * * * *")
    public void createMessages() {
        int numberOfInstruments = RandomUtils.nextInt(1,3);

        StringBuilder sb = new StringBuilder();

        String vendor = RandomStringUtils.random(2, 'A', 'B');

        for (int counter = 0; counter <= numberOfInstruments; counter++) {
            String instrument = generateInstrument();

            if(counter > 0) {
                sb.append(instrumentSeperator);
            }

            sb.append(instrument);
        }

        LOG.info("Generating message [{}]", String.format(PUBLICATION, vendor, sb.toString()));

        producerTemplate.sendBody("activemq:prices", String.format(PUBLICATION, vendor, sb.toString()));
    }

    private String generateInstrument() {
        String symbol = RandomStringUtils.random(2, 'A', 'B');
        BigDecimal price = new BigDecimal(RandomUtils.nextDouble(0, 2));

        return String.format(instrumentTemplate, symbol, price);
    }

}
