package com.constellio.app.ui.pages.base;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.pages.search.criteria.Criterion;

import java.io.Serializable;
import java.util.List;

public interface ConstellioHeader extends Serializable {

	String ACTION_MENU = "header.actionMenu";

	String getSearchExpression();

	void setSearchExpression(String expression);

	void addEmptyCriterion();

	List<Criterion> getAdvancedSearchCriteria();

	void setAdvancedSearchCriteria(List<Criterion> criteria);

	String getAdvancedSearchSchemaType();

	void setAdvancedSearchSchemaType(String schemaTypeCode);

	void selectAdvancedSearchSchemaType(String schemaTypeCode);

	void selectAdvancedSearchSchema(String schemaCode);

	void setAdvancedSearchSchema(String schemaCode);

	void setShowDeactivatedMetadatas(boolean shown);

	ConstellioHeader hideAdvancedSearchPopup();

	CoreViews navigateTo();

	String getCollection();

	ConstellioFactories getConstellioFactories();

	SessionContext getSessionContext();

	void setCollections(List<String> collections);

	void updateUIContent();

	void setSelectionButtonEnabled(boolean enabled);

	void setAdvancedSearchFormVisible(boolean visible);

	void setSelectionPanelVisible(boolean visible, boolean refresh);

	void setSelectionCount(int selectionCount);

	void refreshSelectionPanel();

	void refreshActionButtons();

	void removeRecordsFromPanel(List<String> idList);

	BaseView getCurrentView();

	void setCurrentCollectionQuietly();

	void removeItems(List<String> ids);
}
