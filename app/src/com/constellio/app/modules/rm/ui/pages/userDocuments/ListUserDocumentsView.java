package com.constellio.app.modules.rm.ui.pages.userDocuments;

import com.constellio.app.modules.rm.ui.pages.viewGroups.PersonnalSpaceViewGroup;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.data.utils.Builder;

import java.util.List;

public interface ListUserDocumentsView extends BaseView, PersonnalSpaceViewGroup {

	void setUserContent(List<RecordVODataProvider> dataProviders);

	void setClassifyButtonFactory(Builder<ContainerButton> classifyButtonFactory);

	void refresh();
	
	void showUploadMessage(String message);
	
	void showUploadErrorMessage(String message);

	boolean isInAWindow();
}
