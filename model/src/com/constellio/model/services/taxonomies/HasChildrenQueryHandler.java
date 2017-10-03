package com.constellio.model.services.taxonomies;

import java.util.HashMap;
import java.util.Map;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class HasChildrenQueryHandler {

	private static final String CHILDREN_QUERY = "children";

	String username;

	String cacheMode;

	TaxonomiesSearchServices services;

	LogicalSearchQuery facetQuery;

	Taxonomy taxonomy;

	Map<String, Boolean> cachedValues = new HashMap<>();

	SPEQueryResponse response;

	public HasChildrenQueryHandler(String username, String cacheMode,
			TaxonomiesSearchServices services, LogicalSearchQuery facetQuery) {
		this.username = username;
		this.cacheMode = cacheMode;

		this.services = services;
		this.facetQuery = facetQuery;
		this.taxonomy = taxonomy;
	}

	public void addRecordToCheck(Record record) {
		if (response != null) {
			throw new IllegalStateException("Cannot add record to check after the execution of the query");
		}
		Boolean cachedValue = services.cache.getCachedValue(username, record.getId(), cacheMode);
		cachedValues.put(record.getId(), cachedValue);
		if (cachedValue == null) {
			facetQuery.addQueryFacet(CHILDREN_QUERY, facetQueryFor(record));
		}
	}

	public boolean hasChildren(Record record) {
		Boolean cachedValue = cachedValues.get(record.getId());
		if (cachedValue == null && !cachedValues.containsKey(record.getId())) {
			throw new IllegalStateException("Cannot get has children without calling addRecordToCheck");
		}
		if (cachedValue == null) {

			query();

			boolean hasChildren = response.getQueryFacetCount(facetQueryFor(record)) > 0;
			services.cache.insert(username, record.getId(), cacheMode, hasChildren);
			return hasChildren;
		} else {
			return cachedValue;
		}
	}

	public SPEQueryResponse query() {
		if (response == null) {
			response = services.searchServices.query(facetQuery);
		}
		return response;
	}

	String facetQueryFor(Record record) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("pathParts_ss:");
		stringBuilder.append(record.getId());
		return stringBuilder.toString();
	}

	public static String getCacheMode(MetadataSchemaType selectedSchemaType, String requiredAccess, boolean showInvisible,
			boolean showAllConcepts) {
		return getCacheMode(selectedSchemaType == null ? null : selectedSchemaType.getCode(), requiredAccess, showInvisible,
				showAllConcepts);
	}

	public static String getCacheMode(String selectedSchemaType, String requiredAccess, boolean showInvisible,
			boolean showAllConcepts) {
		String cacheMode;
		if (selectedSchemaType == null) {
			cacheMode = "visible";
		} else if (requiredAccess == null || Role.READ.equals(requiredAccess)) {
			cacheMode = "selecting-" + selectedSchemaType;
		} else {
			cacheMode = "selecting-" + selectedSchemaType + "-" + requiredAccess;
		}

		if (showInvisible) {
			cacheMode += "-inv";
		}

		if (showAllConcepts) {
			cacheMode += "-allConcepts";
		}

		return cacheMode;
	}
}
