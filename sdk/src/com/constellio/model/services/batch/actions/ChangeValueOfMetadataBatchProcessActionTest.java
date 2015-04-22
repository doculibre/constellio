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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.sdk.tests.ConstellioTest;

public class ChangeValueOfMetadataBatchProcessActionTest extends ConstellioTest {

	String changedMetadataCode1 = "code1";
	String changedMetadataCode2 = "code2";
	@Mock Metadata changedMetadata1;
	@Mock Metadata changedMetadata2;
	@Mock MetadataSchemaTypes schemaTypes;

	@Mock Record record1;
	@Mock Record record2;
	@Mock Record record3;
	List<Record> batch;
	Map<String, Object> metadataChangedValues;

	ChangeValueOfMetadataBatchProcessAction action;

	@Mock Object changedValue1;
	@Mock Object changedValue2;

	@Before
	public void setUp()
			throws Exception {
		metadataChangedValues = new HashMap<>();
		metadataChangedValues.put(changedMetadataCode1, changedValue1);
		metadataChangedValues.put(changedMetadataCode2, changedValue2);

		action = new ChangeValueOfMetadataBatchProcessAction(metadataChangedValues);

		batch = asList(record1, record2, record3);
		when(record1.getId()).thenReturn(aString());
		when(record2.getId()).thenReturn(aString());
		when(record3.getId()).thenReturn(aString());

		when(schemaTypes.getMetadata(changedMetadataCode1)).thenReturn(changedMetadata1);
		when(schemaTypes.getMetadata(changedMetadataCode2)).thenReturn(changedMetadata2);
	}

	@Test
	public void whenGetParametersThenReturnMetadataChangedValues()
			throws Exception {

		Object[] parameters = action.getInstanceParameters();
		assertThat(parameters).containsOnly(metadataChangedValues);
	}

	@Test
	public void whenExecutingThenCreateTransactionSetForcedChangeFieldsForAllRecords()
			throws Exception {

		Transaction transaction = action.execute(batch, schemaTypes);

		assertThat(transaction.getRecords()).containsOnly(record1, record2, record3);
		for (Record record : batch) {
			verify(record).set(changedMetadata1, changedValue1);
			verify(record).set(changedMetadata2, changedValue2);
		}
	}

}
