package com.slalom.delorean.interceptors.inbound;

import static com.slalom.delorean.DateFactory.clearTestDate;
import static com.slalom.delorean.DateFactory.setTestDate;

import com.slalom.autoconfiguration.delorean.TimeMachineConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

/**
 * Inbound Request Handler Interceptor to process Inbound Servlet and attempt to set the time travel date based on either a Cookie or HTTP Request Header value
 */
public class InboundRequestInterceptor extends HandlerInterceptorAdapter {

    private static final Logger log = LoggerFactory.getLogger(InboundRequestInterceptor.class);
    private static DateTimeFormatter[] formats = { DateTimeFormatter.ISO_LOCAL_DATE, DateTimeFormatter.ISO_LOCAL_DATE_TIME };

    private final TimeMachineConfigurationProperties properties;
    private final String inboundHeaderName;
    private final String cookieName;

    public InboundRequestInterceptor(final TimeMachineConfigurationProperties properties) {
        this.properties = properties;
        inboundHeaderName = properties.getHeaderName();
        cookieName = properties.getCookie().getName();
    }

    /**
     * Checks cookie and request for Time Machine header
     */
    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {

        if (!properties.isEnabled()) {
            return true;
        }

        String timeTravelDateString = null;

        //First determine if cookie support is enabled, and if so, attempt to retrieve time travel date from cookie
        if (properties.getCookie().isEnabled()) {
            timeTravelDateString = Optional.ofNullable(WebUtils.getCookie(request, cookieName)).map(Cookie::getValue).orElse(timeTravelDateString);
        }

        //If the HTTP Request header is populated it should override any potential cookie value
        timeTravelDateString = Optional.ofNullable(request.getHeader(inboundHeaderName)).orElse(timeTravelDateString);

        log.trace("Interceptor test date: {}", timeTravelDateString);

        if (timeTravelDateString != null && !"".equals(timeTravelDateString)) {
            parseAndSetTestDate(timeTravelDateString);
        }

        return true;
    }

    private void parseAndSetTestDate(final String timeTravelDateString) {
        for (final DateTimeFormatter f : formats) {
            try {
                //As there is no easy way to get a readable pattern from the DateTimeFormatters, in success case we'll use this pre-formatted one
                String pattern = "YYYY-MM-dd";
                TemporalAccessor t = f.parse(timeTravelDateString);

                if (f.equals(DateTimeFormatter.ISO_LOCAL_DATE)) {
                    setTestDate(LocalDate.from(t));
                } else {
                    pattern = "YYYY-MM-ddTHH:mm:ss";
                    setTestDate(LocalDateTime.from(t));
                }

                log.trace("Successfully parsed as pattern {} : {} ", pattern, t.toString());
            } catch (DateTimeParseException e) {
                log.info("Unable to parse date with pattern " + f.toString());
            }
        }
    }

    /**
     * Cleans up Time Machine, resetting the time travel date after request completion
     */
    @Override
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response, final Object handler, final Exception ex)
            throws Exception {
        clearTestDate();
    }

}
