package com.constellio.model.entities.security.global;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

	GlobalGroupStatus groupStatus;

	String hierarchy;

	@Getter
	Boolean locallyCreated;

	Boolean logicallyDeletedStatus;


	//TODO remove!
	String toString;

	public boolean isLocallyCreated() {
		return !Boolean.FALSE.equals(locallyCreated);
	}

	public String toString() {
		return toString;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public List<String> getCollections() {
		return collections;
	}

	@Deprecated
	public GlobalGroupStatus getStatus() {
		return groupStatus;
	}

	@Deprecated
	public GlobalGroupStatus getGroupStatus() {
		return groupStatus;
	}

	public GlobalGroupStatus getStatus(String collection) {
		return groupStatus;
	}

	@Deprecated
	public Boolean getLogicallyDeletedStatus() {
		if (logicallyDeletedStatus == null) {
			return false;
		} else {
			return logicallyDeletedStatus;
		}

	}

	@Deprecated
	public String getHierarchy() {
		return hierarchy;
	}

}
