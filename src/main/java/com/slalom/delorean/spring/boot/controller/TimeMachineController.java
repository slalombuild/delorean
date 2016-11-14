package com.slalom.delorean.spring.boot.controller;

import static com.slalom.delorean.DateFactory.getTestDateString;
import static com.slalom.delorean.DateFactory.timeTraveling;

import com.slalom.autoconfiguration.delorean.TimeMachineConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;

@RestController
@EnableConfigurationProperties(TimeMachineConfigurationProperties.class)
@RequestMapping(path = "${slalom.delorean.path:time-machine}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class TimeMachineController {

    private static final Logger log = LoggerFactory.getLogger(TimeMachineController.class);
    private static DateTimeFormatter[] formats = { DateTimeFormatter.ISO_LOCAL_DATE, DateTimeFormatter.ISO_LOCAL_DATE_TIME };
    private static final String TEST_DATE_JSON = "{ \"testDate\" : \"%s\" }";

    private final TimeMachineConfigurationProperties properties;

    public TimeMachineController(@Autowired TimeMachineConfigurationProperties properties) {
        this.properties = properties;
    }


    /**
     * Return the testing date/time if set, else empty response
     *
     * @return
     */
    @GetMapping
    public ResponseEntity<String> getCurrentTestingDate() {
        String testingDateJson = timeTraveling() ? String.format(TEST_DATE_JSON, getTestDateString()) : "{}";
        return ResponseEntity.ok(testingDateJson);
    }

    /**
     * Remove any previously set time machine cookie
     */
    @GetMapping(path = "/clear")
    public ResponseEntity<Void> clearCurrentTestingDateGet(HttpServletResponse response) {
        return clearCurrentTestingDate(response);
    }


    /**
     * Remove any previously set time machine cookie
     */
    @DeleteMapping
    public ResponseEntity<Void> clearCurrentTestingDate(HttpServletResponse response) {
        log.debug("Removing time machine cookie");
        response.addCookie(getTimeMachineCookie(null, true));
        return ResponseEntity.noContent().build();
    }


    /**
     * Set a cookie with the provided test date/time
     */
    @GetMapping(path = "/{isoDate}")
    public ResponseEntity<String> setTestingDate(@PathVariable("isoDate") final String isoDate, final HttpServletResponse response) {

        log.trace("Attempting to set test date to: {}", isoDate);

        //Ensure that the date is properly formatted by parsing it
        for (final DateTimeFormatter f : formats) {
            try {
                TemporalAccessor t = f.parse(isoDate);

                //As there is no easy way to get a readable pattern from the DateTimeFormatters, in success case we'll use this pre-formatted one
                String pattern = "YYYY-MM-dd";
                if (f == DateTimeFormatter.ISO_LOCAL_DATE_TIME) {
                    pattern = "YYYY-MM-ddTHH:mm:ss";
                }

                response.addCookie(getTimeMachineCookie(isoDate, false));
                log.info("Successfully parsed as pattern {} : {} ", pattern, t.toString());

                return ResponseEntity.ok(String.format(TEST_DATE_JSON, isoDate));
            } catch (DateTimeParseException e) {
                log.trace("Unable to parse date with pattern " + f.toString());
            }
        }

        return ResponseEntity.badRequest().body(String.format("Unable to parse date %s for time travel.", isoDate));
    }

    private Cookie getTimeMachineCookie(String date, boolean expired) {
        Cookie cookie = new Cookie(properties.getCookie().getName(), date);
        cookie.setPath("/");
        if (expired) {
            cookie.setMaxAge(0);
        } else {
            cookie.setMaxAge(-1);
        }
        return cookie;
    }
}
