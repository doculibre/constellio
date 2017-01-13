package com.constellio.model.entities.security.global;

import java.util.List;

public interface GlobalGroup {
	String getCode();

	String getName();

	List<String> getUsersAutomaticallyAddedToCollections();

	String getParent();

	GlobalGroupStatus getStatus();

	boolean isLocallyCreated();

	GlobalGroup withName(String name);

	GlobalGroup withUsersAutomaticallyAddedToCollections(List<String> usersAutomaticallyAddedToCollections);

	GlobalGroup withStatus(GlobalGroupStatus status);

	GlobalGroup withRemovedCollection(String collection);

	GlobalGroup withLocallyCreated(boolean locallyCreated);
}
