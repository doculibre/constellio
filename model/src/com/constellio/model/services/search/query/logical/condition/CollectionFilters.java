package com.constellio.model.services.search.query.logical.condition;

import com.constellio.model.entities.records.wrappers.Event;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

public class CollectionFilters implements DataStoreFilters {

	boolean exceptEvents;

	String collection;

	String dataStore;

	public CollectionFilters(String collection, String dataStore, boolean exceptEvents) {
		this.collection = collection;
		this.dataStore = dataStore;
		this.exceptEvents = exceptEvents;
	}

	@Override
	public List<String> getFilterQueries(boolean hasSecurityFilters) {
		List<String> filters = new ArrayList<>();
		if (!hasSecurityFilters) {
			filters.add("(*:* -type_s:index)");
		}
		filters.add("collection_s:" + collection);
		if (exceptEvents) {
			filters.add("(*:* -schema_s:" + Event.SCHEMA_TYPE + "*)");
		}

		return filters;
	}

	public String getCollection() {
		return collection;
	}

	@Override
	public String getDataStore() {
		return dataStore;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return "CollectionFilters(" + collection + ")";
	}
}
