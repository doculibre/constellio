package com.constellio.app.modules.restapi.core.util;

import com.constellio.app.modules.restapi.core.exception.InvalidDateFormatException;
import org.joda.time.*;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DateUtilsTest {

	@Test
	public void testFormatIsoNoMillisWithLocalDate() {
		LocalDate date = new LocalDate(2017, 1, 1);

		String result = DateUtils.formatIsoNoMillis(date);
		assertThat(result).isEqualTo("20170101T000000" + getTimezoneValue(date));
	}

	@Test
	public void testFormatIsoNoMillisWithLocalDateTime() {
		LocalDateTime date = new LocalDateTime(2017, 12, 1, 2, 11, 34, 123);
		String result = DateUtils.formatIsoNoMillis(date);

		assertThat(result).isEqualTo("20171201T021134" + getTimezoneValue(date));
	}

	@Test
	public void testFormatIsoNoMillisWithDateTime() {
		DateTime date = new DateTime(2017, 2, 12, 23, 59, 1, 666);
		String result = DateUtils.formatIsoNoMillis(date);

		assertThat(result).isEqualTo("20170212T235901" + getTimezoneValue(date));
	}

	@Test
	public void testIsValidLocalDate() {
		DateUtils.validateLocalDate("1970-02-23", "yyyy-MM-dd");
	}

	@Test(expected = InvalidDateFormatException.class)
	public void testIsValidLocalDateWithTime() {
		DateUtils.validateLocalDate("1970-10-10T00:00:00.000", "yyyyy-MM-dd");
	}

	@Test
	public void testIsValidLocalDateTime() {
		DateUtils.validateLocalDateTime("1970-10-10T00:00:00.000", "yyyy-MM-dd'T'HH:mm:ss.SSS");
	}

	@Test(expected = InvalidDateFormatException.class)
	public void testIsValidLocalDateTimeNoMillis() {
		DateUtils.validateLocalDate("1970-10-10T00:00:00", "yyyy-MM-dd'T'HH:mm:ss.SSS");
	}

	@Test(expected = InvalidDateFormatException.class)
	public void testIsValidLocalDateTimeNoTime() {
		DateUtils.validateLocalDate("1970-10-10", "yyyy-MM-dd'T'HH:mm:ss.SSS");
	}

	@Test(expected = InvalidDateFormatException.class)
	public void testIsValidLocalDateTimeWithOffset() {
		DateUtils.validateLocalDate("1970-10-10T00:00:00.000-0400", "yyyy-MM-dd'T'HH:mm:ss.SSS");
	}

	private String getTimezoneValue(LocalDate localDate) {
		return getTimezoneValue(localDate.toLocalDateTime(LocalTime.MIDNIGHT));
	}

	private String getTimezoneValue(LocalDateTime localDateTime) {
		return getTimezoneValue(localDateTime.toDateTime(DateTimeZone.getDefault()));
	}

	private String getTimezoneValue(DateTime dateTime) {
		Instant instant = dateTime.toInstant();
		return DateTimeZone.getDefault().getName(instant.getMillis()).replace(":", "");
	}

}
