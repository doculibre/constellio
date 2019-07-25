package com.constellio.model.services.records.cache.offHeapCollections;

public interface OffHeapBytesSupplier {

	byte getByte(int index);

	short getShort(int index);

	int getInt(int index);

	byte[] toArray();

	short length();
}
