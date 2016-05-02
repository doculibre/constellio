package com.constellio.app.modules.robots.ui.pages;

import java.util.List;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory.Choice;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface AddEditRobotView extends BaseView, AdminViewGroup {
	
	void setCriteriaSchema(String schemaType);

	void addEmptyCriterion();

	void setAvailableActions(List<Choice> choices);

	void setActionParametersFieldEnabled(boolean enabled);

	void resetActionParameters(RecordVO record);
}
