package com.constellio.model.entities.records.wrappers;

import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class SearchEvent extends RecordWrapper {

	public static final String SCHEMA_TYPE = "searchEvent";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String USERNAME = "username";
	public static final String QUERY = "query";
	public static final String CLICK_COUNT = "clickCount";
	public static final String PAGE_NAVIGATION_COUNT = "pageNavigationCount";
	public static final String PARAMS = "params";
	public static final String ORIGINAL_QUERY = "originalQuery";
	public static final String NUM_FOUND = "numFound";
	public static final String Q_TIME = "qTime";

	public SearchEvent(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE + "_");
	}

	public String getUsername() {
		return get(USERNAME);
	}

	public SearchEvent setUsername(String username) {
		set(USERNAME, username);
		return this;
	}

	public String getQuery() {
		return get(QUERY);
	}

	public SearchEvent setQuery(String query) {
		set(QUERY, query);
		return this;
	}

	public String getOriginalQuery() {
		return get(ORIGINAL_QUERY);
	}

	public SearchEvent setOriginalQuery(String query) {
		set(ORIGINAL_QUERY, query);
		return this;
	}

	public SearchEvent setParams(List<String> listParams) {
		set(PARAMS, listParams);
		return this;
	}

	public List<String> getParams() {
		return get(PARAMS);
	}

	public int getClickCount() {
		return getPrimitiveInteger(CLICK_COUNT);
	}

	public SearchEvent setClickCount(int clickCount) {
		set(CLICK_COUNT, clickCount);
		return this;
	}

	public int getPageNavigationCount() {
		return getPrimitiveInteger(PAGE_NAVIGATION_COUNT);
	}

	public SearchEvent setPageNavigationCount(int pageNavigationCount) {
		set(PAGE_NAVIGATION_COUNT, pageNavigationCount);
		return this;
	}

	public long getNumFound() {
		Number value = get(NUM_FOUND);
		return value == null ? 0 : value.longValue();
	}

	public SearchEvent setNumFound(long numFound) {
		set(NUM_FOUND, numFound);
		return this;
	}

	public long getQTime() {
		Number value = get(Q_TIME);
		return value == null ? 0 : value.longValue();
	}

	public SearchEvent setQTime(long qTime) {
		set(Q_TIME, qTime);
		return this;
	}
}
