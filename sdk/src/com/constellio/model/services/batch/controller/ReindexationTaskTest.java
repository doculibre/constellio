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
package com.constellio.model.services.batch.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordModificationImpactHandler;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;

public class ReindexationTaskTest extends ConstellioTest {

	@Mock SearchServices searchServices;
	@Mock List<Metadata> reindexedMetadatas;
	@Mock RecordServices recordServices;

	@Mock List<String> recordIds;

	List<String> errorList;
	List<Record> batch;

	String recordId1 = "record1";
	String recordId2 = "record2";
	String recordId3 = "record3";
	String recordId4 = "record4";

	@Mock Record record1;
	@Mock Record record2;
	@Mock Record record3;
	@Mock Record record4;

	@Mock Transaction transaction;

	@Mock BatchProcessAction action;

	@Mock MetadataSchemaTypes metadataSchemaTypes;

	@Mock RecordModificationImpactHandler handler;

	@Mock TaskList taskList;

	private BatchProcessTask reindexationTask;

	@Before
	public void setUp() {
		errorList = new ArrayList<>();
		batch = new ArrayList<>();
		batch.add(record1);
		batch.add(record2);
		batch.add(record3);
		batch.add(record4);

		reindexationTask = spy(new BatchProcessTask(taskList, recordIds, action, recordServices, metadataSchemaTypes,
				searchServices));

		when(record1.getCollection()).thenReturn(zeCollection);
		when(record1.getId()).thenReturn(recordId1);
		when(record2.getId()).thenReturn(recordId2);
		when(record2.getCollection()).thenReturn(zeCollection);
		when(record3.getId()).thenReturn(recordId3);
		when(record3.getCollection()).thenReturn(zeCollection);
		when(record4.getId()).thenReturn(recordId4);
		when(record4.getCollection()).thenReturn(zeCollection);
		when(metadataSchemaTypes.getCollection()).thenReturn(zeCollection);
		doReturn(handler).when(reindexationTask).createSubTaskImpactHandler();
	}

	@Test
	public void whenComputingThenGetRecordWithIdsAndExecuteReturningPassedErrorList()
			throws Exception {

		ArgumentCaptor<List> errorsList = ArgumentCaptor.forClass(List.class);
		when(recordServices.getRecordsById(metadataSchemaTypes.getCollection(), recordIds)).thenReturn(batch);

		List<String> returnedErrorsList = reindexationTask.compute();

		verify(reindexationTask).execute(eq(batch), errorsList.capture());
		assertThat(errorsList.getValue()).isSameAs(returnedErrorsList);
	}

	@Test
	public void whenExecutingTaskThenExecuteActionAndExecuteTransaction()
			throws Exception {

		when(action.execute(batch, metadataSchemaTypes)).thenReturn(transaction);

		reindexationTask.execute(batch, errorList);

		verify(recordServices).executeWithImpactHandler(transaction, handler);
		assertThat(errorList).isEmpty();
	}

	@Test
	public void givenNullTransactionThenDoNotExecuteItAndDoNotMarkRecordsAsErrors()
			throws Exception {

		when(action.execute(batch, metadataSchemaTypes)).thenReturn(null);

		reindexationTask.execute(batch, errorList);

		verifyZeroInteractions(recordServices);
		assertThat(errorList).isEmpty();
	}

	@Test
	public void givenRecordServicesExceptionWhenExecutingTransactionThenRecordIdInErrorList()
			throws Exception {

		when(action.execute(batch, metadataSchemaTypes)).thenReturn(transaction);
		doThrow(RecordServicesException.class).when(recordServices).executeWithImpactHandler(transaction, handler);

		reindexationTask.execute(batch, errorList);

		assertThat(errorList).containsOnly(recordId1, recordId2, recordId3, recordId4);
	}

	@Test
	public void givenRecordServicesRuntimeExceptionWhenExecutingTransactionThenRecordIdInErrorList()
			throws Exception {

		when(action.execute(batch, metadataSchemaTypes)).thenReturn(transaction);
		doThrow(RecordServicesRuntimeException.class).when(recordServices).executeWithImpactHandler(transaction, handler);

		reindexationTask.execute(batch, errorList);

		assertThat(errorList).containsOnly(recordId1, recordId2, recordId3, recordId4);
	}

	@Test
	public void givenExceptionWhenExecutingActionThenErrorListNotEmpty()
			throws Exception {
		doThrow(RuntimeException.class).when(action).execute(batch, metadataSchemaTypes);

		reindexationTask.execute(batch, errorList);

		assertThat(errorList).containsOnly(recordId1, recordId2, recordId3, recordId4);
		verifyZeroInteractions(recordServices);

	}
}
