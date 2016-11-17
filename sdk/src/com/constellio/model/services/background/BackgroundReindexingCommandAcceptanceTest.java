package com.constellio.model.services.background;

import static com.constellio.data.dao.dto.records.OptimisticLockingResolution.EXCEPTION;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.asMap;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsCalculatedUsingPattern;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class BackgroundReindexingCommandAcceptanceTest extends ConstellioTest {

	TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();
	ThirdSchemaMetadatas thirdSchema = schemas.new ThirdSchemaMetadatas();

	RecordServices recordServices;
	SearchServices searchServices;
	SolrClient solrClient;

	@Test
	public void givenRecordsMarkedForReindexingThenEventuallyReindexed()
			throws Exception {
		defineSchemasManager().using(schemas
				.withAStringMetadata()
				.withANumberMetadata(whichIsCalculatedUsingPattern("stringMetadata.length()")));
		recordServices = getModelLayerFactory().newRecordServices();
		solrClient = getDataLayerFactory().getRecordsVaultServer().getNestedSolrServer();
		searchServices = getModelLayerFactory().newSearchServices();
		LogicalSearchCondition condition = from(zeSchema.type()).where(zeSchema.numberMetadata()).isEqualTo(5.0);

		List<String> idsMarkedForReindexing = new ArrayList<>();
		Transaction transaction = new Transaction().setOptimisticLockingResolution(EXCEPTION);
		for (int i = 0; i < 10000; i++) {
			Record record = new TestRecord(zeSchema).set(zeSchema.stringMetadata(), "pomme");
			transaction.add(record);
			if (i != 42) {
				idsMarkedForReindexing.add(record.getId());
			}
		}
		recordServices.execute(transaction);

		List<SolrInputDocument> updates = new ArrayList<>();
		for (Record record : transaction.getRecords()) {
			SolrInputDocument update = new SolrInputDocument();
			update.setField("id", record.getId());
			update.setField("numberMetadata_d", asMap("inc", -1));
			updates.add(update);
		}

		solrClient.add(updates);
		solrClient.commit(true, true, true);

		Transaction markingForReindexing = new Transaction();
		markingForReindexing.add(recordServices.getDocumentById(zeCollection));
		markingForReindexing.addAllRecordsToReindex(idsMarkedForReindexing);
		recordServices.execute(markingForReindexing);

		assertThat(searchServices.getResultsCount(condition)).isEqualTo(0);

		BackgroundReindexingCommand command = new BackgroundReindexingCommand(getModelLayerFactory());

		command.run();
		assertThat(searchServices.getResultsCount(condition)).isEqualTo(1000);

		command.run();
		assertThat(searchServices.getResultsCount(condition)).isEqualTo(2000);

		command.run();
		assertThat(searchServices.getResultsCount(condition)).isEqualTo(3000);

		command.run();
		assertThat(searchServices.getResultsCount(condition)).isEqualTo(4000);

		command.run();
		assertThat(searchServices.getResultsCount(condition)).isEqualTo(5000);

		command.run();
		assertThat(searchServices.getResultsCount(condition)).isEqualTo(6000);

		command.run();
		assertThat(searchServices.getResultsCount(condition)).isEqualTo(7000);

		command.run();
		assertThat(searchServices.getResultsCount(condition)).isEqualTo(8000);

		command.run();
		assertThat(searchServices.getResultsCount(condition)).isEqualTo(9000);

		command.run();
		assertThat(searchServices.getResultsCount(condition)).isEqualTo(9999);

		command.run();
		assertThat(searchServices.getResultsCount(condition)).isEqualTo(9999);

	}
}
