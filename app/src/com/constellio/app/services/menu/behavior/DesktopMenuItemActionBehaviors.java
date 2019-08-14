package com.constellio.app.services.menu.behavior;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.ui.ClassifyWindow;

import java.util.List;

public class DesktopMenuItemActionBehaviors {
	AppLayerFactory appLayerFactory;

	public DesktopMenuItemActionBehaviors(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	public void classifyWindow(List<String> recordIds, MenuItemActionBehaviorParams param) {
		ClassifyWindow classifyWindow = new ClassifyWindow(appLayerFactory);
		classifyWindow.classfiy(recordIds, param);
	}
}
