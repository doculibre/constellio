package com.constellio.app.ui.entities;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.model.entities.Language;

import java.io.Serializable;
import java.util.Map;

public class MetadataSchemaTypeVO implements Serializable {

	private final String code;
	private final Map<Language, String> labels;

	public MetadataSchemaTypeVO(String code, Map<Language, String> labels) {
		this.code = code;
		this.labels = labels;
	}

	public String getCode() {
		return code;
	}

	public String getLabel(Language locale) {
		return labels.get(locale);
	}

	public Map<Language, String> getLabels() {
		return labels;
	}

	public String getLabel() {
		return getLabel(Language.withCode(ConstellioUI.getCurrentSessionContext().getCurrentLocale().getLanguage()));
	}
}
