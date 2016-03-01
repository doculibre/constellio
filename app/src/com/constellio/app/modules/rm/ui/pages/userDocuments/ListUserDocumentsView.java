package com.constellio.app.modules.rm.ui.pages.userDocuments;

import java.util.List;

import com.constellio.app.ui.entities.UserDocumentVO;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.UserDocumentsViewGroup;
import com.constellio.data.utils.Builder;

public interface ListUserDocumentsView extends BaseView, UserDocumentsViewGroup {

	List<UserDocumentVO> getUserDocuments();

	void setUserDocuments(List<UserDocumentVO> userDocumentVOs);

	void addUserDocument(UserDocumentVO userDocumentVO);

	void removeUserDocument(UserDocumentVO userDocumentVO);

	String getFolderId();

	void setFolderId(String folderId);

	List<UserDocumentVO> getSelectedUserDocuments();

	void setSelectedUserDocuments(List<UserDocumentVO> userDocuments);

	void setClassifyButtonFactory(Builder<ContainerButton> classifyButtonFactory);
}
