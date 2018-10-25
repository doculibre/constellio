package com.constellio.app.modules.rm.migrations;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static org.assertj.core.api.Assertions.tuple;

public class RMMigrationTo8_2_AcceptanceTest extends ConstellioTest {

	@Test
	public void givenSystemIn8_1thenMigrated()
			throws Exception {
		givenSystemIn8_1();

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		SearchServices searchServices = getModelLayerFactory().newSearchServices();

		List<Record> carts = searchServices
				.search(new LogicalSearchQuery(from(rm.cart.schemaType()).returnAll()));

		assertThatRecords(carts).extractingMetadatas(IDENTIFIER).containsOnly(tuple("00000047048", "00000047050", "00000047046"));
	}

	private void givenSystemIn8_1() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "systemstate-20181025.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}
}
