package com.constellio.app.entities.schemasDisplay.enums;

import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.ArrayList;
import java.util.List;

public enum MetadataInputType {

	FIELD,
	RADIO_BUTTONS,
	DROPDOWN,
	CHECKBOXES,
	TEXTAREA,
	RICHTEXT,
	LOOKUP,
	HIDDEN,
	CUSTOM,
	CONTENT,
	CONTENT_CHECK_IN_CHECK_OUT,
	URL,
	PASSWORD;

	public boolean isMaxLenghtSupportedOnInputType() {
		return this != RICHTEXT;
	}

	public static List<MetadataInputType> getAvailableMetadataInputTypesFor(MetadataValueType type,
																			boolean multivalue) {
		List<MetadataInputType> inputTypes = new ArrayList<>();

		switch (type) {
			case BOOLEAN:
				break;
			case STRUCTURE:
				break;
			case ENUM:
				if (multivalue) {
					inputTypes.add(CHECKBOXES);
					inputTypes.add(DROPDOWN);
				} else {
					inputTypes.add(RADIO_BUTTONS);
					inputTypes.add(DROPDOWN);
				}
				break;
			case TEXT:
				inputTypes.add(TEXTAREA);
				inputTypes.add(RICHTEXT);
				break;
			case CONTENT:
				inputTypes.add(CONTENT);
				inputTypes.add(CONTENT_CHECK_IN_CHECK_OUT);
				break;
			case DATE:
				break;
			case DATE_TIME:
				break;
			case INTEGER:
				break;
			case REFERENCE:
				inputTypes.add(LOOKUP);
				inputTypes.add(DROPDOWN);
				if (multivalue) {
					inputTypes.add(CHECKBOXES);
				} else {
					inputTypes.add(RADIO_BUTTONS);
				}
				break;
			case STRING:
				inputTypes.add(FIELD);
				inputTypes.add(URL);
				inputTypes.add(PASSWORD);
				break;
			case NUMBER:
				break;
		}

		return inputTypes;
	}

	public static MetadataInputType getDefaultInputTypeFor(MetadataValueType type, boolean multivalue) {
		switch (type) {
			case BOOLEAN:
				break;
			case STRUCTURE:
				break;
			case ENUM:
				return DROPDOWN;
			case TEXT:
				return TEXTAREA;
			case CONTENT:
				return CONTENT;
			case DATE:
				break;
			case DATE_TIME:
				break;
			case INTEGER:
				break;
			case REFERENCE:
				return LOOKUP;
			case STRING:
				return FIELD;
			case NUMBER:
				break;
		}
		return null;
	}

	public static String getCaptionFor(MetadataInputType type) {
		String caption = "";

		switch (type) {
			case FIELD:
				caption = "MetadataInputType.field";
				break;
			case RADIO_BUTTONS:
				caption = "MetadataInputType.radio";
				break;
			case DROPDOWN:
				caption = "MetadataInputType.dropdown";
				break;
			case CHECKBOXES:
				caption = "MetadataInputType.checkboxe";
				break;
			case TEXTAREA:
				caption = "MetadataInputType.textarea";
				break;
			case RICHTEXT:
				caption = "MetadataInputType.richtxt";
				break;
			case LOOKUP:
				caption = "MetadataInputType.lookup";
				break;
			case HIDDEN:
				caption = "MetadataInputType.hidden";
				break;
			case CUSTOM:
				caption = "MetadataInputType.custom";
				break;
			case CONTENT:
				caption = "MetadataInputType.content";
				break;
			case CONTENT_CHECK_IN_CHECK_OUT:
				caption = "MetadataInputType.contentcheck";
				break;
			case URL:
				caption = "MetadataInputType.url";
				break;
			case PASSWORD:
				caption = "MetadataInputType.password";
				break;
		}

		return caption;
	}

}
