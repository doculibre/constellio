/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;

public class SearchServiceSortAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	SearchServices searchServices;
	RecordDao recordDao;

	SearchServiceAcceptanceTestSchemas schema = new SearchServiceAcceptanceTestSchemas(zeCollection);
	SearchServiceAcceptanceTestSchemas.ZeSchemaMetadatas zeSchema = schema.new ZeSchemaMetadatas();

	@Before
	public void setUp()
			throws Exception {
		recordServices = getModelLayerFactory().newRecordServices();
		recordDao = spy(getDataLayerFactory().newRecordDao());
		searchServices = new SearchServices(recordDao, recordServices);

		defineSchemasManager()
				.using(schema.withADateTimeMetadata().withANumberMetadata().withAStringMetadata());
	}

	@Test
	public void whenSortingOnDatesThenCorrectOrder()
			throws Exception {

		LocalDateTime dateTime1 = null;
		LocalDateTime dateTime2 = new LocalDateTime(2014, 2, 1, 1, 1, 1);
		LocalDateTime dateTime3 = new LocalDateTime(2014, 2, 1, 1, 1, 2);
		LocalDateTime dateTime4 = new LocalDateTime(2015, 2, 1, 1, 1, 1);
		LocalDateTime dateTime5 = new LocalDateTime(2015, 2, 1, 1, 1, 2);

		Transaction transaction = new Transaction();
		Record record4 = transaction.add(new TestRecord(zeSchema).set(zeSchema.dateTimeMetadata(), dateTime4));
		Record record2 = transaction.add(new TestRecord(zeSchema).set(zeSchema.dateTimeMetadata(), dateTime2));
		Record record5 = transaction.add(new TestRecord(zeSchema).set(zeSchema.dateTimeMetadata(), dateTime5));
		Record record3 = transaction.add(new TestRecord(zeSchema).set(zeSchema.dateTimeMetadata(), dateTime3));
		Record recordWithNull = transaction.add(new TestRecord(zeSchema).set(zeSchema.dateTimeMetadata(), dateTime1));
		recordServices.execute(transaction);

		assertThat(searchServices.search(findAllQuery().sortAsc(zeSchema.dateTimeMetadata())))
				.containsExactly(record2, record3, record4, record5, recordWithNull);
		assertThat(searchServices.searchRecordIds(findAllQuery().sortAsc(zeSchema.dateTimeMetadata())))
				.containsExactly(record2.getId(), record3.getId(), record4.getId(), record5.getId(), recordWithNull.getId());
		assertThat(searchServices.search(findAllQuery().sortDesc(zeSchema.dateTimeMetadata())))
				.containsExactly(recordWithNull, record5, record4, record3, record2);
		assertThat(searchServices.searchRecordIds(findAllQuery().sortDesc(zeSchema.dateTimeMetadata())))
				.containsExactly(recordWithNull.getId(), record5.getId(), record4.getId(), record3.getId(), record2.getId());

	}

	@Test
	public void whenSortingOnNumbersThenCorrectOrder()
			throws Exception {

		Double number1 = null;
		Double number2 = -3.0;
		Double number3 = 1.1;
		Double number4 = 1.2;
		Double number5 = 2.1;
		Double number6 = 10.1;
		Double number7 = 3456347.1234;
		Double number8 = 109842477384.1;

		Transaction transaction = new Transaction();
		Record record4 = transaction.add(new TestRecord(zeSchema, "4").set(zeSchema.numberMetadata(), number4));
		Record record8 = transaction.add(new TestRecord(zeSchema, "8").set(zeSchema.numberMetadata(), number8));
		Record record2 = transaction.add(new TestRecord(zeSchema, "2").set(zeSchema.numberMetadata(), number2));
		Record record5 = transaction.add(new TestRecord(zeSchema, "5").set(zeSchema.numberMetadata(), number5));
		Record record7 = transaction.add(new TestRecord(zeSchema, "7").set(zeSchema.numberMetadata(), number7));
		Record record3 = transaction.add(new TestRecord(zeSchema, "3").set(zeSchema.numberMetadata(), number3));
		Record record6 = transaction.add(new TestRecord(zeSchema, "6").set(zeSchema.numberMetadata(), number6));
		Record record1 = transaction.add(new TestRecord(zeSchema, "1").set(zeSchema.numberMetadata(), number1));
		recordServices.execute(transaction);

		assertThat(searchServices.search(findAllQuery().sortAsc(zeSchema.numberMetadata())))
				.containsExactly(record1, record2, record3, record4, record5, record6, record7, record8);
		assertThat(searchServices.searchRecordIds(findAllQuery().sortAsc(zeSchema.numberMetadata())))
				.containsExactly(record1.getId(), record2.getId(), record3.getId(), record4.getId(), record5.getId(),
						record6.getId(), record7.getId(), record8.getId());
		assertThat(searchServices.search(findAllQuery().sortDesc(zeSchema.numberMetadata())))
				.containsExactly(record8, record7, record6, record5, record4, record3, record2, record1);
		assertThat(searchServices.searchRecordIds(findAllQuery().sortDesc(zeSchema.numberMetadata())))
				.containsExactly(record8.getId(), record7.getId(), record6.getId(), record5.getId(), record4.getId(),
						record3.getId(), record2.getId(),
						record1.getId());

	}

	@Test
	public void whenSortingOnTextThenCorrectOrder()
			throws Exception {

		String text1 = null;
		String text2 = "a";
		String text3 = "a b";
		String text4 = "ab";
		String text5 = "";

		Transaction transaction = new Transaction();
		Record record4 = transaction.add(new TestRecord(zeSchema, "4").set(zeSchema.stringMetadata(), text4));
		Record record2 = transaction.add(new TestRecord(zeSchema, "2").set(zeSchema.stringMetadata(), text2));
		Record record5 = transaction.add(new TestRecord(zeSchema, "5").set(zeSchema.stringMetadata(), text5));
		Record record3 = transaction.add(new TestRecord(zeSchema, "3").set(zeSchema.stringMetadata(), text3));
		Record record1 = transaction.add(new TestRecord(zeSchema, "1").set(zeSchema.stringMetadata(), text1));
		recordServices.execute(transaction);

		assertThat(searchServices.search(findAllQuery().sortAsc(zeSchema.stringMetadata())))
				.containsExactly(record1, record2, record3, record4, record5);
		assertThat(searchServices.searchRecordIds(findAllQuery().sortAsc(zeSchema.stringMetadata())))
				.containsExactly(record1.getId(), record2.getId(), record3.getId(), record4.getId(), record5.getId());
		assertThat(searchServices.search(findAllQuery().sortDesc(zeSchema.stringMetadata())))
				.containsExactly(record4, record3, record2, record1, record5);
		assertThat(searchServices.searchRecordIds(findAllQuery().sortDesc(zeSchema.stringMetadata())))
				.containsExactly(record4.getId(), record3.getId(), record2.getId(), record1.getId(), record5.getId());

	}

	@Test
	public void whenSortingOnMultipleFieldsThenCorrectOrder()
			throws Exception {
		LocalDateTime dateTime1 = new LocalDateTime(2014, 2, 1, 1, 1, 1);
		LocalDateTime dateTime2 = new LocalDateTime(2015, 2, 1, 1, 1, 2);
		Double number1 = 1.1;
		Double number2 = 1.2;

		Transaction transaction = new Transaction();
		Record record4 = transaction.add(new TestRecord(zeSchema, "4").set(zeSchema.dateTimeMetadata(), dateTime2).set(
				zeSchema.numberMetadata(), number2));
		Record record2 = transaction.add(new TestRecord(zeSchema, "2").set(zeSchema.dateTimeMetadata(), dateTime1).set(
				zeSchema.numberMetadata(), number2));
		Record record3 = transaction.add(new TestRecord(zeSchema, "3").set(zeSchema.dateTimeMetadata(), dateTime2).set(
				zeSchema.numberMetadata(), number1));
		Record record1 = transaction.add(new TestRecord(zeSchema, "1").set(zeSchema.dateTimeMetadata(), dateTime1).set(
				zeSchema.numberMetadata(), number1));
		recordServices.execute(transaction);

		assertThat(searchServices.search(findAllQuery().sortAsc(zeSchema.dateTimeMetadata()).sortAsc(zeSchema.numberMetadata())))
				.containsExactly(record1, record2, record3, record4);
		assertThat(searchServices.searchRecordIds(findAllQuery().sortAsc(
				zeSchema.dateTimeMetadata()).sortAsc(zeSchema.numberMetadata())))
				.containsExactly(record1.getId(), record2.getId(), record3.getId(), record4.getId());

		assertThat(searchServices.search(findAllQuery().sortDesc(zeSchema.dateTimeMetadata()).sortAsc(zeSchema.numberMetadata())))
				.containsExactly(record3, record4, record1, record2);
		assertThat(searchServices.searchRecordIds(findAllQuery().sortDesc(zeSchema.dateTimeMetadata()).sortAsc(
				zeSchema.numberMetadata())))
				.containsExactly(record3.getId(), record4.getId(), record1.getId(), record2.getId());

		assertThat(searchServices.search(findAllQuery().sortAsc(zeSchema.dateTimeMetadata()).sortDesc(zeSchema.numberMetadata())))
				.containsExactly(record2, record1, record4, record3);
		assertThat(searchServices
				.searchRecordIds(findAllQuery().sortAsc(zeSchema.dateTimeMetadata()).sortDesc(zeSchema.numberMetadata())))
				.containsExactly(record2.getId(), record1.getId(), record4.getId(), record3.getId());

	}

	private LogicalSearchQuery findAllQuery() {
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(zeSchema.instance()).returnAll();
		return new LogicalSearchQuery(condition);
	}
}
