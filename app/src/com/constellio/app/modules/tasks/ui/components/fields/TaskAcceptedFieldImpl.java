package com.constellio.app.modules.tasks.ui.components.fields;

import com.constellio.app.ui.framework.components.fields.BooleanOptionGroup;

public class TaskAcceptedFieldImpl extends BooleanOptionGroup implements TaskAcceptedField {
	public TaskAcceptedFieldImpl() {
		super();
		addStyleName("horizontal");
	}

	@Override
	public Boolean getFieldValue() {
		return (Boolean) getValue();
	}

	@Override
	public void setFieldValue(Object value) {
		setValue(value);
	}
}
