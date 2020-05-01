package com.constellio.model.services.records.cache.offHeapCollections;

import com.constellio.data.utils.LazyIterator;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.OffHeapByteList_ID;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.allocateMemory;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.freeMemory;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.getByte;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.putByte;

public class OffHeapByteList {

	private static final Logger LOGGER = LoggerFactory.getLogger(OffHeapByteList.class);

	private static int batchSize = 10000;

	private LongArrayList adressesOfBatches = new LongArrayList();

	private int lastIndex = -1;

	private long getAdressOfIndex(int index) {
		return readAddressOfIndex(adressesOfBatches, index);
	}

	private static long readAddressOfIndex(LongArrayList adressesOfBatches, int index) {

		int batch = index / batchSize;
		while (adressesOfBatches.size() < batch + 1) {
			long address = allocateMemory(batchSize, OffHeapByteList_ID);

			for (int i = 0; i < batchSize; i++) {
				putByte(address + i, (byte) 0);
			}

			adressesOfBatches.add(address);
		}

		int indexInBatch = index % batchSize;

		return adressesOfBatches.get(batch) + indexInBatch;
	}

	public int getHeapConsumption() {
		return 12 + (12 + Long.BYTES * adressesOfBatches.size()) + Integer.BYTES;
	}

	public int getOffHeapConsumption() {
		return adressesOfBatches.size() * batchSize;
	}

	public void set(int index, byte value) {
		if (index < 0) {
			throw new IllegalArgumentException("index must be >=0");
		}
		long address = getAdressOfIndex(index);
		putByte(address, value);
		lastIndex = Math.max(index, lastIndex);
	}


	/**
	 * This algo could be optimized to copy large range of bytes instead of value by value
	 *
	 * @param index
	 * @param value
	 */
	public void insertValueShiftingAllFollowingValues(int index, byte value) {
		LOGGER.warn("insertValueShiftingAllFollowingValues : this should not happen and could consume a lot of memory");
		LongArrayList newAddressesOfBatches = new LongArrayList();

		for (int i = 0; i <= lastIndex; i++) {
			byte v = get(i);
			long newAddress = readAddressOfIndex(newAddressesOfBatches, i < index ? i : (i + 1));
			OffHeapMemoryAllocator.putByte(newAddress, v);
		}
		clear();
		adressesOfBatches = newAddressesOfBatches;
		set(index, value);
		lastIndex++;
	}

	public byte get(int index) {
		if (index < 0) {
			throw new IllegalArgumentException("index must be >=0");
		}
		long address = getAdressOfIndex(index);
		return getByte(address);
	}

	public int size() {
		return lastIndex + 1;
	}

	public byte getLast() {
		return lastIndex == -1 ? -1 : get(lastIndex);
	}

	public void clear() {
		for (int i = 0; i < this.adressesOfBatches.size(); i++) {
			freeMemory(adressesOfBatches.get(i), batchSize, OffHeapByteList_ID);
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
