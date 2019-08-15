package com.constellio.model.services.records.cache.offHeapCollections;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.records.RecordUtils.toStringId;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.copyAdding;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.putInt;

public class SortedIntIdsList implements SortedIdsList {

	public static final int INITIAL_SIZE = 3;

	public static final int RESIZE_FACTOR = 3;

	public static final int MAX_INCREMENTING_SIZE = 1000;

	long address;

	int size;

	short capacity;

	@Override
	public synchronized void add(String id) {

		int intId = Integer.valueOf(id);

		int placementIndex = findIndexToPlaceNewValue(intId);

		if (placementIndex != -1) {

			if (capacity == 0) {
				if (size == 0) {

					size = (INITIAL_SIZE * Integer.BYTES);
					address = OffHeapMemoryAllocator.allocateMemory(size);
					putInt(address, intId);
					capacity = (short) (size - Integer.BYTES);

				} else {
					int newSize = Math.min(size * RESIZE_FACTOR, size + (Integer.BYTES * MAX_INCREMENTING_SIZE));
					long newAddress = OffHeapMemoryAllocator.allocateMemory(newSize);

					try {
						copyAdding(address, newAddress, size, placementIndex * Integer.BYTES, Integer.BYTES);
						putInt(newAddress + placementIndex * Integer.BYTES, intId);

					} catch (Throwable t) {
						OffHeapMemoryAllocator.freeMemory(newAddress, newSize);

						throw t;
					}


					OffHeapMemoryAllocator.freeMemory(address, size);
					capacity = (short) (newSize - size - Integer.BYTES);
					address = newAddress;
					size = newSize;
				}

			} else {
				copyAdding(address, size - capacity, placementIndex * Integer.BYTES, Integer.BYTES);
				putInt(address + placementIndex * Integer.BYTES, intId);
				capacity -= Integer.BYTES;
			}

		}

	}


	@Override
	public synchronized void remove(String id) {
		int intId = Integer.valueOf(id);
		int index = binarySearch(intId);

		if (index != -1) {
			OffHeapMemoryAllocator.copyRemoving(address, size - capacity, index * Integer.BYTES, Integer.BYTES);
			capacity += Integer.BYTES;
		}
	}

	@Override
	public synchronized int size() {
		return (size - capacity) / Integer.BYTES;
	}

	@Override
	public synchronized void clear() {

		if (size > 0) {
			OffHeapMemoryAllocator.freeMemory(address, size);
		}

		address = 0;
		size = 0;
		capacity = 0;
	}

	@Override
	public synchronized List<String> getValues() {
		List<String> list = new ArrayList<>(capacity / Integer.BYTES);
		for (int i = 0; i < size(); i++) {
			list.add(toStringId(get(i)));
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
