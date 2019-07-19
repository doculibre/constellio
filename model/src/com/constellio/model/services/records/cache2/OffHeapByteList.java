package com.constellio.model.services.records.cache2;

import com.constellio.data.utils.LazyIterator;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;

import java.util.stream.Stream;

public class OffHeapByteList {

	private static int batchSize = 10000;

	private LongArrayList adressesOfBatches = new LongArrayList();

	private int lastIndex = -1;

	private long getAdressOfIndex(int index) {
		return readAddressOfIndex(adressesOfBatches, index);
	}

	private static long readAddressOfIndex(LongArrayList adressesOfBatches, int index) {

		int batch = index / batchSize;
		while (adressesOfBatches.size() < batch + 1) {
			long address = OffHeapMemoryAllocator.allocateMemory(batchSize);

			for (int i = 0; i < batchSize; i++) {
				OffHeapMemoryAllocator.putByte(address + i, (byte) 0);
			}

			adressesOfBatches.add(address);
		}

		int indexInBatch = index % batchSize;

		return adressesOfBatches.get(batch) + indexInBatch;
	}

	public void set(int index, byte value) {

		long address = getAdressOfIndex(index);
		OffHeapMemoryAllocator.putByte(address, value);
		lastIndex = Math.max(index, lastIndex);
	}


	/**
	 * This algo could be optimized to copy large range of bytes instead of value by value
	 *
	 * @param index
	 * @param value
	 */
	public void insertValueShiftingAllFollowingValues(int index, byte value) {
		LongArrayList newAddressesOfBatches = new LongArrayList();

		for (int i = 0; i <= lastIndex; i++) {
			byte v = get(i);
			long newAddress = readAddressOfIndex(newAddressesOfBatches, i < index ? i : (i + 1));
			OffHeapMemoryAllocator.getUnsafe().putByte(newAddress, v);
		}

		adressesOfBatches = newAddressesOfBatches;
		set(index, value);
		lastIndex++;
	}

	public byte get(int index) {
		long address = getAdressOfIndex(index);
		return OffHeapMemoryAllocator.getByte(address);
	}

	public int size() {
		return lastIndex + 1;
	}

	public byte getLast() {
		return lastIndex == -1 ? -1 : get(lastIndex);
	}

	public void clear() {
		for (int i = 0; i < this.adressesOfBatches.size(); i++) {
			OffHeapMemoryAllocator.freeMemory(adressesOfBatches.get(i), batchSize);
		}
		this.adressesOfBatches.clear();
	}

	public Stream<Byte> stream() {
		return new LazyIterator<Byte>() {
			int index = 0;

			@Override
			protected Byte getNextOrNull() {
				if (index <= lastIndex) {
					return get(index++);
				} else {
					return null;
				}
			}
		}.stream();
	}
}
