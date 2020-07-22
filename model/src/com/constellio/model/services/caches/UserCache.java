package com.constellio.model.services.caches;

import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.model.entities.security.global.GlobalGroup;

public interface UserCache {

	void invalidateUser(String username);

	void invalidateUsersInGroup(GlobalGroup group);

	void invalidateAll();

	ConstellioCache getCache();
}
