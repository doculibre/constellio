package com.constellio.model.services.search.query;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery.UserFilter;

import java.util.List;

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

	SearchQuery computeStatsOnField(DataStoreField metadata);

	public SearchQuery sortAsc(DataStoreField metadata);

	public SearchQuery sortDesc(DataStoreField metadata);

	void clearSort();
}
