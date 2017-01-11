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

public class CoreMigrationTo_6_7_AcceptanceTest extends ConstellioTest {

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

		//		SchemasRecordsServices schemasRecordsServices = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
		//		List<SolrAuthorizationDetails> solrAuthorizationDetailsList = schemasRecordsServices.
		//				searchSolrAuthorizationDetailss(LogicalSearchQueryOperators.
		//						from(schemasRecordsServices.authorizationDetails.schemaType()).where(Schemas.IDENTIFIER).isNotNull());
		//
		//		assertThat(solrAuthorizationDetailsList).extracting(
		//				Schemas.IDENTIFIER.getLocalCode(), SolrAuthorizationDetails.SYNCED, SolrAuthorizationDetails.START_DATE,
		//				SolrAuthorizationDetails.END_DATE, SolrAuthorizationDetails.ROLES).containsOnly(
		//
		//				tuple("rwd__3995bc0f-5b1e-4a57-aec9-bbb8a6f1d5fb", false, null, null, asList("READ", "WRITE", "DELETE")),
		//				tuple("rwd__2e91f83c-36ca-41ac-aa68-d493aa85f408", false, null, null, asList("READ", "WRITE", "DELETE")),
		//				tuple("rw__30e8cee1-7204-49a6-bb4c-11a036b323d6", false, null, null, asList("READ", "WRITE")),
		//				tuple("rw__a9037f63-f550-439d-a48d-409dfc33b23c", false, null, null, asList("READ", "WRITE")),
		//				tuple("_M_fa599f22-9f0b-403b-8423-0bf90a2e3474", false, null, null, asList("M")),
		//				tuple("rwd__fc3c59a4-9aab-45af-a43a-4af02f468f6d", false, null, null, asList("READ", "WRITE", "DELETE")),
		//				tuple("rw__9a67dcce-f60f-4267-92cb-fed788282e59", false, null, null, asList("READ", "WRITE")),
		//				tuple("_M_dfe63fb5-cfbb-48b3-97a6-f8b2f784fc9f", false, null, null, asList("M")),
		//				tuple("_M_736ef749-c2bf-4927-86fe-aa8ff17d2953", false, null, null, asList("M")),
		//				tuple("_U_e3d81b36-c0e6-4c26-b483-58770753dfde", false, null, null, asList("U")),
		//				tuple("_M_f405f828-feb5-442c-8431-bbd3030c3b79", false, null, null, asList("M")),
		//				tuple("rwd__00000000401", false, new LocalDate("2016-12-22"), new LocalDate("2017-12-22"), asList("DELETE")),
		//				tuple("r__00000000398", false, new LocalDate("2016-12-22"), null, asList("READ")),
		//				tuple("rwd__00000000394", false, null, null, asList("READ", "WRITE", "DELETE")),
		//				tuple("rw__171c507e-fbc2-43dc-adf3-a56a84d1543e", false, null, null, asList("READ", "WRITE")),
		//				tuple("rwd__e3f6565b-51ce-4577-8c88-c44154f51356", false, null, null, asList("READ", "WRITE", "DELETE")),
		//				tuple("_U_405346b8-f840-48c9-8b04-7e6812bdb2d4", false, null, null, asList("U")),
		//				tuple("_U_d3fa69a8-d34d-4c7c-a073-8edc36d33a92", false, null, null, asList("U")),
		//				tuple("_U_29f05904-ede6-444f-8220-f45acb5342bd", false, null, null, asList("U")),
		//				tuple("rw__00000000396", false, new LocalDate("2007-12-22"), null, asList("READ", "WRITE"))
		//		);
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
