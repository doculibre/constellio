package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.PagesComponentsExtension;
import com.constellio.app.api.extensions.params.DecorateMainComponentAfterInitExtensionParams;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.pages.base.ClickableNotification;
import com.vaadin.ui.Window;

import java.util.Collection;

/**
 * Created by Constellio on 2017-03-16.
 */
public class RemoveClickableNotificationsWhenChangingPage extends PagesComponentsExtension {

	@Override
	public void decorateMainComponentBeforeViewInstanciated(DecorateMainComponentAfterInitExtensionParams params) {
		super.decorateMainComponentBeforeViewInstanciated(params);
		ConstellioUI ui = ConstellioUI.getCurrent();
		if (ui != null) {
			Collection<Window> windows = ui.getWindows();
			for (Window window : windows) {
				if (window != null && window instanceof ClickableNotification) {
					ui.removeWindow(window);
				}
			}
		}
	}
}
