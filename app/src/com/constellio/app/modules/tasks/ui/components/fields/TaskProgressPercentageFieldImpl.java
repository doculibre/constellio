package com.constellio.app.modules.tasks.ui.components.fields;

import com.constellio.app.ui.framework.components.fields.number.BaseDoubleField;

public class TaskProgressPercentageFieldImpl extends BaseDoubleField implements TaskProgressPercentageField {

	@Override
	public String getFieldValue() {
		return getConvertedValue().toString();
	}

	@Override
	public void setFieldValue(Object value) {
		setInternalValue((String) value);
	}
}
