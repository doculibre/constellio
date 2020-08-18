package com.constellio.app.ui.pages.management.schemas.metadata;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface AddEditMetadataView extends BaseView, AdminViewGroup {

	void reloadForm();

	void inputTypeChanged(MetadataInputType metadataInputType);
}
