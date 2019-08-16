package com.constellio.model.services.records.cache.offHeapCollections;

import java.util.List;

public interface SortedIdsList {

	void add(String id);

	void add(int id);

	void remove(String id);

	List<String> getValues();

	int size();

	void clear();

}
