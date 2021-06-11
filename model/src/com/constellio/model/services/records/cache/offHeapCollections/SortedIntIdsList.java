package com.constellio.model.services.records.cache.offHeapCollections;

import com.constellio.data.dao.dto.records.IntegerRecordId;
import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.model.services.records.RecordUtils;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.data.dao.dto.records.RecordDTOUtils.toStringId;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.SortedIntIdsList_ID;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.allocateMemory;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.copy;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.copyAdding;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.putInt;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class SortedIntIdsList implements SortedIdsList {

	public static final int INITIAL_SIZE = 3;

	public static final int ADD_RESIZE_FACTOR = 3;

	/**
	 * Often call when doing massive removes, so we don't need a large factor
	 */
	public static final double REDUCE_RESIZE_FACTOR = 1.10;

	public static final int MAX_INCREMENTING_SIZE = 1000;

	/**
	 * The quantity of elements the structure can take before being reduced
	 */
	public static final int MAX_CAPACITY = MAX_INCREMENTING_SIZE * 5;


	long address;

	int size;

	/**
	 * How many int values the structure can take
	 */
	short itemsCapacity;


	@Override
	public synchronized void add(RecordId id) {
		if (id.isInteger()) {
			add(id.intValue());

		} else {
			throw new UnsupportedOperationException("List does not support legacy String ids");
		}
	}

	@Override
	public synchronized void add(String id) {

		int intId = Integer.valueOf(id);
		add(intId);
	}

	@Override
	public synchronized void add(int intId) {

		int placementIndex = findIndexToPlaceNewValue(intId);

		if (placementIndex != -1) {

			if (itemsCapacity == 0) {
				if (size == 0) {

					size = (INITIAL_SIZE * Integer.BYTES);
					address = allocateMemory(size, SortedIntIdsList_ID);
					putInt(address, intId);
					itemsCapacity = INITIAL_SIZE - 1;

				} else {
					int dataSize = size - (itemsCapacity * Integer.BYTES);
					int newSize = min(dataSize * ADD_RESIZE_FACTOR, dataSize + (Integer.BYTES * MAX_INCREMENTING_SIZE));
					long newAddress = allocateMemory(newSize, SortedIntIdsList_ID);

					try {
						copyAdding(address, newAddress, size, placementIndex * Integer.BYTES, Integer.BYTES);
						putInt(newAddress + placementIndex * Integer.BYTES, intId);

					} catch (Throwable t) {
						OffHeapMemoryAllocator.freeMemory(newAddress, newSize, SortedIntIdsList_ID);

						throw t;
					}


					OffHeapMemoryAllocator.freeMemory(address, size, SortedIntIdsList_ID);
					int bytesCapacity = newSize - size - Integer.BYTES;
					itemsCapacity = (short) (bytesCapacity / Integer.BYTES);
					address = newAddress;
					size = newSize;
				}

			} else {
				copyAdding(address, size - bytesCapacity(), placementIndex * Integer.BYTES, Integer.BYTES);
				putInt(address + placementIndex * Integer.BYTES, intId);
				itemsCapacity--;
			}

		}

	}


	@Override
	public synchronized void remove(RecordId id) {
		if (id.isInteger()) {
			remove(id.intValue());
		}
	}

	@Override
	public synchronized void remove(String id) {
		int intId = RecordUtils.toIntKey(id);
		if (intId != RecordUtils.KEY_IS_NOT_AN_INT) {
			remove(intId);
		}
	}

	public synchronized void remove(int intId) {
		int index = binarySearch(intId);

		if (index != -1) {

			OffHeapMemoryAllocator.copyRemoving(address, size - bytesCapacity(), index * Integer.BYTES, Integer.BYTES);
			itemsCapacity++;
			if (itemsCapacity > MAX_CAPACITY) {
				//Reducing the structure
				int dataSize = size() * Integer.BYTES;
				int newSize =
						min(
								max(
										dataSize + 3 * Integer.BYTES,
										(int) ((dataSize) * REDUCE_RESIZE_FACTOR)
								),
								dataSize + (Integer.BYTES * MAX_INCREMENTING_SIZE));

				long newAddress = allocateMemory(newSize, SortedIntIdsList_ID);

				try {
					copy(address, newAddress, dataSize);

				} catch (Throwable t) {
					OffHeapMemoryAllocator.freeMemory(newAddress, newSize, SortedIntIdsList_ID);

					throw t;
				}


				OffHeapMemoryAllocator.freeMemory(address, size, SortedIntIdsList_ID);
				int bytesCapacity = newSize - dataSize - Integer.BYTES;
				itemsCapacity = (short) (bytesCapacity / Integer.BYTES);
				address = newAddress;
				size = newSize;


				itemsCapacity++;
			}

		}
	}

	private int bytesCapacity() {
		return Integer.BYTES * (int) itemsCapacity;
	}

	@Override
	public synchronized int size() {
		return (size - bytesCapacity()) / Integer.BYTES;
	}

	@Override
	public synchronized void clear() {

		if (size > 0) {
			OffHeapMemoryAllocator.freeMemory(address, size, SortedIntIdsList_ID);
		}

		address = 0;
		size = 0;
		itemsCapacity = 0;
	}

	@Override
	public boolean isSupportingLegacyId() {
		return false;
	}

	@Override
	public long valuesHeapLength() {
		return 12 + Long.BYTES + Integer.BYTES + Short.BYTES;
	}

	@Override
	public long valuesOffHeapLength() {
		return this.size;
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
		List<String> list = new ArrayList<>(size());
		for (int i = 0; i < size(); i++) {
			list.add(toStringId(get(i)));
		}

		return list;
	}

	@Override
	public List<RecordId> getValuesIdWithoutSynchronizing() {
		List<RecordId> list = new ArrayList<>(size());
		for (int i = 0; i < size(); i++) {
			list.add(new IntegerRecordId(get(i)));
		}
		return list;
	}

	//Found on https://www.geeksforgeeks.org/binary-search/

	/**
	 * If binary searching for adding :
	 * Return NULL if the values
	 *
	 * @param value
	 * @return
	 */
	private Integer binarySearch(int value) {
		int l = 0, r = size() - 1;
		while (l <= r) {
			int m = l + (r - l) / 2;

			// Check if x is present at mid
			int testedValue = get(m);
			if (testedValue == value) {
				return m;
			}

			// If x greater, ignore left half
			if (testedValue < value) {
				l = m + 1;
			}

			// If x is smaller, ignore right half
			else {
				r = m - 1;
			}
		}

		// if we reach here, then element was
		// not present
		return -1;
	}

	/**
	 * @param value
	 * @return index for adding the new value or -1 if it is already present
	 */
	int findIndexToPlaceNewValue(int value) {
		int l = 0, r = size() - 1;
		int placementIndex = 0;
		while (l <= r) {
			int m = l + (r - l) / 2;

			// Check if x is present at mid
			int testedValue = get(m);
			if (testedValue == value) {
				return -1;
			}

			// If x greater, ignore left half
			if (testedValue < value) {
				placementIndex = m + 1;
				l = m + 1;
			}

			// If x is smaller, ignore right half
			else {
				r = m - 1;
			}
		}

		// if we reach here, then element was
		// not present
		return placementIndex;
	}

	private int get(int i) {
		return OffHeapMemoryAllocator.getInt(address + i * 4);
	}
}
