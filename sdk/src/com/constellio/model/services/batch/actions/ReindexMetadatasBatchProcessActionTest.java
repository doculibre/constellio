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
package com.constellio.model.services.batch.actions;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.sdk.tests.ConstellioTest;

public class ReindexMetadatasBatchProcessActionTest extends ConstellioTest {

	@Mock List<String> reindexedMetadataCodes;
	@Mock List<Metadata> reindexedMetadatas;
	@Mock MetadataSchemaTypes schemaTypes;

	@Mock Record record1;
	@Mock Record record2;
	@Mock Record record3;
	List<Record> batch;

	ReindexMetadatasBatchProcessAction action;

	@Before
	public void setUp()
			throws Exception {
		action = new ReindexMetadatasBatchProcessAction(reindexedMetadataCodes);

		batch = asList(record1, record2, record3);
		when(record1.getId()).thenReturn(aString());
		when(record2.getId()).thenReturn(aString());
		when(record3.getId()).thenReturn(aString());

		when(schemaTypes.getMetadatas(reindexedMetadataCodes)).thenReturn(reindexedMetadatas);
	}

	@Test
	public void whenGetParametersThenReturnMetadataCodes()
			throws Exception {

		Object[] parameters = action.getInstanceParameters();
		assertThat(parameters).containsOnly(reindexedMetadataCodes);
	}

	@Test
	public void whenExecutingThenCreateTransactionSetForcedReindexedFieldsOptionsAndAddRecords()
			throws Exception {

		Transaction transaction = action.execute(batch, schemaTypes);

		assertThat(transaction.getRecordUpdateOptions().getTransactionRecordsReindexation())
				.isEqualTo(new TransactionRecordsReindexation(reindexedMetadatas));
		assertThat(transaction.getRecords()).containsOnly(record1, record2, record3);
	}

}
