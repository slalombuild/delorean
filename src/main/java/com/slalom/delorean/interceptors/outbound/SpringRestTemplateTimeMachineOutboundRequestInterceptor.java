package com.slalom.delorean.interceptors.outbound;

import com.slalom.autoconfiguration.delorean.TimeMachineConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import java.io.IOException;

public class SpringRestTemplateTimeMachineOutboundRequestInterceptor extends AbstractOutboundTimeMachineRequestInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger log = LoggerFactory.getLogger(SpringRestTemplateTimeMachineOutboundRequestInterceptor.class);

    public SpringRestTemplateTimeMachineOutboundRequestInterceptor(final TimeMachineConfigurationProperties properties) {
        super(properties);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        addHeader(headerDate -> {
            HttpHeaders headers = request.getHeaders();
            headers.add(outboundHeaderName, headerDate);
        });

        return execution.execute(request, body);
    }
}
