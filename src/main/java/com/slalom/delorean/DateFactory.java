package com.slalom.delorean;


import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Provides a mechanism for setting a date/time for testing purposes
 * <p>
 * In order to work successfully all "current" dates in the system must be sourced from this DateFactory.
 * <p>
 * The value set is saved in ThreadLocal storage so all calls made on the same thread will get the same date/time. If no test date is explicitly set,
 * then the default system date/time is used
 */
public class DateFactory {

    private static ThreadLocal<Clock> timeMachineClock = new InheritableThreadLocal<>();
    private static final ZoneId defaultZone = ZoneId.systemDefault();

    private DateFactory() {

    }

    /**
     * Sets the test date to a fixed date
     *
     * @param testingDate The fixed date to be used
     */
    public static void setTestDate(final LocalDate testingDate) {
        timeMachineClock.set(new FixedDateClock(testingDate));
    }

    /**
     * Sets the test date to a fixed instant in time
     *
     * @param testingDateTime The fixed date/time to be used
     */
    public static void setTestDate(final LocalDateTime testingDateTime) {
        timeMachineClock.set(Clock.fixed(testingDateTime.atZone(defaultZone).toInstant(), defaultZone));
    }


    /**
     * Sets the test date based on a {@link Clock}
     *
     * @param clock
     */
    public static void setTestClock(final Clock clock) {
        timeMachineClock.set(clock);
    }

    /**
     * Clears any configured test date - setting it back to the default system date
     */
    public static void clearTestDate() {
        timeMachineClock.remove();
    }

    /**
     * Returns the current set testing timeMachineClock
     *
     * @return The current testing Clock or null if none set
     */
    public static Clock getTestingClock() {
        return timeMachineClock.get();
    }

    /**
     * Returns whether time travel is in effect
     *
     * @return True if time travel is currently enabled, else false
     */
    public static boolean timeTraveling() {
        return timeMachineClock.get() != null;
    }


    /**
     * Returns whether the test date is set to a fixed instant
     *
     * @return True if time travel is enabled and set to a fixed instant, else false
     */
    public static boolean fixedInstant() {
        if (!timeTraveling()) {
            return false;
        }

        //Since we cannot access the package private Clock.FixedClock we need to check by name
        String clockType = clock().getClass().getCanonicalName();
        return "java.Time.Clock.FixedClock".equalsIgnoreCase(clockType) ? true : false;
    }

    /**
     * Returns the currently set test date
     *
     * @return If a test date/time is set this will return the date portion of the test date, else will return the result of {@link LocalDate#now()}
     */
    public static LocalDate today() {
        return LocalDate.now(clock());
    }

    /**
     * Returns the currently set test date/time  DateTime based on the test timeMachineClock (if set) or else current system timeMachineClock set to the
     *
     * @return If a date/time is set this will return the that value, if only a test date is set this will return that date along with the time portion of
     * the system's default clock, if no test date is set this will return the result of {@link LocalDateTime#now()}
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(clock());
    }

    /**
     * Synonym for {@link #today()}
     */
    public static LocalDate currentDate() {
        return today();
    }

    /**
     * Synonym for {@link #now()}
     */
    public static LocalDateTime currentDateTime() {
        return now();
    }

    /**
     * Returns an ISO Formatted String representation of the configured date/time or null if none is configured.
     *
     * @return If the time travel date/time is set to a fixed date, the string will contain only a date (e.g. 2016-01-01). If set to a fixed instant the
     * string will contain a full datetime (e.g. 2016-01-01T16:30:30)
     */
    public static String getTestDateString() {
        if (!timeTraveling()) {
            return null;
        }

        if (fixedInstant()) {
            return currentDateTimeString();
        }

        return currentDateString();
    }

    /**
     * Convenience method for getting current test date as an ISO formatted String e.g. 2016-01-01
     *
     * @return ISO Formatted date portion of the test date or current system clock date if test date not set
     */
    public static String currentDateString() {
        return currentDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Convenience method for getting current test date time as an ISO formatted String e.g. 2016-01-01T00:00:00
     *
     * @return ISO Formatted date/time of the test date or current system clock datetime if test date not set
     */
    public static String currentDateTimeString() {
        return currentDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }


    /**
     * Returns a ZonedDateTime for the provided timezone based on the test timeMachineClock (if set) or else the current time in the provided timezone.
     * <p>
     * For instance if the actual wall-clock time was 12:30pm UTC on 2017-01-01 and the test timeMachineClock is set to 2016-10-21 and provided Zone is
     * "UTC/Greenwich" this will return a ZoneDateTime for 2016-10-21 12:30:00
     *
     * @param zone The timezone used to obtain the time portion of the resulting ZonedDateTime
     * @return
     */
    public static ZonedDateTime currentDateTimeAtZone(ZoneId zone) {
        return ZonedDateTime.now(clock().withZone(zone));
    }


    /**
     * Implementation of a clock that returns an instant with a fixed date portion, but with the time set to the current time in
     * the provided (defaulting to system) timezone. This clock is not meant for distributed or public use so does not implement serializable as recommended
     * in the {@link Clock} documentation.
     */
    private static final class FixedDateClock extends Clock  {
        private final LocalDate date;
        private final ZoneId zone;

        FixedDateClock(LocalDate date, ZoneId zone) {
            this.zone = zone;
            this.date = date;
        }

        FixedDateClock(LocalDate date) {
            this.zone = ZoneId.systemDefault();
            this.date = date;
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            if (zone.equals(this.zone)) {  // intentional NPE
                return this;
            }
            return new FixedDateClock(this.date, zone);
        }

        @Override
        public long millis() {
            return date.atTime(LocalTime.now(zone)).atZone(zone).toInstant().toEpochMilli();
        }

        @Override
        public Instant instant() {
            return date.atTime(LocalTime.now(zone)).atZone(zone).toInstant();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof FixedDateClock) {
                FixedDateClock other = (FixedDateClock) obj;
                return date.equals(other.date) && zone.equals(other.zone);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return date.hashCode() ^ zone.hashCode();
        }

        @Override
        public String toString() {
            return "FixedDateClock[" + date + "," + zone + "]";
        }
    }


    private static Clock clock() {
        return timeMachineClock.get() == null ? Clock.systemDefaultZone() : timeMachineClock.get();
    }
}
