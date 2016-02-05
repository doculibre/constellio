package com.constellio.model.services.search;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.LoadTest;

@LoadTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SearchServiceLoadTest extends ConstellioTest {

	private List<String> hundredRecordsId;
	private List<String> fiveHundredRecordsId;
	private List<String> thousandRecordsId;
	private List<String> fiveThousandRecordsId;
	private List<String> tenThousandsRecordsId;
	private boolean recordsCreated = false;
	private SearchServiceAcceptanceTestSchemas schema = new SearchServiceAcceptanceTestSchemas();
	private SearchServiceAcceptanceTestSchemas.ZeSchemaMetadatas zeSchema = schema.new ZeSchemaMetadatas();
	private RecordServices recordServices;
	private SearchServices searchServices;
	private LogicalSearchCondition condition;

	@Before
	public void setUp()
			throws Exception {
		RecordDao recordDao = getDataLayerFactory().newRecordDao();
		searchServices = new SearchServices(recordDao, getModelLayerFactory());

		defineSchemasManager().using(schema.withAStringMetadata());

		hundredRecordsId = addAndGetIdRecords(100);
		fiveHundredRecordsId = addAndGetIdRecords(500);
		thousandRecordsId = addAndGetIdRecords(1000);
		fiveThousandRecordsId = addAndGetIdRecords(5000);
		tenThousandsRecordsId = addAndGetIdRecords(10000);

	}

	@Test
	public void isInSearchOnIdIn100records() {
		condition = from(zeSchema.instance()).where(Schemas.IDENTIFIER).isIn(hundredRecordsId);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);

		long startTime = System.currentTimeMillis();
		List<Record> result = searchServices.search(query);
		long diff = System.currentTimeMillis() - startTime;
		assertThat(result).hasSize(100);
		System.out.println("millis duration for 100: " + diff);
	}

	@Test
	public void isInSearchOnIdIn500records() {
		condition = from(zeSchema.instance()).where(Schemas.IDENTIFIER).isIn(fiveHundredRecordsId);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);

		long startTime = System.currentTimeMillis();
		List<Record> result = searchServices.search(query);
		long diff = System.currentTimeMillis() - startTime;
		assertThat(result).hasSize(500);
		System.out.println("millis duration for 500: " + diff);
	}

	@Test
	public void isInSearchOnIdIn1000records() {
		condition = from(zeSchema.instance()).where(Schemas.IDENTIFIER).isIn(thousandRecordsId);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);

		long startTime = System.currentTimeMillis();
		List<Record> result = searchServices.search(query);
		long diff = System.currentTimeMillis() - startTime;
		assertThat(result).hasSize(1000);
		System.out.println("millis duration for 1000: " + diff);
	}

	@Test
	public void isNotInSearchOnIdOn100records() {
		condition = from(zeSchema.instance()).where(Schemas.IDENTIFIER).isNotIn(hundredRecordsId);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);

		long startTime = System.currentTimeMillis();
		searchServices.search(query);
		long diff = System.currentTimeMillis() - startTime;
		System.out.println("millis duration for not in 100: " + diff);
	}

	@Test
	public void isNotInSearchOnIdOn500records() {
		condition = from(zeSchema.instance()).where(Schemas.IDENTIFIER).isNotIn(fiveHundredRecordsId);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);

		long startTime = System.currentTimeMillis();
		searchServices.search(query);
		long diff = System.currentTimeMillis() - startTime;
		System.out.println("millis duration for not in 500: " + diff);
	}

	@Test
	public void isNotInSearchOnIdOn1000records() {
		condition = from(zeSchema.instance()).where(Schemas.IDENTIFIER).isNotIn(thousandRecordsId);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);

		long startTime = System.currentTimeMillis();
		searchServices.search(query);
		long diff = System.currentTimeMillis() - startTime;
		System.out.println("millis duration for not in 1000: " + diff);
	}

	private Record newRecordOfZeSchema() {
		return recordServices.newRecordWithSchema(zeSchema.instance());
	}

	private List<String> addAndGetIdRecords(int numberRecords)
			throws Exception {

		Transaction transaction = new Transaction();
		for (int i = 0; i < numberRecords; i++) {
			transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "records#" + i));
		}
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		recordServices.execute(transaction);
		return transaction.getRecordIds();
	}

}
