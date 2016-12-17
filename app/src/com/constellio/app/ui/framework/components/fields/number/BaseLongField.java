package com.constellio.app.ui.framework.components.fields.number;

import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.StringToLongConverter;

public class BaseLongField extends BaseTextField {

	public BaseLongField() {
		init();
	}

	public BaseLongField(Property<?> dataSource) {
		super(dataSource);
		init();
	}

	public BaseLongField(String caption, Property<?> dataSource) {
		super(caption, dataSource);
		init();
	}

	public BaseLongField(String caption, String value) {
		super(caption, value);
		init();
	}

	public BaseLongField(String caption) {
		super(caption);
		init();
	}
	
	private void init() {
		setConverter(new StringToLongConverter());
	}

}
