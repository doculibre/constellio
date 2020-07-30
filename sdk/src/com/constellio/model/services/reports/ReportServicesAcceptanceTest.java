package com.constellio.model.services.reports;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.reports.model.search.ReportTestUtils;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;

public class ReportServicesAcceptanceTest extends ConstellioTest {
	private RMTestRecords records = new RMTestRecords(zeCollection);
	private MetadataSchemaTypes types;

	private ReportServices reportServices;
	private final String folderSchemaType = Folder.SCHEMA_TYPE;

	final String reportTitle = "zReportTitle";
	final String reportDeletableTitle = "deletableReportTitle";
	private ReportTestUtils reportTestUtils;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withAllTestUsers()
		);

		reportServices = new ReportServices(getModelLayerFactory(), zeCollection);
		MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		types = metadataSchemasManager.getSchemaTypes(zeCollection);

		UserServices userServices = getModelLayerFactory().newUserServices();
		userServices.execute(userServices.getUserCredential(chuckNorris).getUsername(), (req) -> req.addCollection(zeCollection));

		reportTestUtils = new ReportTestUtils(getModelLayerFactory(), zeCollection, records);
	}

	@Test
	public void whenDeleteReportThenReportDeleted() {
		reportTestUtils.addUserReport(reportDeletableTitle, chuckNorris);
		MetadataSchema reportSchema = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getSchema(Report.DEFAULT_SCHEMA);
		Metadata schemaTypeCodeMetadata = reportSchema.getMetadata(Report.SCHEMA_TYPE_CODE);
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(reportSchema)
				.where(schemaTypeCodeMetadata).isEqualTo(folderSchemaType));
		List<Record> results = searchServices.search(query);
		assertThat(results.size()).isEqualTo(1);
		Report chuckReport = new Report(results.get(0), types);
		reportTestUtils.validateUserReport(chuckReport, chuckNorris);
		assertThat(chuckReport.getTitle()).isEqualTo(reportDeletableTitle);

		// Validate
		reportServices.deleteReport(records.getChuckNorris(), chuckReport);

		results = searchServices.search(query);
		assertThat(results.size()).isEqualTo(0);
	}

	@Test
	public void whenSaveReportThenReportSaved() {
		reportTestUtils.addUserReport(reportTitle, chuckNorris);
		MetadataSchema reportSchema = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getSchema(Report.DEFAULT_SCHEMA);
		Metadata schemaTypeCodeMetadata = reportSchema.getMetadata(Report.SCHEMA_TYPE_CODE);
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(reportSchema)
				.where(schemaTypeCodeMetadata).isEqualTo(folderSchemaType));
		List<Record> results = searchServices.search(query);
		assertThat(results.size()).isEqualTo(1);
		Report chuckReport = new Report(results.get(0), types);
		reportTestUtils.validateUserReport(chuckReport, chuckNorris);
		assertThat(chuckReport.getTitle()).isEqualTo(reportTitle);
	}

	@Test
	public void whenChuckReportAndDefaultReportThenReturnChuckReportForChuck() {
		reportTestUtils.addUserReport(reportTitle, chuckNorris);
		reportTestUtils.addDefaultReport(reportTitle);
		Report report = reportServices.getUserReport(chuckNorris, folderSchemaType, reportTitle);
		assertThat(report).isNotNull();
		reportTestUtils.validateUserReport(report, chuckNorris);
		assertThat(report.getTitle()).isEqualTo(reportTitle);
	}

	@Test
	public void whenChuckReportAndDefaultReportThenReturnDefaultReportForBob() {
		reportTestUtils.addUserReport(reportTitle, chuckNorris);
		reportTestUtils.addDefaultReport(reportTitle);
		Report report = reportServices.getUserReport(bobGratton, folderSchemaType, reportTitle);
		assertThat(report).isNotNull();
		reportTestUtils.validateDefaultReport(report);
		assertThat(report.getTitle()).isEqualTo(reportTitle);
	}

	@Test
	public void whenChuckReportAndDefaultReportThenReturnDefaultReport() {
		reportTestUtils.addUserReport(reportTitle, chuckNorris);
		reportTestUtils.addDefaultReport(reportTitle);
		Report report = reportServices.getReport(folderSchemaType, reportTitle);
		assertThat(report).isNotNull();
		reportTestUtils.validateDefaultReport(report);
		assertThat(report.getTitle()).isEqualTo(reportTitle);
	}

	@Test
	public void whenChuckReport1AndDefaultReport1AndChuckReport2AndDefaultReport3ThenReturnChuckReport1AndChuckReport2AndDefaultReport3ForChuck() {
		reportTestUtils.addUserReport("reportTitle1", chuckNorris);
		reportTestUtils.addDefaultReport("reportTitle1");
		reportTestUtils.addUserReport("reportTitle2", chuckNorris);
		reportTestUtils.addDefaultReport("reportTitle3");
		List<String> report = reportServices.getUserReportTitles(records.getChuckNorris(), folderSchemaType);
		assertThat(report).containsOnly("reportTitle1", "reportTitle2", "reportTitle3");
		Report report1 = reportServices.getUserReport(chuckNorris, folderSchemaType, "reportTitle1");
		reportTestUtils.validateUserReport(report1, chuckNorris);
		Report report2 = reportServices.getUserReport(chuckNorris, folderSchemaType, "reportTitle2");
		reportTestUtils.validateUserReport(report2, chuckNorris);
		Report report3 = reportServices.getUserReport(chuckNorris, folderSchemaType, "reportTitle3");
		reportTestUtils.validateDefaultReport(report3);
	}
}
