package com.slalom.autoconfiguration.delorean;

import com.slalom.delorean.interceptors.outbound.OkHttp3OutboundTimeMachineRequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TimeMachineConfigurationProperties.class)
@ConditionalOnProperty(prefix = "slalom.delorean", name = "enabled", havingValue = "true")
@ConditionalOnClass(okhttp3.Interceptor.class)
public class TimeMachineOkHttp3AutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TimeMachineOkHttp3AutoConfiguration.class);

    private final TimeMachineConfigurationProperties properties;

    public TimeMachineOkHttp3AutoConfiguration(@Autowired TimeMachineConfigurationProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "slalom.delorean.outboundRequestHeader", name = "enabled", havingValue = "true", matchIfMissing = true)
    public OkHttp3OutboundTimeMachineRequestInterceptor deLoreanOkHttp3OutboundRequestInterceptor() {
        log.trace("Adding OkHttp3OutboundRequestInterceptor to context");
        return new OkHttp3OutboundTimeMachineRequestInterceptor(properties);
    }

}
