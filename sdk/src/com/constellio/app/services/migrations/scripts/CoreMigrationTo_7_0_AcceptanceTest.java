package com.constellio.app.services.migrations.scripts;

import static com.constellio.model.services.records.reindexing.ReindexationMode.RECALCULATE_AND_REWRITE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.extractions.RecordPopulateServices;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.dev.tools.SecurityUtils;
import com.constellio.sdk.tests.ConstellioTest;

import java.io.File;

public class CoreMigrationTo_7_0_AcceptanceTest extends ConstellioTest {

	@Test
	public void startApplicationWithSaveState()
			throws Exception {
		RecordPopulateServices.LOG_CONTENT_MISSING = false;
		givenTransactionLogIsEnabled();

		String expectedSecurityReport = FileUtils.readFileToString(getTestResourceFile("securityReport.txt"));

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(getTestResourceFile("savestate.zip"))
				.withPasswordsReset().withFakeEncryptionServices();

		ReindexingServices reindexingServices = getModelLayerFactory().newReindexingServices();
		reindexingServices.reindexCollections(RECALCULATE_AND_REWRITE);

		String securityReport = SecurityUtils.printSecurityReport(zeCollection, getAppLayerFactory());
		assertThat(securityReport).isEqualTo(expectedSecurityReport);

	}

	@Test
	public void startApplicationWithSaveStateWithSpecialAuths()
			throws Exception {
		RecordPopulateServices.LOG_CONTENT_MISSING = false;
		givenTransactionLogIsEnabled();

		getCurrentTestSession().getFactoriesTestFeatures()
				.givenSystemInState(getTestResourceFile("savestateWithSpecialAuths.zip")).withPasswordsReset()
				.withFakeEncryptionServices();

		ReindexingServices reindexingServices = getModelLayerFactory().newReindexingServices();
		reindexingServices.reindexCollections(RECALCULATE_AND_REWRITE);

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

		User sasquatch = getModelLayerFactory().newUserServices().getUserInCollection("sasquatch", zeCollection);

		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.folder.schemaType()).returnAll());
		query.filteredWithUser(sasquatch);
		assertThat(getModelLayerFactory().newSearchServices().search(query)).hasSize(2);
	}
}
