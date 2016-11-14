package com.slalom.delorean.interceptors.outbound

import com.slalom.autoconfiguration.delorean.TimeMachineConfigurationProperties
import com.slalom.delorean.DateFactory
import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.message.BasicHttpRequest
import org.apache.http.protocol.HttpContext
import spock.lang.Specification
import spock.lang.Unroll
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Test the functionality of the
 */
class ApacheHttpClientOutboundRequestInterceptorTest extends Specification {

    private static final String HEADER_NAME = "X-Test-Header"
    private static final TEST_DATE_STRING = '2000-01-01'
    private static final TEST_DATE_TIME_STRING = '2010-01-01T13:30:30'
    private static final TEST_DATE = LocalDate.parse(TEST_DATE_STRING, DateTimeFormatter.ISO_LOCAL_DATE)
    private static final TEST_DATE_TIME = LocalDateTime.parse(TEST_DATE_TIME_STRING, DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    private TimeMachineConfigurationProperties properties
    private HttpContext context
    private HttpRequest request
    private HttpRequestInterceptor interceptor

    def setup() {
        properties = new TimeMachineConfigurationProperties()
        properties.enabled = true
        properties.outboundRequestHeader.enabled = true
        properties.outboundRequestHeader.name = HEADER_NAME

        context = HttpClientContext.create()
        request = new BasicHttpRequest("GET", "/")

        interceptor = new ApacheHttpClientOutboundTimeMachineRequestInterceptor(properties)
    }

    def cleanup() {
        DateFactory.clearTestDate()
    }

    @Unroll
    def "When Time Machine test date is set to #testDate then Request includes header"() {
        given:
        DateFactory.setTestDate(testDate)

        when:
        interceptor.process(request, context)

        then:
        request.getHeaders(HEADER_NAME).size() == 1
        request.getHeaders(HEADER_NAME)[0].value == testDateString

        where:
        testDate | testDateString
        TEST_DATE | TEST_DATE_STRING
        TEST_DATE_TIME | TEST_DATE_TIME_STRING
    }

    def "When Time Machine test date is not set then Request does not include header"() {

        given:
        DateFactory.clearTestDate()

        when:
        interceptor.process(request, context)
        print request.getHeaders(HEADER_NAME)

        then:
        request.getHeaders(HEADER_NAME).size() == 0
    }
}
