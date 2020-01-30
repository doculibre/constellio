package com.constellio.app.entities.schemasDisplay.enums;

import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.ArrayList;
import java.util.List;

public enum MetadataSortingType implements EnumWithSmallCode {

	ENTRY_ORDER("EO"),
	ALPHANUMERICAL_ORDER("AO");

	private String code;

	MetadataSortingType(String code) {
		this.code = code;
	}

	public static String getCaptionFor(MetadataSortingType type) {
		String caption = "";

		switch (type) {
			case ENTRY_ORDER:
				caption = "MetadataSortingType.entryOrder";
				break;
			case ALPHANUMERICAL_ORDER:
				caption = "MetadataSortingType.alphanumericalOrder";
				break;
		}

		return caption;
	}

	public static List<MetadataSortingType> getAvailableMetadataSortingTypesFor(MetadataValueType type,
																				boolean isMultivalue) {
		List<MetadataSortingType> displayTypes = new ArrayList<>();


		if (type != null && type.equals(MetadataValueType.REFERENCE) && isMultivalue) {

			displayTypes.add(ALPHANUMERICAL_ORDER);
		}

		displayTypes.add(ENTRY_ORDER);

		return displayTypes;
	}

	@Override
	public String getCode() {
		return code;
	}
}