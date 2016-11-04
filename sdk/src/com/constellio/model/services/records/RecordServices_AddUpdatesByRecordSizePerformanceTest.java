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
public class RecordServices_AddUpdatesByRecordSizePerformanceTest extends ConstellioTestWithGlobalContext {

	static TestsSchemasSetup schemas = new TestsSchemasSetup();
	static ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	static RecordServices recordServices;
	static RandomWordsIterator randomWordsIterator;
	//@Rule public TestRule benchmarkRun = new BenchmarkRule();

	@Test
	public void __prepareTests__()
			throws Exception {
		defineSchemasManager().using(schemas.withALargeTextMetadata());

		recordServices = getModelLayerFactory().newRecordServices();
		File dictionaryFolder = getFoldersLocator().getDict();
		randomWordsIterator = RandomWordsIterator.createFor(new File(dictionaryFolder, "fr_FR_avec_accents.dic"));
	}

	@BenchmarkOptions(warmupRounds = 1, benchmarkRounds = 3)
	@BenchmarkHistoryChart(maxRuns = 20)
	@Test
	public void a_add1000EmptyRecords()
			throws Exception {

		Transaction transaction = new Transaction();
		for (int i = 0; i < 1000; i++) {
			transaction.addUpdate(newRecordWithTitleAndContent("emptyRecord_" + i, randomWordsIterator.nextWords(1)));
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
			transaction.addUpdate(newRecordWithTitleAndContent("10kbRecord_" + i, randomWordsIterator.nextWordsOfLength(
					Octets.kilooctets(10))));
		}
		recordServices.execute(transaction);
	}

	@BenchmarkOptions(warmupRounds = 1, benchmarkRounds = 3)
	@BenchmarkHistoryChart(maxRuns = 20)
	@Test
	public void c_add1000RecordsOf20Kb()
			throws Exception {
		Transaction transaction = new Transaction();
		for (int i = 0; i < 1000; i++) {
			transaction.addUpdate(newRecordWithTitleAndContent("20kbRecord_" + i, randomWordsIterator.nextWordsOfLength(
					Octets.kilooctets(20))));
		}
		recordServices.execute(transaction);
	}

	@BenchmarkOptions(warmupRounds = 1, benchmarkRounds = 3)
	@BenchmarkHistoryChart(maxRuns = 20)
	@Test
	public void d_add1000RecordsOf100kb()
			throws Exception {

		Transaction transaction = new Transaction();
		for (int i = 0; i < 1000; i++) {
			transaction.addUpdate(newRecordWithTitleAndContent("100kbRecord_" + i, randomWordsIterator.nextWordsOfLength(
					Octets.kilooctets(100))));
		}
		recordServices.execute(transaction);
	}

	@BenchmarkOptions(warmupRounds = 1, benchmarkRounds = 3)
	@BenchmarkHistoryChart(maxRuns = 20)
	@Test
	public void e_update1000EmptyRecords()
			throws Exception {
		List<Record> records = findRecordsWithTitleStartingWith("emptyRecord_");
		Transaction transaction = new Transaction();
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		for (Record record : records) {
			transaction.addUpdate(record.set(zeSchema.largeTextMetadata(), randomWordsIterator.nextWords(1)));
		}
		recordServices.execute(transaction);
	}

	@BenchmarkOptions(warmupRounds = 1, benchmarkRounds = 3)
	@BenchmarkHistoryChart(maxRuns = 20)
	@Test
	public void f_update1000RecordsOf10Kb()
			throws Exception {
		List<Record> records = findRecordsWithTitleStartingWith("10kbRecord_");
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
	public void g_update1000RecordsOf20Kb()
			throws Exception {
		List<Record> records = findRecordsWithTitleStartingWith("20kbRecord_");
		Transaction transaction = new Transaction();
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		for (Record record : records) {
			transaction.addUpdate(record.set(zeSchema.largeTextMetadata(), randomWordsIterator.nextWordsOfLength(
					Octets.kilooctets(20))));
		}
		recordServices.execute(transaction);
	}

	@BenchmarkOptions(warmupRounds = 1, benchmarkRounds = 3)
	@BenchmarkHistoryChart(maxRuns = 20)
	@Test
	public void h_update1000RecordsOf100kb()
			throws Exception {
		List<Record> records = findRecordsWithTitleStartingWith("100kbRecord_");
		Transaction transaction = new Transaction();
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		for (Record record : records) {
			transaction.addUpdate(record.set(zeSchema.largeTextMetadata(), randomWordsIterator.nextWordsOfLength(
					Octets.kilooctets(100))));
		}
		recordServices.execute(transaction);
	}

	private List<Record> findRecordsWithTitleStartingWith(String criterion) {
		LogicalSearchCondition condition = from(zeSchema.instance()).where(Schemas.TITLE).isStartingWithText(criterion);
		return getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery(condition).setNumberOfRows(1000));
	}

	private Record newRecordWithTitleAndContent(String title, String content) {
		Record record = recordServices.newRecordWithSchema(zeSchema.instance());
		record.set(Schemas.TITLE, title);
		record.set(zeSchema.largeTextMetadata(), content);
		return record;
	}

}
