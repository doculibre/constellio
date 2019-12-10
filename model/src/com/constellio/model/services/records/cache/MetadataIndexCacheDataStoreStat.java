package com.constellio.model.services.records.cache;

public class MetadataIndexCacheDataStoreStat {

	String name;

	int keysCount;

	int valuesCount;

	long keysHeapLength;

	long valuesHeapLength;

	long valuesOffHeapLength;

	long estimatedMapHeapLength;

	public MetadataIndexCacheDataStoreStat(String name, int keysCount, int valuesCount, long keysHeapLength,
										   long valuesHeapLength, long valuesOffHeapLength,
										   long estimatedMapHeapLength) {
		this.name = name;
		this.keysCount = keysCount;
		this.valuesCount = valuesCount;
		this.keysHeapLength = keysHeapLength;
		this.valuesHeapLength = valuesHeapLength;
		this.valuesOffHeapLength = valuesOffHeapLength;
		this.estimatedMapHeapLength = estimatedMapHeapLength;
	}

	public String getName() {
		return name;
	}

	public int getKeysCount() {
		return keysCount;
	}

	public int getValuesCount() {
		return valuesCount;
	}

	public long getKeysHeapLength() {
		return keysHeapLength;
	}

	public long getValuesHeapLength() {
		return valuesHeapLength;
	}

	public long getValuesOffHeapLength() {
		return valuesOffHeapLength;
	}

	public long getEstimatedMapHeapLength() {
		return estimatedMapHeapLength;
	}

	public long length() {
		return keysHeapLength + valuesHeapLength + valuesOffHeapLength + estimatedMapHeapLength;
	}

	public long getTotalHeap() {
		return this.keysHeapLength + this.valuesHeapLength + this.estimatedMapHeapLength;

	}
}
