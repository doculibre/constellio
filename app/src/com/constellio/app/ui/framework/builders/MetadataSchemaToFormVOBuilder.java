package com.constellio.app.ui.framework.builders;

import java.io.Serializable;

import com.constellio.app.ui.entities.FormMetadataSchemaVO;
import com.constellio.model.entities.schemas.MetadataSchema;

@SuppressWarnings("serial")
public class MetadataSchemaToFormVOBuilder implements Serializable {

	public FormMetadataSchemaVO build(MetadataSchema schema) {
		String code = schema.getCode();
		String collection = schema.getCollection();
		String label = schema.getLabel();

		return new FormMetadataSchemaVO(code, collection, label);
	}

}
