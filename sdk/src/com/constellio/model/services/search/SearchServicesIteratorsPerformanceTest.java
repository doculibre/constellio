package com.constellio.model.services.search;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTestWithGlobalContext;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.annotations.PerformanceTest;

@PerformanceTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SearchServicesIteratorsPerformanceTest extends ConstellioTestWithGlobalContext {

	static SearchServiceAcceptanceTestSchemas schema = new SearchServiceAcceptanceTestSchemas("zeCollection");
	static SearchServiceAcceptanceTestSchemas.ZeSchemaMetadatas zeSchema = schema.new ZeSchemaMetadatas();

	@Test
	public void __prepareTests__()
			throws RecordServicesException {
		defineSchemasManager().using(schema);

		Transaction transaction = new Transaction();
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		for (int i = 0; i < 10000; i++) {
			Record record = new TestRecord(schema.zeDefaultSchema(), "" + i);
			record.set(Schemas.TITLE, "zeTitleInitial");
			transaction.addUpdate(record);
		}
		getModelLayerFactory().newRecordServices().execute(transaction);
	}

	@BenchmarkOptions(warmupRounds = 1, benchmarkRounds = 10)
	@BenchmarkHistoryChart(maxRuns = 20)
	@Test
	public void iterateOver10000Records() {
		Set<String> ids = new HashSet<>();

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchema());
		query.setCondition(from(schema.zeDefaultSchema()).where(Schemas.TITLE).isStartingWithText("zeTitle"));
		Iterator<Record> records = getModelLayerFactory().newSearchServices().recordsIterator(query, 10000);

		int i = 0;
		while (records.hasNext()) {

			ids.add(records.next().getId());
		}

		assertThat(ids).hasSize(10000);
	}

	@BenchmarkOptions(warmupRounds = 1, benchmarkRounds = 10)
	@BenchmarkHistoryChart(maxRuns = 20)
	@Test
	public void iterateOver10000RecordIds() {
		Set<String> ids = new HashSet<>();

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchema());
		query.setCondition(from(schema.zeDefaultSchema()).where(Schemas.TITLE).isStartingWithText("zeTitle"));
		Iterator<String> records = getModelLayerFactory().newSearchServices().recordsIdsIterator(query);

		int i = 0;
		while (records.hasNext()) {

			ids.add(records.next());
		}

		assertThat(ids).hasSize(10000);
	}

}
