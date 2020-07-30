package com.constellio.model.entities.security.global;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDateTime;

import java.util.List;

@Builder
public class SystemWideGroup {
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
	private String id;

	@Getter
	private String code;

	@Getter
	private String name;

	@Getter
	private String caption;

	@Getter
	private List<String> ancestors;

	@Getter
	private List<String> collections;

	@Getter
	private String parent;

	@Getter
	GlobalGroupStatus groupStatus;

	@Getter
	String hierarchy;

	@Getter
	Boolean locallyCreated;

	@Getter
	Boolean logicallyDeletedStatus;

	@Getter
	LocalDateTime logicallyDeletedOn;

	//TODO remove!
	String toString;

	public boolean isLocallyCreated() {
		return Boolean.TRUE.equals(locallyCreated);
	}

	public String toString() {
		return toString;
	}

	public boolean isLogicallyDeletedStatus() {
		return Boolean.TRUE.equals(logicallyDeletedStatus);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Deprecated
	public List<String> getUsersAutomaticallyAddedToCollections() {
		return collections;
	}

	@Deprecated
	public GlobalGroupStatus getStatus() {
		return groupStatus;
	}

	public GlobalGroupStatus getStatus(String collection) {
		return groupStatus;
	}
}
