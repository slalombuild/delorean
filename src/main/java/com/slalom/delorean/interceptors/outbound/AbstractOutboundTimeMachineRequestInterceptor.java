package com.slalom.delorean.interceptors.outbound;

import com.slalom.autoconfiguration.delorean.TimeMachineConfigurationProperties;
import com.slalom.delorean.DateFactory;
import org.slf4j.Logger;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Base functionality for implementing an Outbound Request interceptor. Implementors may use the addHeader(...) methods to inject headers for whatever client
 * library the functionality is being implemented for.
 */
public abstract class AbstractOutboundTimeMachineRequestInterceptor {

    protected final TimeMachineConfigurationProperties properties;
    protected final String outboundHeaderName;

    public AbstractOutboundTimeMachineRequestInterceptor(final TimeMachineConfigurationProperties properties) {
        this.properties = properties;
        this.outboundHeaderName = properties.getOutboundRequestHeader().getName();
    }

    /**
     * Abstract method to ensure that logging is done using the subclasses' logger
     *
     * @return A logger instance to be used to log any messages to
     */
    protected abstract Logger getLogger();


    /**
     * Derive the appropriate header date
     *
     * @return Returns an optional containing the configured TimeTravel date/time header value if it is set and Outbound Request Header functionality is
     * enabled - else returns an empty optional
     */
    protected Optional<String> getHeaderDate() {

        if (properties.getOutboundRequestHeader().isEnabled() && DateFactory.timeTraveling()) {
            return Optional.of(DateFactory.getTestDateString());
        }
        return Optional.empty();
    }

    /**
     * Method for accepting a lambda for header injection that does not need to return a result
     *
     * @param consumer A lambda that consumes the headerDate as a String and injects it into the Http Request header
     */
    protected void addHeader(Consumer<String> consumer) {
        Objects.requireNonNull(consumer);

        getHeaderDate().ifPresent(headerDate -> {
            log();
            consumer.accept(headerDate);
        });
    }

    /**
     * Method for accepting a lambda for header injection that must return a result. (For instance for OkHttp3Client)
     *
     * @param mapper A lambda that consumes the headerDate as a String and injects it into the Http Request headermapper
     * @param <U>    The return type of the lambda (useful for libraries using fluent interfaces)
     * @return The result of applying the lambda with the headerDate
     */
    protected <U> Optional<U> addHeader(Function<String, U> mapper) {
        Objects.requireNonNull(mapper);

        return getHeaderDate().map(headerDate -> {
            log();
            return Optional.ofNullable(mapper.apply(headerDate));
        }).orElse(Optional.empty());
    }

    protected void log() {
        getLogger().debug("Adding Time Machine header to outbound request. {} = {}", outboundHeaderName, getHeaderDate().get());
    }
}
