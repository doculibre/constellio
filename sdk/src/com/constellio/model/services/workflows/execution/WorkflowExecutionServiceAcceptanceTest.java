package com.constellio.model.services.workflows.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.workflows.definitions.WorkflowDefinition;
import com.constellio.model.entities.workflows.execution.WorkflowExecutedTask;
import com.constellio.model.entities.workflows.execution.WorkflowExecution;
import com.constellio.model.entities.workflows.trigger.ActionCompletion;
import com.constellio.model.entities.workflows.trigger.Trigger;
import com.constellio.model.entities.workflows.trigger.TriggerType;
import com.constellio.model.entities.workflows.trigger.TriggeredWorkflowDefinition;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.workflows.execution.WorkflowExecutionIndexRuntimeException.WorkflowExecutionIndexRuntimeException_WorkflowExecutionNotFound;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;

@SlowTest
public class WorkflowExecutionServiceAcceptanceTest extends ConstellioTest {

	private static final String ZE_SCHEMA_TYPE_SCHEMA1 = "zeSchemaType_schema1";

	ConfigManager configManager;
	ModelLayerFactory modelLayerFactory;
	DataLayerFactory dataLayerFactory;

	WorkflowExecution workflowExecution1;
	WorkflowExecution workflowExecution2;
	WorkflowExecution workflowExecution3;
	WorkflowExecutionService workflowExecutionService;
	WorkflowExecutionIndexManager workflowExecutionIndexManager;
	Trigger trigger;

	@Mock WorkflowDefinition workflowDefinition;
	@Mock Record record;
	@Mock User user;

	@Before
	public void setup()
			throws Exception {

		withSpiedServices(WorkflowExecutionIndexManager.class);
		prepareSystem(withZeCollection());
		modelLayerFactory = getModelLayerFactory();
		dataLayerFactory = getDataLayerFactory();

		when(record.getId()).thenReturn("recordId");
		when(user.getUsername()).thenReturn(bobGratton);
		when(workflowDefinition.getConfigId()).thenReturn("configId");
		when(workflowDefinition.getCollection()).thenReturn(zeCollection);

		workflowExecutionIndexManager = modelLayerFactory.getWorkflowExecutionIndexManager();
		workflowExecutionService = spy(new WorkflowExecutionService(workflowExecutionIndexManager, dataLayerFactory));

		trigger = newTrigger(TriggerType.RECORD_CREATED, ZE_SCHEMA_TYPE_SCHEMA1);
		Map<String, String> variables = new HashMap<>();
		variables.put("key", "value");
		List<WorkflowExecutedTask> executedTasks = new ArrayList<>();
		WorkflowExecutedTask workflowExecutedTask = new WorkflowExecutedTask("taskId", new LocalDateTime(),
				new LocalDateTime().plusHours(1), "finishedBy");
		executedTasks.add(workflowExecutedTask);
		workflowExecution1 = newWorkflowExecution(1, trigger, variables, executedTasks);
		workflowExecution2 = newWorkflowExecution(2, trigger, variables, executedTasks);
		workflowExecution3 = newWorkflowExecution(3, trigger, variables, executedTasks);
	}

	@Test
	public void whenAddWorflowExcecutionThenItIsAddedInBothFiles()
			throws Exception {

		workflowExecutionService.addUpdateWorkflowExecution(workflowExecution1);
		workflowExecutionService.addUpdateWorkflowExecution(workflowExecution2);

		verify(workflowExecutionIndexManager).addUpdate(workflowExecution1);
		WorkflowExecution retrievedWorkflowExecution = workflowExecutionService.getWorkflow(zeCollection,
				workflowExecution1.getId());
		WorkflowExecution retrievedWorkflowExecution2 = workflowExecutionService.getWorkflow(zeCollection,
				workflowExecution2.getId());

		assertThatWorkflowExecutionsAreEquals(retrievedWorkflowExecution, workflowExecution1);
		assertThatWorkflowExecutionsAreEquals(retrievedWorkflowExecution2, workflowExecution2);
	}

