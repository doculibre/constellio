package com.constellio.model.services.records.cache.dataStore;

public class RecordsCacheStat {

	String name;

	long heapSize;

	long offHeapSize;

	public RecordsCacheStat(String name, long heapSize, long offHeapSize) {
		this.name = name;
		this.heapSize = heapSize;
		this.offHeapSize = offHeapSize;
	}

	public String getName() {
		return name;
	}

	public long getHeapSize() {
		return heapSize;
	}

	public long getOffHeapSize() {
		return offHeapSize;
	}
}
