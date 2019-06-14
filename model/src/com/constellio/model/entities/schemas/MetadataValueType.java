package com.constellio.model.entities.schemas;

public enum MetadataValueType {

	//TODO Rename NUMBER to FLOATING_POINT
	DATE, DATE_TIME, STRING, TEXT, INTEGER, NUMBER, BOOLEAN, REFERENCE, CONTENT, STRUCTURE, ENUM;

	public boolean isStringOrText() {
		return this == TEXT || this == STRING;
	}

	public boolean isDateOrDateTime() {
		return this == DATE || this == DATE_TIME;
	}

	public boolean isStructureOrContent() {
		return this == STRUCTURE || this == CONTENT;
	}

	public boolean isIntegerOrFloatingPoint() {
		return this == INTEGER || this == NUMBER;
	}

	public static String getCaptionFor(MetadataValueType type) {
		String caption = "";

		switch (type) {
			case DATE:
				caption = "MetadataValueType.date";
				break;
			case DATE_TIME:
				caption = "MetadataValueType.datetime";
				break;
			case STRING:
				caption = "MetadataValueType.string";
				break;
			case TEXT:
				caption = "MetadataValueType.text";
				break;
			case INTEGER:
				caption = "MetadataValueType.integer";
				break;
			case NUMBER:
				caption = "MetadataValueType.number";
				break;
			case BOOLEAN:
				caption = "MetadataValueType.boolean";
				break;
			case REFERENCE:
				caption = "MetadataValueType.reference";
				break;
			case CONTENT:
				caption = "MetadataValueType.content";
				break;
			case STRUCTURE:
				caption = "MetadataValueType.structure";
				break;
			case ENUM:
				caption = "MetadataValueType.enum";
				break;
		}

		return caption;
	}
}
