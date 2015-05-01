/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
	public List<String> getFilterQueries() {
		List<String> filters = new ArrayList<>();
		filters.add("-type_s:index");
		filters.add("collection_s:" + collection);
		if (exceptEvents) {
			filters.add("-schema_s:" + Event.SCHEMA_TYPE + "*");
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
