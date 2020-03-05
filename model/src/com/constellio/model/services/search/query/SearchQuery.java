package com.constellio.model.services.search.query;

import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.VisibilityStatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery.UserFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuerySort;

import java.util.List;
import java.util.Locale;

public interface SearchQuery {


	List<String> getFilterQueries();

	int getStartRow();

	SearchQuery setStartRow(int row);

	int getNumberOfRows();

	SearchQuery setNumberOfRows(int number);

	SearchQuery filteredWith(UserFilter userFilter);

	SearchQuery filteredWithUser(User user);

	SearchQuery filteredWithUser(User user, String access);

	SearchQuery filteredWithUser(User user, List<String> accessOrPermissions);

	List<UserFilter> getUserFilters();

	SearchQuery computeStatsOnField(DataStoreField metadata);

	SearchQuery sortAsc(DataStoreField metadata);

	SearchQuery sortDesc(DataStoreField metadata);

	SearchQuery clone();

	void clearSort();

	SearchQuery setLanguage(String language);

	SearchQuery setLanguage(Locale locale);

	String getLanguage();

	List<String> getFieldFacets();

	KeySetMap<String, String> getQueryFacets();

	void clearFacets();

	VisibilityStatusFilter getVisibilityStatusFilter();

	StatusFilter getStatusFilter();

	List<LogicalSearchQuerySort> getSortFields();
}
