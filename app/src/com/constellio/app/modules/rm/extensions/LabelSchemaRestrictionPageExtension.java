package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.GenericRecordPageExtension;
import com.constellio.app.modules.rm.wrappers.RMReport;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;

/**
 * Created by Marco on 2017-01-24.
 */
public class LabelSchemaRestrictionPageExtension extends GenericRecordPageExtension {
    @Override
    public ExtensionBooleanResult canManageSchema(User user, MetadataSchemaType schemaType) {
        return schemaType.equals(RMReport.SCHEMA_NAME) ? ExtensionBooleanResult.FALSE : ExtensionBooleanResult.TRUE;
    }

    @Override
    public ExtensionBooleanResult isSchemaTypeConfigurable(MetadataSchemaType schemaType) {
        return schemaType.equals(RMReport.SCHEMA_NAME) ? ExtensionBooleanResult.FALSE : ExtensionBooleanResult.TRUE;
    }
}
