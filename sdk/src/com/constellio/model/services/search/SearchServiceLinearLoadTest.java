package com.constellio.model.services.search;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.BulkRecordTransactionHandler;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.LoadTest;

@LoadTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SearchServiceLinearLoadTest extends ConstellioTest {

	private RecordServices recordServices;
	private SearchServices searchServices;
	private SearchServiceAcceptanceTestSchemas schema = new SearchServiceAcceptanceTestSchemas();
	private SearchServiceAcceptanceTestSchemas.ZeSchemaMetadatas zeSchema = schema.new ZeSchemaMetadatas();

	private LogicalSearchCondition condition;

	@Before
	public void setUp()
			throws Exception {
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();

		defineSchemasManager().using(schema.withAStringMetadata());

	}

	@Test
	public void isInSearch50IdIn100records()
			throws Exception {

		List<String> ids = addAndGetIdRecords(100).subList(0, 50);

		condition = from(zeSchema.instance()).where(Schemas.IDENTIFIER).isIn(ids);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);

		long startTime = System.currentTimeMillis();
		searchServices.search(query);
		long diff = System.currentTimeMillis() - startTime;
		System.out.println("millis duration for 100: " + diff);
	}

	@Test
	public void isInSearch50IdIn500records()
			throws Exception {
		List<String> ids = addAndGetIdRecords(500).subList(0, 50);

		condition = from(zeSchema.instance()).where(Schemas.IDENTIFIER).isIn(ids);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);

		long startTime = System.currentTimeMillis();
		searchServices.search(query);
		long diff = System.currentTimeMillis() - startTime;
		System.out.println("millis duration for 500: " + diff);
	}

	@Test
	public void isInSearch50IdIn1000records()
			throws Exception {
		List<String> ids = addAndGetIdRecords(1000).subList(0, 50);

		condition = from(zeSchema.instance()).where(Schemas.IDENTIFIER).isIn(ids);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);

		long startTime = System.currentTimeMillis();
		searchServices.search(query);
		long diff = System.currentTimeMillis() - startTime;
		System.out.println("millis duration for 1000: " + diff);
	}

	@Test
	public void isInSearch50IdIn5000records()
			throws Exception {
		List<String> ids = addAndGetIdRecords(5000).subList(0, 50);

		condition = from(zeSchema.instance()).where(Schemas.IDENTIFIER).isIn(ids);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);

		long startTime = System.currentTimeMillis();
		searchServices.search(query);
		long diff = System.currentTimeMillis() - startTime;
		System.out.println("millis duration for 5000: " + diff);
	}

	@Test
	public void isInSearch50IdIn10000records()
			throws Exception {
		List<String> ids = addAndGetIdRecords(10000).subList(0, 50);

		condition = from(zeSchema.instance()).where(Schemas.IDENTIFIER).isIn(ids);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);

		long startTime = System.currentTimeMillis();
		searchServices.search(query);
		long diff = System.currentTimeMillis() - startTime;
		System.out.println("millis duration for 10000: " + diff);
	}

	@Test
	public void isInSearch500IdIn20000records()
			throws Exception {
		List<String> ids = addAndGetIdRecords(20000).subList(0, 500);

		condition = from(zeSchema.instance()).where(Schemas.IDENTIFIER).isIn(ids);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);

		long startTime = System.currentTimeMillis();
		searchServices.search(query);
		long diff = System.currentTimeMillis() - startTime;
		System.out.println("millis duration for 500 in 20k: " + diff);
	}

	@Test
	public void isInSearch1000IdIn20000records()
			throws Exception {
		List<String> ids = addAndGetIdRecords(20000).subList(0, 1000);

		condition = from(zeSchema.instance()).where(Schemas.IDENTIFIER).isIn(ids);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);

		long startTime = System.currentTimeMillis();
		searchServices.search(query);
		long diff = System.currentTimeMillis() - startTime;
		System.out.println("millis duration for 1000 in 20k: " + diff);
	}

	@Test
	public void isInSearch1000IdIn40000records()
			throws Exception {
		List<String> ids = addAndGetIdRecords(40000).subList(0, 1000);

		condition = from(zeSchema.instance()).where(Schemas.IDENTIFIER).isIn(ids);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);

		long startTime = System.currentTimeMillis();
		searchServices.search(query);
		long diff = System.currentTimeMillis() - startTime;
		System.out.println("millis duration for 1000 in 40k: " + diff);
	}

	@Test
	public void isNotInSearch50IdIn100records()
			throws Exception {
		List<String> ids = addAndGetIdRecords(100).subList(0, 50);
		condition = from(zeSchema.instance()).where(Schemas.IDENTIFIER).isNotIn(ids);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);

		long startTime = System.currentTimeMillis();
		searchServices.search(query);
		long diff = System.currentTimeMillis() - startTime;
		System.out.println("millis duration for not in 100: " + diff);
	}

	@Test
	public void isNotInSearch50IdIn500records()
			throws Exception {
		List<String> ids = addAndGetIdRecords(500).subList(0, 50);

		condition = from(zeSchema.instance()).where(Schemas.IDENTIFIER).isNotIn(ids);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);

		long startTime = System.currentTimeMillis();
		searchServices.search(query);
		long diff = System.currentTimeMillis() - startTime;
		System.out.println("millis duration for not in 500: " + diff);
	}

	@Test
	public void isNotInSearch50IdIn1000records()
			throws Exception {
		List<String> ids = addAndGetIdRecords(1000).subList(0, 50);

		condition = from(zeSchema.instance()).where(Schemas.IDENTIFIER).isNotIn(ids);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);

		long startTime = System.currentTimeMillis();
		searchServices.search(query);
		long diff = System.currentTimeMillis() - startTime;
		System.out.println("millis duration for not in 1000: " + diff);
	}

	@Test
	public void isNotInSearch50IdIn5000records()
			throws Exception {
		List<String> ids = addAndGetIdRecords(5000).subList(0, 50);

		condition = from(zeSchema.instance()).where(Schemas.IDENTIFIER).isNotIn(ids);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);

		long startTime = System.currentTimeMillis();
		searchServices.search(query);
		long diff = System.currentTimeMillis() - startTime;
		System.out.println("millis duration for not in 5000: " + diff);
	}

	@Test
	public void isNotInSearch50IdIn10000records()
			throws Exception {
		List<String> ids = addAndGetIdRecords(10000).subList(0, 50);

		condition = from(zeSchema.instance()).where(Schemas.IDENTIFIER).isNotIn(ids);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);

		long startTime = System.currentTimeMillis();
		searchServices.search(query);
		long diff = System.currentTimeMillis() - startTime;
		System.out.println("millis duration for not in 10000: " + diff);
	}

	@Test
	public void isNotInSearch500IdIn20000records()
			throws Exception {
		List<String> ids = addAndGetIdRecords(20000).subList(0, 500);

		condition = from(zeSchema.instance()).where(Schemas.IDENTIFIER).isNotIn(ids);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);

		long startTime = System.currentTimeMillis();
		searchServices.search(query);
		long diff = System.currentTimeMillis() - startTime;
		System.out.println("millis duration for not 500 in 20k: " + diff);
	}

	@Test
	public void isNotInSearch1000IdIn20000records()
			throws Exception {
		List<String> ids = addAndGetIdRecords(20000).subList(0, 1000);

		condition = from(zeSchema.instance()).where(Schemas.IDENTIFIER).isNotIn(ids);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);

		long startTime = System.currentTimeMillis();
		searchServices.search(query);
		long diff = System.currentTimeMillis() - startTime;
		System.out.println("millis duration for not 1000 in 20k: " + diff);
	}

	@Test
	public void isNotInSearch1000IdIn40000records()
			throws Exception {
		List<String> ids = addAndGetIdRecords(40000).subList(0, 1000);

		condition = from(zeSchema.instance()).where(Schemas.IDENTIFIER).isNotIn(ids);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);

		long startTime = System.currentTimeMillis();
		searchServices.search(query);
		long diff = System.currentTimeMillis() - startTime;
		System.out.println("millis duration for not 1000 in 40k: " + diff);
	}

	private Record newRecordOfZeSchema() {
		return recordServices.newRecordWithSchema(zeSchema.instance());
	}

	private List<String> addAndGetIdRecords(int numberRecords)
			throws Exception {

		List<String> ids = new ArrayList<>();
		BulkRecordTransactionHandler handler = new BulkRecordTransactionHandler(recordServices, "SearchServiceLinearLoadTest");
		for (int i = 0; i < numberRecords; i++) {
			Record record = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "records#" + i);
			handler.append(record);
			ids.add(record.getId());
		}
		handler.closeAndJoin();

		return ids;
	}

}
