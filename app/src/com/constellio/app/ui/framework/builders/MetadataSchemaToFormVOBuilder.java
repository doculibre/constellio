package com.constellio.app.ui.framework.builders;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.ui.entities.FormMetadataSchemaVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataSchema;

@SuppressWarnings("serial")
public class MetadataSchemaToFormVOBuilder implements Serializable {

	public FormMetadataSchemaVO build(MetadataSchema schema, SessionContext sessionContext, SchemaTypeDisplayConfig schemaTypeDisplayConfig) {
		String code = schema.getCode();
		String collection = schema.getCollection();
		Map<String, String> labels = configureLabels(schema.getLabels());

		if (schemaTypeDisplayConfig != null) {
			boolean advancedSearch = schemaTypeDisplayConfig.isAdvancedSearch();
			return new FormMetadataSchemaVO(code, collection, labels, advancedSearch);
		}
		return new FormMetadataSchemaVO(code, collection, labels);
	}

	public FormMetadataSchemaVO build(MetadataSchema schema, SessionContext sessionContext) {
		return build(schema, sessionContext, null);
	}

	private Map<String, String> configureLabels(Map<Language, String> labels) {
		Map<String, String> newLabels = new HashMap<>();
		for (Entry<Language, String> entry : labels.entrySet()) {
			newLabels.put(entry.getKey().getCode(), entry.getValue());
		}
		return newLabels;
	}
}
