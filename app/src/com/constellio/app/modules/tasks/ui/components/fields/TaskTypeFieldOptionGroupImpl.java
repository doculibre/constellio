package com.constellio.app.modules.tasks.ui.components.fields;

import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.app.ui.framework.components.fields.record.RecordComboBox;

public class TaskTypeFieldOptionGroupImpl extends RecordComboBox implements TaskTypeField {

	public TaskTypeFieldOptionGroupImpl() {
		super(TaskType.DEFAULT_SCHEMA);
		setImmediate(true);
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
