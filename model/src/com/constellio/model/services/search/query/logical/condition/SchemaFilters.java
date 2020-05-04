package com.constellio.model.services.search.query.logical.condition;

import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.schemas.SchemaUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

public class SchemaFilters extends CollectionFilters implements DataStoreFilters {

	MetadataSchema schema;

	MetadataSchemaType schemaType;

	public SchemaFilters(MetadataSchemaType schemaType) {
		super(schemaType.getCollection(), schemaType.getDataStore(), false);
		this.schema = schemaType.getDefaultSchema();
		this.schemaType = schemaType;
	}

	public SchemaFilters(MetadataSchema schema) {
		super(schema.getCollection(), schema.getDataStore(), false);
		this.schema = schema;
	}

	@Override
	public List<String> getFilterQueries(boolean hasSecurityFilters) {
		List<String> filters = new ArrayList<>();
		filters.add("collection_s:" + collection);
		if (schemaType != null) {
			filters.add("schema_s:" + schemaType.getCode() + "_*");
		} else {
			filters.add("schema_s:" + "\"" + schema.getCode() + "\"");
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

	public MetadataSchemaType getSchemaTypeFilter() {
		return schemaType;
	}

	public MetadataSchema getSchemaFilter() {
		return schema;
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
