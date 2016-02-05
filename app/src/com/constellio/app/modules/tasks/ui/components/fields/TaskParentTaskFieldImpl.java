package com.constellio.app.modules.tasks.ui.components.fields;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;

public class TaskParentTaskFieldImpl extends LookupRecordField implements TaskParentTaskField {

	public TaskParentTaskFieldImpl(String[] taxonomyCodes) {
		super(Task.SCHEMA_TYPE);
	}

}
