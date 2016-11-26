package com.constellio.app.modules.rm.reports.builders.search.stats;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;

public class StatsReportWriterFactoryAcceptanceTest extends ConstellioTest {
	private RMTestRecords records = new RMTestRecords(zeCollection);
	private SessionContext sessionContext;
	private RecordServices recordServices;
	private SearchServices searchServices;
	private RMSchemasRecordsServices schemas;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withEvents()
		);
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		searchServices = getModelLayerFactory().newSearchServices();
		schemas = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);

		recordServices = getModelLayerFactory().newRecordServices();

	}

	@Test
	public void givenQueryWithoutResultsThenReturnNull()
			throws Exception {
		LogicalSearchCondition condition = from(schemas.folderSchemaType()).where(Schemas.IDENTIFIER).isEqualTo("inexistingid");
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		assertThat(searchServices.getResultsCount(query)).isEqualTo(0);
		StatsReportBuilderFactory statsReportBuilderFactory = new StatsReportBuilderFactory(zeCollection, getModelLayerFactory(),
				query);
		assertThat(statsReportBuilderFactory.getStatistics()).isNull();
	}

	@Test
	public void givenQueryThatReturnsOneFolderWithMissingLinearMeasureAndAnotherWithAValidMeasureThenReturnValidResult()
			throws Exception {
		Folder folderWithMissingLinearMeasure = records.getFolder_A01();
		Folder folderWithValidLinearMeasure = records.getFolder_A55();
		recordServices.add(folderWithValidLinearMeasure.setLinearSize(12d));
		List<String> foldersIds = Arrays.asList(new String[] { records.folder_A01, records.folder_A55 });
		LogicalSearchCondition condition = from(schemas.folderSchemaType()).where(Schemas.IDENTIFIER).isIn(foldersIds);
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		StatsReportBuilderFactory statsReportBuilderFactory = new StatsReportBuilderFactory(zeCollection, getModelLayerFactory(),
				query);
		Map<String, Object> stats = statsReportBuilderFactory.getStatistics();
		assertThat(stats).isNotNull();
		assertThat(stats.get("missing")).isEqualTo(1l);
		assertThat(stats.get("sum")).isEqualTo(12d);
	}

	@Test
	public void givenQueryThatReturnsTwoFoldersWithValidMeasureThenReturnValidResult()
			throws Exception {
		Folder folderWithMissingLinearMeasure = records.getFolder_A01();
		recordServices.add(folderWithMissingLinearMeasure.setLinearSize(8d));
		Folder folderWithValidLinearMeasure = records.getFolder_A55();
		recordServices.add(folderWithValidLinearMeasure.setLinearSize(12d));
		List<String> foldersIds = Arrays.asList(new String[] { records.folder_A01, records.folder_A55 });
		LogicalSearchCondition condition = from(schemas.folderSchemaType()).where(Schemas.IDENTIFIER).isIn(foldersIds);
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		StatsReportBuilderFactory statsReportBuilderFactory = new StatsReportBuilderFactory(zeCollection, getModelLayerFactory(),
				query);
		Map<String, Object> stats = statsReportBuilderFactory.getStatistics();
		assertThat(stats).isNotNull();
		assertThat(stats.get("missing")).isEqualTo(0l);
		assertThat(stats.get("sum")).isEqualTo(20d);
	}

	@Test
	public void givenQueryThatReturnsTwoFoldersWithoutLinearMeasureThenReturnValidNull()
			throws Exception {
		List<String> foldersIds = Arrays.asList(new String[] { records.folder_A01, records.folder_A55 });
		LogicalSearchCondition condition = from(schemas.folderSchemaType()).where(Schemas.IDENTIFIER).isIn(foldersIds);
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		StatsReportBuilderFactory statsReportBuilderFactory = new StatsReportBuilderFactory(zeCollection, getModelLayerFactory(),
				query);
		Map<String, Object> stats = statsReportBuilderFactory.getStatistics();
		assertThat(stats).isNull();
	}
}