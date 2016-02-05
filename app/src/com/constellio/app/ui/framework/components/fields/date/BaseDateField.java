package com.constellio.app.ui.framework.components.fields.date;

import java.util.Date;

import com.constellio.app.ui.util.DateFormatUtils;
import com.vaadin.data.Property;
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

}
