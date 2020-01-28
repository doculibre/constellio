package com.constellio.model.services.records.cache.offHeapCollections;

import com.constellio.data.utils.dev.Toggle;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class OffHeapMemoryAllocator {

	static AtomicLong totalAllocatedMemory = new AtomicLong();
	static AtomicLong[] memoryAllocationByUsingClass = new AtomicLong[]{
			new AtomicLong(),
			new AtomicLong(),
			new AtomicLong(),
			new AtomicLong(),
			new AtomicLong(),
			new AtomicLong(),
			new AtomicLong()
	};

	/**
	 * Not only unsafe, but also not thread safe
	 */
	static Unsafe unsafe;

	static Map<Long, Long> allocatedMemory = new HashMap<>();

	public static final int OffHeapByteArrayList_ID = 0;
	public static final int OffHeapByteList_ID = 1;
	public static final int OffHeapIntList_ID = 2;
	public static final int OffHeapLongList_ID = 3;
	public static final int OffHeapShortList_ID = 4;
	public static final int SortedIntIdsList_ID = 5;
	public static final int SDK = 6;


	static synchronized long allocateMemory(int length, int classId) {
		totalAllocatedMemory.addAndGet(length);
		memoryAllocationByUsingClass[classId].addAndGet(length);
		Unsafe unsafe = getUnsafe();
		long adr = unsafe.allocateMemory(length);

		if (Toggle.OFF_HEAP_ADDRESS_VALIDATOR.isEnabled()) {
			synchronized (allocatedMemory) {
				allocatedMemory.put(adr, adr + length);
			}
		}

		return adr;

	}

	static synchronized void freeMemory(long address, long length, int classId) {
		totalAllocatedMemory.addAndGet(-1 * length);
		memoryAllocationByUsingClass[classId].addAndGet(-1 * length);
		Unsafe unsafe = getUnsafe();
		unsafe.freeMemory(address);
		if (Toggle.OFF_HEAP_ADDRESS_VALIDATOR.isEnabled()) {
			synchronized (allocatedMemory) {
				allocatedMemory.remove(address);
			}
		}
	}

	static void putByte(long address, byte value) {

		validateMemoryUsage(address, Byte.BYTES);
		getUnsafe().putByte(address, value);
	}

	static void putShort(long address, short value) {
		validateMemoryUsage(address, Short.BYTES);
		getUnsafe().putShort(address, value);
	}

	static void putInt(long address, int value) {
		validateMemoryUsage(address, Integer.BYTES);
		getUnsafe().putInt(address, value);
	}

	static void putLong(long address, long value) {
		validateMemoryUsage(address, Long.BYTES);
		getUnsafe().putLong(address, value);
	}

	static byte getByte(long address) {
		return getUnsafe().getByte(address);
	}

	static short getShort(long address) {
		return getUnsafe().getShort(address);
	}

	static int getInt(long address) {
		return getUnsafe().getInt(address);
	}

	static long getLong(long address) {
		return getUnsafe().getLong(address);
	}

	static void copyAdding(long address, long length, long addedBytesIndex,
						   long addedBytesLength) {

		if (addedBytesIndex < length && addedBytesLength > 0) {

			for (long i = length + addedBytesLength - 1; i > addedBytesIndex + addedBytesLength - 1; i--) {
				byte b = getUnsafe().getByte(address + i - addedBytesLength);
				putByte(address + i, b);
			}

		}

		for (long i = 0; i < addedBytesLength; i++) {
			putByte(address + addedBytesIndex + i, (byte) 0);
		}

	}

	static void copyAdding(long fromAddress, long toAddress, long length, long addedBytesIndex,
						   long addedBytesLength) {

		if (addedBytesIndex < length && addedBytesLength > 0) {


			copy(fromAddress + addedBytesIndex, toAddress + addedBytesIndex + addedBytesLength, length - addedBytesIndex);
			if (addedBytesIndex > 0) {
				copy(fromAddress, toAddress, addedBytesIndex);
			}

		} else {
			copy(fromAddress, toAddress, length);
		}

	}

	static void copyRemoving(long address, long length, long removedBytesIndex, long removedBytesLength) {

		if (length == removedBytesIndex + removedBytesLength) {
			for (int i = 0; i < removedBytesLength; i++) {
				putByte(address + removedBytesIndex + i, (byte) 0);
			}
		} else {
			copyRemoving(address, address, length, removedBytesIndex, removedBytesLength);
			for (int i = 0; i < removedBytesLength; i++) {
				putByte(address + length - removedBytesLength + i, (byte) 0);
			}
		}
	}

	static void copyRemoving(long fromAddress, long toAddress, long length, long removedBytesIndex,
							 long removedBytesLength) {

		if (removedBytesIndex < length && removedBytesLength > 0) {


			if (removedBytesIndex > 0) {
				copy(fromAddress, toAddress, removedBytesIndex);
			}

			copy(fromAddress + removedBytesIndex + removedBytesLength, toAddress + removedBytesIndex,
					length - removedBytesIndex - removedBytesLength);


		} else {
			copy(fromAddress, toAddress, length);
		}

	}

	static void copy(long fromAddress, long toAddress, long length) {
		//getUnsafe().copyMemory(fromAddress, toAddress, length);

		for (long i = 0; i < length; i++) {
			byte b = getUnsafe().getByte(fromAddress + i);
			putByte(toAddress + i, b);
		}
	}

	static Unsafe getUnsafe() {
		if (unsafe == null) {
			try {
				Field f = Unsafe.class.getDeclaredField("theUnsafe");
				f.setAccessible(true);
				unsafe = (Unsafe) f.get(null);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}
		return unsafe;
	}

	public static long getAllocatedMemory() {
		return totalAllocatedMemory.get();
	}

	public static long getAllocatedMemory(int classId) {
		return memoryAllocationByUsingClass[classId].get();
	}

	private static void validateMemoryUsage(long address, int size) {
		if (Toggle.OFF_HEAP_ADDRESS_VALIDATOR.isEnabled()) {
			synchronized (allocatedMemory) {
				boolean found = false;

				long toAddressExclusive = address + size;

				for (Map.Entry<Long, Long> rangeEntry : allocatedMemory.entrySet()) {
					if (rangeEntry.getKey().longValue() <= address && rangeEntry.getValue().longValue() > address &&
						rangeEntry.getKey().longValue() < toAddressExclusive && rangeEntry.getValue().longValue() >= toAddressExclusive) {
						found = true;
						break;
					}

				}
				if (!found) {
					throw new IllegalArgumentException("Writing at an invalid memory address");
				}
			}
		}
	}
}
