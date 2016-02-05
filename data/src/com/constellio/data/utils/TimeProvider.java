package com.constellio.data.utils;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

public abstract class TimeProvider {

	private static TimeProvider timeProvider = new DefaultTimeProvider();

	public static void setTimeProvider(TimeProvider timeProvider) {
		TimeProvider.timeProvider = timeProvider;
	}

	public static LocalDateTime getLocalDateTime() {
		return timeProvider.getTimeProviderLocalDateTime();
	}

	public static LocalDate getLocalDate() {
		return timeProvider.getTimeProviderLocalDate();
	}

	public abstract LocalDateTime getTimeProviderLocalDateTime();

	public abstract LocalDate getTimeProviderLocalDate();

	public static class DefaultTimeProvider extends TimeProvider {

		@Override
		public LocalDateTime getTimeProviderLocalDateTime() {
			return new LocalDateTime();
		}

		@Override
		public LocalDate getTimeProviderLocalDate() {
			return new LocalDate();
		}
	}
}
