package com.constellio.model.services.records;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.File;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.test.RandomWordsIterator;
import com.constellio.data.utils.Octets;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTestWithGlobalContext;
import com.constellio.sdk.tests.annotations.PerformanceTest;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

@PerformanceTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RecordServices_AddUpdatesByTransactionSizePerformanceTest extends ConstellioTestWithGlobalContext {

	static TestsSchemasSetup schemas = new TestsSchemasSetup();
	static ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	static RecordServices recordServices;
	static RandomWordsIterator randomWordsIterator;
	@Rule public TestRule benchmarkRun = new BenchmarkRule();

	@Test
	public void __prepareTests__()
			throws Exception {
		schemas = new TestsSchemasSetup();
		zeSchema = schemas.new ZeSchemaMetadatas();
		defineSchemasManager().using(schemas.withALargeTextMetadata());

		recordServices = getModelLayerFactory().newRecordServices();
		File dictionaryFolder = getFoldersLocator().getDict();
		randomWordsIterator = RandomWordsIterator.createFor(new File(dictionaryFolder, "fr_FR_avec_accents.dic"));
	}

	@BenchmarkOptions(warmupRounds = 1, benchmarkRounds = 3)
	@BenchmarkHistoryChart(maxRuns = 20)
	@Test
	public void a_add100RecordsOf10Kb()
			throws Exception {
		Transaction transaction = new Transaction();
		for (int i = 0; i < 100; i++) {
			transaction.addUpdate(newRecordWithTitleAndContent(randomWordsIterator.next(), randomWordsIterator.nextWordsOfLength(
					Octets.kilooctets(10))));
		}
		recordServices.execute(transaction);
	}

	@BenchmarkOptions(warmupRounds = 1, benchmarkRounds = 3)
	@BenchmarkHistoryChart(maxRuns = 20)
	@Test
	public void b_add1000RecordsOf10Kb()
			throws Exception {
		Transaction transaction = new Transaction();
		for (int i = 0; i < 1000; i++) {
			transaction.addUpdate(newRecordWithTitleAndContent(randomWordsIterator.next(), randomWordsIterator.nextWordsOfLength(
					Octets.kilooctets(10))));
		}
		recordServices.execute(transaction);
	}

	@BenchmarkOptions(warmupRounds = 1, benchmarkRounds = 3)
	@BenchmarkHistoryChart(maxRuns = 20)
	@Test
	public void c_add10000RecordsOf10Kb()
			throws Exception {
		Transaction transaction = new Transaction();
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		for (int i = 0; i < 10000; i++) {
			transaction.addUpdate(newRecordWithTitleAndContent(randomWordsIterator.next(), randomWordsIterator.nextWordsOfLength(
					Octets.kilooctets(10))));
		}
		recordServices.execute(transaction);
	}

	@BenchmarkOptions(warmupRounds = 1, benchmarkRounds = 3)
	@BenchmarkHistoryChart(maxRuns = 20)
	@Test
	public void d_update100RecordsOf10Kb()
			throws Exception {
		List<Record> records = getSomeRecords(100);
		Transaction transaction = new Transaction();
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		for (Record record : records) {
			transaction.addUpdate(record.set(zeSchema.largeTextMetadata(), randomWordsIterator.nextWordsOfLength(
					Octets.kilooctets(10))));
		}
		recordServices.execute(transaction);
	}

	@BenchmarkOptions(warmupRounds = 1, benchmarkRounds = 3)
	@BenchmarkHistoryChart(maxRuns = 20)
	@Test
	public void e_update1000RecordsOf10Kb()
			throws Exception {
		List<Record> records = getSomeRecords(1000);
		Transaction transaction = new Transaction();
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		for (Record record : records) {
			transaction.addUpdate(record.set(zeSchema.largeTextMetadata(), randomWordsIterator.nextWordsOfLength(
					Octets.kilooctets(10))));
		}
		recordServices.execute(transaction);
	}

	@BenchmarkOptions(warmupRounds = 1, benchmarkRounds = 3)
	@BenchmarkHistoryChart(maxRuns = 20)
	@Test
	public void f_update10000RecordsOf10Kb()
			throws Exception {
		List<Record> records = getSomeRecords(10000);
		Transaction transaction = new Transaction();
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		for (Record record : records) {
			transaction.addUpdate(record.set(zeSchema.largeTextMetadata(), randomWordsIterator.nextWordsOfLength(
					Octets.kilooctets(10))));
		}
		recordServices.execute(transaction);
	}

	@BenchmarkOptions(warmupRounds = 1, benchmarkRounds = 3)
	@BenchmarkHistoryChart(maxRuns = 20)
	@Test
	public void g_updateAndMerge100RecordsOf10Kb()
			throws Exception {
		List<Record> records = getSomeRecords(100);
		List<Record> records2 = getSomeRecords(100);
		Transaction transaction = new Transaction();
		for (Record record : records) {
			transaction.addUpdate(record.set(Schemas.TITLE, randomWordsIterator.nextWords(1)));
		}
		recordServices.execute(transaction);

		transaction = new Transaction();
		for (Record record : records2) {
			transaction.addUpdate(record.set(zeSchema.largeTextMetadata(), randomWordsIterator.nextWordsOfLength(
					Octets.kilooctets(10))));
		}
		recordServices.execute(transaction);
	}

	@BenchmarkOptions(warmupRounds = 1, benchmarkRounds = 3)
	@BenchmarkHistoryChart(maxRuns = 20)
	@Test
	public void h_updateAndMerge1000RecordsOf10Kb()
			throws Exception {
		List<Record> records = getSomeRecords(1000);
		List<Record> records2 = getSomeRecords(1000);
		Transaction transaction = new Transaction();
		for (Record record : records) {
			transaction.addUpdate(record.set(Schemas.TITLE, randomWordsIterator.nextWords(1)));
		}
		recordServices.execute(transaction);

		transaction = new Transaction();
		for (Record record : records2) {
			transaction.addUpdate(record.set(zeSchema.largeTextMetadata(), randomWordsIterator.nextWordsOfLength(
					Octets.kilooctets(10))));
		}
		recordServices.execute(transaction);
	}

	private List<Record> getSomeRecords(int qty) {
		LogicalSearchCondition condition = from(zeSchema.instance()).returnAll();
		return getModelLayerFactory().newSearchServices()
				.search(new LogicalSearchQuery(condition).setNumberOfRows(qty).sortAsc(Schemas.IDENTIFIER));
	}

	private Record newRecordWithTitleAndContent(String title, String content) {
		Record record = recordServices.newRecordWithSchema(zeSchema.instance());
		record.set(Schemas.TITLE, title);
		record.set(zeSchema.largeTextMetadata(), content);
		return record;
	}

}
