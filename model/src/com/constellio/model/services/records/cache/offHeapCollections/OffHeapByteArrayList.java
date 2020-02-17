package com.constellio.model.services.records.cache.offHeapCollections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.OffHeapByteArrayList_ID;
import static com.constellio.model.services.records.cache.offHeapCollections.OffHeapMemoryAllocator.allocateMemory;

public class OffHeapByteArrayList {

	private static final Logger LOGGER = LoggerFactory.getLogger(OffHeapByteArrayList.class);

	private OffHeapShortList byteArraySizes = new OffHeapShortList();
	private OffHeapLongList byteArrayMemoryAdresses = new OffHeapLongList();

	public OffHeapByteArrayList() {
	}

	public int getHeapConsumption() {
		return byteArraySizes.getHeapConsumption() + byteArrayMemoryAdresses.getHeapConsumption();
	}

	public long getOffHeapConsumption() {
		AtomicLong offHeap = new AtomicLong(
				byteArraySizes.getOffHeapConsumption() + byteArrayMemoryAdresses.getOffHeapConsumption());

		byteArraySizes.stream().filter((s) -> s > 0).forEach((s) -> {
			offHeap.addAndGet(s);
		});

		return offHeap.get();
	}

	public void set(int index, byte[] value) {
		long previousAddress = byteArrayMemoryAdresses.get(index);
		long previousLength = byteArraySizes.get(index);

		try {
			if (value != null) {
				synchronized (this) {
					long address = allocateMemory(value.length, OffHeapByteArrayList_ID);
					if (address == 0) {
						address = -1;
					}


					byteArrayMemoryAdresses.set(index, address);
					byteArraySizes.set(index, (short) value.length);
					if (address != -1) {
						for (int i = 0; i < value.length; i++) {
							OffHeapMemoryAllocator.putByte(address + i, value[i]);
						}
					}
				}
			} else {
				byteArrayMemoryAdresses.set(index, 0);
				byteArraySizes.set(index, (short) -1);
			}

		} finally {
			if (previousAddress != 0 && previousAddress != -1) {
				synchronized (this) {
					OffHeapMemoryAllocator.freeMemory(previousAddress, previousLength, OffHeapByteArrayList_ID);
				}
			}
		}

	}

	public byte[] get(int index) {
		OffHeapBytesSupplier supplier = getSupplier(index);
		return supplier == null ? null : supplier.toArray();
	}

	/**
	 * This algo could be optimized to copy large range of bytes instead of value by value
	 *
	 * @param index
	 * @param value
	 */
	public void insertValueShiftingAllFollowingValues(int index, byte[] value) {
		LOGGER.warn("insertValueShiftingAllFollowingValues : this should not happen and could consume a lot of memory");
		byteArraySizes.insertValueShiftingAllFollowingValues(index, (short) 0);
		byteArrayMemoryAdresses.insertValueShiftingAllFollowingValues(index, 0L);
		set(index, value);
		LOGGER.warn("insertValueShiftingAllFollowingValues : finished");
	}

	public void clear() {
		for (int i = 0; i < this.byteArraySizes.size(); i++) {
			long address = this.byteArrayMemoryAdresses.get(i);
			long size = this.byteArraySizes.get(i);
			if (address > 0 && size > 0) {
				OffHeapMemoryAllocator.freeMemory(address, size, OffHeapByteArrayList_ID);
			}
		}
		this.byteArrayMemoryAdresses.clear();
		this.byteArraySizes.clear();
	}

	private OffHeapBytesSupplier getSupplier(int index) {

		long address = byteArrayMemoryAdresses.get(index);
		short size = byteArraySizes.get(index);

		if (address == 0) {
			return null;
		}

		return new OffHeapBytesSupplier() {

			@Override
			public byte getByte(int index) {
				return OffHeapMemoryAllocator.getByte(address + index);
			}

			@Override
			public int getInt(int index) {
				return OffHeapMemoryAllocator.getInt(address + index);
			}

			@Override
			public byte[] toArray() {
				byte[] array = new byte[size];

				for (int i = 0; i < size; i++) {
					array[i] = getByte(i);
				}

				return array;
			}

			@Override
			public short length() {
				return size;
			}
		};


	}

	public int size() {
		return byteArrayMemoryAdresses.size();
	}

}
