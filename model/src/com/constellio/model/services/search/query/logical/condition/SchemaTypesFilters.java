package com.constellio.model.services.search.query.logical.condition;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.model.entities.schemas.MetadataSchemaType;

public class SchemaTypesFilters extends CollectionFilters {
	List<MetadataSchemaType> schemaTypes;

	boolean except;

	public SchemaTypesFilters(List<MetadataSchemaType> schemaTypes) {
		this(schemaTypes, false);
	}

	public SchemaTypesFilters(List<MetadataSchemaType> schemaTypes, boolean except) {
		super(schemaTypes.get(0).getCollection(), false);
		this.schemaTypes = schemaTypes;
		this.except = except;
	}

	@Override
	public List<String> getFilterQueries() {
		List<String> filters = super.getFilterQueries();
		String schemasORList = "(" + StringUtils.join(buildFilterElements(), " OR ") + ")";

		filters.add((except ? "-" : "") + "schema_s:" + schemasORList);
		return filters;
	}

	private List<String> buildFilterElements() {
		List<String> result = new ArrayList<>();
		for (MetadataSchemaType schemaType : schemaTypes) {
			result.add(schemaType.getCode() + "_*");
		}
		return result;
	}

	public List<MetadataSchemaType> getSchemaTypes() {
		return schemaTypes;
	}
}

