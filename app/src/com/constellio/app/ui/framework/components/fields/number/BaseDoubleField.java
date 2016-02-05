package com.constellio.app.ui.framework.components.fields.number;

import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.StringToDoubleConverter;

public class BaseDoubleField extends BaseTextField {

	public BaseDoubleField() {
		init();
	}

	public BaseDoubleField(Property<?> dataSource) {
		super(dataSource);
		init();
	}

	public BaseDoubleField(String caption, Property<?> dataSource) {
		super(caption, dataSource);
		init();
	}

	public BaseDoubleField(String caption, String value) {
		super(caption, value);
		init();
	}

	public BaseDoubleField(String caption) {
		super(caption);
		init();
	}

	@Override
	public Double getConvertedValue() {
		return (Double) super.getConvertedValue();
	}

	private void init() {
		setConverter(new StringToDoubleConverter());
		setWidth("100px");
	}
}
