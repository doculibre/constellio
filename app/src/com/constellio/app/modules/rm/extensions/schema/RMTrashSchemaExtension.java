package com.constellio.app.modules.rm.extensions.schema;

import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.extensions.behaviors.SchemaExtension;
import com.constellio.model.extensions.events.schemas.SchemaEvent;
import com.constellio.model.services.schemas.SchemaUtils;

public class RMTrashSchemaExtension extends SchemaExtension {

	public ExtensionBooleanResult isPutInTrashBeforePhysicalDelete(SchemaEvent event) {
		String schemaType = new SchemaUtils().getSchemaTypeCode(event.getSchemaCode());
		switch (schemaType) {
		case Folder.SCHEMA_TYPE:
		case RetentionRule.SCHEMA_TYPE:
		case Category.SCHEMA_TYPE:
		case Document.SCHEMA_TYPE:
			return ExtensionBooleanResult.TRUE;
		}
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}
}
