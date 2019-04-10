package com.constellio.app.modules.tasks.ui.components.fields;

import java.util.List;

import com.constellio.app.modules.tasks.ui.components.TaskFieldFactory;
import com.constellio.app.modules.tasks.ui.pages.tasks.AddEditTaskViewImpl;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.base.SessionContext;

public abstract class TaskFormImpl extends RecordForm implements TaskForm {

	public TaskFormImpl(RecordVO record, boolean isEditView) {
		super(record, new TaskFieldFactory(isEditView));
	}

	public TaskFormImpl(RecordVO recordVO, boolean isEditView, List<String> unavailablesTaskTypes,
			AddEditTaskViewImpl addEditTaskView) {
		super(recordVO, new TaskFieldFactory(isEditView, unavailablesTaskTypes, recordVO, addEditTaskView));
	}

	@Override
	public CustomTaskField<?> getCustomField(String metadataCode) {
		return (CustomTaskField<?>) getField(metadataCode);
	}

	@Override
	public ConstellioFactories getConstellioFactories() {
		return ConstellioFactories.getInstance();
	}


	@Override
	public SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

}
