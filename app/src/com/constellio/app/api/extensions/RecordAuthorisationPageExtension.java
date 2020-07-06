package com.constellio.app.api.extensions;

import com.constellio.app.api.extensions.params.RecordAuthorisationPageExtensionParams;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;

public class RecordAuthorisationPageExtension {
	public ExtensionBooleanResult isAuthorsationPageAvalibleForUser(RecordAuthorisationPageExtensionParams param) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}
}
