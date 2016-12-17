package com.constellio.app.modules.rm.migrations;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;

public class RMMigrationTo5_1_9_AcceptanceTest extends ConstellioTest {

	@Test
	public void givenOldVersionWhenMigrateTo5_1_9ThenTableConfigurationOk()
			throws Exception {

		givenSystemAtVersion5_1_4_1();
		waitForBatchProcess();

		List<MetadataSchemaType> schemaTypes = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(zeCollection).getSchemaTypes();

		SchemasDisplayManager metadataSchemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		for (MetadataSchemaType schemaType : schemaTypes) {
			for (MetadataSchema metadataSchema : schemaType.getAllSchemas()) {
				String code = metadataSchema.getCode();
				List<String> tableMetadataCodes = metadataSchemasDisplayManager
						.getSchema(zeCollection, code).getTableMetadataCodes();
				if (code.contains("default")) {
					if (!code.startsWith("event_")) {
						assertThat(tableMetadataCodes).isEqualTo(metadataSchemasDisplayManager
								.getSchema(zeCollection, code).getSearchResultsMetadataCodes()).isNotEmpty();
					}
				} else {
					assertThat(tableMetadataCodes).isEmpty();
				}
			}
		}
	}

	private void givenSystemAtVersion5_1_4_1() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "given_system_in_5.1.4.1_with_tasks,rm_modules__with_manual_modifications.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}
}
