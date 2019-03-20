package com.constellio.app.ui.pages.management.schemas.display.group;

import java.util.List;
import java.util.Map;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface ListMetadataGroupSchemaTypeView extends BaseView, AdminViewGroup {
	
	void setMetadataGroups(List<String> metadataGroups);
	
	void addMetadataGroup(String code, Map<String, String> labels);
	
	void updateMetadataGroup(String code, Map<String, String> labels);
	
	void removeMetadataGroup(String code);

	void displayAddError();

	void displayDeleteError();

	void invalidCodeOrLabels();
	
	void showAddWindow(List<String> languageCodes);
	
	void showEditWindow(String code, Map<String, String> labels);
	
}
