package com.constellio.data.utils;

public class CacheStat {

	String name;

	long heapSize;

	long offHeapSize;

	public CacheStat(String name, long heapSize, long offHeapSize) {
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
