package com.constellio.data.dao.services.cache;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

public interface ConstellioCache {
	
	String getName();
	
	<T extends Serializable> T get(String key);
	
	<T extends Serializable> void put(String key, T value);
	
	void remove(String key);
	
	void removeAll(Set<String> keys);
	
	void clear();
	
	Iterator<String> keySet();
	
	int size();

}
