package com.constellio.app.ui.pages.management.schemas.display.group;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface ListMetadataGroupSchemaTypeView extends BaseView, AdminViewGroup {
	void refreshTable();

	void displayAddError();

	void displayDeleteError();

	void invalidCodeOrLabels();
}
