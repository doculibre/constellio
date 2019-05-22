package com.constellio.model.services.records.cache2;

import com.constellio.data.utils.LazyIterator;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;

import java.util.stream.Stream;

public class OffHeapShortList {

	private static int batchSize = 1000;

	private LongArrayList adressesOfBatches = new LongArrayList();

	private int lastIndex = -1;

	private long getAdressOfIndex(int index) {
		return readAddressOfIndex(adressesOfBatches, index);
	}

	private static long readAddressOfIndex(LongArrayList adressesOfBatches, int index) {

		int batch = index / batchSize;
		while (adressesOfBatches.size() < batch + 1) {
			long address = OffHeapMemoryAllocator.allocateMemory(batchSize * Short.BYTES);

			for (int i = 0; i < batchSize; i++) {
				OffHeapMemoryAllocator.putShort(address + i * Short.BYTES, (short) 0);
			}

			adressesOfBatches.add(address);
		}

		int indexInBatch = index % batchSize;

		return adressesOfBatches.get(batch) + indexInBatch * Short.BYTES;
	}

	public void set(int index, short value) {
		lastIndex = Math.max(index, lastIndex);
		long address = getAdressOfIndex(index);
		OffHeapMemoryAllocator.putShort(address, value);
	}


	/**
	 * This algo could be optimized to copy large range of bytes instead of value by value
	 *
	 * @param index
	 * @param value
	 */
	public void insertValueShiftingAllFollowingValues(int index, short value) {
		LongArrayList newAddressesOfBatches = new LongArrayList();

		for (int i = 0; i <= lastIndex; i++) {
			short v = get(i);
			long newAddress = readAddressOfIndex(newAddressesOfBatches, i < index ? i : (i + 1));
			OffHeapMemoryAllocator.getUnsafe().putShort(newAddress, v);
		}

		lastIndex++;
		adressesOfBatches = newAddressesOfBatches;
		set(index, value);
	}

	public short get(int index) {
		long address = getAdressOfIndex(index);
		return OffHeapMemoryAllocator.getShort(address);
	}

	public int size() {
		return lastIndex + 1;
	}

	public short getLast() {
		return lastIndex == -1 ? -1 : get(lastIndex);
	}

	public Stream<Short> stream() {
		return new LazyIterator<Short>() {
			int index = 0;

			@Override
			protected Short getNextOrNull() {
				if (index <= lastIndex) {
					return get(index++);
				} else {
					return null;
				}
			}
		}.stream();
	}
}
