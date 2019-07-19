package com.constellio.model.services.records.cache2;

import com.constellio.data.utils.LazyIterator;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;

import java.util.stream.Stream;

public class OffHeapIntList {

	private static int batchSize = 1000;

	private LongArrayList adressesOfBatches = new LongArrayList();
	private int lastIndex = -1;

	private long getAdressOfIndex(int index) {
		return readAddressOfIndex(adressesOfBatches, index);
	}

	private static long readAddressOfIndex(LongArrayList adressesOfBatches, int index) {

		int batch = index / batchSize;
		while (adressesOfBatches.size() < batch + 1) {
			long address = OffHeapMemoryAllocator.allocateMemory(batchSize * Integer.BYTES);

			for (int i = 0; i < batchSize; i++) {
				OffHeapMemoryAllocator.putInt(address + i * Integer.BYTES, 0);
			}

			adressesOfBatches.add(address);
		}

		int indexInBatch = index % batchSize;

		return adressesOfBatches.get(batch) + indexInBatch * Integer.BYTES;
	}

	public void set(int index, int value) {
		long address = getAdressOfIndex(index);
		OffHeapMemoryAllocator.getUnsafe().putInt(address, value);
		lastIndex = Math.max(index, lastIndex);
	}

	/**
	 * This algo could be optimized to copy large range of bytes instead of value by value
	 *
	 * @param index
	 * @param value
	 */
	public void insertValueShiftingAllFollowingValues(int index, int value) {
		LongArrayList newAddressesOfBatches = new LongArrayList();

		for (int i = 0; i <= lastIndex; i++) {
			int v = get(i);
			long newAddress = readAddressOfIndex(newAddressesOfBatches, i < index ? i : (i + 1));
			OffHeapMemoryAllocator.getUnsafe().putInt(newAddress, v);
		}


		adressesOfBatches = newAddressesOfBatches;
		set(index, value);
		lastIndex++;
	}

	public int get(int index) {
		long address = getAdressOfIndex(index);
		return OffHeapMemoryAllocator.getInt(address);
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
			OffHeapMemoryAllocator.freeMemory(adressesOfBatches.get(i), batchSize * Integer.BYTES);
		}
		this.adressesOfBatches.clear();
	}
}
