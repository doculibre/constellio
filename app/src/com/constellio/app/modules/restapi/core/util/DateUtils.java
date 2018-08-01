package com.constellio.app.modules.restapi.core.util;

import com.constellio.app.modules.restapi.core.exception.InvalidDateFormatException;
import lombok.experimental.UtilityClass;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;

@UtilityClass
public final class DateUtils {

	public static void validateLocalDate(String date, String pattern) {
		try {
			DateTimeFormat.forPattern(pattern).parseLocalDate(date);
		} catch (IllegalArgumentException e) {
			throw new InvalidDateFormatException(date, pattern);
		}
	}

	public static void validateLocalDateTime(String date, String pattern) {
		try {
			DateTimeFormat.forPattern(pattern).parseLocalDateTime(date);
		} catch (IllegalArgumentException e) {
			throw new InvalidDateFormatException(date, pattern);
		}
	}

	public static LocalDateTime parseIsoNoMillis(String dateIsoFormat) {
		return LocalDateTime.parse(dateIsoFormat, ISODateTimeFormat.basicDateTimeNoMillis());
	}

	public static LocalDate parseLocalDate(String date, String pattern) {
		return DateTimeFormat.forPattern(pattern).parseLocalDate(date);
	}

	public static LocalDateTime parseLocalDateTime(String date, String pattern) {
		return DateTimeFormat.forPattern(pattern).parseLocalDateTime(date);
	}

	public static String formatIsoNoMillis(LocalDate localDate) {
		if (localDate == null) {
			return null;
		}
		return formatIsoNoMillis(localDate.toDateTimeAtStartOfDay(DateTimeZone.getDefault()));
	}

	public static String formatIsoNoMillis(LocalDateTime localDateTime) {
		if (localDateTime == null) {
			return null;
		}
		return formatIsoNoMillis(localDateTime.toDateTime(DateTimeZone.getDefault()));
	}

	public static String formatIsoNoMillis(DateTime dateTime) {
		if (dateTime == null) {
			return null;
		}
		return ISODateTimeFormat.basicDateTimeNoMillis().print(dateTime);
	}

	public static String format(LocalDate date, String pattern) {
		return date.toString(pattern);
	}

	public static String format(LocalDateTime date, String pattern) {
		return date.toString(pattern);
	}

}
