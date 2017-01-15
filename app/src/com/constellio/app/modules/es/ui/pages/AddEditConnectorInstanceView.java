package com.constellio.app.modules.es.ui.pages;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface AddEditConnectorInstanceView extends BaseView, AdminViewGroup {

	void setRecordVO(RecordVO recordVO);

}
