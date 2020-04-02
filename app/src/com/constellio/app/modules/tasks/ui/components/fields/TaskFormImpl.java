package com.constellio.app.modules.tasks.ui.components.fields;

import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.modules.tasks.extensions.api.TaskModuleExtensions;
import com.constellio.app.modules.tasks.extensions.param.HasReferenceWithModifiedRecordParam;
import com.constellio.app.modules.tasks.ui.components.TaskFieldFactory;
import com.constellio.app.modules.tasks.ui.pages.tasks.AddEditTaskViewImpl;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.base.SessionContext;

import java.util.List;

public abstract class TaskFormImpl extends RecordForm implements TaskForm {

	private String collection;

	public TaskFormImpl(RecordVO recordVO, boolean isEditView, List<String> unavailablesTaskTypes,
						AddEditTaskViewImpl addEditTaskView, ConstellioFactories constellioFactories) {
		super(recordVO, new TaskFieldFactory(isEditView, unavailablesTaskTypes, recordVO, addEditTaskView), constellioFactories);
		this.collection = addEditTaskView.getCollection();
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

	@Override
	public SaveAction showConfirmationMessage() {
		SaveAction saveAction = super.showConfirmationMessage();

		TaskModuleExtensions taskModuleExtensions = getConstellioFactories().getAppLayerFactory().getExtensions().forCollection(collection)
				.forModule(TaskModule.ID);

		boolean hasModification = taskModuleExtensions.hasReferenceWithModifiedRecord(new HasReferenceWithModifiedRecordParam(recordVO));

		if (saveAction == SaveAction.cancelSave && hasModification) {
			return SaveAction.saveSilently;
		}

		return saveAction;
	}
}
