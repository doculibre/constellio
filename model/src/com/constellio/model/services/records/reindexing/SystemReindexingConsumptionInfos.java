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

		long value;

		boolean memory;

		public SystemReindexingConsumptionHeapInfo(String name, long value, boolean memory) {
			this.name = name;
			this.value = value;
			this.memory = memory;
		}

		public String getName() {
			return name;
		}

		public long getValue() {
			return value;
		}

		public boolean isMemory() {
			return memory;
		}
	}

}
