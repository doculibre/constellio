package com.constellio.app.ui.framework.components.fields.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.constellio.app.ui.framework.components.converters.JodaDateToUtilConverter;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter.ConversionException;

public class JodaDateField extends BaseDateField {

	public JodaDateField() {
		super();
		init();
	}

	public JodaDateField(Property<?> dataSource)
			throws IllegalArgumentException {
		super(dataSource);
		init();
	}

	public JodaDateField(String caption, Date value) {
		super(caption, value);
		init();
	}

	public JodaDateField(String caption, Property<?> dataSource) {
		super(caption, dataSource);
		init();
	}

	public JodaDateField(String caption) {
		super(caption);
		init();
	}

	private void init() {
		setConverter(new JodaDateToUtilConverter());
	}
}
