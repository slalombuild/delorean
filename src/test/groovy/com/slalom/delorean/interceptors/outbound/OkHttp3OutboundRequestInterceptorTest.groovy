package com.slalom.delorean.interceptors.outbound

import com.slalom.autoconfiguration.delorean.TimeMachineConfigurationProperties
import com.slalom.delorean.DateFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import spock.lang.Specification
import spock.lang.Unroll
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class OkHttp3OutboundRequestInterceptorTest extends Specification{

    private static final String HEADER_NAME = "X-Test-Header"
    private static final TEST_DATE_STRING = '2000-01-01'
    private static final TEST_DATE_TIME_STRING = '2010-01-01T13:30:30'
    private static final TEST_DATE = LocalDate.parse(TEST_DATE_STRING, DateTimeFormatter.ISO_LOCAL_DATE)
    private static final TEST_DATE_TIME = LocalDateTime.parse(TEST_DATE_TIME_STRING, DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    private TimeMachineConfigurationProperties properties

    private MockWebServer server
    private OkHttpClient client

    def setup() {
        properties = new TimeMachineConfigurationProperties()
        properties.enabled = true
        properties.outboundRequestHeader.enabled = true
        properties.outboundRequestHeader.name = HEADER_NAME

        server = new MockWebServer()
        server.start()
        server.enqueue(new MockResponse())

        client = new OkHttpClient().newBuilder().addInterceptor(new OkHttp3OutboundTimeMachineRequestInterceptor(properties)).build()
    }

    def cleanup() {
        server.shutdown()
        DateFactory.clearTestDate()
    }

    @Unroll
    def "When Time Machine test date is set to #testDate then Request includes header"() {
        given:
        DateFactory.setTestDate(testDate)

        when:
        client.newCall(new Request.Builder().url(server.url("/")).build()).execute()
        RecordedRequest request = server.takeRequest();

        then:
        request.getHeader(HEADER_NAME) != null
        request.getHeader(HEADER_NAME) == testDateString

        where:
        testDate | testDateString

        TEST_DATE | TEST_DATE_STRING
        TEST_DATE_TIME | TEST_DATE_TIME_STRING
    }

    def "When Time Machine test date is not set then Request does not include header"() {

        given:
        DateFactory.clearTestDate()

        when:
        client.newCall(new Request.Builder().url(server.url("/")).build()).execute()
        RecordedRequest request = server.takeRequest();

        then:
        request.getHeader(HEADER_NAME) == null
    }




}
