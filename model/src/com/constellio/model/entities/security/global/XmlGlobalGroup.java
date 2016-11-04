package com.constellio.model.entities.security.global;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class XmlGlobalGroup implements GlobalGroup {
	final String parent;
	final String code;
	final String name;
	final List<String> usersAutomaticallyAddedToCollections;
	final GlobalGroupStatus status;
    final boolean locallyCreated;

	public XmlGlobalGroup(String code, String name, List<String> collections, String parent, GlobalGroupStatus status, boolean locallyCreated) {
		this.code = code;
		this.name = name;
		this.usersAutomaticallyAddedToCollections = Collections.unmodifiableList(collections);
		this.parent = parent;
		this.status = status;
        this.locallyCreated = locallyCreated;
	}

	public XmlGlobalGroup(String code, String parent, GlobalGroupStatus status, boolean locallyCreated) {
		this(code, code, Collections.<String>emptyList(), parent, status, locallyCreated);
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<String> getUsersAutomaticallyAddedToCollections() {
		return usersAutomaticallyAddedToCollections;
	}

	@Override
	public String getParent() {
		return parent;
	}

	@Override
	public GlobalGroupStatus getStatus() {
		return status;
	}

    @Override
	public GlobalGroup withName(String name) {
		return new XmlGlobalGroup(code, name, usersAutomaticallyAddedToCollections, parent, status, locallyCreated);
	}

	@Override
	public GlobalGroup withUsersAutomaticallyAddedToCollections(List<String> usersAutomaticallyAddedToCollections) {
		return new XmlGlobalGroup(code, name, usersAutomaticallyAddedToCollections, parent, status, locallyCreated);
	}

	@Override
	public GlobalGroup withStatus(GlobalGroupStatus status) {
		return new XmlGlobalGroup(code, name, usersAutomaticallyAddedToCollections, parent, status, locallyCreated);
	}

	@Override
	public GlobalGroup withRemovedCollection(String collection) {
		List<String> collections = new ArrayList<>(usersAutomaticallyAddedToCollections);
		collections.remove(collection);
		return new XmlGlobalGroup(code, name, collections, parent, status, locallyCreated);
	}

    @Override
    public boolean isLocallyCreated() {
        return locallyCreated;
    }

    @Override
    public GlobalGroup withLocallyCreated(boolean locallyCreated) {
        return new XmlGlobalGroup(code, name, usersAutomaticallyAddedToCollections, parent, status, locallyCreated);
    }

    @Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return code;
	}
}
