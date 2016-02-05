package com.constellio.app.ui.framework.components.fields.number;

import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.StringToIntegerConverter;

public class BaseIntegerField extends BaseTextField {

	public BaseIntegerField() {
		init();
	}

	public BaseIntegerField(Property<?> dataSource) {
		super(dataSource);
		init();
	}

	public BaseIntegerField(String caption, Property<?> dataSource) {
		super(caption, dataSource);
		init();
	}

	public BaseIntegerField(String caption, String value) {
		super(caption, value);
		init();
	}

	public BaseIntegerField(String caption) {
		super(caption);
		init();
	}
	
	private void init() {
		setConverter(new StringToIntegerConverter());
	}

}
