package com.constellio.app.modules.tasks.extensions;

import com.constellio.app.modules.tasks.extensions.api.params.TaskFormParams;
import com.constellio.app.modules.tasks.extensions.api.params.TaskFormRetValue;
import com.constellio.app.modules.tasks.extensions.param.HasReferenceWithModifiedRecordParam;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;

public class TaskFormExtension {
	public TaskFormRetValue taskBeingSave(TaskFormParams taskFormParams) {
		return null;
	}

	public ExtensionBooleanResult hasReferenceWithModifiedRecord(HasReferenceWithModifiedRecordParam param) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}
}
