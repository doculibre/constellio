package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.ui.pages.viewGroups.ArchivesManagementViewGroup;
import com.constellio.app.ui.pages.search.SearchView;
import com.constellio.app.ui.pages.search.criteria.Criterion;

import java.util.List;

public interface AddExistingContainerView extends SearchView, ArchivesManagementViewGroup {
	String SEARCH_TYPE = "addExistingContainerView";

	void addEmptyCriterion();

	void setCriteriaSchemaType(String schemaType);

	void setSearchCriteria(List<Criterion> criteria);

	void setAdministrativeUnit(String administrativeUnitID);

	List<Criterion> getSearchCriteria();
}
