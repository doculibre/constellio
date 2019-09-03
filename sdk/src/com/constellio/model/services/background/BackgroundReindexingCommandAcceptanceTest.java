package com.constellio.model.services.background;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.data.dao.dto.records.OptimisticLockingResolution.EXCEPTION;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.Schemas.MARKED_FOR_REINDEXING;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.asList;
import static com.constellio.sdk.tests.TestUtils.asMap;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsCalculatedUsingPattern;
import static org.assertj.core.api.Assertions.assertThat;

public class BackgroundReindexingCommandAcceptanceTest extends ConstellioTest {

	TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();
	ThirdSchemaMetadatas thirdSchema = schemas.new ThirdSchemaMetadatas();

	RecordServices recordServices;
	SearchServices searchServices;
	SolrClient solrClient;

	LogicalSearchCondition whereNumberIsFive;

	@Before
	public void setUp()
			throws Exception {
		givenDisabledAfterTestValidations();
		defineSchemasManager().using(schemas
				.withAStringMetadata()
				.withANumberMetadata(whichIsCalculatedUsingPattern("stringMetadata.length()")));
		recordServices = getModelLayerFactory().newRecordServices();
		solrClient = getDataLayerFactory().getRecordsVaultServer().getNestedSolrServer();
		searchServices = getModelLayerFactory().newSearchServices();
		whereNumberIsFive = from(zeSchema.type()).where(zeSchema.numberMetadata()).isEqualTo(5.0);
	}

	@Test
	public void givenRecordsMarkedForReindexingThenEventuallyReindexed()
			throws Exception {

		List<String> idsMarkedForReindexing = new ArrayList<>();
		Transaction transaction = new Transaction().setOptimisticLockingResolution(EXCEPTION);
		for (int i = 0; i < 40; i++) {
			Record record = new TestRecord(zeSchema).set(zeSchema.stringMetadata(), "pomme");
			transaction.add(record);
			if (i != 17) {
				idsMarkedForReindexing.add(record.getId());
			}
		}
		recordServices.execute(transaction);

		setNumberMetadataToABadValueTo(transaction.getRecords());
		markForReindexing(idsMarkedForReindexing);
		assertThat(searchServices.getResultsCount(whereNumberIsFive)).isEqualTo(0);

		RecordsReindexingBackgroundAction command = new RecordsReindexingBackgroundAction(getModelLayerFactory());

		command.run();
		assertThat(searchServices.getResultsCount(whereNumberIsFive)).isEqualTo(10);

		command.run();
		assertThat(searchServices.getResultsCount(whereNumberIsFive)).isEqualTo(20);

		command.run();
		assertThat(searchServices.getResultsCount(whereNumberIsFive)).isEqualTo(30);

		command.run();
		assertThat(searchServices.getResultsCount(whereNumberIsFive)).isEqualTo(39);

		command.run();
		assertThat(searchServices.getResultsCount(whereNumberIsFive)).isEqualTo(39);
	}

	@Test
	public void givenOptimisticLockingWithTwoTransactionsSettingFlagToDifferentValuesWhenExecutingSecondTransactionThenResolvedBySettingToTrue()
			throws Exception {

		List<String> idsMarkedForReindexing = new ArrayList<>();
		Record record = new TestRecord(zeSchema).set(zeSchema.stringMetadata(), "pomme");
		idsMarkedForReindexing.add(record.getId());
		recordServices.add(record);

		setNumberMetadataToABadValueTo(asList(record));
		recordServices.refresh(record);

		markForReindexing(idsMarkedForReindexing);
		recordServices.flush();

		record.set(Schemas.TITLE, "New title!");
		record.set(Schemas.MARKED_FOR_REINDEXING, true);

		recordServices.flush();
		Thread.sleep(1000);

		assertThat(searchServices.getResultsCount(from(zeSchema.type()).where(MARKED_FOR_REINDEXING).isTrue())).isEqualTo(1);
		Thread.sleep(1000);
		recordServices.flush();
		new RecordsReindexingBackgroundAction(getModelLayerFactory()).run();

		recordServices.flush();
		Thread.sleep(1000);
		assertThat(searchServices.getResultsCount(from(zeSchema.type()).where(MARKED_FOR_REINDEXING).isTrue())).isEqualTo(0);

		Transaction transaction = new Transaction();
		//transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		transaction.add(record);
		transaction.addRecordToReindex(record.getId());
		recordServices.execute(transaction);

		assertThat(searchServices.getResultsCount(whereNumberIsFive)).isEqualTo(1);
		assertThat(searchServices.getResultsCount(from(zeSchema.type()).where(MARKED_FOR_REINDEXING).isTrue())).isEqualTo(1);

	}

