package com.constellio.app.modules.rm.ui.components.task;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;

public class TaskFieldLookupImpl extends LookupRecordField implements TaskTypeField {

	public TaskFieldLookupImpl() {
		super(Task.SCHEMA_TYPE);
	}

	@Override
	public String getFieldValue() {
		return (String) getConvertedValue();
	}

	@Override
	public void setFieldValue(Object value) {
		setInternalValue((String) value);
	}

}