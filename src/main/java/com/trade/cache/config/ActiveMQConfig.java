package com.trade.cache.config;

import com.trade.cache.config.properties.AppProperties;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.apache.camel.component.jms.JmsConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.ConnectionFactory;

@Configuration
public class ActiveMQConfig {

    @Autowired
    private AppProperties appProperties;

    @Bean
    public ActiveMQConnectionFactory coreConnectionFactory() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(appProperties.getBrokerUrl());
        return connectionFactory;
    }

    @Bean
    public JmsConfiguration jmsConfiguration(ActiveMQConnectionFactory coreConnectionFactory) {
        JmsConfiguration jmsConfiguration = new JmsConfiguration();
        ConnectionFactory connectionFactory = new PooledConnectionFactory(coreConnectionFactory);
        jmsConfiguration.setConnectionFactory(connectionFactory);
        return jmsConfiguration;
    }

    @Bean
    public ActiveMQComponent activeMQComponent(JmsConfiguration jmsConfiguration) {
        ActiveMQComponent component = new ActiveMQComponent();
        component.setConfiguration(jmsConfiguration);
        return component;
    }

}