	@Test
	public void whenUpdateWorkflowThenItIsUpdated()
			throws Exception {
		workflowExecutionService.addUpdateWorkflowExecution(workflowExecution1);
		workflowExecution1.setVariable("key11", "value11");

		workflowExecutionService.addUpdateWorkflowExecution(workflowExecution1);
		WorkflowExecution retrievedWorkflowExecution = workflowExecutionService.getWorkflow(zeCollection,
				workflowExecution1.getId());

		assertThatWorkflowExecutionsAreEquals(retrievedWorkflowExecution, workflowExecution1);
		assertThat(retrievedWorkflowExecution.getVariables()).hasSize(2);
		assertThat(retrievedWorkflowExecution.getVariables().get("key11")).isEqualTo("value11");
	}

	@Test(expected = WorkflowExecutionIndexRuntimeException_WorkflowExecutionNotFound.class)
	public void whenRemoveThenCannotRetrieveIt()
			throws Exception {
		workflowExecutionService.addUpdateWorkflowExecution(workflowExecution1);

		workflowExecutionService.remove(workflowExecution1);

		workflowExecutionService.getWorkflow(zeCollection, workflowExecution1.getId());
	}

	@Test
	public void whenMarkAsWaitingForSystemThenOk()
			throws Exception {
		workflowExecutionService.addUpdateWorkflowExecution(workflowExecution1);
		assertThat(workflowExecution1.isMarkAsWaitingForSystem()).isFalse();

		workflowExecutionService.markAsWaitingForSystem(zeCollection, workflowExecution1.getId());

		WorkflowExecution retrievedWorkflowExecution = workflowExecutionService
				.getWorkflow(zeCollection, workflowExecution1.getId());
		assertThat(retrievedWorkflowExecution.isMarkAsWaitingForSystem()).isTrue();
	}

	@Test
	public void whenMarkAsNotWaitingForSystemThenOk()
			throws Exception {
		workflowExecutionService.addUpdateWorkflowExecution(workflowExecution1);
		assertThat(workflowExecution1.isMarkAsWaitingForSystem()).isFalse();

		workflowExecutionService.markAsWaitingForSystem(zeCollection, workflowExecution1.getId());
		WorkflowExecution retrievedWorkflowExecution = workflowExecutionService
				.getWorkflow(zeCollection, workflowExecution1.getId());
		assertThat(retrievedWorkflowExecution.isMarkAsWaitingForSystem()).isTrue();
		workflowExecutionService.markAsNotWaitingForSystem(zeCollection, workflowExecution1.getId());

		retrievedWorkflowExecution = workflowExecutionService.getWorkflow(zeCollection, workflowExecution1.getId());
		assertThat(retrievedWorkflowExecution.isMarkAsWaitingForSystem()).isFalse();
	}

	@Test
	public void whenGetNextWorkflowWaitingForSystemProcessingTheneturnThem()
			throws Exception {
		workflowExecution1.setMarkAsWaitingForSystem(true);
		workflowExecution3.setMarkAsWaitingForSystem(true);
		workflowExecutionService.addUpdateWorkflowExecution(workflowExecution1);
		workflowExecutionService.addUpdateWorkflowExecution(workflowExecution2);
		workflowExecutionService.addUpdateWorkflowExecution(workflowExecution3);

		List<WorkflowExecution> workflowExecutions = workflowExecutionService
				.getNextWorkflowWaitingForSystemProcessing(zeCollection);

		assertThat(workflowExecutions).hasSize(2);
		assertThat(workflowExecutions.get(0).getId()).isEqualTo("id1");
		assertThat(workflowExecutions.get(1).getId()).isEqualTo("id3");
	}

