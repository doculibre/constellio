package com.constellio.model.services.caches;

import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.model.entities.security.global.SystemWideGroup;

public interface UserCache {

	void invalidateUser(String username);

	void invalidateUsersInGroup(SystemWideGroup group);

	void invalidateAll();

	ConstellioCache getCache();
}
