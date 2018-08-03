package com.constellio.model.services.search.query.logical.condition;

import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.query.logical.LogicalSearchConditionRuntimeException.CannotSearchInMultipleDataStore;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class SchemaTypesFilters implements DataStoreFilters {
	String collection;
	List<MetadataSchemaType> schemaTypes;
	List<String> schemaTypesCodes;
	String dataStore;

	boolean except;

	public SchemaTypesFilters(List<String> schemaTypesCodes, String collection, String dataStore) {
		this.collection = collection;
		this.schemaTypesCodes = schemaTypesCodes;
		this.dataStore = dataStore;
	}

	public SchemaTypesFilters(List<MetadataSchemaType> schemaTypes) {
		this(schemaTypes, false);
	}

	public SchemaTypesFilters(List<MetadataSchemaType> schemaTypes, boolean except) {
		this.collection = schemaTypes.get(0).getCollection();
		this.schemaTypes = schemaTypes;
		this.dataStore = dataStoreOf(schemaTypes);
		this.except = except;
	}

	private static String dataStoreOf(List<MetadataSchemaType> schemaTypes) {
		String dataStore = null;
		for (MetadataSchemaType schemaType : schemaTypes) {
			if (dataStore == null) {
				dataStore = schemaType.getDataStore();

			} else if (!dataStore.equals(schemaType.getDataStore())) {
				throw new CannotSearchInMultipleDataStore(asList(dataStore, schemaType.getDataStore()));
			}
		}
		return dataStore;

	}

	@Override
	public List<String> getFilterQueries(boolean hasSecurityFilters) {
		List<String> filters = new ArrayList<>();

		String schemasORList = "(" + StringUtils.join(buildFilterElements(), " OR ") + ")";

		filters.add((except ? "-" : "") + "schema_s:" + schemasORList);
		filters.add("collection_s:" + collection);
		return filters;
	}

	@Override
	public String getCollection() {
		return collection;
	}

	@Override
	public String getDataStore() {
		return dataStore;
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

	public List<String> getSchemaTypesCodes() {
		return schemaTypesCodes;
	}
}

