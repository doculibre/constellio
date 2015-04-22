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
package com.constellio.model.services.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ApprovalTask;
import com.constellio.model.entities.records.wrappers.Task;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.workflows.execution.WorkflowExecution;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.workflows.execution.WorkflowExecutionService;
import com.constellio.sdk.tests.ConstellioTest;

public class TaskServicesUnitTest extends ConstellioTest {

	@Mock RecordServices recordServices;
	@Mock WorkflowExecutionService workflowExecutionService;
	@Mock SearchServices searchServices;
	@Mock MetadataSchemasManager metadataSchemasManager;
	@Mock MetadataSchemaTypes metadataSchemaTypes;
	@Mock MetadataSchemaType metadataSchemaType;
	@Mock MetadataSchema defaultSchema;
	@Mock MetadataSchema taskSchema;
	@Mock Metadata workflowIdMetadata;
	@Mock LogicalSearchCondition condition;
	@Mock Record record;
	@Mock Task task;
	@Mock ApprovalTask approvalTask;
	@Mock User user;
	@Mock WorkflowExecution execution;

	TaskServices taskServices;

	@Before
	public void setup()
			throws Exception {

		when(execution.getCollection()).thenReturn(zeCollection);
		when(execution.getVariables()).thenReturn(new HashMap<String, String>());
		when(metadataSchemasManager.getSchemaTypes(zeCollection)).thenReturn(metadataSchemaTypes);
		when(metadataSchemaTypes.getSchemaType(Task.SCHEMA_TYPE)).thenReturn(metadataSchemaType);
		when(metadataSchemaType.getDefaultSchema()).thenReturn(defaultSchema);
		when(defaultSchema.getMetadata(Task.WORKFLOW_ID)).thenReturn(workflowIdMetadata);

		taskServices = spy(new TaskServices(recordServices, searchServices, workflowExecutionService, metadataSchemasManager));

		doReturn(condition).when(taskServices).newCondition(execution, metadataSchemaType, workflowIdMetadata);
		doReturn(record).when(searchServices).searchSingleResult(condition);
		doReturn(task).when(taskServices).newTask(record, metadataSchemaTypes);
		doReturn(approvalTask).when(taskServices).newAprovalTask(record, metadataSchemaTypes);
	}

	@Test
	public void whenNewTaskThenReturnIt()
			throws Exception {
		when(record.getSchemaCode()).thenReturn(Task.SCHEMA_TYPE);

		taskServices.newRelativeTask(record, metadataSchemaTypes);

		verify(taskServices).newTask(record, metadataSchemaTypes);
	}

	@Test
	public void givenAprovalRecordSchemaCodeWhenNewTaskThenReturnIt()
			throws Exception {
		when(record.getSchemaCode()).thenReturn(ApprovalTask.SCHEMA_CODE);

		taskServices.newRelativeTask(record, metadataSchemaTypes);

		verify(taskServices).newAprovalTask(record, metadataSchemaTypes);
	}

	@Test
	public void whenGetCurrentWorkflowManualTaskThenReturnTask()
			throws Exception {

		when(record.getSchemaCode()).thenReturn(ApprovalTask.SCHEMA_CODE);
		doReturn(task).when(taskServices).newRelativeTask(record, metadataSchemaTypes);

		taskServices.getCurrentWorkflowManualTask(execution);

		InOrder inOrder = Mockito.inOrder(execution, metadataSchemasManager, metadataSchemaTypes, metadataSchemaType,
				defaultSchema, searchServices, taskServices);

		inOrder.verify(execution).getCollection();
		inOrder.verify(metadataSchemasManager).getSchemaTypes(zeCollection);
		inOrder.verify(metadataSchemaTypes).getSchemaType(Task.SCHEMA_TYPE);
		inOrder.verify(metadataSchemaType).getDefaultSchema();
		inOrder.verify(defaultSchema).getMetadata(Task.WORKFLOW_ID);
		inOrder.verify(searchServices).searchSingleResult(condition);
		inOrder.verify(taskServices).newRelativeTask(record, metadataSchemaTypes);
	}

	@Test
	public void givenNullRecordWhenGetCurrentWorkflowManualTaskThenReturnNull()
			throws Exception {
		when(searchServices.searchSingleResult(condition)).thenReturn(null);

		Task retrievedTask = taskServices.getCurrentWorkflowManualTask(execution);

		assertThat(retrievedTask).isNull();
	}

	@Test
	public void whenFinishThenOk()
			throws Exception {

		when(task.getWorkflowId()).thenReturn("workflowId");
		when(task.getSchema()).thenReturn(taskSchema);
		when(taskSchema.getCollection()).thenReturn(zeCollection);

		InOrder inOrder = verifyCalledMethodsOrder();
		inOrder.verify(workflowExecutionService).markAsWaitingForSystem(task.getSchema().getCollection(), "workflowId");
	}

	@Test
	public void givenWorkflowIdNullWhenFinishThenOk()
			throws Exception {

		when(workflowExecutionService.getWorkflow(anyString(), anyString())).thenReturn(execution);
		taskServices.finish(task, user);

		verifyCalledMethodsOrder();
	}

	private InOrder verifyCalledMethodsOrder()
			throws RecordServicesException {

		when(workflowExecutionService.getWorkflow(anyString(), anyString())).thenReturn(execution);
		taskServices.finish(task, user);

		InOrder inOrder = Mockito.inOrder(task, taskSchema, recordServices, workflowExecutionService);
		inOrder.verify(task).set(Task.FINISHED_BY, user.getId());
		inOrder.verify(task).set(eq(Task.FINISHED_ON), any(LocalDateTime.class));
		inOrder.verify(recordServices).update(task);
		inOrder.verify(task).getWorkflowId();
		return inOrder;
	}
}
