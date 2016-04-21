package com.constellio.app.ui.framework.builders;

import java.io.Serializable;

import com.constellio.app.ui.entities.FormMetadataSchemaVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataSchema;

@SuppressWarnings("serial")
public class MetadataSchemaToFormVOBuilder implements Serializable {

	public FormMetadataSchemaVO build(MetadataSchema schema, SessionContext sessionContext) {
		String code = schema.getCode();
		String collection = schema.getCollection();
		Language language = Language.withCode(sessionContext.getCurrentLocale().getLanguage());
		String label = schema.getLabel(language);

		return new FormMetadataSchemaVO(code, collection, label);
	}

	@Deprecated
	public FormMetadataSchemaVO build(MetadataSchema schema) {
		String code = schema.getCode();
		String collection = schema.getCollection();
		String label = schema.getLabel(Language.French);

		return new FormMetadataSchemaVO(code, collection, label);
	}

}
