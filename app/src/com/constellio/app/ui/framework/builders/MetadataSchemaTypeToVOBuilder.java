package com.constellio.app.ui.framework.builders;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataSchemaType;

@SuppressWarnings("serial")
public class MetadataSchemaTypeToVOBuilder implements Serializable {

	public MetadataSchemaTypeVO build(MetadataSchemaType type) {
		Map<Locale, String> labels = new HashMap<>();
		Locale locale = ConstellioUI.getCurrentSessionContext().getCurrentLocale();
		Language language = Language.withCode(locale.getLanguage());
		labels.put(ConstellioUI.getCurrentSessionContext().getCurrentLocale(), type.getLabel(language));
		return new MetadataSchemaTypeVO(type.getCode(), labels);
	}

	public MetadataSchemaTypeVO build(MetadataSchemaType type, SessionContext sessionContext) {
		Map<Locale, String> labels = new HashMap<>();
		labels.put(sessionContext.getCurrentLocale(),
				type.getLabel(Language.withCode(sessionContext.getCurrentLocale().getLanguage())));
		return new MetadataSchemaTypeVO(type.getCode(), labels);
	}
}
