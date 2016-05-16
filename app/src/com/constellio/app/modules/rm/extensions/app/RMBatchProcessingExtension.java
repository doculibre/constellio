package com.constellio.app.modules.rm.extensions.app;

import static java.util.Arrays.asList;

import java.util.List;

import com.constellio.app.api.extensions.BatchProcessingExtension;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;

public class RMBatchProcessingExtension extends BatchProcessingExtension {

	final List<String> hiddenFolderMetadatas = asList(Folder.CATEGORY_ENTERED, Folder.ADMINISTRATIVE_UNIT_ENTERED,
			Folder.UNIFORM_SUBDIVISION_ENTERED, Folder.RETENTION_RULE_ENTERED, Folder.COPY_STATUS_ENTERED,
			Folder.MAIN_COPY_RULE_ID_ENTERED, Folder.APPLICABLE_COPY_RULES, Folder.ACTIVE_RETENTION_CODE,
			Folder.ACTIVE_RETENTION_TYPE, Folder.SEMIACTIVE_RETENTION_CODE, Folder.SEMIACTIVE_RETENTION_TYPE,
			Folder.INACTIVE_DISPOSAL_TYPE, Folder.RETENTION_RULE_ADMINISTRATIVE_UNITS, Folder.COPY_RULES_EXPECTED_DEPOSIT_DATES,
			Folder.COPY_RULES_EXPECTED_TRANSFER_DATES, Folder.COPY_RULES_EXPECTED_DESTRUCTION_DATES, Folder.DECOMMISSIONING_DATE);

	@Override
	public ExtensionBooleanResult isMetadataDisplayedWhenModified(IsMetadataDisplayedWhenModifiedParams params) {

		if (params.isSchemaType(Folder.SCHEMA_TYPE)) {
			return ExtensionBooleanResult.falseIf(hiddenFolderMetadatas.contains(params.getMetadata().getLocalCode()));
		}

		return ExtensionBooleanResult.NOT_APPLICABLE;
	}
}
