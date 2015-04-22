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

import org.apache.commons.lang3.StringUtils;

import com.constellio.model.entities.schemas.MetadataSchemaType;

public class SchemaTypesFilters extends CollectionFilters {
	List<MetadataSchemaType> schemaTypes;

	public SchemaTypesFilters(List<MetadataSchemaType> schemaTypes) {
		super(schemaTypes.get(0).getCollection(), false);
		this.schemaTypes = schemaTypes;
	}

	@Override
	public List<String> getFilterQueries() {
		List<String> filters = super.getFilterQueries();
		filters.add("schema_s:(" + StringUtils.join(buildFilterElements(), " OR ") + ")");
		return filters;
	}

	private List<String> buildFilterElements() {
		List<String> result = new ArrayList<>();
		for (MetadataSchemaType schemaType : schemaTypes) {
			result.add(schemaType.getCode() + "_*");
		}
		return result;
	}
}
