package com.constellio.model.services.caches;

import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.UserCredential;

public interface UserCache {

	void invalidateUser(UserCredential user);

	void invalidateUsersInGroup(GlobalGroup group);

	void invalidateAll();

	ConstellioCache getCache();
}
