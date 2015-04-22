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
package com.constellio.model.services.workflows;

import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Task;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.workflows.definitions.AllUsersSelector;
import com.constellio.model.entities.workflows.definitions.BPMNProperty;
import com.constellio.model.entities.workflows.definitions.WorkflowAction;
import com.constellio.model.entities.workflows.definitions.WorkflowDefinition;
import com.constellio.model.entities.workflows.definitions.WorkflowRouting;
import com.constellio.model.entities.workflows.definitions.WorkflowRoutingDestination;
import com.constellio.model.entities.workflows.definitions.WorkflowServiceTask;
import com.constellio.model.entities.workflows.definitions.WorkflowUserTask;
import com.constellio.model.entities.workflows.execution.WorkflowExecutedTask;
import com.constellio.model.entities.workflows.execution.WorkflowExecution;
import com.constellio.model.entities.workflows.trigger.Trigger;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.tasks.TaskServices;
import com.constellio.model.services.workflows.config.WorkflowsConfigManager;
import com.constellio.model.services.workflows.execution.WorkflowExecutionService;
import com.constellio.sdk.tests.ConstellioTest;

public class WorkflowExecutorUnitTest extends ConstellioTest {

	@Mock BPMNProperty bpmnProperty, bpmnProperty2, bpmnProperty3;
	@Mock AllUsersSelector allUsersSelector;

	@Mock Record newTaskRecord;
	@Mock Task newTask;
	@Mock MetadataSchemasManager metadataSchemasManager;
	@Mock MetadataSchemaTypes metadataSchemaTypes;
	@Mock MetadataSchemaType metadataSchemaType;
	@Mock MetadataSchema metadataCustomSchema;
	@Mock Metadata zeMetadata;
	String zeMetadataCode = "zeMetadata";

	@Mock ModelLayerFactory modelLayerFactory;
	@Mock WorkflowAction systemTaskAction;
	@Mock WorkflowServiceTask systemTask;
	@Mock WorkflowUserTask userTask;
	@Mock WorkflowRouting routingFromStartToLEtape, routingFromLEtapeToSystemTask, routingFromSystemTaskToUserTask;
	String workflowExecutionId = "workflowExecutionId";
	@Mock WorkflowExecution workflowExecution, workflowExecution2, workflowExecution3;
	@Mock WorkflowDefinition workflowDefinition, workflowDefinition2, workflowDefinition3;

	@Mock CollectionsListManager collectionsListManager;
	@Mock WorkflowExecutionService workflowExecutionService;
	@Mock WorkflowsConfigManager workflowsConfigManager;
	@Mock TaskServices taskServices;
	@Mock RecordServices recordServices;
	WorkflowExecutor workflowExecutor;

	@Before
	public void setUp()
			throws Exception {

		when(modelLayerFactory.newWorkflowExecutionService()).thenReturn(workflowExecutionService);
		when(modelLayerFactory.getWorkflowsConfigManager()).thenReturn(workflowsConfigManager);
		when(modelLayerFactory.newTaskServices()).thenReturn(taskServices);
		when(modelLayerFactory.getCollectionsListManager()).thenReturn(collectionsListManager);
		when(modelLayerFactory.getMetadataSchemasManager()).thenReturn(metadataSchemasManager);
		when(modelLayerFactory.newRecordServices()).thenReturn(recordServices);

		when(workflowExecution.getId()).thenReturn(workflowExecutionId);
		when(workflowExecution.getCollection()).thenReturn("zeUltimateCollection");
		when(metadataCustomSchema.getMetadata(zeMetadataCode)).thenReturn(zeMetadata);

		workflowExecutor = spy(new WorkflowExecutor(modelLayerFactory));

	}

