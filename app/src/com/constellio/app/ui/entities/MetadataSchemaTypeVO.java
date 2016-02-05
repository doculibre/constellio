package com.constellio.app.ui.entities;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

import com.constellio.app.ui.application.ConstellioUI;

public class MetadataSchemaTypeVO implements Serializable {

	private final String code;
	private final Map<Locale, String> labels;

	public MetadataSchemaTypeVO(String code, Map<Locale, String> labels) {
		this.code = code;
		this.labels = labels;
	}

	public String getCode() {
		return code;
	}

	public String getLabel(Locale locale) {
		return labels.get(locale);
	}

	public String getLabel() {
		return getLabel(ConstellioUI.getCurrentSessionContext().getCurrentLocale());
	}
}
