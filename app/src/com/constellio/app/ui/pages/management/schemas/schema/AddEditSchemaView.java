package com.constellio.app.ui.pages.management.schemas.schema;

import com.constellio.app.ui.entities.FormMetadataSchemaVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface AddEditSchemaView extends BaseView, AdminViewGroup {
	
	void setSchemaVO(FormMetadataSchemaVO schemaVO);
	
}
