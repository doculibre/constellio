package com.constellio.app.ui.framework.components.fields.date;

import java.util.Date;

import com.constellio.app.ui.framework.components.converters.JodaDateTimeToUtilConverter;
import com.vaadin.data.Property;

public class JodaDateTimeField extends BaseDateTimeField {

	public JodaDateTimeField() {
		super();
		init();
	}
	
	public JodaDateTimeField(Property<?> dataSource)
			throws IllegalArgumentException {
		super(dataSource);
		init();
	}

	public JodaDateTimeField(String caption, Date value) {
		super(caption, value);
		init();
	}

	public JodaDateTimeField(String caption, Property<?> dataSource) {
		super(caption, dataSource);
		init();
	}

	public JodaDateTimeField(String caption) {
		super(caption);
		init();
	}

	private void init() {
		setConverter(new JodaDateTimeToUtilConverter());
	}

}
