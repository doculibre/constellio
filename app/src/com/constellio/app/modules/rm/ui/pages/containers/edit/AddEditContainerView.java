package com.constellio.app.modules.rm.ui.pages.containers.edit;

import com.constellio.app.modules.rm.ui.pages.viewGroups.ArchivesManagementViewGroup;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.vaadin.ui.Component;

public interface AddEditContainerView extends BaseView, ArchivesManagementViewGroup {
	
	void reloadWithContainer(RecordVO container);

	void setType(String type);

	RecordVO getUpdatedContainer();

	Component buildMultipleModeWindowContent();
}
