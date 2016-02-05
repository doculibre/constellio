package com.constellio.app.modules.rm.ui.pages.decommissioning;

import java.util.List;

import com.constellio.app.modules.rm.ui.pages.viewGroups.ArchivesManagementViewGroup;
import com.constellio.app.ui.pages.search.SearchView;
import com.constellio.app.ui.pages.search.criteria.Criterion;

public interface AddExistingContainerView extends SearchView, ArchivesManagementViewGroup {
	void addEmptyCriterion();

	void setCriteriaSchemaType(String schemaType);

	List<Criterion> getSearchCriteria();
}
