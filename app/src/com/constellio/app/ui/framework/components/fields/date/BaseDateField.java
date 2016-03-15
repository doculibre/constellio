package com.constellio.app.ui.framework.components.fields.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.constellio.app.ui.util.DateFormatUtils;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.DateField;

public class BaseDateField extends DateField {
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
		setDateFormat(DateFormatUtils.DATE_FORMAT);
	}

	@Override
	protected Date handleUnparsableDateString(String dateString)
			throws ConversionException {
		String pattern;
		if (dateString.matches("\\d{8}")) {
			pattern = "ddMMyyyy";
		} else if (dateString.matches("\\d{1,2}-\\d{1,2}-\\d{4}")) {
			pattern = "dd-MM-yyyy";
		} else if (dateString.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
			pattern = "dd/MM/yyyy";
		} else if (dateString.matches("\\d{4}/\\d{1,2}/\\d{1,2}")) {
			pattern = "yyyy/dd/MM";
		} else {
			return super.handleUnparsableDateString(dateString);
		}
		try {
			return new SimpleDateFormat(pattern).parse(dateString);
		} catch (ParseException e) {
			throw new ConversionException(e);
		}
	}
}
