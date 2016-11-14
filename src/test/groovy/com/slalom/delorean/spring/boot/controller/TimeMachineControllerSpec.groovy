package com.slalom.delorean.spring.boot.controller

import com.slalom.autoconfiguration.delorean.TimeMachineConfigurationProperties
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup

class TimeMachineControllerSpec extends Specification {
    def tmController = new TimeMachineController(new TimeMachineConfigurationProperties())

    MockMvc mm = standaloneSetup(tmController).build()

    def setup() {
    }

    def "Test Successful LocalDate"() {
        when:
        def response = mm.perform(get('/time-machine/2011-01-21')).andReturn().response

        then:
        response.status == 200
    }

    def "Test Failing Date"() {
        when:
        def response = mm.perform(get('/time-machine/20110101')).andReturn().response

        then:
        response.status == 400
    }
}
