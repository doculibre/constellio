package com.constellio.model.services.records.cache.offHeapCollections;

import com.constellio.data.dao.dto.records.RecordId;

import java.util.List;

public interface SortedIdsList {

	void add(RecordId id);

	void add(String id);

	void add(int id);

	void remove(RecordId id);

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

	default boolean isEmpty() {
		return size() > 0;
	}

	void clear();

	boolean isSupportingLegacyId();

	long valuesHeapLength();

	long valuesOffHeapLength();

}
