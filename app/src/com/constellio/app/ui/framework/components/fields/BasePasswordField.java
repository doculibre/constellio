package com.constellio.app.ui.framework.components.fields;

import com.vaadin.data.Property;
import com.vaadin.ui.PasswordField;
import org.apache.commons.lang.StringUtils;

public class BasePasswordField extends PasswordField {

	public BasePasswordField() {
		init();
	}

	public BasePasswordField(Property<?> dataSource) {
		super(dataSource);
		init();
	}

	public BasePasswordField(String caption) {
		super(caption);
		init();
	}

	public BasePasswordField(String caption, Property<?> dataSource) {
		super(caption, dataSource);
		init();
	}

	public BasePasswordField(String caption, String value) {
		super(caption, value);
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

	@Override
	public void setInternalValue(String newValue) {
		super.setInternalValue(newValue);
	}

	@Override
	public void fireValueChange(boolean repaintIsNotNeeded) {
		super.fireValueChange(repaintIsNotNeeded);
	}
}
