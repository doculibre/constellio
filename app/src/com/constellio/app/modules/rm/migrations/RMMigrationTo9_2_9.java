package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.restapi.RestApiConfigs;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;

public class RMMigrationTo9_2_9 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.2.9";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		String displayDateFormatConfig = appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.DATE_FORMAT);
		String displayDatetimeFormatConfig = appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.DATE_TIME_FORMAT);
		if (appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager().getValue(RestApiConfigs.REST_API_DATE_FORMAT) == null) {
			appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager().setValue(RestApiConfigs.REST_API_DATE_FORMAT, displayDateFormatConfig);
		}
		if (appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager().getValue(RestApiConfigs.REST_API_DATETIME_FORMAT) == null) {
			appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager().setValue(RestApiConfigs.REST_API_DATETIME_FORMAT, displayDatetimeFormatConfig);
		}
	}
}
