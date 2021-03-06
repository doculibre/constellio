package com.constellio.model.entities.security.global;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.schemas.Schemas;
import lombok.Getter;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupAddUpdateRequest {
	public static final String SCHEMA_TYPE = "globalGroup";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String CODE = "code";
	public static final String NAME = "name";
	public static final String COLLECTIONS = "collections";
	public static final String PARENT = "parent";
	public static final String STATUS = "status";
	public static final String HIERARCHY = "hierarchy";
	public static final String LOCALLY_CREATED = "locallyCreated";

	@Getter
	String code;

	@Getter
	Map<String, Object> modifiedAttributes = new HashMap<>();

	@Getter
	List<String> newCollections;

	@Getter
	List<String> removedCollections;

	@Getter
	private boolean markedForDeletionInAllCollections;

	@Getter
	private List<String> markedForDeletionInCollections;

	private boolean ldapSyncRequest;

	public GroupAddUpdateRequest(String code) {
		this.code = code;
	}

	public boolean isLdapSyncRequest() {
		return ldapSyncRequest;
	}

	public GroupAddUpdateRequest ldapSyncRequest() {
		this.ldapSyncRequest = true;
		return this;
	}

	public GroupAddUpdateRequest setName(String name) {
		set(NAME, name);
		return this;
	}

	public GroupAddUpdateRequest setParent(String parentGroupCode) {
		set(PARENT, parentGroupCode);
		return this;
	}

	public GroupAddUpdateRequest setStatusInAllCollections(GlobalGroupStatus status) {
		set(STATUS, status);
		return this;
	}

	@Deprecated
	public GroupAddUpdateRequest setHierarchy(String path) {
		set(HIERARCHY, path);
		return this;
	}

	@Deprecated
	public GroupAddUpdateRequest setLogicallyDeletedStatus(Boolean status) {
		set(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode(), status);
		LocalDateTime dateTime = Boolean.TRUE.equals(status) ? TimeProvider.getLocalDateTime() : null;
		set(Schemas.LOGICALLY_DELETED_ON.getLocalCode(), dateTime);

		return this;
	}


	public GroupAddUpdateRequest setLocallyCreated(boolean locallyCreated) {
		if (!locallyCreated) {
			ldapSyncRequest();
		}
		set(LOCALLY_CREATED, locallyCreated);
		return this;
	}

	private void set(String metadataLocalCode, Object value) {
		modifiedAttributes.put(metadataLocalCode, value);
	}

	public GroupAddUpdateRequest removeCollection(String collection) {
		if (removedCollections == null) {
			removedCollections = new ArrayList<>();
		}
		removedCollections.add(collection);
		return this;
	}

	public GroupAddUpdateRequest removeCollections(String... collections) {
		Arrays.stream(collections).forEach(this::removeCollection);
		return this;
	}

	public GroupAddUpdateRequest addCollections(String... collections) {
		if (newCollections == null) {
			newCollections = new ArrayList<>();
		}
		for (String collection : collections) {
			newCollections.add(collection);
		}
		return this;
	}

	public GroupAddUpdateRequest addCollections(List<String> collections) {
		if (newCollections == null) {
			newCollections = new ArrayList<>();
		}
		newCollections.addAll(collections);
		return this;
	}

	public GroupAddUpdateRequest addCollection(String collection) {
		if (newCollections == null) {
			newCollections = new ArrayList<>();
		}
		newCollections.add(collection);
		return this;
	}

	public void markForDeletionInAllCollections() {
		markedForDeletionInAllCollections = true;
	}

	public void markForDeletionInCollections(List<String> collections) {
		if (markedForDeletionInCollections == null) {
			markedForDeletionInCollections = new ArrayList<>();
		}
		markedForDeletionInCollections.addAll(collections);
	}

	public boolean isModified(String metadataCode) {
		return modifiedAttributes.containsKey(metadataCode);
	}
}
