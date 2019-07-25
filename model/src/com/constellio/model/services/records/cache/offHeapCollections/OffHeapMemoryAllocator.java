package com.constellio.model.services.records.cache.offHeapCollections;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;

public class OffHeapMemoryAllocator {

	static AtomicLong totalAllocatedMemory = new AtomicLong();

	static Unsafe unsafe;

	static long allocateMemory(int length) {
		totalAllocatedMemory.addAndGet(length);
		return getUnsafe().allocateMemory(length);
	}

	static void freeMemory(long address, long length) {
		totalAllocatedMemory.addAndGet(-1 * length);
		getUnsafe().freeMemory(address);
	}

	static void putByte(long address, byte value) {
		getUnsafe().putByte(address, value);
	}

	static void putShort(long address, short value) {
		getUnsafe().putShort(address, value);
	}

	static void putInt(long address, int value) {
		getUnsafe().putInt(address, value);
	}

	static void putLong(long address, long value) {
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
}
