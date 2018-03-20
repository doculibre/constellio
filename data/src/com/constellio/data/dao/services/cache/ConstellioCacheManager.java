package com.constellio.data.dao.services.cache;

import java.util.List;

import com.constellio.data.dao.managers.StatefulService;

public interface ConstellioCacheManager extends StatefulService {

	List<String> getCacheNames();

	//TODO Rename getOrCreateCache
	ConstellioCache getCache(String name);

	ConstellioCache createCache(String name, ConstellioCacheOptions options);

	void clearAll();

}
