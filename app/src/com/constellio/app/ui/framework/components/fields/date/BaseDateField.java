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
		setDateFormat(DateFormatUtils.getDateFormat());
	}

	@Override
	protected Date handleUnparsableDateString(String dateString)
			throws ConversionException {
		if (dateString.matches("\\d{8}")) {
			try {
				String format = DateFormatUtils.getDateFormat();
				int first = format.indexOf("-");
				int second = format.indexOf("-", first + 1) - 1;
				String corrected = dateString.substring(0, first) + "-" + dateString.substring(4, second) + "-" +
						dateString.substring(second);
				return new SimpleDateFormat(DateFormatUtils.getDateFormat()).parse(corrected);
			} catch (ParseException e) {
				throw new ConversionException(e);
			}
		}
		return super.handleUnparsableDateString(dateString);
	}
}
