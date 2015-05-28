/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.PageAccessExtension;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.management.authorizations.ListContentAuthorizationsPresenter;
import com.constellio.app.ui.pages.management.authorizations.ShareContentPresenter;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public class RMModulePageAccessExtension implements PageAccessExtension {
	@Override
	public ExtensionBooleanResult hasPageAccess(Class<? extends BasePresenter> presenterClass, String params, User user) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	@Override
	public ExtensionBooleanResult hasRestrictedRecordAccess(Class<? extends BasePresenter> presenterClass, String params,
			User user,
			Record restrictedRecord) {

		if (presenterClass.equals(ListContentAuthorizationsPresenter.class)) {
			return hasAccessToListContentAuthorizationPage(user, restrictedRecord);

		} else if (presenterClass.equals(ShareContentPresenter.class)) {
			return shareContent(user, restrictedRecord);

		} else
			return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	private ExtensionBooleanResult hasAccessToListContentAuthorizationPage(User user, Record restrictedRecord) {

		if (restrictedRecord.getSchemaCode().startsWith(Folder.SCHEMA_TYPE)) {
			return ExtensionBooleanResult.trueIf(user.has(RMPermissionsTo.MANAGE_FOLDER_AUTHORIZATIONS).on(restrictedRecord));

		} else if (restrictedRecord.getSchemaCode().startsWith(Document.SCHEMA_TYPE)) {
			return ExtensionBooleanResult.trueIf(user.has(RMPermissionsTo.MANAGE_DOCUMENT_AUTHORIZATIONS).on(restrictedRecord));

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
