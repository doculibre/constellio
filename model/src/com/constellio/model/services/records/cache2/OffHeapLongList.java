package com.constellio.model.services.records.cache2;

import com.constellio.data.utils.LazyIterator;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;

import java.util.stream.Stream;

public class OffHeapLongList {

	private static int batchSize = 1000;

	private LongArrayList adressesOfBatches = new LongArrayList();

	private int lastIndex = -1;

	private long getAdressOfIndex(int index) {
		return readAddressOfIndex(adressesOfBatches, index);
	}

	private static long readAddressOfIndex(LongArrayList adressesOfBatches, int index) {
		int batch = index / batchSize;
		while (adressesOfBatches.size() < batch + 1) {
			long address = OffHeapMemoryAllocator.allocateMemory(batchSize * Long.BYTES);

			for (int i = 0; i < batchSize; i++) {
				OffHeapMemoryAllocator.putLong(address + i * Long.BYTES, 0);
			}

			adressesOfBatches.add(address);
		}

		int indexInBatch = index % batchSize;

		return adressesOfBatches.get(batch) + indexInBatch * Long.BYTES;
	}

	public void set(int index, long value) {

		long address = getAdressOfIndex(index);
		OffHeapMemoryAllocator.getUnsafe().putLong(address, value);
		lastIndex = Math.max(index, lastIndex);
	}


	/**
	 * This algo could be optimized to copy large range of bytes instead of value by value
	 *
	 * @param index
	 * @param value
	 */
	public void insertValueShiftingAllFollowingValues(int index, long value) {
		LongArrayList newAddressesOfBatches = new LongArrayList();

		for (int i = 0; i <= lastIndex; i++) {
			long v = get(i);
			long newAddress = readAddressOfIndex(newAddressesOfBatches, i < index ? i : (i + 1));
			OffHeapMemoryAllocator.getUnsafe().putLong(newAddress, v);
		}

		adressesOfBatches = newAddressesOfBatches;
		set(index, value);
		lastIndex++;
	}

	public long get(int index) {
		long address = getAdressOfIndex(index);
		return OffHeapMemoryAllocator.getLong(address);
	}

	public int size() {
		return lastIndex + 1;
	}

	public long getLast() {
		return lastIndex == -1 ? -1 : get(lastIndex);
	}

	public void clear() {
		for (int i = 0; i < this.adressesOfBatches.size(); i++) {
			OffHeapMemoryAllocator.freeMemory(adressesOfBatches.get(i), batchSize * Long.BYTES);
		}
		this.adressesOfBatches.clear();
	}

	public Stream<Long> stream() {
		return new LazyIterator<Long>() {
			int index = 0;

			@Override
			protected Long getNextOrNull() {
				if (index <= lastIndex) {
					return get(index++);
				} else {
					return null;
				}
			}
		}.stream();
	}
}
