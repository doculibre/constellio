package com.constellio.app.ui.util;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

public class DateFormatUtils implements Serializable {

	public static final String DATE_FORMAT = "yyyy-MM-dd";

	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static String formatDate(Date date) {
		return date != null ? new SimpleDateFormat(DATE_FORMAT).format(date) : null;
	}

	public static String formatDateTime(Date dateTime) {
		return dateTime != null ? new SimpleDateFormat(DATE_TIME_FORMAT).format(dateTime) : null;
	}

	public static String format(LocalDate date) {
		return date != null ? date.toString(DATE_FORMAT) : null;
	}

	public static String format(LocalDateTime dateTime) {
		return dateTime != null ? dateTime.toString(DATE_TIME_FORMAT) : null;
	}

}
