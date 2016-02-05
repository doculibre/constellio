package com.constellio.app.modules.es.ui.pages.mapping;

import com.constellio.app.ui.pages.base.BaseView;

public interface DisplayConnectorMappingsView extends BaseView {
	void reload();

	void displayQuickConfig(String documentType);
}
