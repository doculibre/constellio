package com.constellio.app.modules.tasks.ui.components.fields;

import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;

public class TaskTypeFieldLookupImpl extends LookupRecordField implements TaskTypeField {

	public TaskTypeFieldLookupImpl() {
		super(TaskType.SCHEMA_TYPE);
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
