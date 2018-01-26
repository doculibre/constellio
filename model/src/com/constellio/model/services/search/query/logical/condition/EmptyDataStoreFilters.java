package com.constellio.model.services.search.query.logical.condition;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class EmptyDataStoreFilters implements DataStoreFilters {

	String dataStore;

	public EmptyDataStoreFilters(String dataStore) {
		this.dataStore = dataStore;
	}

	@Override
	public List<String> getFilterQueries(boolean hasSecurityFilters) {
		return Collections.emptyList();
	}

	public String getCollection() {
		return null;
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
		return "CollectionFilters(" + dataStore + ")";
	}
}
