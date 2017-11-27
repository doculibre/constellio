package com.constellio.model.entities.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum MemoryConsumptionLevel implements EnumWithSmallCode {

	LEAST_MEMORY_CONSUMPTION("MM"),
	LESS_MEMORY_CONSUMPTION("M"),
	NORMAL("N"),
	BETTER_PERFORMANCE("P"),
	BEST_PERFORMANCE("PP");

	private final String code;

	MemoryConsumptionLevel(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public boolean isPrioritizingMemoryConsumption() {
		return this == LEAST_MEMORY_CONSUMPTION || this == LESS_MEMORY_CONSUMPTION;
	}

	public boolean isPrioritizingMemoryConsumptionOrNormal() {
		return this == LEAST_MEMORY_CONSUMPTION || this == LESS_MEMORY_CONSUMPTION || this == NORMAL;
	}

	public boolean isPrioritizingPerformance() {
		return this == BETTER_PERFORMANCE || this == BEST_PERFORMANCE;
	}

	public boolean isPrioritizingPerformanceOrNormal() {
		return this == BETTER_PERFORMANCE || this == BEST_PERFORMANCE || this == NORMAL;
	}
}
