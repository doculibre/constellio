package com.constellio.data.dao.services.cache;

import java.util.List;

public interface ConstellioCacheManager {
	
	List<String> getCacheNames();
	
	ConstellioCache getCache(String name);
	
}
