package com.constellio.model.services.records.cache.dataStore;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.model.entities.schemas.sort.StringSortFieldNormalizer;
import com.constellio.model.services.records.RecordId;
import com.constellio.model.services.records.cache.offHeapCollections.OffHeapIntList;

import java.util.Iterator;
import java.util.function.Function;

/**
 * The goal of this datastore is to optimize sorts on a specific field
 * For each record id, a global sort value is kept in cache. Initially, each sort value is unique, even for identical
 * values, making subsequent sorts impossible. A space of 7 values is inserted between each sort value to accomate
 * new values. It is possible for two values to received the same sort value, requiring value comparison during sorts (slower)
 * <p>
 * Exemple : "Record 1" is given the value 7 and "Record 40" is given the value 14. If "Record 32" is added, it gets the
 * value 11. Then "Record 37" would get the value 13 and then "Record 36" would get the value 12. Then, if "Record 35"
 * is added, we have no choice than reuse value 11 and compare "Record 32" and "Record 35" during sorts
 */
public class MetadataSortValueDataStore {

	OffHeapIntList sortValues = new OffHeapIntList();
	Function<RecordDTO, String> sortedValueExtractionFunction;

	public MetadataSortValueDataStore(
			Function<RecordDTO, String> sortedValueExtractionFunction) {
		this.sortedValueExtractionFunction = sortedValueExtractionFunction;
	}

	public void initialize(Iterator<RecordId> idIterator) {
	}

	public void onAddUpdate(int index, StringSortFieldNormalizer normalizer) {

	}

}
