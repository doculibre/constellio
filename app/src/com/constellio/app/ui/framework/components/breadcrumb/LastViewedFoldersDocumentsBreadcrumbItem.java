package com.constellio.app.ui.framework.components.breadcrumb;

import com.constellio.app.modules.rm.wrappers.Document;

import static com.constellio.app.ui.i18n.i18n.$;

public class LastViewedFoldersDocumentsBreadcrumbItem implements BreadcrumbItem {

	private final String recentItemsSchemaType;

	public LastViewedFoldersDocumentsBreadcrumbItem(String recentItemsSchemaType) {
		this.recentItemsSchemaType = recentItemsSchemaType;
	}

	@Override
	public String getLabel() {
		String label;
		if (recentItemsSchemaType == Document.SCHEMA_TYPE) {
			label = "HomeView.tab.lastViewedDocuments";
		} else {
			label = "HomeView.tab.lastViewedFolders";
		}
		return $(label);
	}

	public String getRecentItemsSchemaType() {
		return recentItemsSchemaType;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
