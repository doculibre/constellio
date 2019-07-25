package com.constellio.model.entities.schemas;

import com.constellio.model.entities.EnumWithSmallCode;

public enum RecordCacheType implements EnumWithSmallCode {


	NOT_CACHED("N"), SUMMARY_CACHED_WITHOUT_VOLATILE("X"), SUMMARY_CACHED_WITH_VOLATILE("S"), FULLY_CACHED("F"), HOOK_ONLY("H");

	String code;

	RecordCacheType(String code) {
		this.code = code;
	}

	public boolean isSummaryCache() {
		return this == SUMMARY_CACHED_WITH_VOLATILE || this == SUMMARY_CACHED_WITHOUT_VOLATILE;
	}

	public boolean hasPermanentCache() {
		return this == SUMMARY_CACHED_WITH_VOLATILE || this == SUMMARY_CACHED_WITHOUT_VOLATILE || this == FULLY_CACHED;
	}

	public boolean hasVolatileCache() {
		return this == SUMMARY_CACHED_WITH_VOLATILE;
	}

	@Override
	public String getCode() {
		return code;
	}
}
