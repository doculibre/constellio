package com.constellio.model.services.search.query.logical.condition;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.records.wrappers.Event;

public class CollectionFilters implements DataStoreFilters {

	boolean exceptEvents;

	String collection;

	public CollectionFilters(String collection, boolean exceptEvents) {
		this.collection = collection;
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
