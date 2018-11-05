package com.constellio.app.modules.rm.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Cart extends RecordWrapper {
	public static final String SCHEMA_TYPE = "cart";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String OWNER = "owner";
	public static final String SHARED_WITH_USERS = "sharedWithUsers";

	public Cart(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public String getOwner() {
		return get(OWNER);
	}

	public Cart setOwner(String owner) {
		set(OWNER, owner);
		return this;
	}

	public Cart setOwner(User owner) {
		set(OWNER, owner);
		return this;
	}

	public List<String> getSharedWithUsers() {
		return getList(SHARED_WITH_USERS);
	}

	public Cart setSharedWithUsers(List<String> users) {
		set(SHARED_WITH_USERS, users);
		return this;
	}

	public Cart addSharedWithUsers(List<String> users) {
		return addWithoutDuplicates(SHARED_WITH_USERS, users);
	}

	public Cart removeSharedWithUsers(String id) {
		return removeFrom(SHARED_WITH_USERS, id);
	}

	private Cart addWithoutDuplicates(String metadata, List<String> items) {
		Set<String> result = new HashSet<>(this.<String>getList(metadata));
		result.addAll(items);
		set(metadata, new ArrayList<>(result));
		return this;
	}

	private Cart removeFrom(String metadata, String id) {
		List<String> result = new ArrayList<>(this.<String>getList(metadata));
		result.remove(id);
		set(metadata, result);
		return this;
	}
}
