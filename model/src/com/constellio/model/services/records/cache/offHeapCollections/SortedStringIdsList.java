package com.constellio.model.services.records.cache.offHeapCollections;

import com.constellio.model.services.records.RecordId;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.records.StringRecordId;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.records.RecordUtils.sizeOf;

public class SortedStringIdsList implements SortedIdsList {

	SortedIntIdsList intIdsList;

	List<String> stringIds = new ArrayList<>();

	public SortedStringIdsList(SortedIntIdsList intIdsList) {
		this.intIdsList = intIdsList;
	}

	public SortedStringIdsList() {
		this.intIdsList = new SortedIntIdsList();
	}

	@Override
	public synchronized void add(String id) {
		int intId = RecordUtils.toIntKey(id);

		if (intId == RecordUtils.KEY_IS_NOT_AN_INT) {

			boolean insertAtEnd = true;
			for (int i = 0; i < stringIds.size(); i++) {

				int comparison = id.compareTo(stringIds.get(i));
				if (comparison < 0) {
					stringIds.add(i, id);
					insertAtEnd = false;
					break;

				} else if (comparison == 0) {
					insertAtEnd = false;
					break;
				}

			}

			if (insertAtEnd) {
				stringIds.add(id);
			}

		} else {
			intIdsList.add(intId);
		}

	}

	@Override
	public void add(int id) {
		intIdsList.add(id);
	}

	@Override
	public synchronized void remove(String id) {
		int intId = RecordUtils.toIntKey(id);

		if (intId == RecordUtils.KEY_IS_NOT_AN_INT) {
			stringIds.remove(id);
		} else {
			intIdsList.remove(intId);
		}
	}

	@Override
	public synchronized List<String> getValues() {
		return getValuesWithoutSynchronizing();
	}

	@Override
	public synchronized List<RecordId> getValuesId() {
		return getValuesIdWithoutSynchronizing();
	}

	@Override
	public List<String> getValuesWithoutSynchronizing() {
		List<String> values = intIdsList.getValues();
		values.addAll(stringIds);
		return values;
	}

	@Override
	public List<RecordId> getValuesIdWithoutSynchronizing() {
		List<RecordId> list = intIdsList.getValuesId();
		for (String id : stringIds) {
			list.add(new StringRecordId(id));
		}
		return list;
	}

	@Override
	public int size() {
		return intIdsList.size() + stringIds.size();
	}

	@Override
	public void clear() {
		intIdsList.clear();
		stringIds.clear();
	}

	@Override
	public long valuesHeapLength() {
		return this.intIdsList.valuesHeapLength();
	}

	@Override
	public long valuesOffHeapLength() {
		return 12 + sizeOf(stringIds) + intIdsList.valuesHeapLength();
	}
}
