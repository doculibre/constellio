package com.constellio.app.modules.tasks.ui.components.fields;

import com.constellio.app.modules.tasks.model.wrappers.TaskStatusType;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeOptionGroup;

public class TaskStatusFieldImpl extends EnumWithSmallCodeOptionGroup<TaskStatusType> implements TaskStatusField {

	public TaskStatusFieldImpl() {
		super(TaskStatusType.class);
	}

	/*@Override
	public TaskStatusType getFieldValue() {
		return (TaskStatusType) getConvertedValue();
	}

	@Override
	public void setFieldValue(Object value) {
		setInternalValue(value);
	}*/
}

