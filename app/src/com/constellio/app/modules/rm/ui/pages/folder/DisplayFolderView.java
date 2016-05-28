package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.pages.viewGroups.RecordsManagementViewGroup;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;

public interface DisplayFolderView extends BaseView, RecordsManagementViewGroup {

	void setRecord(RecordVO recordVO);

	void setDocuments(RecordVODataProvider dataProvider);

	void setSubFolders(RecordVODataProvider dataProvider);

	void setTasks(RecordVODataProvider dataProvider);

	void selectMetadataTab();

	void selectDocumentsTab();

	void selectSubFoldersTab();

	void selectTasksTab();

	void setLogicallyDeletable(ComponentState state);

	void setEditButtonState(ComponentState state);

	void setMoveInFolderState(ComponentState state);

	void setAddDocumentButtonState(ComponentState state);

	void setAddSubFolderButtonState(ComponentState state);

	void setDuplicateFolderButtonState(ComponentState state);

	void setPrintButtonState(ComponentState state);

	void setShareFolderButtonState(ComponentState state);

	void setAuthorizationButtonState(ComponentState authorizationButtonState);

	void setBorrowButtonState(ComponentState state);

	void setReturnFolderButtonState(ComponentState state);

	void setReminderReturnFolderButtonState(ComponentState state);

	void setAlertWhenAvailableButtonState(ComponentState state);

	void setStartWorkflowButtonState(ComponentState state);

	void setBorrowedMessage(String borrowedMessage);

	void refreshDocumentsTab();

	void openDocumenContentVersiontWindow(DocumentVO documentVO, ContentVersionVO contentVersionVO);

	void closeDocumentContentVersionWindow();

	void openAgentURL(String agentURL);

	void downloadContentVersion(RecordVO recordVO, ContentVersionVO contentVersionVO);

}
