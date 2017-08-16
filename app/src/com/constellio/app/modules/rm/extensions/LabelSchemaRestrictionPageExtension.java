package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.GenericRecordPageExtension;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;

public class LabelSchemaRestrictionPageExtension extends GenericRecordPageExtension {
    @Override
    public ExtensionBooleanResult canManageSchema(User user, MetadataSchemaType schemaType) {
        return schemaType.equals(PrintableLabel.SCHEMA_NAME) ? ExtensionBooleanResult.FALSE : ExtensionBooleanResult.TRUE;
    }

    @Override
    public ExtensionBooleanResult isSchemaTypeConfigurable(MetadataSchemaType schemaType) {
        return schemaType.equals(PrintableLabel.SCHEMA_NAME) ? ExtensionBooleanResult.FALSE : ExtensionBooleanResult.TRUE;
    }
}
