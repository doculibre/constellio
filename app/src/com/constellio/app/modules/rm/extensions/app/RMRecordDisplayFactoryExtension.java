package com.constellio.app.modules.rm.extensions.app;

import com.constellio.app.api.extensions.RecordDisplayFactoryExtension;
import com.constellio.app.modules.rm.ui.components.retentionRule.AdministrativeUnitReferenceDisplay;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.model.entities.schemas.AllowedReferences;

public class RMRecordDisplayFactoryExtension extends RecordDisplayFactoryExtension {

	private AppLayerFactory appLayerFactory;
	private String collection;

	public RMRecordDisplayFactoryExtension(AppLayerFactory appLayerFactory, String collection) {
		this.appLayerFactory = appLayerFactory;
		this.collection = collection;
	}

	@Override
	public ReferenceDisplay getDisplayForReference(AllowedReferences allowedReferences, String id) {
		if(allowedReferences != null && allowedReferences.getAllowedSchemaType() != null &&
				allowedReferences.getAllowedSchemaType().contains(AdministrativeUnit.SCHEMA_TYPE)) {
			return getReferenceDisplayForAdministrativeUnit(id);
		}
		return null;
	}
	public AdministrativeUnitReferenceDisplay getReferenceDisplayForAdministrativeUnit(String id) {
		return new AdministrativeUnitReferenceDisplay(id);
	}
}
