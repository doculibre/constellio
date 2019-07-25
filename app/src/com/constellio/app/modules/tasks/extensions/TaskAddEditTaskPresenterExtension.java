package com.constellio.app.modules.tasks.extensions;

import com.constellio.model.entities.records.RecordUpdateOptions;

public abstract class TaskAddEditTaskPresenterExtension {

	public abstract RecordUpdateOptions getRecordUpdateOption();

	public abstract boolean adjustRequiredUSRMetadatasFields();

}
