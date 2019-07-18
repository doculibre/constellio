package com.constellio.model.services.records.cache2;

public interface OffHeapBytesSupplier {

	byte getByte(int index);

	short getShort(int index);

	int getInt(int index);

	byte[] toArray();

	short length();
}
