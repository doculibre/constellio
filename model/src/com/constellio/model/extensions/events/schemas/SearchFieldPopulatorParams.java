package com.constellio.model.extensions.events.schemas;

import java.util.Locale;

import com.constellio.model.entities.schemas.Metadata;

public class SearchFieldPopulatorParams {
	Metadata metadata;
	Object value;
	Locale locale;

	public SearchFieldPopulatorParams(Metadata metadata, Object value, Locale locale) {
		this.metadata = metadata;
		this.value = value;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public Object getValue() {
		return value;
	}

	public SearchFieldPopulatorParams setValue(Object value) {
		this.value = value;
		return this;
	}

	public Locale getLocale() {
		return locale;
	}
}
