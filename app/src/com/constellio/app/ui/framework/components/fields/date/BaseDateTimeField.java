package com.constellio.app.ui.framework.components.fields.date;

import java.util.Date;

import com.constellio.app.ui.util.DateFormatUtils;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.DateField;

public class BaseDateTimeField extends DateField {

	public BaseDateTimeField() {
		super();
		init();
	}

	public BaseDateTimeField(Property<?> dataSource)
			throws IllegalArgumentException {
		super(dataSource);
		init();
	}

	public BaseDateTimeField(String caption, Date value) {
		super(caption, value);
		init();
	}

	public BaseDateTimeField(String caption, Property<?> dataSource) {
		super(caption, dataSource);
		init();
	}

	public BaseDateTimeField(String caption) {
		super(caption);
		init();
	}

	private void init() {
		setDateFormat(DateFormatUtils.getDateTimeFormat());
		setResolution(Resolution.SECOND);
	}
}
