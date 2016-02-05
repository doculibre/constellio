package com.constellio.app.modules.tasks.ui.navigation;

import java.io.IOException;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.navigation.AbstractRecordNavigationHandler;

public class TasksRecordNavigationHandler extends AbstractRecordNavigationHandler {

	public TasksRecordNavigationHandler(ConstellioFactories constellioFactories) {
		super(constellioFactories);
		initTransientObjects();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
	}

	@Override
	public boolean isViewForSchemaTypeCode(String schemaTypeCode) {
		return Task.SCHEMA_TYPE.equals(schemaTypeCode);
	}

	@Override
	protected void navigateToView(String recordId, String schemaTypeCode) {
		if (Task.SCHEMA_TYPE.equals(schemaTypeCode)) {
			ConstellioUI.getCurrent().navigateTo().displayTask(recordId);
		} else {
			throw new UnsupportedOperationException("No navigation for schema type code " + schemaTypeCode);
		}
	}
}
