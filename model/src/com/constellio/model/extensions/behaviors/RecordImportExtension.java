package com.constellio.model.extensions.behaviors;

import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.extensions.events.recordsImport.BuildParams;
import com.constellio.model.extensions.events.recordsImport.PrevalidationParams;
import com.constellio.model.extensions.events.recordsImport.ValidationParams;

public abstract class RecordImportExtension {

	public abstract String getDecoratedSchemaType();

	public void prevalidate(PrevalidationParams event) {
	}

	public void validate(ValidationParams event) {
	}

	public void build(BuildParams event) {
	}

	public ExtensionBooleanResult skipPrevalidation(PrevalidationParams event) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult skipValidation(ValidationParams event) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}
}
