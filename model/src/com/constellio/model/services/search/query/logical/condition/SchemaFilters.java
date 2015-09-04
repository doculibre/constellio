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

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.schemas.SchemaUtils;

public class SchemaFilters extends CollectionFilters implements DataStoreFilters {

	MetadataSchema schema;

	MetadataSchemaType schemaType;

	public SchemaFilters(MetadataSchemaType schemaType) {
		super(schemaType.getCollection(), false);
		this.schema = schemaType.getDefaultSchema();
		this.schemaType = schemaType;
	}

	public SchemaFilters(MetadataSchema schema) {
		super(schema.getCollection(), false);
		this.schema = schema;
	}

	@Override
	public List<String> getFilterQueries() {
		List<String> filters = super.getFilterQueries();
		if (schemaType != null) {
			filters.add("schema_s:" + schemaType.getCode() + "_*");
		} else {
			filters.add("schema_s:" + schema.getCode());
		}
		return filters;
	}

	@Deprecated
	public MetadataSchema getSchema() {
		return schema;
	}

	public String getSchemaType() {
		return new SchemaUtils().getSchemaTypeCode(schema.getCode());
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
		return "SchemaFilters(" + (schemaType == null ? schema.getCode() : schemaType.getCode()) + ")";
	}
}
