package com.constellio.model.services.records.cache.offHeapCollections;

import com.constellio.data.utils.LazyIterator;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.OffHeapIntList_ID;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.allocateMemory;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.freeMemory;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.getInt;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.putInt;

public class OffHeapIntList {

	private static final Logger LOGGER = LoggerFactory.getLogger(OffHeapIntList.class);

	private static int batchSize = 1000;

	private LongArrayList adressesOfBatches = new LongArrayList();
	private int lastIndex = -1;

	private long getAdressOfIndex(int index) {
		return readAddressOfIndex(adressesOfBatches, index);
	}

	private static long readAddressOfIndex(LongArrayList adressesOfBatches, int index) {

		int batch = index / batchSize;
		while (adressesOfBatches.size() < batch + 1) {
			long address = allocateMemory(batchSize * Integer.BYTES, OffHeapIntList_ID);

			for (int i = 0; i < batchSize; i++) {
				putInt(address + i * Integer.BYTES, 0);
			}

			adressesOfBatches.add(address);
		}

		int indexInBatch = index % batchSize;

		return adressesOfBatches.get(batch) + indexInBatch * Integer.BYTES;
	}

	public int getHeapConsumption() {
		return 12 + (12 + Long.BYTES * adressesOfBatches.size()) + Integer.BYTES;
	}

	public int getOffHeapConsumption() {
		return adressesOfBatches.size() * batchSize * Integer.BYTES;
	}

	public void set(int index, int value) {
		if (index < 0) {
			throw new IllegalArgumentException("index must be >=0");
		}
		long address = getAdressOfIndex(index);
		OffHeapMemoryAllocator.putInt(address, value);
		lastIndex = Math.max(index, lastIndex);
	}

	/**
	 * This algo could be optimized to copy large range of bytes instead of value by value
	 *
	 * @param index
	 * @param value
	 */
	public void insertValueShiftingAllFollowingValues(int index, int value) {
		LOGGER.warn("insertValueShiftingAllFollowingValues to insert value '" + value + "' at index '" + index + "' : this should not happen and could consume a lot of memory");
		LongArrayList newAddressesOfBatches = new LongArrayList();

		for (int i = 0; i <= lastIndex; i++) {
			int v = get(i);
			long newAddress = readAddressOfIndex(newAddressesOfBatches, i < index ? i : (i + 1));
			OffHeapMemoryAllocator.putInt(newAddress, v);
		}

		clear();
		adressesOfBatches = newAddressesOfBatches;
		set(index, value);
		lastIndex++;
		LOGGER.warn("insertValueShiftingAllFollowingValues : finished");
	}

	public int get(int index) {
		if (index < 0) {
			throw new IllegalArgumentException("index must be >=0");
		}
		long address = getAdressOfIndex(index);
		return getInt(address);
	}


	//Found on https://www.geeksforgeeks.org/binary-search/

	public int binarySearch(int value) {
		int l = 0, r = lastIndex;
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

	public int size() {
		return lastIndex + 1;
	}

	public int getLast() {
		return lastIndex == -1 ? -1 : get(lastIndex);
	}

	public boolean isEmpty() {
		return lastIndex == -1;
	}

	public Stream<Integer> stream() {
		return new LazyIterator<Integer>() {
			int index = 0;

			@Override
			protected Integer getNextOrNull() {
				if (index <= lastIndex) {
					return get(index++);
				} else {
					return null;
				}
			}
		}.stream();
	}


	public void clear() {
		for (int i = 0; i < this.adressesOfBatches.size(); i++) {
			freeMemory(adressesOfBatches.get(i), batchSize * Integer.BYTES, OffHeapIntList_ID);
		}
		this.adressesOfBatches.clear();
	}
}
