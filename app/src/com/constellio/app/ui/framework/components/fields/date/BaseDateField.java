package com.constellio.app.ui.framework.components.fields.date;

import com.constellio.app.ui.util.DateFormatUtils;
import com.google.common.base.CharMatcher;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.DateField;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import java.util.Date;
import java.util.Map;

public class BaseDateField extends DateField {

	private static final String DATE_WITHOUT_SEPARATOR_PATTERN = "\\d{8}";

	private static final String SEPARATOR_PATTERN = "[ ./-]";

	public BaseDateField() {
		super();
		init();
	}

	public BaseDateField(Property<?> dataSource)
			throws IllegalArgumentException {
		super(dataSource);
		init();
	}

	public BaseDateField(String caption, Date value) {
		super(caption, value);
		init();
	}

	public BaseDateField(String caption, Property<?> dataSource) {
		super(caption, dataSource);
		init();
	}

	public BaseDateField(String caption) {
		super(caption);
		init();
	}

	private void init() {
		setDateFormat(DateFormatUtils.getDateFormat());
	}

	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		Integer year = (Integer) variables.get("year");
		if (year != null && ("" + year).length() != 4) {
			variables.remove("year");
			variables.remove("month");
			variables.remove("day");
			variables.put("dateString", "[INVALID]");
		}
		super.changeVariables(source, variables);
	}

	@Override
	protected Date handleUnparsableDateString(String dateString)
			throws ConversionException {
		return handleUnparsableDateString(dateString, DateFormatUtils.getDateFormat());
	}

	static Date handleUnparsableDateString(final String dateString, final String dateFormat) {
		final CharMatcher separatorCharMatcher = CharMatcher.anyOf(SEPARATOR_PATTERN).precomputed();
		if (dateString.matches(DATE_WITHOUT_SEPARATOR_PATTERN)) {
			final String dateFormatWithoutSeparator = separatorCharMatcher.removeFrom(dateFormat);
			return parseBidirectionallyDateString(dateString, dateFormatWithoutSeparator);
		} else {
			final String dateFormatSeparator = String.valueOf(separatorCharMatcher.retainFrom(dateFormat).charAt(1));
			final String dateStringWithDateFormatSeparator = dateString.replaceAll(SEPARATOR_PATTERN, dateFormatSeparator);
			return parseBidirectionallyDateString(dateStringWithDateFormatSeparator, dateFormat);
		}
	}

	private static Date parseBidirectionallyDateString(final String dateString, final String dateFormat) {
		try {
			return LocalDate.parse(dateString, DateTimeFormat.forPattern(dateFormat)).toDate();
		} catch (final IllegalArgumentException e1) {
			try {
				final String reversedDateFormat = new StringBuilder(dateFormat).reverse().toString();
				return LocalDate.parse(dateString, DateTimeFormat.forPattern(reversedDateFormat)).toDate();
			} catch (final IllegalArgumentException e2) {
				throw new ConversionException(e2.getLocalizedMessage());
			}
		}
	}
}
