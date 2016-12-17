package com.constellio.app.modules.tasks.extensions.schema;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.extensions.behaviors.SchemaExtension;
import com.constellio.model.extensions.events.schemas.SchemaEvent;
import com.constellio.model.services.schemas.SchemaUtils;

public class TaskTrashSchemaExtension extends SchemaExtension{
	@Override
	public ExtensionBooleanResult isPutInTrashBeforePhysicalDelete(SchemaEvent event) {
		String schemaType = new SchemaUtils().getSchemaTypeCode(event.getSchemaCode());
		switch (schemaType) {
		case Task.SCHEMA_TYPE:
			return ExtensionBooleanResult.TRUE;
		}
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}
}
