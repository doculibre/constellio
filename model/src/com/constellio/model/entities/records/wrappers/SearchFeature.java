package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.List;

public class SearchFeature extends RecordWrapper {

	public static final String SCHEMA_TYPE = "searchFeature";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String STORE = "store";
	public static final String NAME = "name";
	public static final String CLAZZ = "class";
	public static final String PARAMS = "params";

	public SearchFeature(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE + "_");
	}

	public String getStore() {
		return get(STORE);
	}

	public SearchFeature setStore(String store) {
		set(STORE, store);
		return this;
	}

	public String getName() {
		return get(NAME);
	}

	public SearchFeature setName(String name) {
		set(NAME, name);
		return this;
	}

	public String getClazz() {
		return get(CLAZZ);
	}

	public SearchFeature setClazz(String clazz) {
		set(CLAZZ, clazz);
		return this;
	}

	public List<String> getParams() {
		return get(PARAMS);
	}

	public SearchFeature setParams(List<String> params) {
		set(PARAMS, params);
		return this;
	}
}
