package com.constellio.app.extensions.records;

import com.constellio.app.extensions.records.params.HasUserReadAccessParams;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;

public class UserHavePermissionOnRecordExtension {

	public ExtensionBooleanResult hasUserReadAccess(HasUserReadAccessParams hasUserReadAccessParams) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}
}
