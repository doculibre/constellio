package com.constellio.app.ui.pages.management.schemaRecords;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface DisplaySchemaRecordView extends BaseView, AdminViewGroup {

	void setRecordVO(RecordVO recordVO);

	void setSubRecords(RecordVODataProvider dataProvider);

	boolean isViewRecordMode();

}
