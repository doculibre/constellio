package com.constellio.app.ui.framework.builders;

import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataSchemaType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("serial")
public class MetadataSchemaTypeToVOBuilder implements Serializable {

	public MetadataSchemaTypeVO build(MetadataSchemaType type) {
		Map<Locale, String> labels = new HashMap<>();

		for (Language currentLanguage : type.getLabels().keySet()) {
			labels.put(Locale.forLanguageTag(currentLanguage.getCode()), type.getLabel(currentLanguage));
		}

		return new MetadataSchemaTypeVO(type.getCode(), type.getLabels());
	}

	public MetadataSchemaTypeVO build(MetadataSchemaType type, SessionContext sessionContext) {
		Map<Locale, String> labels = new HashMap<>();
		labels.put(sessionContext.getCurrentLocale(),
				type.getLabel(Language.withCode(sessionContext.getCurrentLocale().getLanguage())));
		return new MetadataSchemaTypeVO(type.getCode(), type.getLabels());
	}
}