	@Test
	public void givenMultipleWaitingWorkflowThenHandleEachWorkflow()
			throws Exception {

		when(workflowExecution.getWorkflowDefinitionId()).thenReturn("workflowDefinition");
		when(workflowExecution2.getWorkflowDefinitionId()).thenReturn("workflowDefinition2");
		when(workflowExecution3.getWorkflowDefinitionId()).thenReturn("workflowDefinition3");
		when(workflowsConfigManager.getWorkflowDefinition("collection1", "workflowDefinition")).thenReturn(workflowDefinition);
		when(workflowsConfigManager.getWorkflowDefinition("collection1", "workflowDefinition2")).thenReturn(workflowDefinition2);
		when(workflowsConfigManager.getWorkflowDefinition("collection2", "workflowDefinition3")).thenReturn(workflowDefinition3);
		doNothing().when(workflowExecutor).handleWaitingWorkflow(any(WorkflowExecution.class), any(WorkflowDefinition.class));
		when(collectionsListManager.getCollections()).thenReturn(asList("collection1", "collection2"));
		when(workflowExecutionService.getNextWorkflowWaitingForSystemProcessing("collection1")).thenReturn(
				asList(workflowExecution, workflowExecution2));
		when(workflowExecutionService.getNextWorkflowWaitingForSystemProcessing("collection2")).thenReturn(
				asList(workflowExecution3));

		workflowExecutor.handleWaitingWorkflows();

		verify(workflowExecutor).handleWaitingWorkflow(workflowExecution, workflowDefinition);
		verify(workflowExecutor).handleWaitingWorkflow(workflowExecution2, workflowDefinition2);
		verify(workflowExecutor).handleWaitingWorkflow(workflowExecution3, workflowDefinition3);
	}

	@Test
	public void givenTwoRoutingAndOneSystemTaskBeforeReachingNextManualTaskThenAllExecuted()
			throws Exception {

		WorkflowExecution workflowExecution = spy(new WorkflowExecution(workflowExecutionId, "workflowDefinition",
				LocalDateTime.now(), null, Trigger.manual("folder_default"), Arrays.asList("zeRecordId"), "start", null,
				new HashMap<String, String>(), new ArrayList<WorkflowExecutedTask>(), zeCollection));

		doNothing().when(workflowExecutor).prepareUserTask(userTask, workflowExecution);

		when(workflowDefinition.getRouting("start")).thenReturn(routingFromStartToLEtape);
		when(workflowDefinition.getRouting("letape")).thenReturn(routingFromLEtapeToSystemTask);
		when(workflowDefinition.getRouting("aSystemTaskRouting")).thenReturn(routingFromSystemTaskToUserTask);
		when(routingFromStartToLEtape.getDestination(workflowExecution)).thenReturn("letape");
		when(routingFromLEtapeToSystemTask.getDestination(workflowExecution)).thenReturn("aSystemTask");
		when(routingFromSystemTaskToUserTask.getDestination(workflowExecution)).thenReturn("aUserTask");
		when(systemTask.getSingleDestination()).thenReturn("aSystemTaskRouting");

		when(workflowDefinition.getTask("aSystemTask")).thenReturn(systemTask);
		when(workflowDefinition.getTask("aUserTask")).thenReturn(userTask);
		when(systemTask.getAction()).thenReturn(systemTaskAction);

		workflowExecutor.handleWaitingWorkflow(workflowExecution, workflowDefinition);

		InOrder inOrder = inOrder(routingFromStartToLEtape, routingFromLEtapeToSystemTask, routingFromSystemTaskToUserTask,
				systemTaskAction, workflowExecutor, workflowExecutionService);
		inOrder.verify(routingFromStartToLEtape).getDestination(workflowExecution);
		inOrder.verify(routingFromLEtapeToSystemTask).getDestination(workflowExecution);
		inOrder.verify(systemTaskAction).execute(workflowExecution, modelLayerFactory);
		inOrder.verify(routingFromSystemTaskToUserTask).getDestination(workflowExecution);
		inOrder.verify(workflowExecutor).prepareUserTask(userTask, workflowExecution);
		inOrder.verify(workflowExecutionService).addUpdateWorkflowExecution(workflowExecution);
		inOrder.verify(workflowExecutionService).markAsNotWaitingForSystem(zeCollection, workflowExecutionId);

	}

