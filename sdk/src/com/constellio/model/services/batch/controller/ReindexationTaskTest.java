package com.constellio.model.services.batch.controller;

import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.*;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class ReindexationTaskTest extends ConstellioTest {

	@Mock SearchServices searchServices;
	@Mock List<Metadata> reindexedMetadatas;
	@Mock RecordServices recordServices;

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
	@Mock User user;

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

		when(record1.getCollection()).thenReturn(zeCollection);
		when(record1.getId()).thenReturn(recordId1);
		when(record2.getId()).thenReturn(recordId2);
		when(record2.getCollection()).thenReturn(zeCollection);
		when(record3.getId()).thenReturn(recordId3);
		when(record3.getCollection()).thenReturn(zeCollection);
		when(record4.getId()).thenReturn(recordId4);
		when(record4.getCollection()).thenReturn(zeCollection);
		when(metadataSchemaTypes.getCollection()).thenReturn(zeCollection);

		reindexationTask = spy(new BatchProcessTask(taskList, batch, action, recordServices, metadataSchemaTypes,
				searchServices, user));
		doReturn(handler).when(reindexationTask).createSubTaskImpactHandler();
	}

	@Test
	public void whenComputingThenGetRecordWithIdsAndExecuteReturningPassedErrorList()
			throws Exception {

		ArgumentCaptor<List> errorsList = ArgumentCaptor.forClass(List.class);

		List<String> returnedErrorsList = reindexationTask.compute();

		verify(reindexationTask).execute(eq(batch), errorsList.capture());
		assertThat(errorsList.getValue()).isSameAs(returnedErrorsList);
	}

	@Test
	public void whenExecutingTaskThenExecuteActionAndExecuteTransaction()
			throws Exception {

		when(action.execute(eq(batch), eq(metadataSchemaTypes), any(RecordProvider.class))).thenReturn(transaction);

		reindexationTask.execute(batch, errorList);

		verify(recordServices).executeWithImpactHandler(transaction, handler);
		assertThat(errorList).isEmpty();
	}

	@Test
	public void givenNullTransactionThenDoNotExecuteItAndDoNotMarkRecordsAsErrors()
			throws Exception {

		when(action.execute(eq(batch), eq(metadataSchemaTypes), any(RecordProvider.class))).thenReturn(null);

		reindexationTask.execute(batch, errorList);

		verifyZeroInteractions(recordServices);
		assertThat(errorList).isEmpty();
	}

	@Test
	public void givenRecordServicesExceptionWhenExecutingTransactionThenRecordIdInErrorList()
			throws Exception {

		when(action.execute(eq(batch), eq(metadataSchemaTypes), any(RecordProvider.class))).thenReturn(transaction);
		doThrow(RecordServicesException.class).when(recordServices).executeWithImpactHandler(transaction, handler);

		reindexationTask.execute(batch, errorList);

		assertThat(errorList).containsOnly(recordId1, recordId2, recordId3, recordId4);
	}

	@Test
	public void givenRecordServicesRuntimeExceptionWhenExecutingTransactionThenRecordIdInErrorList()
			throws Exception {

		when(action.execute(eq(batch), eq(metadataSchemaTypes), any(RecordProvider.class))).thenReturn(transaction);
		doThrow(RecordServicesRuntimeException.class).when(recordServices).executeWithImpactHandler(transaction, handler);

		reindexationTask.execute(batch, errorList);

		assertThat(errorList).containsOnly(recordId1, recordId2, recordId3, recordId4);
	}

	@Test
	public void givenExceptionWhenExecutingActionThenErrorListNotEmpty()
			throws Exception {
		doThrow(RuntimeException.class).when(action).execute(eq(batch), eq(metadataSchemaTypes), any(RecordProvider.class));

		reindexationTask.execute(batch, errorList);

		assertThat(errorList).containsOnly(recordId1, recordId2, recordId3, recordId4);
		verifyZeroInteractions(recordServices);

	}
}
