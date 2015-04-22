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
package com.constellio.model.services.search.iterators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServiceAcceptanceTestSchemas;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.eaio.uuid.UUID;

public class OptimizedLogicalSearchIteratorAcceptTest extends ConstellioTest {
	RecordServices recordServices;
	SearchServices searchServices;

	SearchServiceAcceptanceTestSchemas schema = new SearchServiceAcceptanceTestSchemas();
	SearchServiceAcceptanceTestSchemas.ZeSchemaMetadatas zeSchema = schema.new ZeSchemaMetadatas();
	SearchServiceAcceptanceTestSchemas.AnotherSchemaMetadatas anotherSchema = schema.new AnotherSchemaMetadatas();

	@Before
	public void setUp() {
		recordServices = getModelLayerFactory().newRecordServices();
		RecordDao recordDao = getDataLayerFactory().newRecordDao();
		searchServices = spy(new SearchServices(recordDao, recordServices));

	}

	@Test
	public void testName()
			throws Exception {
		System.out.println(new UUID().clockSeqAndNode);
		System.out.println(new com.eaio.uuid.UUID());
		System.out.println(new com.eaio.uuid.UUID());
		System.out.println(new com.eaio.uuid.UUID());
		System.out.println(new com.eaio.uuid.UUID());
		System.out.println(new com.eaio.uuid.UUID());
		System.out.println(new com.eaio.uuid.UUID());

		//System.out.println(new UUID());
	}

	@Test
	public void givenIsInSearchWhenIteratingThenSplitIsIn()
			throws Exception {

		ArgumentCaptor<LogicalSearchQuery> queryCaptor = ArgumentCaptor.forClass(LogicalSearchQuery.class);
		defineSchemasManager().using(schema.withAStringMetadata());
		Transaction transaction = new Transaction();

		for (int i = 1; i <= 2; i++) {
			TestRecord record = new TestRecord(zeSchema, "id_a" + i);
			record.set(zeSchema.stringMetadata(), "a");
			transaction.addUpdate(record);
		}
		for (int i = 1; i <= 2; i++) {
			TestRecord record = new TestRecord(zeSchema, "id_b" + i);
			record.set(zeSchema.stringMetadata(), "b");
			transaction.addUpdate(record);
		}
		for (int i = 1; i <= 2; i++) {
			TestRecord record = new TestRecord(zeSchema, "id_c" + i);
			record.set(zeSchema.stringMetadata(), "c");
			transaction.addUpdate(record);
		}
		recordServices.execute(transaction);

		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(zeSchema.instance())
				.where(zeSchema.stringMetadata()).isIn(Arrays.asList("a", "b", "c"));
		LogicalSearchQuery query = new LogicalSearchQuery(condition);

		Iterator<String> iterator = searchServices.optimizesRecordsIdsIterator(query, 2);
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo("id_a1");
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo("id_a2");
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo("id_b1");
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo("id_b2");
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo("id_c1");
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo("id_c2");
		assertThat(iterator.hasNext()).isFalse();

		verify(searchServices, times(3)).recordsIterator(queryCaptor.capture(), eq(2));

		assertThat(queryCaptor.getAllValues().get(0).getCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(zeSchema.instance())
						.where(zeSchema.stringMetadata()).isEqualTo("a"));

		assertThat(queryCaptor.getAllValues().get(1).getCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(zeSchema.instance())
						.where(zeSchema.stringMetadata()).isEqualTo("b"));

		assertThat(queryCaptor.getAllValues().get(2).getCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(zeSchema.instance())
						.where(zeSchema.stringMetadata()).isEqualTo("c"));

	}
}
