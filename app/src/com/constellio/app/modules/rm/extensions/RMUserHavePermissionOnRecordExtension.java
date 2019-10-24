package com.constellio.app.modules.rm.extensions;

import com.constellio.app.extensions.records.UserHavePermissionOnRecordExtension;
import com.constellio.app.extensions.records.params.HasUserReadAccessParams;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.pages.containers.DisplayContainerPresenter;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;

public class RMUserHavePermissionOnRecordExtension extends UserHavePermissionOnRecordExtension {
	private RMSchemasRecordsServices rmSchemasRecordsServices;

	public RMUserHavePermissionOnRecordExtension(String collection, AppLayerFactory appLayerFactory) {
		this.rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	public ExtensionBooleanResult hasUserReadAccess(HasUserReadAccessParams hasUserReadAccessParams) {
		if (hasUserReadAccessParams.getRecord().getTypeCode().equals(ContainerRecord.SCHEMA_TYPE)) {
			return ExtensionBooleanResult.trueIf(DisplayContainerPresenter
					.hasRestrictedRecordAccess(rmSchemasRecordsServices, hasUserReadAccessParams.getUser(),
							hasUserReadAccessParams.getRecord()));
		} else {
			return ExtensionBooleanResult.NOT_APPLICABLE;
		}
	}
}
