package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.RecordAuthorisationPageExtension;
import com.constellio.app.api.extensions.params.RecordAuthorisationPageExtensionParams;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public class RMRecordAuthorisationPageExtension extends RecordAuthorisationPageExtension {

	public RMRecordAuthorisationPageExtension() {
	}

	public ExtensionBooleanResult isAuthorsationPageAvalibleForUser(RecordAuthorisationPageExtensionParams param) {
		Record record = param.getRecord();
		User user = param.getUser();

		if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
			return ExtensionBooleanResult.forceTrueIf(user.has(RMPermissionsTo.VIEW_DOCUMENT_AUTHORIZATIONS).on(record));
		} else if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
			return ExtensionBooleanResult.forceTrueIf(user.has(RMPermissionsTo.VIEW_FOLDER_AUTHORIZATIONS).on(record));
		} else {
			return ExtensionBooleanResult.NOT_APPLICABLE;
		}
	}
}
