package com.constellio.app.services.importExport.settings;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.importExport.settings.model.ImportedSettings;
import com.constellio.model.services.records.RecordServicesException.ValidationException;

public class SettingsImportServices {

	AppLayerFactory appLayerFactory;

	public SettingsImportServices(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	public void importSettings(ImportedSettings settings)
			throws ValidationException {

	}
}
