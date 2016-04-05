package com.constellio.app.ui.util;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;

public class DateFormatUtils implements Serializable {
	public static String format(Date date) {
		return date != null ? new SimpleDateFormat(getDateFormat()).format(date) : null;
	}

	public static String format(LocalDate date) {
		return date != null ? date.toString(getDateFormat()) : null;
	}

	public static String format(DateTime dateTime) {
		return dateTime != null ? new SimpleDateFormat(getDateTimeFormat()).format(dateTime) : null;
	}

	public static String format(LocalDateTime dateTime) {
		return dateTime != null ? dateTime.toString(getDateTimeFormat()) : null;
	}

	public static String getDateFormat() {
		return configs().getDateFormat();
	}

	public static String getDateTimeFormat() {
		return configs().getDateTimeFormat();
	}

	private static ConstellioEIMConfigs configs() {
		return ConstellioFactories.getInstance().getModelLayerFactory().getSystemConfigs();
	}
}
