package com.slalom.autoconfiguration.delorean;

import com.slalom.delorean.interceptors.outbound.SpringRestTemplateTimeMachineOutboundRequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TimeMachineConfigurationProperties.class)
@ConditionalOnProperty(prefix = "slalom.delorean", name = "enabled", havingValue = "true")
@ConditionalOnClass(org.springframework.web.client.RestTemplate.class)
public class TimeMachineSpringRestTemplateAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TimeMachineSpringRestTemplateAutoConfiguration.class);

    private final TimeMachineConfigurationProperties properties;

    public TimeMachineSpringRestTemplateAutoConfiguration(TimeMachineConfigurationProperties properties) {
        this.properties = properties;
    }


    @Bean
    @ConditionalOnProperty(prefix = "slalom.delorean.outboundRequestHeader", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SpringRestTemplateTimeMachineOutboundRequestInterceptor deLoreanSpringRestTemplateOutboundRequestInterceptor() {
        log.trace("Adding SpringRestTemplateOutboundRequestInterceptor to context");
        return new SpringRestTemplateTimeMachineOutboundRequestInterceptor(properties);
    }
}
