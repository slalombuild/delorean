package com.slalom.autoconfiguration.delorean;

import com.slalom.delorean.interceptors.inbound.InboundRequestInterceptor;
import com.slalom.delorean.spring.boot.controller.TimeMachineController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(TimeMachineConfigurationProperties.class)
@ConditionalOnProperty(prefix = "slalom.delorean", name = "enabled", havingValue = "true")
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TimeMachineAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TimeMachineAutoConfiguration.class);

    private final TimeMachineConfigurationProperties properties;

    public TimeMachineAutoConfiguration(@Autowired TimeMachineConfigurationProperties properties) {
        this.properties = properties;
    }

    //Enable setting test time based on inbound request headers
    @Bean
    @ConditionalOnProperty(prefix = "slalom.delorean.inboundRequestHeader", name = "enabled", havingValue = "true", matchIfMissing = true)
    public InboundRequestInterceptor deLoreanInboundRequestInterceptor() {
        log.info("slalom.delorean.inboundRequestHeader set to true - adding InboundRequestInterceptor to context");
        return new InboundRequestInterceptor(properties);
    }

    //If setting DateFactory via a cookie is desired inject a Controller to allow it
    @Bean
    @ConditionalOnProperty(prefix = "slalom.delorean.cookie", name = "enabled", havingValue = "true", matchIfMissing = false)
    public TimeMachineController timeMachineController() {
        log.info("Registering TimeMachineController");
        return new TimeMachineController(properties);
    }

    @Configuration
    protected static class TimeMachineMvcConfiguration extends WebMvcConfigurerAdapter {

        private InboundRequestInterceptor timeMachineRequestInterceptor;

        protected TimeMachineMvcConfiguration(@Autowired InboundRequestInterceptor deLoreanInboundRequestInterceptor) {
            this.timeMachineRequestInterceptor = deLoreanInboundRequestInterceptor;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            log.debug("Registering TimeMachineRequestInterceptor with WebMvcConfiguration");
            registry.addInterceptor(this.timeMachineRequestInterceptor);
        }
    }

}
