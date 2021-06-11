package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.GenericRecordPageExtension;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMTypes;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.modules.rm.wrappers.LegalReference;
import com.constellio.app.modules.rm.wrappers.LegalRequirement;
import com.constellio.app.modules.rm.wrappers.LegalRequirementReference;
import com.constellio.app.modules.rm.wrappers.RetentionRuleDocumentType;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;

import static com.constellio.app.modules.rm.constants.RMTypes.CONTAINER_RECORD;
import static com.constellio.app.modules.rm.constants.RMTypes.RETENTION_RULE;
import static com.constellio.app.modules.rm.constants.RMTypes.UNIFORM_SUBDIVISION;
import static com.constellio.app.modules.rm.extensions.RMListSchemaTypeExtension.RM_TAB;
import static com.constellio.data.frameworks.extensions.ExtensionBooleanResult.FORCE_TRUE;
import static com.constellio.data.frameworks.extensions.ExtensionBooleanResult.NOT_APPLICABLE;
import static java.util.Arrays.asList;

public class RMGenericRecordPageExtension extends GenericRecordPageExtension {

	@Override
	public ExtensionBooleanResult canManageSchema(User user, MetadataSchemaType schemaType) {
		if (schemaType.getCode().startsWith(FilingSpace.SCHEMA_TYPE)) {
			return ExtensionBooleanResult.forceTrueIf(user.has(CorePermissions.MANAGE_SECURITY).globally());

		} else if (schemaType.getCode().startsWith(UniformSubdivision.SCHEMA_TYPE)) {
			return ExtensionBooleanResult.forceTrueIf(user.has(RMPermissionsTo.MANAGE_UNIFORMSUBDIVISIONS).globally());

			//		} else if (schemaType.getCode().startsWith(RetentionRule.SCHEMA_TYPE)) {
			//			return ExtensionBooleanResult.forceTrueIf(user.has(RMPermissionsTo.MANAGE_RETENTIONRULE).globally());

		} else {
			return NOT_APPLICABLE;
		}
	}

	@Override
	public ExtensionBooleanResult isSchemaTypeConfigurable(MetadataSchemaType schemaType) {
		if (schemaType.getCode().equals(Capsule.SCHEMA_TYPE)) {
			return ExtensionBooleanResult.FALSE;
		}

		if (Toggle.DISPLAY_LEGAL_REQUIREMENTS.isEnabled() &&
			(schemaType.getCode().equals(LegalRequirement.SCHEMA_TYPE) || schemaType.getCode().equals(LegalReference.SCHEMA_TYPE))) {
			return FORCE_TRUE;
		}

		return asList(RETENTION_RULE, UNIFORM_SUBDIVISION, CONTAINER_RECORD).contains(schemaType.getCode()) ?
			   FORCE_TRUE : NOT_APPLICABLE;
	}

	@Override
	public String getSchemaTypeDisplayGroup(MetadataSchemaType schemaType) {
		if (RMTypes.STORAGE_SPACE.equals(schemaType.getCode())) {
			return TAXONOMY_TAB;
		} else if (RMTypes.getAllTypes().contains(schemaType.getCode()) || Task.SCHEMA_TYPE.equals(schemaType.getCode())
				   || LegalRequirement.SCHEMA_TYPE.equals(schemaType.getCode()) || LegalReference.SCHEMA_TYPE.equals(schemaType.getCode())) {
			return RM_TAB;
		} else if (LegalRequirementReference.SCHEMA_TYPE.equals(schemaType.getCode())) {
			return null;
		} else if (RetentionRuleDocumentType.SCHEMA_TYPE.equals(schemaType.getCode())) {
			return null;
		}
		return super.getSchemaTypeDisplayGroup(schemaType);
	}
}
