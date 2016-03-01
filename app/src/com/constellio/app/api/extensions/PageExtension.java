package com.constellio.app.api.extensions;

import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public class 	PageExtension {

	public ExtensionBooleanResult hasPageAccess(Class<? extends BasePresenter> presenterClass, String params, User user) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult hasRestrictedRecordAccess(Class<? extends BasePresenter> presenterClass, String params,
			User user, Record restrictedRecord) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

}
