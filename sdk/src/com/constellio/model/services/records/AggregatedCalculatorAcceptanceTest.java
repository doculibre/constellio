package com.constellio.model.services.records;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.AggregatedCalculator;
import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;
import com.constellio.model.entities.schemas.entries.SearchAggregatedValuesParams;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.constellio.model.services.records.RecordServicesAgregatedMetadatasMechanicAcceptTest.clearAggregateMetadatasThenReindexReturningQtyOfQueriesOf;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class AggregatedCalculatorAcceptanceTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);

	String AGGREGATED_METADATA = "aggregatedMetadata";

	@Before
	public void setUp()
			throws Exception {

		givenBackgroundThreadsEnabled();
		givenSystemLanguageIs("fr");
		givenTransactionLogIsEnabled();

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRobotsModule()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus()
		);
	}

	@Test
	public void givenADependencyForAggregatedCalculatorIsModifiedThenRecordMarkedForReindexing()
			throws Exception {
		getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
				.modify(zeCollection, new MetadataSchemaTypesAlteration() {
					@Override
					public void alter(MetadataSchemaTypesBuilder types) {
						types.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).create(AGGREGATED_METADATA)
								.setType(MetadataValueType.STRING).defineDataEntry()
								.asCalculatedAggregation(types.getDefaultSchema(Folder.SCHEMA_TYPE).getMetadata(Folder.CONTAINER),
										TestCalculatorThatConcatenatesTitles.class);
					}
				});

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		ContainerRecord containerBac13 = records.getContainerBac13();
		recordServices.update(records.getFolder_A01().setContainer(containerBac13));
		waitForBatchProcess();

		assertThatRecord(records.getContainerBac13()).extracting(AGGREGATED_METADATA)
				.isEqualTo(asList("Abeille - Crocodile - Dauphin - Dindon"));

		recordServices.update(records.getFolder_A42().setTitle("new Title"));
		assertThat(fetchBac13FromSolr().<Boolean>get(Schemas.MARKED_FOR_REINDEXING)).isEqualTo(Boolean.TRUE);
		waitForBatchProcess();
		assertThatRecord(records.getContainerBac13()).extracting(AGGREGATED_METADATA)
				.isEqualTo(asList("Abeille - Dauphin - Dindon - new Title"));

		int nbQueries = clearAggregateMetadatasThenReindexReturningQtyOfQueriesOf(Folder.SCHEMA_TYPE);

		assertThatRecord(records.getContainerBac13()).extracting(AGGREGATED_METADATA)
				.isEqualTo(asList("Abeille - Dauphin - Dindon - new Title"));
		assertThat(nbQueries).isEqualTo(4);
	}

	@Test
	public void givenAggregatedMinimumMetadataThenReturnsMinimum()
			throws Exception {
		getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
				.modify(zeCollection, new MetadataSchemaTypesAlteration() {
					@Override
					public void alter(MetadataSchemaTypesBuilder types) {
						types.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).create(AGGREGATED_METADATA)
								.setType(MetadataValueType.NUMBER).defineDataEntry()
								.asMin(types.getDefaultSchema(Folder.SCHEMA_TYPE).getMetadata(Folder.CONTAINER),
										types.getDefaultSchema(Folder.SCHEMA_TYPE).getMetadata(Folder.LINEAR_SIZE));
					}
				});

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		tx = new Transaction();
		tx.add(records.getContainerBac13());
		tx.getRecordUpdateOptions().setUpdateAggregatedMetadatas(true);
		recordServices.execute(tx);
		waitForBatchProcess();

		assertThatRecord(records.getContainerBac13()).extracting(AGGREGATED_METADATA).isEqualTo(asList(0D));

		recordServices.update(records.getFolder_A42().setLinearSize(3D));
		recordServices.update(records.getFolder_A43().setLinearSize(2D));
		recordServices.update(records.getFolder_A44().setLinearSize(1D));
		assertThat(fetchBac13FromSolr().<Boolean>get(Schemas.MARKED_FOR_REINDEXING)).isEqualTo(Boolean.TRUE);
		waitForBatchProcess();
		assertThatRecord(records.getContainerBac13()).extracting(AGGREGATED_METADATA).isEqualTo(asList(1D));

		int nbQueries = clearAggregateMetadatasThenReindexReturningQtyOfQueriesOf(Folder.SCHEMA_TYPE,
				ContainerRecord.SCHEMA_TYPE);

		assertThatRecord(records.getContainerBac13()).extracting(AGGREGATED_METADATA).isEqualTo(asList(1D));
		assertThat(nbQueries).isEqualTo(5);
	}

	@Test
	public void givenAggregatedMaximumMetadataThenReturnsMaximum()
			throws Exception {
		getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
				.modify(zeCollection, new MetadataSchemaTypesAlteration() {
					@Override
					public void alter(MetadataSchemaTypesBuilder types) {
						types.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).create(AGGREGATED_METADATA)
								.setType(MetadataValueType.NUMBER).defineDataEntry()
								.asMax(types.getDefaultSchema(Folder.SCHEMA_TYPE).getMetadata(Folder.CONTAINER),
										types.getDefaultSchema(Folder.SCHEMA_TYPE).getMetadata(Folder.LINEAR_SIZE));
					}
				});

		getDataLayerFactory().getDataLayerLogger().setMonitoredIds(asList("00000000061"));

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		Transaction tx = new Transaction();
		tx.add(records.getContainerBac13());
		tx.getRecordUpdateOptions().setUpdateAggregatedMetadatas(true);

		recordServices.execute(tx);
		waitForBatchProcess();
		assertThatRecord(records.getContainerBac13()).extracting(AGGREGATED_METADATA).isEqualTo(asList(0D));

		recordServices.update(records.getFolder_A42().setLinearSize(3D));
		recordServices.update(records.getFolder_A43().setLinearSize(2D));
		recordServices.update(records.getFolder_A44().setLinearSize(1D));
		assertThat(fetchBac13FromSolr().<Boolean>get(Schemas.MARKED_FOR_REINDEXING)).isEqualTo(Boolean.TRUE);
		waitForBatchProcess();
		assertThatRecord(records.getContainerBac13()).extracting(AGGREGATED_METADATA).isEqualTo(asList(3D));

		getDataLayerFactory().getDataLayerLogger().setQueryDebuggingMode(true);
		int nbQueries = clearAggregateMetadatasThenReindexReturningQtyOfQueriesOf(Folder.SCHEMA_TYPE,
				ContainerRecord.SCHEMA_TYPE);

		assertThatRecord(records.getContainerBac13()).extracting(AGGREGATED_METADATA).isEqualTo(asList(3D));
		assertThat(nbQueries).isEqualTo(5);
	}

	public Record fetchBac13FromSolr() {
		return getModelLayerFactory().newSearchServices().search(
				new LogicalSearchQuery().setCondition(LogicalSearchQueryOperators.fromAllSchemasIn("zeCollection")
						.where(Schemas.IDENTIFIER).isEqualTo("bac13"))).get(0);
	}

	static public class TestCalculatorThatConcatenatesTitles implements AggregatedCalculator<String> {

		@Override
		public String calculate(SearchAggregatedValuesParams params) {
			SearchServices searchServices = params.getSearchServices();
			LogicalSearchQuery query = params.getCombinedQuery();
			query.sortAsc(Schemas.TITLE);
			List<Record> referenceRecords = searchServices.search(query);
			StringBuilder stringBuilder = new StringBuilder();

			List<String> titles = new ArrayList<>();
			for (Record record : referenceRecords) {
				titles.add(record.getTitle());
			}
			Collections.sort(titles);
			return StringUtils.join(titles, " - ");

		}

		@Override
		public String calculate(InMemoryAggregatedValuesParams params) {
			List<String> titles = new ArrayList<>(params.<String>getValues());
			Collections.sort(titles);
			return StringUtils.join(titles, " - ");
		}

		@Override
		public List<String> getMetadataDependencies() {
			return asList(Folder.DEFAULT_SCHEMA + "_" + Folder.TITLE);
		}
	}
}