	@Test
	public void givenRecordsMarkedForReindexingWhenDoingFullCollectionReindexingThenUnmarked()
			throws Exception {
		ReindexingServices reindexingServices = new ReindexingServices(getModelLayerFactory());
		List<String> idsMarkedForReindexing = new ArrayList<>();
		Record record = new TestRecord(zeSchema).set(zeSchema.stringMetadata(), "pomme");
		idsMarkedForReindexing.add(record.getId());
		recordServices.add(record);

		setNumberMetadataToABadValueTo(asList(record));

		markForReindexing(idsMarkedForReindexing);

		recordServices.refresh(record);

		record.set(Schemas.TITLE, "New title!");
		record.set(Schemas.MARKED_FOR_REINDEXING, true);

		assertThat(searchServices.getResultsCount(from(zeSchema.type()).where(MARKED_FOR_REINDEXING).isTrue())).isEqualTo(1);

		reindexingServices.reindexCollection(zeCollection, ReindexationMode.REWRITE);
		assertThat(searchServices.getResultsCount(from(zeSchema.type()).where(MARKED_FOR_REINDEXING).isTrue())).isEqualTo(1);

		reindexingServices.reindexCollection(zeCollection, ReindexationMode.RECALCULATE);
		assertThat(searchServices.getResultsCount(from(zeSchema.type()).where(MARKED_FOR_REINDEXING).isTrue())).isEqualTo(0);

	}

	@Test
	public void givenARecordMarkedForReindexingIsCreatingANewReferenceToLogicallyDeletedMetadataThenReindexedAnyway()
			throws Exception {

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).create("aStringMetadataContainingReferences").setType(STRING);
				types.getSchema(zeSchema.code()).create("calculatedRef").setType(REFERENCE)
						.defineReferencesTo(types.getSchemaType(anotherSchema.typeCode()))
						.defineDataEntry().asJexlScript("aStringMetadataContainingReferences");
			}
		});

		Record anotherSchemaRecord = new TestRecord(anotherSchema, "anotherSchemaRecord");
		recordServices.add(anotherSchemaRecord);
		List<String> idsMarkedForReindexing = new ArrayList<>();
		final Transaction firstTransaction = new Transaction().setOptimisticLockingResolution(EXCEPTION);
		for (int i = 0; i < 100; i++) {
			Record record = new TestRecord(zeSchema).set(zeSchema.stringMetadata(), "pomme");
			record.set(zeSchema.metadata("aStringMetadataContainingReferences"), "anotherSchemaRecord");

			firstTransaction.add(record);
			idsMarkedForReindexing.add(record.getId());
		}
		recordServices.execute(firstTransaction);
		recordServices.logicallyDelete(anotherSchemaRecord, User.GOD);

		setRefMetadataToABadValueTo(firstTransaction.getRecords());
		markForReindexing(idsMarkedForReindexing);

		RecordsReindexingBackgroundAction command = new RecordsReindexingBackgroundAction(getModelLayerFactory());

		command.run();
		assertThat(searchServices.getResultsCount(from(zeSchema.type()).where(zeSchema.metadata("calculatedRef"))
				.isEqualTo(anotherSchemaRecord.getId()))).isEqualTo(100);

	}

	private void markForReindexing(List<String> idsMarkedForReindexing)
			throws RecordServicesException {
		Transaction markingForReindexing = new Transaction();
		markingForReindexing.add(recordServices.getDocumentById(zeCollection));
		markingForReindexing.addAllRecordsToReindex(idsMarkedForReindexing);
		recordServices.execute(markingForReindexing);
	}

	private void setRefMetadataToABadValueTo(List<Record> records)
			throws Exception {
		List<SolrInputDocument> updates = new ArrayList<>();
		for (Record record : records) {
			SolrInputDocument update = new SolrInputDocument();
			update.setField("id", record.getId());
			update.setField("calculatedRefId_s", asMap("set", "toto"));
			updates.add(update);
		}

		solrClient.add(updates);
		solrClient.commit(true, true, true);
	}

	private void setNumberMetadataToABadValueTo(List<Record> records)
			throws Exception {
		List<SolrInputDocument> updates = new ArrayList<>();
		for (Record record : records) {
			SolrInputDocument update = new SolrInputDocument();
			update.setField("id", record.getId());
			update.setField("numberMetadata_d", asMap("inc", -1));
			updates.add(update);
		}

		solrClient.add(updates);
		solrClient.commit(true, true, true);
	}
}
