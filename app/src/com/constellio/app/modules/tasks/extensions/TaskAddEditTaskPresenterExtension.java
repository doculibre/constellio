package com.constellio.app.modules.tasks.extensions;

import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.modules.tasks.ui.pages.tasks.AddEditTaskView;
import com.constellio.model.entities.records.RecordUpdateOptions;

public abstract class TaskAddEditTaskPresenterExtension {

	public abstract RecordUpdateOptions getRecordUpdateOption();

	public abstract boolean adjustRequiredUSRMetadatasFields();

	public abstract void adjustDisabledFields(AddEditTaskView view, TaskVO taskVO);

}
