package com.constellio.app.ui.framework.components.fields;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Property;
import com.vaadin.ui.TextField;

public class BaseTextField extends TextField {
	
	public BaseTextField() {
		super();
		init();
	}

	public BaseTextField(Property<?> dataSource) {
		super(dataSource);
		init();
	}

	public BaseTextField(String caption, Property<?> dataSource) {
		super(caption, dataSource);
		init();
	}

	public BaseTextField(String caption, String value) {
		super(caption, value);
		init();
	}

	public BaseTextField(String caption) {
		super(caption);
		init();
	}

	private void init() {
		setNullRepresentation("");
	}

	@Override
	public void setValue(String newValue)
			throws com.vaadin.data.Property.ReadOnlyException {
		newValue = StringUtils.trim(newValue);
		super.setValue(newValue);
	}

}
