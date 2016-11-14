package com.slalom.autoconfiguration.delorean;

import com.slalom.delorean.interceptors.outbound.ApacheHttpClientOutboundTimeMachineRequestInterceptor;
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
@ConditionalOnClass(org.apache.http.client.HttpClient.class)
public class TimeMachineApacheHttpClientAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TimeMachineApacheHttpClientAutoConfiguration.class);

    private final TimeMachineConfigurationProperties properties;

    public TimeMachineApacheHttpClientAutoConfiguration(TimeMachineConfigurationProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "slalom.delorean.outboundRequestHeader", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ApacheHttpClientOutboundTimeMachineRequestInterceptor deLoreanApacheHttpClientRequestInterceptor() {
        log.trace("Adding ApacheHttpClientOutboundRequestInterceptor to context");
        return new ApacheHttpClientOutboundTimeMachineRequestInterceptor(properties);
    }

}
