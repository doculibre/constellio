package com.constellio.app.extensions.sequence;

import java.util.Map;

import com.constellio.model.entities.Language;

public class AvailableSequence {

	String code;

	Map<Language, String> titles;

	public AvailableSequence(String code, Map<Language, String> titles) {
		this.code = code;
		this.titles = titles;
	}

	public String getCode() {
		return code;
	}

	public Map<Language, String> getTitles() {
		return titles;
	}
}
