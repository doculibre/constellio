package com.constellio.model.entities.enums;

public enum AutocompleteSplitCriteria {
	SPACE(" "),
	SPACE_AND_UNDERSCORE("[\\s+_]"),
	SPACE_AND_COMMA("[\\s+,]"),
	SPACE_AND_COMMA_AND_UNDERSCORE("[\\s+,_]"),
	SPACE_AND_APOSTROPHE("[\\s+'’]"),
	SPACE_AND_APOSTROPHE_AND_UNDERSCORE("[\\s+'’_]"),
	SPACE_AND_COMMA_AND_UNDERSCORE_APOSTROPHE("[\\s+,_'’]");

	public final String regex;

	AutocompleteSplitCriteria(String regex) {
		this.regex = regex;
	}
}
