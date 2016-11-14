package com.slalom.delorean.interceptors.inbound

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

import com.slalom.autoconfiguration.delorean.TimeMachineConfigurationProperties
import com.slalom.delorean.DateFactory
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Specification
import spock.lang.Unroll
import javax.servlet.http.Cookie
import java.time.LocalDate

class InboundRequestInterceptorTest extends Specification {

    private TimeMachineConfigurationProperties properties
    private InboundRequestInterceptor interceptor

    private static final String COOKIE_NAME = "TestCookie"
    private static final String HEADER_NAME = "X-Test-Header"
    private static final String PAST_DATE_STR = '2000-01-01'
    private static final LocalDate PAST_DATE = LocalDate.parse(PAST_DATE_STR, ISO_LOCAL_DATE)
    private static final String FUTURE_DATE_STR = '2040-01-01'
    private static final LocalDate FUTURE_DATE = LocalDate.parse(FUTURE_DATE_STR, ISO_LOCAL_DATE)

    def setup() {
        properties = new TimeMachineConfigurationProperties()
        properties.enabled = true
        properties.cookie.enabled = true
        properties.cookie.name = COOKIE_NAME
        properties.headerName = HEADER_NAME
    }

    def cleanup() {
        DateFactory.clearTestDate()
    }

    @Unroll
    def "When Time Traveling via Cookie is #cookieEnabled and Cookie value is #testDateString - #result"() {

        given:
        properties.cookie.enabled = cookieEnabled
        interceptor = new InboundRequestInterceptor(properties)

        Cookie cookie = new Cookie(COOKIE_NAME, testDateString)
        cookie.maxAge = -1
        def request = get("/").cookie(cookie).buildRequest(null)

        when:
        interceptor.preHandle(request, new MockHttpServletResponse(), null)

        then:

        DateFactory.timeTraveling() == isTestDateSet
        DateFactory.currentDate() == expectedTestDate

        where:
        cookieEnabled | testDateString  || isTestDateSet | expectedTestDate | result
        true          | PAST_DATE_STR   || true          | PAST_DATE        | 'Test timeMachineClock is set from cookie'
        true          | FUTURE_DATE_STR || true          | FUTURE_DATE      | 'Test timeMachineClock is set from cookie'
        true          | null            || false         | LocalDate.now()  | 'No test timeMachineClock is set'
        false         | PAST_DATE_STR   || false         | LocalDate.now()  | 'No test timeMachineClock is set'
        false         | FUTURE_DATE_STR || false         | LocalDate.now()  | 'No test timeMachineClock is set'
    }

    @Unroll
    def "When Time Traveling via Header is enabled and Header value is #testDate - #result"() {
        given:
        interceptor = new InboundRequestInterceptor(properties)

        def requestBuilder = get("/")
        if (testDate) {
            requestBuilder.header(HEADER_NAME, testDate)
        }
        def request = requestBuilder.buildRequest(null)

        when:
        interceptor.preHandle(request, new MockHttpServletResponse(), null)

        then:

        DateFactory.timeTraveling() == isTestDateSet
        DateFactory.currentDate() == expectedTestDate

        where:
        testDate    || isTestDateSet | expectedTestDate | result
        PAST_DATE   || true          | PAST_DATE        | 'Test timeMachineClock is set from header'
        FUTURE_DATE || true          | FUTURE_DATE      | 'Test timeMachineClock is set from header'
        null        || false         | LocalDate.now()  | 'No test timeMachineClock is set'
    }

    @Unroll
    def "When Time Traveling with both Header and Cookie enabled and Header value is #headerDate and Cookie value is #cookieDateStr - #result"() {
        given:
        properties.cookie.enabled = true
        interceptor = new InboundRequestInterceptor(properties)

        Cookie cookie = new Cookie(COOKIE_NAME, cookieDateStr)
        cookie.maxAge = -1
        def builder = get("/").cookie(cookie)
        if (headerDate != null) {
            builder = builder.header(HEADER_NAME, headerDate)
        }

        def request = builder.buildRequest(null)

        when:
        interceptor.preHandle(request, new MockHttpServletResponse(), null)

        then:

        DateFactory.timeTraveling() == isTestDateSet
        DateFactory.currentDate() == expectedTestDate

        where:
        cookieDateStr   | headerDate      || isTestDateSet | expectedTestDate | result
        FUTURE_DATE_STR | PAST_DATE       || true          | PAST_DATE        | 'Header overrides cookie'
        PAST_DATE_STR   | FUTURE_DATE_STR || true          | FUTURE_DATE      | 'Header overrides cookie'
    }

    @Unroll
    "When Time Traveling disabled and Header value is #headerDate and Cookie value is #cookieDateStr - No test date is set"() {
        given:
        properties.enabled = false
        interceptor = new InboundRequestInterceptor(properties)

        def builder = get("/")
        if (cookieDateStr != null) {
            Cookie cookie = new Cookie(COOKIE_NAME, cookieDateStr)
            cookie.maxAge = -1
            builder.cookie(cookie)
        }
        if (headerDate != null) {
            builder = builder.header(HEADER_NAME, headerDate)
        }

        def request = builder.buildRequest(null)

        when:
        interceptor.preHandle(request, new MockHttpServletResponse(), null)

        then:

        DateFactory.timeTraveling() == false;
        DateFactory.currentDate() == LocalDate.now()

        where:
        cookieDateStr   | headerDate
        FUTURE_DATE_STR | PAST_DATE
        PAST_DATE_STR   | FUTURE_DATE_STR
        PAST_DATE_STR   | null
        null            | FUTURE_DATE_STR
        null            | null
    }

    def "Test Date is cleared after Interceptor completes"() {
        given:
        interceptor = new InboundRequestInterceptor(properties)

        when:
        DateFactory.setTestDate(PAST_DATE)

        then:
        DateFactory.currentDate() == PAST_DATE
        DateFactory.timeTraveling() == true;

        interceptor.afterCompletion(null, null, null, null)

        DateFactory.timeTraveling() == false
        DateFactory.currentDate() == LocalDate.now()
    }

}
