package com.constellio.model.entities.records.wrappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;

public class Group extends RecordWrapper {
	public static final String SCHEMA_TYPE = "group";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String CODE = "code";
	public static final String IS_GLOBAL = "isGlobal";
	public static final String ROLES = "roles";
	public static final String TITLE = Schemas.TITLE_CODE;
	public static final String PARENT = "parent";

	public Group(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public static List<Group> wrap(List<Record> records, MetadataSchemaTypes types) {
		List<Group> groups = new ArrayList<>();

		for (Record record : records) {
			groups.add(new Group(record, types));
		}

		return Collections.unmodifiableList(groups);
	}

	public static Group wrapNullable(Record record, MetadataSchemaTypes types) {
		return record == null ? null : new Group(record, types);
	}

	public String getCode() {
		return get(CODE);
	}

	public List<String> getRoles() {
		return getList(ROLES);
	}

	public Group setRoles(List<String> roles) {
		set(ROLES, roles);
		return this;
	}

	public List<String> getParent() {
		return get(PARENT);
	}

	public Group setParent(String parent) {
		set(PARENT, parent);
		return this;
	}

	public boolean isGlobal() {
		return get(IS_GLOBAL);
	}

	public String getCollection() {
		return wrappedRecord.getCollection();
	}

	public boolean isDirty() {
		return wrappedRecord.isDirty();
	}
}
