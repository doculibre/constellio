package com.constellio.model.extensions.behaviors;

import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.extensions.events.schemas.SchemaEvent;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class SchemaExtension {
	public ExtensionBooleanResult isPutInTrashBeforePhysicalDelete(SchemaEvent event) {
		//TODO
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public LogicalSearchCondition getPhysicallyDeletableRecordsForSchemaType(SchemaEvent event) {
		//TODO
		return null;
	}
}
