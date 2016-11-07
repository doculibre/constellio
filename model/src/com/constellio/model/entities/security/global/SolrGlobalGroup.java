package com.constellio.model.entities.security.global;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class SolrGlobalGroup extends RecordWrapper implements GlobalGroup {
	public static final String SCHEMA_TYPE = "globalGroup";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String CODE = "code";
	public static final String NAME = "name";
	public static final String COLLECTIONS = "collections";
	public static final String PARENT = "parent";
	public static final String STATUS = "status";
	public static final String HIERARCHY = "hierarchy";
	public static final String LOCALLY_CREATED = "locallyCreated";

	public SolrGlobalGroup(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	@Override
	public String getCode() {
		return get(CODE);
	}

	public SolrGlobalGroup setCode(String code) {
		set(CODE, code);
		return this;
	}

	@Override
	public String getName() {
		return get(NAME);
	}

	public SolrGlobalGroup setName(String name) {
		set(NAME, name);
		return this;
	}

	@Override
	public List<String> getUsersAutomaticallyAddedToCollections() {
		return getList(COLLECTIONS);
	}

	public SolrGlobalGroup setUsersAutomaticallyAddedToCollections(List<String> collections) {
		set(COLLECTIONS, collections);
		return this;
	}

	@Override
	public String getParent() {
		return get(PARENT);
	}

	public SolrGlobalGroup setParent(String parent) {
		set(PARENT, parent);
		return this;
	}

	@Override
	public GlobalGroupStatus getStatus() {
		return get(STATUS);
	}

	public SolrGlobalGroup setStatus(GlobalGroupStatus status) {
		set(STATUS, status);
		return this;
	}

	public String getHierarchy() {
		return get(HIERARCHY);
	}

	public SolrGlobalGroup setHierarchy(String path) {
		set(HIERARCHY, path);
		return this;
	}

	@Override
	public GlobalGroup withName(String name) {
		return setName(name);
	}

	@Override
	public GlobalGroup withUsersAutomaticallyAddedToCollections(List<String> users) {
		return setUsersAutomaticallyAddedToCollections(users);
	}

	@Override
	public GlobalGroup withStatus(GlobalGroupStatus status) {
		return setStatus(status);
	}

	@Override
	public GlobalGroup withRemovedCollection(String collection) {
		List<String> collections = new ArrayList<>(getUsersAutomaticallyAddedToCollections());
		collections.remove(collection);
		return setUsersAutomaticallyAddedToCollections(collections);
	}

	@Override
	public boolean isLocallyCreated() {
		return get(LOCALLY_CREATED);
	}

	@Override
	public GlobalGroup withLocallyCreated(boolean locallyCreated) {
		RecordWrapper recordWrapper = set(LOCALLY_CREATED, (Object) locallyCreated);
		return (GlobalGroup) recordWrapper;
	}

}
