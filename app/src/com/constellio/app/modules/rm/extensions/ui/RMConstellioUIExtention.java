package com.constellio.app.modules.rm.extensions.ui;

import com.constellio.app.extensions.ui.ConstellioUIExtention;
import com.constellio.app.modules.rm.ui.contextmenu.RMRecordContextMenuHandler;
import com.constellio.app.modules.rm.ui.menuBar.RMRecordMenuBarHandler;
import com.constellio.app.services.factories.AppLayerFactory;

public class RMConstellioUIExtention extends ConstellioUIExtention {
	private AppLayerFactory appLayerFactory;

	public RMConstellioUIExtention(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public void addToInitialisation(ConstellioUIExtentionParams constellioUIExtentionParams) {
		constellioUIExtentionParams.getConstellioUI().addRecordContextMenuHandler(new RMRecordContextMenuHandler(appLayerFactory));
		constellioUIExtentionParams.getConstellioUI().addRecordMenuBarHandler(new RMRecordMenuBarHandler(appLayerFactory));
	}
}
