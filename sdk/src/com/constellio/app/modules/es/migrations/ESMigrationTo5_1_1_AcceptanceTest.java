package com.constellio.app.modules.es.migrations;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Test;

import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;

public class ESMigrationTo5_1_1_AcceptanceTest extends ConstellioTest {

	@Test
	public void givenSystemInVersion5_1WithAConnectorWhenMigratingThenSchemasAndRecordsDeleted()
			throws OptimisticLockingConfiguration {

		givenSystemAtVersion5_1WithAConnector();

		ESSchemasRecordsServices es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		SearchServices searchServices = es.getSearchServices();

		assertThat(searchServices.hasResults(from(es.connectorInstance.schemaType()).returnAll())).isFalse();

	}

	private void givenSystemAtVersion5_1WithAConnector() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder,
				"es" + File.separator + "given_system_in_5.1_with_es_module__with_manual_modifications.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

}
