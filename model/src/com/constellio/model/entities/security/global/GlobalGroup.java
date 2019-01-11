package com.constellio.model.entities.security.global;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.ArrayList;
import java.util.List;

public class GlobalGroup extends RecordWrapper {
	public static final String SCHEMA_TYPE = "globalGroup";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String CODE = "code";
	public static final String NAME = "name";
	public static final String COLLECTIONS = "collections";
	public static final String PARENT = "parent";
	public static final String STATUS = "status";
	public static final String HIERARCHY = "hierarchy";
	public static final String LOCALLY_CREATED = "locallyCreated";

	public GlobalGroup(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public String getCode() {
		return get(CODE);
	}

	public GlobalGroup setCode(String code) {
		set(CODE, code);
		return this;
	}

	public String getName() {
		return get(NAME);
	}

	public GlobalGroup setName(String name) {
		set(NAME, name);
		return this;
	}

	public List<String> getUsersAutomaticallyAddedToCollections() {
		return getList(COLLECTIONS);
	}

	public GlobalGroup setUsersAutomaticallyAddedToCollections(List<String> collections) {
		set(COLLECTIONS, collections);
		return this;
	}

	public String getParent() {
		return get(PARENT);
	}

	public GlobalGroup setParent(String parent) {
		set(PARENT, parent);
		return this;
	}

	public GlobalGroupStatus getStatus() {
		return get(STATUS);
	}

	public GlobalGroup setStatus(GlobalGroupStatus status) {
		set(STATUS, status);
		return this;
	}

	public String getHierarchy() {
		return get(HIERARCHY);
	}

	public GlobalGroup setHierarchy(String path) {
		set(HIERARCHY, path);
		return this;
	}

	public GlobalGroup removeCollection(String collection) {
		List<String> collections = new ArrayList<>(getUsersAutomaticallyAddedToCollections());
		collections.remove(collection);
		return setUsersAutomaticallyAddedToCollections(collections);
	}

	public boolean isLocallyCreated() {
		return Boolean.TRUE.equals(get(LOCALLY_CREATED));
	}


	public GlobalGroup setLocallyCreated(boolean locallyCreated) {
		RecordWrapper recordWrapper = set(LOCALLY_CREATED, (Object) locallyCreated);
		return (GlobalGroup) recordWrapper;
	}


	public String toString() {
		return toStringPrintingCodes(CODE, NAME, PARENT, STATUS);
	}
}
