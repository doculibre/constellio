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
package com.constellio.model.services.workflows.bpmn;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.api.impl.workflows.approval.DeleteRecordsWorkflowAction;
import com.constellio.model.entities.workflows.definitions.AllUsersSelector;
import com.constellio.model.entities.workflows.definitions.BPMNProperty;
import com.constellio.model.entities.workflows.definitions.GroupSelector;
import com.constellio.model.entities.workflows.definitions.RoleSelector;
import com.constellio.model.entities.workflows.definitions.UserSelector;
import com.constellio.model.entities.workflows.definitions.WorkflowCondition;
import com.constellio.model.entities.workflows.definitions.WorkflowConfiguration;
import com.constellio.model.entities.workflows.definitions.WorkflowDefinition;
import com.constellio.model.entities.workflows.definitions.WorkflowRouting;
import com.constellio.model.entities.workflows.definitions.WorkflowRoutingDestination;
import com.constellio.model.entities.workflows.definitions.WorkflowServiceTask;
import com.constellio.model.entities.workflows.definitions.WorkflowTask;
import com.constellio.model.entities.workflows.definitions.WorkflowUserTask;
import com.constellio.model.entities.workflows.trigger.ActionCompletion;
import com.constellio.model.entities.workflows.trigger.Trigger;
import com.constellio.model.entities.workflows.trigger.TriggerType;
import com.constellio.model.services.workflows.general.WorkflowConditions;
import com.constellio.sdk.tests.ConstellioTest;

public class BPMNParserAcceptTest extends ConstellioTest {

	public static final String EXECUTE_REQUEST = "executeRequest";
	public static final String ASK_APPROVAL = "askApproval";
	public static final String IS_APPROVED = "isApproved";
	public static final String X2 = "x2";
	BPMNParser parser;
	Document approbationWorkflowDocument;
	Map<String, String> mapping;
	WorkflowConfiguration workflowConfiguration;

	@Before
	public void setUp()
			throws Exception {
		mapping = new HashMap<>();
		mapping.put("user:responsable", "responsable");
		mapping.put("role:archiviste", "archiviste");
		mapping.put("role:write", "write");
		approbationWorkflowDocument = new SAXBuilder().build(getTestResourceFile("process.bpmn20.xml"));
		createWorkflowConfigurationRecordCreated();
	}

	@Test
	public void givenValidApprobationWorkflowBPMNThenWorkflowCreated()
			throws Exception {
		givenValidApprobationWorkflowBPMN();
		WorkflowDefinition workflow = parser.build();
		thenApprobationWorkflowDefinitionIsValid(workflow);
	}

	private void thenApprobationWorkflowDefinitionIsValid(WorkflowDefinition parsedWorkflow) {
		WorkflowDefinition validApprobationWorkflow = createValidApprobationWorkflow();

		assertThatWorkflowsAreSimilar(parsedWorkflow, validApprobationWorkflow);
	}

	private void assertThatWorkflowsAreSimilar(WorkflowDefinition parsedWorkflow, WorkflowDefinition validApprobationWorkflow) {
		WorkflowServiceTask executeRequest = (WorkflowServiceTask) parsedWorkflow.getTask(EXECUTE_REQUEST);
		assertThat(executeRequest.getTaskId()).isEqualTo(EXECUTE_REQUEST);
		assertThat(executeRequest.getAction()).isInstanceOf(DeleteRecordsWorkflowAction.class);
		WorkflowRoutingDestination routingDestination = executeRequest.getRoutings().get(0);
		assertThat(routingDestination.getDestinationTask()).isEqualTo(X2);
		assertThat(routingDestination.getSource()).isEqualTo(EXECUTE_REQUEST);
		assertThat(routingDestination.getCondition()).isNotNull();

		WorkflowUserTask askApproval = (WorkflowUserTask) parsedWorkflow.getTask(ASK_APPROVAL);
		WorkflowUserTask expectedAskApproval = (WorkflowUserTask) validApprobationWorkflow.getTask(ASK_APPROVAL);
		assertThat(askApproval.getTaskId()).isEqualTo(ASK_APPROVAL);
		AllUsersSelector userSelector = askApproval.getUserSelector();
		AllUsersSelector expectedUserSelector = expectedAskApproval.getUserSelector();
		assertThat(userSelector.getRoleSelector()).isEqualToComparingFieldByField(expectedUserSelector.getRoleSelector());
		assertThat(userSelector.getGroupSelector()).isEqualToComparingFieldByField(expectedUserSelector.getGroupSelector());
		assertThat(userSelector.getUserSelector()).isEqualToComparingFieldByField(expectedUserSelector.getUserSelector());
		assertThat(askApproval.getFields().get(0)).isEqualToComparingFieldByField(expectedAskApproval.getFields().get(0));
		assertThat(askApproval.getTaskSchema()).isEqualTo(expectedAskApproval.getTaskSchema());
		assertThat(askApproval.getDueDateInDays()).isEqualTo(expectedAskApproval.getDueDateInDays());

		assertThat(parsedWorkflow.getRoutings().get(WorkflowRoutingDestination.DESTINATION_START)).isEqualToComparingFieldByField(
				validApprobationWorkflow.getRoutings().get(WorkflowRoutingDestination.DESTINATION_START));
		assertThat(parsedWorkflow.getRoutings().get(IS_APPROVED))
				.isEqualToComparingFieldByField(validApprobationWorkflow.getRoutings().get(IS_APPROVED));
		assertThat(parsedWorkflow.getRoutings().get(X2))
				.isEqualToComparingFieldByField(validApprobationWorkflow.getRoutings().get(X2));
	}

