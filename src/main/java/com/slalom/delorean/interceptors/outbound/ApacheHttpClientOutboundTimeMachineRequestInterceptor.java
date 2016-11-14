package com.slalom.delorean.interceptors.outbound;

import com.slalom.autoconfiguration.delorean.TimeMachineConfigurationProperties;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApacheHttpClientOutboundTimeMachineRequestInterceptor extends AbstractOutboundTimeMachineRequestInterceptor implements HttpRequestInterceptor {
    private static final Logger log = LoggerFactory.getLogger(ApacheHttpClientOutboundTimeMachineRequestInterceptor.class);

    public ApacheHttpClientOutboundTimeMachineRequestInterceptor(final TimeMachineConfigurationProperties properties) {
        super(properties);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(final HttpRequest request, final HttpContext context) {

        addHeader((String headerDate) -> request.setHeader(outboundHeaderName, headerDate));
    }
}
