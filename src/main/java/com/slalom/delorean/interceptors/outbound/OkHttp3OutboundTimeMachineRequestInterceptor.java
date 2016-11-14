package com.slalom.delorean.interceptors.outbound;

import com.slalom.autoconfiguration.delorean.TimeMachineConfigurationProperties;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class OkHttp3OutboundTimeMachineRequestInterceptor extends AbstractOutboundTimeMachineRequestInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(OkHttp3OutboundTimeMachineRequestInterceptor.class);

    public OkHttp3OutboundTimeMachineRequestInterceptor(final TimeMachineConfigurationProperties properties) {
        super(properties);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    public Response intercept(final Chain chain) throws IOException {
        final Request request = chain.request();

        Request updatedRequest = addHeader((String headerDate) -> request.newBuilder().addHeader(outboundHeaderName, headerDate).build()).orElse(request);
        return chain.proceed(updatedRequest);
    }
}
