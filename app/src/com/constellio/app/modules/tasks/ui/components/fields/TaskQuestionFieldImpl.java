package com.constellio.app.modules.tasks.ui.components.fields;

import com.constellio.app.ui.framework.components.fields.BaseTextField;

public class TaskQuestionFieldImpl extends BaseTextField implements TaskQuestionField {
	@Override
	public String getFieldValue() {
		return getConvertedValue().toString();
	}

	@Override
	public void setFieldValue(Object value) {
		setInternalValue((String) value);
	}
}
