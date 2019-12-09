package com.constellio.model.services.records.cache.offHeapCollections;

import com.constellio.model.services.records.RecordId;

import java.util.List;

public interface SortedIdsList {

	void add(String id);

	void add(int id);

	void remove(String id);

	List<String> getValues();

	List<RecordId> getValuesId();

	/**
	 * WARNING - MAKE SURE that the list is never written at the same moment, otherwise it will crash the JVM
	 *
	 * @return
	 */
	List<String> getValuesWithoutSynchronizing();

	/**
	 * WARNING - MAKE SURE that the list is never written at the same moment, otherwise it will crash the JVM
	 *
	 * @return
	 */
	List<RecordId> getValuesIdWithoutSynchronizing();

	int size();

	void clear();

	long valuesHeapLength();

	long valuesOffHeapLength();

}
