package com.constellio.app.modules.tasks.ui.components.fields;

import com.constellio.app.ui.framework.components.fields.number.BaseDoubleField;

public class TaskRelativeDueDateFieldImpl extends BaseDoubleField implements TaskRelativeDueDateField {

	@Override
	public String getFieldValue() {
		return getConvertedValue().toString();
	}

	@Override
	public void setFieldValue(Object value) {
		setInternalValue((String) value);
	}
}
