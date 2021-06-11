package com.constellio.app.ui.pages.management.schemaRecords;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface AddEditSchemaRecordView extends BaseView, AdminViewGroup {

	void setRecordVO(RecordVO recordVO);

	RecordForm getForm();

}
