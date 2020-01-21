package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.modules.rm.ui.components.folder.FolderForm;
import com.constellio.app.modules.rm.ui.pages.viewGroups.RecordsManagementViewGroup;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseView;

public interface AddEditFolderView extends BaseView, RecordsManagementViewGroup {

	void setRecord(RecordVO recordVO);

	RecordVO getRecord();

	FolderForm getForm();

}
