package com.constellio.model.services.search.query.logical.condition;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.model.entities.schemas.MetadataSchemaType;

public class SchemaTypesFilters implements DataStoreFilters {
	String collection;
	List<MetadataSchemaType> schemaTypes;
	List<String> schemaTypesCodes;

	boolean except;

	public SchemaTypesFilters(List<String> schemaTypesCodes, String collection) {
		this.collection = collection;
		this.schemaTypesCodes = schemaTypesCodes;
	}

	public SchemaTypesFilters(List<MetadataSchemaType> schemaTypes) {
		this(schemaTypes, false);
	}

	public SchemaTypesFilters(List<MetadataSchemaType> schemaTypes, boolean except) {
		this.collection = schemaTypes.get(0).getCollection();
		this.schemaTypes = schemaTypes;
		this.except = except;
	}

	@Override
	public List<String> getFilterQueries(boolean hasSecurityFilters) {
		List<String> filters = new ArrayList<>();
		filters.add("collection_s:" + collection);
		String schemasORList = "(" + StringUtils.join(buildFilterElements(), " OR ") + ")";

		filters.add((except ? "-" : "") + "schema_s:" + schemasORList);
		return filters;
	}

	@Override
	public String getCollection() {
		return collection;
	}

	private List<String> buildFilterElements() {
		List<String> result = new ArrayList<>();
		if (schemaTypes != null) {
			for (MetadataSchemaType schemaType : schemaTypes) {
				result.add(schemaType.getCode() + "_*");
			}
		} else {
			for (String schemaTypeCode : schemaTypesCodes) {
				result.add(schemaTypeCode + "_*");
			}
		}
		return result;
	}

	public List<MetadataSchemaType> getSchemaTypes() {
		return schemaTypes;
	}
}

