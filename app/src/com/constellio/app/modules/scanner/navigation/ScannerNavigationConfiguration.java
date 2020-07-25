package com.constellio.app.modules.scanner.navigation;

import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.scanner.ui.ScanDocumentWindow;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.pages.base.ConstellioHeader;
import com.constellio.model.entities.records.wrappers.User;

import static com.constellio.app.ui.framework.components.ComponentState.enabledIf;

public class ScannerNavigationConfiguration {

	public static final String SCAN_DOCUMENT = "scanDocument";

	public void configureNavigation(NavigationConfig config) {
		config.add(ConstellioHeader.ACTION_MENU, new NavigationItem.Active(SCAN_DOCUMENT) {
			@Override
			public void activate(Navigation navigate) {
				ConstellioUI.getCurrent().addWindow(new ScanDocumentWindow());
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return enabledIf(user.has(RMPermissionsTo.CREATE_DOCUMENTS).onSomething());
			}
		}, 0);
	}

}
