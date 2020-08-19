package com.constellio.app.extensions.ui;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.menuBar.CoreRecordMenuBarHandler;

public class CoreConstellioUIExtension extends ConstellioUIExtention {
	AppLayerFactory appLayerFactory;

	public CoreConstellioUIExtension(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	public void addToInitialisation(ConstellioUIExtentionParams constellioUIExtentionParams) {
		constellioUIExtentionParams.getConstellioUI().addRecordMenuBarHandler(new CoreRecordMenuBarHandler(appLayerFactory));
	}
}
