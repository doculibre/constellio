package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.modules.rm.ui.pages.viewGroups.RecordsManagementViewGroup;
import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;

public interface DisplayFolderView extends BaseView, RecordsManagementViewGroup {

	RecordVO getSummaryRecord();

	void setSummaryRecord(RecordVO recordVO);

	void setEvents(RecordVODataProvider dataProvider);

	void setFolderContent(RecordVODataProvider dataProvider);

	void setTasks(RecordVODataProvider dataProvider);

	void selectSharesTab();

	void selectMetadataTab();

	void selectFolderContentTab();

	void selectTasksTab();

	void selectEventsTab();

	void hideAllActionMenuButtons();

	String getFolderOrSubFolderButtonTitle(String key);

	String getFolderOrSubFolderButtonKey(String key);

	void setLogicallyDeletable(ComponentState state);

	void setDisplayButtonState(ComponentState state);

	void setEditButtonState(ComponentState state);

	void setAddDocumentButtonState(ComponentState state);

	void setBorrowedMessage(String borrowedMessage);

	void refreshFolderContentTab();

	void closeDocumentContentVersionWindow();

	//	void openAgentURL(String agentURL);

	void downloadContentVersion(RecordVO recordVO, ContentVersionVO contentVersionVO);

	void setTaxonomyCode(String taxonomyCode);

	void clearUploadField();

	void refreshFolderContentAndFacets();

	void refreshFolderContent();

	void showVersionUpdateWindow(RecordVO recordVO, ContentVersionVO dragAndDropEvent);

	boolean scrollIntoView(Integer contentIndex, String recordId);

	Integer getReturnIndex();

	RecordVO getReturnRecordVO();

	void removeAuthorization(AuthorizationVO authorizationVO);
}
