package com.constellio.app.modules.rm.ui.pages.document;

import com.constellio.app.modules.rm.ui.components.document.DocumentActionsComponent;
import com.constellio.app.modules.rm.ui.pages.viewGroups.RecordsManagementViewGroup;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;

import java.util.List;

public interface DisplayDocumentView extends BaseView, RecordsManagementViewGroup, DocumentActionsComponent {

	void refreshMetadataDisplay();

	void setContentVersions(List<ContentVersionVO> contentVersions);

	void setTasks(RecordVODataProvider tasksDataProvider);

	void setEvents(RecordVODataProvider tasksDataProvider);

	void setPublishButtons(boolean published);
	
	void setTaxonomyCode(String taxonomyCode);
	
}
