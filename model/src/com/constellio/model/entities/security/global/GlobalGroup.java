package com.constellio.model.entities.security.global;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

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

	public String getName() {
		return get(NAME);
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

	public GlobalGroupStatus getStatus() {
		return get(STATUS);
	}

	public String getHierarchy() {
		return get(HIERARCHY);
	}

	public GlobalGroup setHierarchy(String path) {
		set(HIERARCHY, path);
		return this;
	}


	public boolean isLocallyCreated() {
		return Boolean.TRUE.equals(get(LOCALLY_CREATED));
	}

	public String toString() {
		return toStringPrintingCodes(CODE, NAME, PARENT, STATUS);
	}
}