	@Test
	public void givenTwoRoutingAndOneSystemTaskBeforeReachinEndThenAllExecutedAndWorkflowDeleted()
			throws Exception {

		WorkflowExecution workflowExecution = spy(new WorkflowExecution(workflowExecutionId, "workflowDefinition",
				LocalDateTime.now(), null, Trigger.manual("folder_default"), Arrays.asList("zeRecordId"), "start", null,
				new HashMap<String, String>(), new ArrayList<WorkflowExecutedTask>(), zeCollection));

		doNothing().when(workflowExecutor).prepareUserTask(userTask, workflowExecution);

		when(workflowDefinition.getRouting("start")).thenReturn(routingFromStartToLEtape);
		when(workflowDefinition.getRouting("letape")).thenReturn(routingFromLEtapeToSystemTask);
		when(workflowDefinition.getRouting("aSystemTaskRouting")).thenReturn(routingFromSystemTaskToUserTask);
		when(routingFromStartToLEtape.getDestination(workflowExecution)).thenReturn("letape");
		when(routingFromLEtapeToSystemTask.getDestination(workflowExecution)).thenReturn("aSystemTask");
		when(routingFromSystemTaskToUserTask.getDestination(workflowExecution)).thenReturn(
				WorkflowRoutingDestination.DESTINATION_END);
		when(systemTask.getSingleDestination()).thenReturn("aSystemTaskRouting");

		when(workflowDefinition.getTask("aSystemTask")).thenReturn(systemTask);
		when(workflowDefinition.getTask("aUserTask")).thenReturn(userTask);
		when(systemTask.getAction()).thenReturn(systemTaskAction);

		workflowExecutor.handleWaitingWorkflow(workflowExecution, workflowDefinition);

		InOrder inOrder = inOrder(routingFromStartToLEtape, routingFromLEtapeToSystemTask, routingFromSystemTaskToUserTask,
				systemTaskAction, workflowExecutor, workflowExecutionService);
		inOrder.verify(routingFromStartToLEtape).getDestination(workflowExecution);
		inOrder.verify(routingFromLEtapeToSystemTask).getDestination(workflowExecution);
		inOrder.verify(systemTaskAction).execute(workflowExecution, modelLayerFactory);
		inOrder.verify(routingFromSystemTaskToUserTask).getDestination(workflowExecution);
		inOrder.verify(workflowExecutionService).remove(workflowExecution);

	}

	@Test
	public void whenPreparingWorkflowTaskUserTaskThenCreateRecordOfCorrectSchemaSetMetadatasBasedOnBPMNFieldsAndSaveTheRecord()
			throws Exception {

		ArgumentCaptor<Transaction> transactionArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);

		doNothing().when(workflowExecutor).fillTaskMetadatas(any(Task.class), any(WorkflowUserTask.class),
				any(MetadataSchema.class), any(WorkflowExecution.class));
		when(recordServices.newRecordWithSchema(metadataCustomSchema)).thenReturn(newTaskRecord);
		when(taskServices.newRelativeTask(newTaskRecord, metadataSchemaTypes)).thenReturn(newTask);
		when(userTask.getTaskSchema()).thenReturn("task_custom");
		when(metadataSchemasManager.getSchemaTypes("zeUltimateCollection")).thenReturn(metadataSchemaTypes);
		when(metadataSchemaTypes.getSchema("task_custom")).thenReturn(metadataCustomSchema);

		workflowExecutor.prepareUserTask(userTask, workflowExecution);

		InOrder inOrder = inOrder(workflowExecutor, recordServices, taskServices);
		inOrder.verify(recordServices).newRecordWithSchema(metadataCustomSchema);
		inOrder.verify(taskServices).newRelativeTask(newTaskRecord, metadataSchemaTypes);
		inOrder.verify(workflowExecutor).fillTaskMetadatas(newTask, userTask, metadataCustomSchema, workflowExecution);
		inOrder.verify(recordServices).execute(transactionArgumentCaptor.capture());

