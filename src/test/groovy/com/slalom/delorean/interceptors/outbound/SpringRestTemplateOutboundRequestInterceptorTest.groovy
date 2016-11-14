package com.slalom.delorean.interceptors.outbound

import static java.time.LocalDate.parse
import static org.springframework.test.util.AssertionErrors.assertTrue
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

import com.slalom.autoconfiguration.delorean.TimeMachineConfigurationProperties
import com.slalom.delorean.DateFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.client.ClientHttpRequest
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import java.time.format.DateTimeFormatter

class SpringRestTemplateOutboundRequestInterceptorTest extends Specification {

    private static final String HEADER_NAME = "X-Test-Header"
    private static final TEST_DATE_STRING = '2000-01-01'
    private static final TEST_DATE = parse(TEST_DATE_STRING, DateTimeFormatter.ISO_LOCAL_DATE)


    private RestTemplate template
    private TimeMachineConfigurationProperties properties
    private MockRestServiceServer server

    def setup() {
        properties = new TimeMachineConfigurationProperties()
        properties.enabled = true
        properties.outboundRequestHeader.enabled = true
        properties.outboundRequestHeader.name = HEADER_NAME

        RestTemplateBuilder builder = new RestTemplateBuilder()
        template = builder.additionalInterceptors(new SpringRestTemplateTimeMachineOutboundRequestInterceptor(properties)).build()

        server = MockRestServiceServer.createServer(template)
    }

    def cleanup() {
        DateFactory.clearTestDate()
    }


    def "When Time Machine test date is set then Request includes header"() {
        given:
        server.expect(requestTo("/test")).andExpect(header(HEADER_NAME, TEST_DATE_STRING)).andRespond(withSuccess())

        when:
        DateFactory.setTestDate(TEST_DATE)
        template.getForObject("/test", String.class)

        then:
        server.verify()
    }

    def "When Time Machine test date is not set then Request does not include header"() {

        given:
        server.expect(requestTo("/test")).andExpect({
            ClientHttpRequest request -> assertTrue("Header should not be set, but found: " + HEADER_NAME, request.headers.get(HEADER_NAME) == null)
        }).andRespond(withSuccess())

        when:
        template.getForObject("/test", String.class)

        then:
        server.verify()
    }
}
