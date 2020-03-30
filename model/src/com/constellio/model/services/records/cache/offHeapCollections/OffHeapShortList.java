package com.constellio.model.services.records.cache.offHeapCollections;

import com.constellio.data.utils.LazyIterator;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.OffHeapShortList_ID;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.allocateMemory;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.freeMemory;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.getShort;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.putShort;

public class OffHeapShortList {

	private static final Logger LOGGER = LoggerFactory.getLogger(OffHeapShortList.class);

	private static int batchSize = 1000;

	private LongArrayList adressesOfBatches = new LongArrayList();

	private int lastIndex = -1;

	private long getAdressOfIndex(int index) {
		return readAddressOfIndex(adressesOfBatches, index);
	}

	private static long readAddressOfIndex(LongArrayList adressesOfBatches, int index) {

		int batch = index / batchSize;
		while (adressesOfBatches.size() < batch + 1) {
			long address = allocateMemory(batchSize * Short.BYTES, OffHeapShortList_ID);

			for (int i = 0; i < batchSize; i++) {
				putShort(address + i * Short.BYTES, (short) 0);
			}

			adressesOfBatches.add(address);
		}

		int indexInBatch = index % batchSize;

		return adressesOfBatches.get(batch) + indexInBatch * Short.BYTES;
	}

	public int getHeapConsumption() {
		return 12 + (12 + Long.BYTES * adressesOfBatches.size()) + Integer.BYTES;
	}

	public int getOffHeapConsumption() {
		return adressesOfBatches.size() * batchSize * Short.BYTES;
	}

	public void set(int index, short value) {
		long address = getAdressOfIndex(index);
		putShort(address, value);
		lastIndex = Math.max(index, lastIndex);
	}


	/**
	 * This algo could be optimized to copy large range of bytes instead of value by value
	 *
	 * @param index
	 * @param value
	 */
	public void insertValueShiftingAllFollowingValues(int index, short value) {
		LOGGER.warn("insertValueShiftingAllFollowingValues : this should not happen and could consume a lot of memory");
		LongArrayList newAddressesOfBatches = new LongArrayList();

		for (int i = 0; i <= lastIndex; i++) {
			short v = get(i);
			long newAddress = readAddressOfIndex(newAddressesOfBatches, i < index ? i : (i + 1));
			OffHeapMemoryAllocator.putShort(newAddress, v);
		}
		clear();
		adressesOfBatches = newAddressesOfBatches;
		set(index, value);
		lastIndex++;
		LOGGER.warn("insertValueShiftingAllFollowingValues : finished");
	}

	public short get(int index) {
		long address = getAdressOfIndex(index);
		return getShort(address);
	}

	public int size() {
		return lastIndex + 1;
	}

	public short getLast() {
		return lastIndex == -1 ? -1 : get(lastIndex);
	}

	public void clear() {
		for (int i = 0; i < this.adressesOfBatches.size(); i++) {
			freeMemory(adressesOfBatches.get(i), batchSize * Short.BYTES, OffHeapShortList_ID);
		}
		this.adressesOfBatches.clear();
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
