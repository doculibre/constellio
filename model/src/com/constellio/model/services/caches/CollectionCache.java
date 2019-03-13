package com.constellio.model.services.caches;

import com.constellio.data.dao.services.cache.ConstellioCache;

public interface CollectionCache {

	void invalidateAll();

	ConstellioCache getCache();

}
