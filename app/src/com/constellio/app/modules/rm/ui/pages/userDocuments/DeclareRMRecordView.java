package com.constellio.app.modules.rm.ui.pages.userDocuments;

import java.util.Map;

import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.UserDocumentVO;
import com.constellio.app.ui.pages.base.BaseView;

public interface DeclareRMRecordView extends BaseView {

	UserDocumentVO getUserDocumentVO();

	String getNewVersionDocumentId();

	String getFolderId();

	void setFolderId(String folderId);

	void setDuplicates(Map<DocumentVO, ContentVersionVO> duplicateVOs);

	void setSimilarDocuments(Map<DocumentVO, Double> similarDocumentVOs);

	void setSuggestedFolders(Map<FolderVO, Double> suggestedFolderVOs);

	void closeWindow();

	void setDeleteButtonVisible(boolean visible);

}
