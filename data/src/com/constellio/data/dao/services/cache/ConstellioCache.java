package com.constellio.data.dao.services.cache;

import java.io.Serializable;
import java.util.Iterator;

public interface ConstellioCache {
	
	String getName();
	
	<T extends Serializable> T get(String key);
	
	<T extends Serializable> void put(String key, T value);
	
	void remove(String key);
	
	void clear();
	
	Iterator<String> keySet();

}
