package com.slalom.delorean

import static com.slalom.delorean.DateFactory.*
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

import spock.lang.Specification
import java.time.*

class DateFactoryTest extends Specification {

    def "Test No-Op Case"() {
        when:
        def currentDate = currentDate()

        then:
        currentDate == LocalDate.now()
        timeTraveling() == false
    }

    def "Test LocalDate with date #setDate "() {

        given:
        setTestDate(testDate)

        expect:
        timeTraveling() == true
        fixedInstant() == false
        today() == testDate
        getTestDateString() == testDate.format(ISO_LOCAL_DATE)

        //As the time returned for a date-only test date should be the system clock time - ensure this is within 1 second of the system clock
        Math.abs(now().toLocalTime().toSecondOfDay() - LocalTime.now().toSecondOfDay()) <= 1

        clearTestDate()
        timeTraveling() == false



        where:

        testDate                       | _
        LocalDate.now().minusYears(10) | _
        LocalDate.now().plusYears(10)  | _
        LocalDate.now()                | _
    }

    def "Test LocalDateTime with date #setDate "() {

        given:
        setTestDate(testDateTime)

        expect:
        timeTraveling() == true
        fixedInstant() == true

        now() == testDateTime
        today() == testDateTime.toLocalDate()
        getTestDateString() == testDateTime.format(ISO_LOCAL_DATE_TIME)

        clearTestDate()
        timeTraveling() == false
        today() == LocalDateTime.now().toLocalDate()
        //Ugly - but ensuring that now() returns the same value as the the current system time... within 1000 milliseconds to handle any slight jitter
        1000 > now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - ZonedDateTime.now().toInstant().toEpochMilli()

        where:

        testDateTime                       | _
        LocalDateTime.now().minusYears(10) | _
        LocalDateTime.now().plusYears(10)  | _
        LocalDateTime.now()                | _
    }
}
