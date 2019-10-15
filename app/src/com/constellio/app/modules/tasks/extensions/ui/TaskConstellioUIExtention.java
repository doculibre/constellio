package com.constellio.app.modules.tasks.extensions.ui;

import com.constellio.app.extensions.ui.ConstellioUIExtention;
import com.constellio.app.modules.tasks.ui.menubar.TaskRecordMenuBarHandler;
import com.constellio.app.services.factories.AppLayerFactory;

public class TaskConstellioUIExtention extends ConstellioUIExtention {
	private AppLayerFactory appLayerFactory;

	public TaskConstellioUIExtention(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public void addToInitialisation(ConstellioUIExtentionParams constellioUIExtentionParams) {
		constellioUIExtentionParams.getConstellioUI().addRecordMenuBarHandler(new TaskRecordMenuBarHandler(appLayerFactory));
	}
}