	@Test
	public void givenTriggeredWorkflowDefinitionRecordAndUserWhenStartWorkflowExecutionThenReturnIt()
			throws Exception {
		TriggeredWorkflowDefinition triggeredWorkflowDefinition = new TriggeredWorkflowDefinition(workflowDefinition, trigger);

		WorkflowExecution startedWorkflowExecution = workflowExecutionService
				.startWorkflow(triggeredWorkflowDefinition, Arrays.asList(record), user);

		assertThat(startedWorkflowExecution.getCurrentTaskId()).isEqualTo("start");
		assertThat(startedWorkflowExecution.getWorkflowDefinitionId()).isEqualTo("configId");
		assertThat(startedWorkflowExecution.getCurrentTaskStartedOn().toDate()).isCloseTo(new LocalDateTime().toDate(), 500L);
		assertThat(startedWorkflowExecution.getExecutedTasks()).hasSize(0);
		assertThat(startedWorkflowExecution.getRecordIds()).containsOnly(record.getId());
		assertThat(startedWorkflowExecution.getStartedBy()).isEqualTo(user.getUsername());
		assertThat(startedWorkflowExecution.getStartedOn().toDate()).isCloseTo(new LocalDateTime().toDate(), 500L);
		assertThat(startedWorkflowExecution.getTrigger())
				.isEqualToComparingFieldByField(triggeredWorkflowDefinition.getTrigger());
		assertThat(startedWorkflowExecution.getVariables()).hasSize(0);
		assertThat(startedWorkflowExecution.isMarkAsWaitingForSystem()).isTrue();

	}

	void assertThatWorkflowExecutionsAreEquals(WorkflowExecution retrievedWorkflowExecution,
			WorkflowExecution workflowExecution) {
		assertThat(retrievedWorkflowExecution.getId()).isEqualTo(workflowExecution.getId());
		assertThat(retrievedWorkflowExecution.getCurrentTaskId()).isEqualTo(workflowExecution.getCurrentTaskId());
		assertThat(retrievedWorkflowExecution.getCurrentTaskStartedOn()).isEqualTo(workflowExecution.getCurrentTaskStartedOn());
		assertThat(retrievedWorkflowExecution.getRecordIds()).isEqualTo(workflowExecution.getRecordIds());
		assertThat(retrievedWorkflowExecution.getStartedBy()).isEqualTo(workflowExecution.getStartedBy());
		assertThat(retrievedWorkflowExecution.getStartedOn()).isEqualTo(workflowExecution.getStartedOn());
		assertThat(workflowExecution.getTrigger()).isEqualToComparingFieldByField(workflowExecution.getTrigger());
		assertThat(retrievedWorkflowExecution.getVariables()).isEqualTo(workflowExecution.getVariables());
		assertThat(retrievedWorkflowExecution.getWorkflowDefinitionId()).isEqualTo(workflowExecution.getWorkflowDefinitionId());

		List<WorkflowExecutedTask> retrievedExecutedTasks = retrievedWorkflowExecution.getExecutedTasks();
		for (int i = 0; i < retrievedExecutedTasks.size(); i++) {
			assertThat(retrievedExecutedTasks.get(i).getFinishedBy()).isEqualTo(
					workflowExecution.getExecutedTasks().get(i).getFinishedBy());
			assertThat(retrievedExecutedTasks.get(i).getFinishedOn()).isEqualTo(
					workflowExecution.getExecutedTasks().get(i).getFinishedOn());
			assertThat(retrievedExecutedTasks.get(i).getStartedOn()).isEqualTo(
					workflowExecution.getExecutedTasks().get(i).getStartedOn());
			assertThat(retrievedExecutedTasks.get(i).getTaskId()).isEqualTo(
					workflowExecution.getExecutedTasks().get(i).getTaskId());
		}
	}

	private WorkflowExecution newWorkflowExecution(int id, Trigger trigger, Map<String, String> variables,
			List<WorkflowExecutedTask> executedTasks) {
		return new WorkflowExecution("id" + id, "workflowDefinitionId" + id, new LocalDateTime(), "startedBy" + id, trigger,
				Arrays.asList("recordId" + id), "currentTaskId" + id, new LocalDateTime(), variables, executedTasks,
				zeCollection);
	}

	private Trigger newTrigger(TriggerType triggerType, String profileCode) {
		Trigger trigger = new Trigger(triggerType, profileCode, null, ActionCompletion.EXECUTE);
		return trigger;
	}
}
