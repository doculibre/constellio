package com.constellio.app.modules.rm.ui.pages.document;

import com.constellio.app.modules.rm.ui.components.document.DocumentForm;
import com.constellio.app.modules.rm.ui.pages.viewGroups.RecordsManagementViewGroup;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseView;

public interface AddEditDocumentView extends BaseView, RecordsManagementViewGroup {

	void setRecord(RecordVO recordVO);
	
	DocumentForm getForm();
	
}
