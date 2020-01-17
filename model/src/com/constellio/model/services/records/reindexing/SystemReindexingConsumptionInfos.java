package com.constellio.model.services.records.reindexing;

import java.util.ArrayList;
import java.util.List;

public class SystemReindexingConsumptionInfos {

	private List<SystemReindexingConsumptionHeapInfo> heapInfos = new ArrayList<>();

	public List<SystemReindexingConsumptionHeapInfo> getHeapInfos() {
		return heapInfos;
	}

	public static class SystemReindexingConsumptionHeapInfo {

		String name;

		long consumedMemory;

		public SystemReindexingConsumptionHeapInfo(String name, long consumedMemory) {
			this.name = name;
			this.consumedMemory = consumedMemory;
		}

		public String getName() {
			return name;
		}

		public long getConsumedMemory() {
			return consumedMemory;
		}
	}

}
