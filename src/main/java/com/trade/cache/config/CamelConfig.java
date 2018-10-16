package com.trade.cache.config;

import com.trade.cache.camel.CamelRoutes;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.spring.CamelBeanPostProcessor;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelConfig {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CamelRoutes camelRoutes;

    @Bean(destroyMethod = "stop")
    public CamelContext camelContext(ActiveMQComponent activeMQComponent) throws Exception {
        SpringCamelContext camelContext = new SpringCamelContext(applicationContext);
        camelContext.disableJMX();

        camelContext.addRoutes(camelRoutes);

        camelContext.addComponent("activemq", activeMQComponent);
        camelContext.start();
        return camelContext;
    }

    @Bean
    public CamelBeanPostProcessor camelBeanPostProcessor(CamelContext camelContext, ApplicationContext applicationContext) {
        CamelBeanPostProcessor processor = new CamelBeanPostProcessor();
        processor.setCamelContext(camelContext);
        processor.setApplicationContext(applicationContext);
        return processor;
    }


}