	private WorkflowDefinition createValidApprobationWorkflow() {
		Map<String, WorkflowTask> validTasks = new HashMap<>();
		// Create routings
		WorkflowRoutingDestination start_waitForApproval = new WorkflowRoutingDestination(directCondition(),
				ASK_APPROVAL, WorkflowRoutingDestination.DESTINATION_START);
		WorkflowRouting start = new WorkflowRouting(WorkflowRoutingDestination.DESTINATION_START,
				Arrays.asList(start_waitForApproval));

		WorkflowRoutingDestination waitForApproval_isApproved = new WorkflowRoutingDestination(directCondition(), IS_APPROVED,
				ASK_APPROVAL);

		WorkflowRoutingDestination isApproved_approved = new WorkflowRoutingDestination(approvalCondition(), EXECUTE_REQUEST,
				IS_APPROVED);
		WorkflowRoutingDestination isApproved_refused = new WorkflowRoutingDestination(approvalCondition(), X2, IS_APPROVED);
		WorkflowRouting isApproved = new WorkflowRouting(IS_APPROVED, Arrays.asList(isApproved_approved, isApproved_refused));

		WorkflowRoutingDestination executeRequest_x2 = new WorkflowRoutingDestination(directCondition(), X2, EXECUTE_REQUEST);
		WorkflowRoutingDestination x2_end = new WorkflowRoutingDestination(directCondition(),
				WorkflowRoutingDestination.DESTINATION_END, X2);
		WorkflowRouting x2 = new WorkflowRouting(X2, Arrays.asList(x2_end));

		// Create Tasks
		RoleSelector roleSelector = new RoleSelector(Arrays.asList("archiviste", "write"));
		GroupSelector groupSelector = new GroupSelector(Arrays.asList("theClassificationStation"));
		UserSelector userSelector = new UserSelector(Arrays.asList("alice", "bob", "responsable"));
		AllUsersSelector allUsersSelector = new AllUsersSelector(roleSelector, groupSelector, userSelector);
		// Create BPMNProperties
		BPMNProperty decisionProperty = new BPMNProperty("decision", null, "decision");
		BPMNProperty titleProperty = new BPMNProperty("title",
				"Veuillez approuver la suppression du ${record:schema} ${record:id} - ${record:title}   ", null);
		WorkflowUserTask askApproval = new WorkflowUserTask(ASK_APPROVAL, "task_default",
				Arrays.asList(waitForApproval_isApproved), allUsersSelector, 14, Arrays.asList(decisionProperty, titleProperty));
		WorkflowTask executeRequest = new WorkflowServiceTask(EXECUTE_REQUEST, new DeleteRecordsWorkflowAction(),
				Arrays.asList(executeRequest_x2));

		// Create Workflow
		validTasks.put(ASK_APPROVAL, askApproval);
		validTasks.put(EXECUTE_REQUEST, executeRequest);
		Map<String, WorkflowRouting> routings = new HashMap<>();
		routings.put(WorkflowRoutingDestination.DESTINATION_START, start);
		routings.put(IS_APPROVED, isApproved);
		routings.put(X2, x2);
		return new WorkflowDefinition(workflowConfiguration.getId(), validTasks, true, routings, zeCollection);
	}

	private WorkflowCondition directCondition() {
		return WorkflowConditions.directCondition();
	}

	private WorkflowCondition approvalCondition() {
		return WorkflowConditions.approvalCondition();
	}

	private void givenValidApprobationWorkflowBPMN() {
		parser = new BPMNParser(approbationWorkflowDocument, mapping, workflowConfiguration);
	}

	void createWorkflowConfigurationRecordCreated() {
		String id = "id5";
		Trigger trigger = new Trigger(TriggerType.RECORD_CREATED, "zeSchemaType_schema1", null, ActionCompletion.EXECUTE);
		List<Trigger> triggers = new ArrayList<>();
		triggers.add(trigger);
		Map<String, String> mapping = new HashMap<>();
		mapping.put("key1", "value1");
		String bpmnFileName1 = "bpmnFileName1";
		workflowConfiguration = new WorkflowConfiguration(id, zeCollection, true, mapping, triggers, bpmnFileName1);
	}
}
