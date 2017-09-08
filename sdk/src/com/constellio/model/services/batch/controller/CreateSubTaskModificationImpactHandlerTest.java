package com.constellio.model.services.batch.controller;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.ModificationImpact;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class CreateSubTaskModificationImpactHandlerTest extends ConstellioTest {

	@Mock SearchServices searchServices;
	@Mock RecordServices recordServices;

	@Mock MetadataSchemaTypes metadataSchemaTypes;

	@Mock TaskList taskList;

	@Mock ModificationImpact modificationImpact;
	@Mock ModificationImpact anotherModificationImpact;
	CreateSubTaskModificationImpactHandler handler;

	@Mock List<Record> firstBatch;
	@Mock List<Record> secondBatch;

	@Mock List<Metadata> reindexedMetadatas;
	@Mock List<String> reindexedMetadatasCodes;
	@Mock SchemaUtils schemaUtils;
	@Mock User user;

	@Before
	public void setUp()
			throws Exception {
		handler = spy(new CreateSubTaskModificationImpactHandler(searchServices, recordServices, metadataSchemaTypes, taskList, user));

		doReturn(schemaUtils).when(handler).newSchemaUtils();
		when(schemaUtils.toMetadataCodes(reindexedMetadatas)).thenReturn(reindexedMetadatasCodes);

		when(modificationImpact.getMetadataToReindex()).thenReturn(reindexedMetadatas);
	}

	@Test
	public void givenMultipleModificationsImpactThenHandleAllOfThem()
			throws Exception {

		doNothing().when(handler).handleModificationImpact(any(ModificationImpact.class));

		handler.prepareToHandle(modificationImpact);
		handler.prepareToHandle(anotherModificationImpact);
		verify(handler, never()).handleModificationImpact(any(ModificationImpact.class));

		handler.handle();
		verify(handler).handleModificationImpact(modificationImpact);
		verify(handler).handleModificationImpact(anotherModificationImpact);
	}

	@Test
	public void givenNoModificationsImpactThenNothingToHandle()
			throws Exception {

		handler.handle();
		verify(handler, never()).handleModificationImpact(any(ModificationImpact.class));
	}

	@Test
	public void givenMultipleBatchOfIdsThenStartMultipleSubTasks()
			throws Exception {

		doNothing().when(handler).createSubTask(any(List.class), any(List.class));

		Iterator<List<Record>> iterator = Arrays.asList(firstBatch, secondBatch).iterator();
		doReturn(iterator).when(handler).getBatchsIterator(modificationImpact);

		handler.handleModificationImpact(modificationImpact);

		verify(handler).createSubTask(firstBatch, reindexedMetadatasCodes);
		verify(handler).createSubTask(secondBatch, reindexedMetadatasCodes);
	}

	@Test
	public void whenCreateSubTaskThenAddTask()
			throws Exception {

		handler.createSubTask(new ArrayList<Record>(), new ArrayList<String>());

		verify(taskList).addSubTask(any(BatchProcessTask.class));
	}
}
