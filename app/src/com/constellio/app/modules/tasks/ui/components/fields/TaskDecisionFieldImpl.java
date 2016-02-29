package com.constellio.app.modules.tasks.ui.components.fields;

import com.vaadin.ui.ComboBox;

public class TaskDecisionFieldImpl extends ComboBox implements TaskDecisionField {
	@Override
	public String getFieldValue() {
		return (String) getValue();
	}

	@Override
	public void setFieldValue(Object value) {
		setValue(value);
	}

	@Override
	public void addItem(String code) {
		super.addItem(code);
	}
}