		assertThat(transactionArgumentCaptor.getValue().isSkippingRequiredValuesValidation()).isTrue();
		assertThat(transactionArgumentCaptor.getValue().getRecords()).containsOnly(newTaskRecord);

	}

	@Test
	public void whenFillingTaskMetadatasThenSetRecordIdUserCandidatesDueDateAndOtherMetadatasBasedOnBPMNFields() {

		doNothing().when(workflowExecutor).setAssignCandidates(any(Task.class), any(AllUsersSelector.class),
				any(WorkflowExecution.class));
		doNothing().when(workflowExecutor).setDueDate(any(Task.class), anyInt());
		doNothing().when(workflowExecutor).setMetadataBasedOnBPMNFields(any(Task.class), any(BPMNProperty.class),
				any(MetadataSchema.class), any(WorkflowExecution.class));

		when(workflowExecution.getId()).thenReturn("theWorkflowId");
		when(workflowExecution.getRecordIds()).thenReturn(Arrays.asList("theWorkflowRecordId"));

		when(userTask.getUserSelector()).thenReturn(allUsersSelector);
		when(userTask.getDueDateInDays()).thenReturn(42);
		when(userTask.getFields()).thenReturn(asList(bpmnProperty, bpmnProperty2, bpmnProperty3));

		workflowExecutor.fillTaskMetadatas(newTask, userTask, metadataCustomSchema, workflowExecution);

		verify(newTask).setWorkflowId("theWorkflowId");
		verify(newTask).setWorkflowRecordIds(Arrays.asList("theWorkflowRecordId"));
		verify(workflowExecutor).setAssignCandidates(newTask, allUsersSelector, workflowExecution);
		verify(workflowExecutor).setDueDate(newTask, 42);
		verify(workflowExecutor).setMetadataBasedOnBPMNFields(newTask, bpmnProperty, metadataCustomSchema, workflowExecution);
		verify(workflowExecutor).setMetadataBasedOnBPMNFields(newTask, bpmnProperty2, metadataCustomSchema, workflowExecution);
		verify(workflowExecutor).setMetadataBasedOnBPMNFields(newTask, bpmnProperty3, metadataCustomSchema, workflowExecution);

	}

	@Test
	public void whenSetAssignedUserThenSetValidValues() {

		User alice = mock(User.class);
		when(alice.getId()).thenReturn("aliceId");
		User dakota = mock(User.class);
		when(dakota.getId()).thenReturn("dakotaId");
		User bob = mock(User.class);
		when(bob.getId()).thenReturn("bobId");

		when(allUsersSelector.getCandidateUsers(workflowExecution, modelLayerFactory)).thenReturn(
				Arrays.asList(alice, dakota, bob));

		workflowExecutor.setAssignCandidates(newTask, allUsersSelector, workflowExecution);

		verify(newTask).setAssignCandidates(asList("aliceId", "dakotaId", "bobId"));

	}

	@Test
	public void givenExpressionInFieldWhenSetMetadataBasedOnBPMNFieldThenMetadataSet() {
		when(bpmnProperty.getFieldId()).thenReturn(zeMetadataCode);
		when(bpmnProperty.getExpressionValue()).thenReturn("zeExpressionValue");
		when(zeMetadata.getLocalCode()).thenReturn(zeMetadataCode);

		workflowExecutor.setMetadataBasedOnBPMNFields(newTask, bpmnProperty, metadataCustomSchema, workflowExecution);
		verify(newTask).set(zeMetadataCode, "zeExpressionValue");
	}

	@Test
	public void givenVariableInFieldWhenSetMetadataBasedOnBPMNFieldThenMetadataSet() {
		when(bpmnProperty.getFieldId()).thenReturn(zeMetadataCode);
		when(bpmnProperty.getVariableCode()).thenReturn("zeVariableCode");
		when(zeMetadata.getLocalCode()).thenReturn(zeMetadataCode);
		when(workflowExecution.getVariable("zeVariableCode")).thenReturn("zeVariableValue");

		workflowExecutor.setMetadataBasedOnBPMNFields(newTask, bpmnProperty, metadataCustomSchema, workflowExecution);
		verify(newTask).set(zeMetadataCode, "zeVariableValue");
	}
}
