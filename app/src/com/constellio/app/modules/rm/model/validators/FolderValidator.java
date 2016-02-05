package com.constellio.app.modules.rm.model.validators;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class FolderValidator implements RecordValidator {

	@Override
	public void validate(Record record, MetadataSchemaTypes types, MetadataSchema schema, ConfigProvider configProvider,
			ValidationErrors validationErrors) {
		Folder folder = new Folder(record, types);
		validate(folder, schema, validationErrors);
	}

	void validate(Folder folder, MetadataSchema schema, ValidationErrors validationErrors) {

	}
}
