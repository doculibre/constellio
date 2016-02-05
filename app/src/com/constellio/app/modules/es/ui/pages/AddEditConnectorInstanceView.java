package com.constellio.app.modules.es.ui.pages;

import com.constellio.app.modules.rm.ui.pages.viewGroups.RecordsManagementViewGroup;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseView;

public interface AddEditConnectorInstanceView extends BaseView, RecordsManagementViewGroup {

	void setRecordVO(RecordVO recordVO);

}
