package com.constellio.app.modules.rm.ui.pages.decommissioning;

import java.util.List;

import com.constellio.app.modules.rm.ui.pages.viewGroups.ArchivesManagementViewGroup;
import com.constellio.app.ui.pages.search.SearchView;
import com.constellio.app.ui.pages.search.criteria.Criterion;

public interface DecommissioningBuilderView extends SearchView, ArchivesManagementViewGroup {
	String SEARCH_TYPE = "decommissioningView";

	void addEmptyCriterion();

	void setCriteriaSchemaType(String schemaType);

	void setSearchCriteria(List<Criterion> criteria);

	void setAdministrativeUnit(String administrativeUnitID);

	List<Criterion> getSearchCriteria();

}
