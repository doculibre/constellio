package com.constellio.model.services.search.query;

import java.util.List;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.DataStoreField;

public interface SearchQuery {
	String getQuery(String language);

	List<String> getFilterQueries();

	int getStartRow();

	SearchQuery setStartRow(int row);

	int getNumberOfRows();

	SearchQuery setNumberOfRows(int number);

	String getSort();

	SearchQuery filteredWithUser(User user);

	SearchQuery filteredWithUser(User user, String access);

	SearchQuery computeStatsOnField(String metadata);

	public SearchQuery sortAsc(DataStoreField metadata);

	public SearchQuery sortDesc(DataStoreField metadata);

	void clearSort();
}
