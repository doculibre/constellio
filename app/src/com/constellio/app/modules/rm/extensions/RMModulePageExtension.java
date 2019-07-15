package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.PageExtension;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.management.authorizations.ListContentAccessAuthorizationsPresenter;
import com.constellio.app.ui.pages.management.authorizations.ShareContentPresenter;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public class RMModulePageExtension extends PageExtension {

	@Override
	public ExtensionBooleanResult hasRestrictedRecordAccess(Class<? extends BasePresenter> presenterClass,
															String params,
															User user,
															Record restrictedRecord) {

		if (presenterClass.equals(ListContentAccessAuthorizationsPresenter.class)) {
			return hasAccessToListContentAuthorizationPage(user, restrictedRecord);

		} else if (presenterClass.equals(ShareContentPresenter.class)) {
			return shareContent(user, restrictedRecord);

		} else {
			return ExtensionBooleanResult.NOT_APPLICABLE;
		}
	}

	private ExtensionBooleanResult hasAccessToListContentAuthorizationPage(User user, Record restrictedRecord) {

		if (restrictedRecord.getSchemaCode().startsWith(Folder.SCHEMA_TYPE)) {
			return ExtensionBooleanResult.trueIf(user.hasAny(RMPermissionsTo.MANAGE_FOLDER_AUTHORIZATIONS, RMPermissionsTo.VIEW_FOLDER_AUTHORIZATIONS).on(restrictedRecord));

		} else if (restrictedRecord.getSchemaCode().startsWith(Document.SCHEMA_TYPE)) {
			return ExtensionBooleanResult.trueIf(user.hasAny(RMPermissionsTo.MANAGE_DOCUMENT_AUTHORIZATIONS, RMPermissionsTo.VIEW_DOCUMENT_AUTHORIZATIONS).on(restrictedRecord));

		} else {
			return ExtensionBooleanResult.NOT_APPLICABLE;
		}
	}

	private ExtensionBooleanResult shareContent(User user, Record restrictedRecord) {

		if (restrictedRecord.getSchemaCode().startsWith(Folder.SCHEMA_TYPE)) {
			return ExtensionBooleanResult.trueIf(user.hasAny(RMPermissionsTo.MANAGE_FOLDER_AUTHORIZATIONS,
					RMPermissionsTo.SHARE_FOLDER).on(restrictedRecord));

		} else if (restrictedRecord.getSchemaCode().startsWith(Document.SCHEMA_TYPE)) {
			return ExtensionBooleanResult
					.trueIf(user.hasAny(RMPermissionsTo.MANAGE_DOCUMENT_AUTHORIZATIONS, RMPermissionsTo.SHARE_DOCUMENT)
							.on(restrictedRecord));

		} else {
			return ExtensionBooleanResult.NOT_APPLICABLE;
		}
	}

}
