package com.constellio.app.modules.es.connectors.smb;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.sdk.tests.ConstellioTest;

public class ESMigrationSmbAcceptanceTest extends ConstellioTest {
	private ESSchemasRecordsServices es;

	@Test
	public void givenAnyMigrationWhenAllMigrationsRunThenSmbConnectorStillHasPasswordHidden() {
		prepareSystem(withZeCollection().withConstellioESModule()
				.withAllTestUsers());

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		MetadataDisplayConfig config = es.getMetadataSchemasDisplayManager()
				.getMetadata(zeCollection, ConnectorSmbInstance.SCHEMA_CODE + "_" + ConnectorSmbInstance.PASSWORD);

		assertThat(config.getInputType()
				.equals(MetadataInputType.PASSWORD)).isTrue();
	}
}