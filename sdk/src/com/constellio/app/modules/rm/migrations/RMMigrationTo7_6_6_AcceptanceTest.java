package com.constellio.app.modules.rm.migrations;

import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static org.assertj.core.api.Assertions.tuple;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;

public class RMMigrationTo7_6_6_AcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);

	@Test
	public void givenSystemMigratedAndBackgroundScriptsHaveBeenExecutedThenContainersModified()
			throws Exception {

		givenSystemIn7_6_5();

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		List<Record> auths = searchServices
				.search(new LogicalSearchQuery(from(rm.authorizationDetails.schemaType()).returnAll()));

		assertThatRecords(auths).extractingMetadatas(IDENTIFIER, rm.authorizationDetails.targetSchemaType()).containsOnly(
				tuple("todo")
		);
	}

	private void givenSystemIn7_6_5() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "given_system_in_7.6.3_with_tasks,rm_modules.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

}
