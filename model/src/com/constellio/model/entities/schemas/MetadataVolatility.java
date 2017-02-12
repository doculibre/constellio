package com.constellio.model.entities.schemas;

import com.constellio.model.entities.EnumWithSmallCode;

public enum MetadataVolatility implements EnumWithSmallCode {

	/**
	 * The metadata is persisted, is available with search conditions and is only computed once. This is the default behavior.
	 */
	PERSISTED("P"),

	/**
	 * The metadata is not persisted, and will not be loaded when the record is retrieved from the datastore.
	 * The value is loaded when calling com.constellio.model.services.records.RecordServices.recalculate
	 * For sake of uniformity, the metadata is not kept in caches
	 */
	VOLATILE_LAZY("L"),

	/**
	 * The metadata is not persisted, and will be loaded when the record is retrieved from the datastore.
	 * For sake of uniformity, the metadata is kept in caches.
	 *
	 */
	VOLATILE_EAGER("E");

	private String code;

	MetadataVolatility(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
