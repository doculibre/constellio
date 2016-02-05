package com.constellio.model.conf.ldap;

import org.joda.time.Duration;

public class TooShortDurationRuntimeException extends RuntimeException {
    public TooShortDurationRuntimeException(Duration durationBetweenExecution) {
        super("Duration too short " + durationBetweenExecution.getStandardSeconds() + " seconds");
    }
}
