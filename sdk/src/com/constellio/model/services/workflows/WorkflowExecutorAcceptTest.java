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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Condition;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ApprovalTask;
import com.constellio.model.entities.records.wrappers.Task;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.CustomizedAuthorizationsBehavior;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.workflows.definitions.WorkflowConfiguration;
import com.constellio.model.entities.workflows.execution.WorkflowExecution;
import com.constellio.model.entities.workflows.trigger.Trigger;
import com.constellio.model.entities.workflows.trigger.TriggeredWorkflowDefinition;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.tasks.TaskServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.workflows.bpmn.WorkflowBPMNDefinitionsService;
import com.constellio.model.services.workflows.config.WorkflowsConfigManager;
import com.constellio.model.services.workflows.execution.WorkflowExecutionIndexRuntimeException.WorkflowExecutionIndexRuntimeException_WorkflowExecutionNotFound;
import com.constellio.model.services.workflows.execution.WorkflowExecutionService;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.setups.Users;

public class WorkflowExecutorAcceptTest extends ConstellioTest {

	LocalDateTime shishOClock = new LocalDateTime();
	LocalDateTime tockOClock = shishOClock.plusHours(1);

	String aliceId, dakotaId, edouardId, gandalfId, bobId;

	WorkflowManagerAcceptTestSetup schemas = new WorkflowManagerAcceptTestSetup(zeCollection);
	Users users = new Users();
	WorkflowsConfigManager workflowsConfigManager;
	WorkflowExecutionService workflowExecutionService;
	WorkflowExecutor workflowExecutor;
	TaskServices taskServices;
	String zeUltimateWorkflow = "zeUltimateWorkflow";
	String zeAuthorization = "zeAuthorization";
	String zeOtherAuthorization = "zeOtherAuthorization";
	String requiredApprovalRole = "zeRole";
	String otherApprovalRole = "zeOtherRole";
	Record folder;
	Record folder2;
	Record folder3;

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection);
		defineSchemasManager().using(schemas);
		users.setUp(getModelLayerFactory().newUserServices());

		UserServices userServices = getModelLayerFactory().newUserServices();
		userServices.addUserToCollection(users.alice(), zeCollection);
		userServices.addUserToCollection(users.bob(), zeCollection);
		userServices.addUserToCollection(users.dakotaLIndien(), zeCollection);
		userServices.addUserToCollection(users.edouardLechat(), zeCollection);
		userServices.addUserToCollection(users.gandalfLeblanc(), zeCollection);

		workflowsConfigManager = getModelLayerFactory().getWorkflowsConfigManager();
		workflowExecutionService = getModelLayerFactory().newWorkflowExecutionService();
		workflowExecutor = getModelLayerFactory().getWorkflowsManager();
		taskServices = getModelLayerFactory().newTaskServices();
		getModelLayerFactory().getRolesManager()
				.addRole(new Role(zeCollection, requiredApprovalRole, "zeRole", Arrays.asList("operation1")));
		getModelLayerFactory().getRolesManager()
				.addRole(new Role(zeCollection, otherApprovalRole, "zeOtherRole", Arrays.asList("operation2")));

		folder = createFolder();
		folder2 = createFolder();
		folder3 = createFolder();

		aliceId = users.aliceIn(zeCollection).getId();
		dakotaId = users.dakotaLIndienIn(zeCollection).getId();
		edouardId = users.edouardLechatIn(zeCollection).getId();
		gandalfId = users.gandalfLeblancIn(zeCollection).getId();
		bobId = users.bobIn(zeCollection).getId();
	}

	@Test
	public void givenApprovalWorkflowsStartedWhenApprovingThenFolderLogicallyDeleted()
			throws Exception {
		addApprovalRoleAuthorizationToDakotaAndLegendsTo(folder, folder2, folder3);
		TriggeredWorkflowDefinition triggeredWorkflowDefinition = configureWorkflow();

		givenTimeIs(shishOClock);
		WorkflowExecution execution = workflowExecutionService
				.startWorkflow(triggeredWorkflowDefinition, Arrays.asList(folder), users.aliceIn(zeCollection));

		assertThat(taskServices.getCurrentWorkflowManualTask(execution)).isNull();

		workflowExecutor.handleWaitingWorkflows();
		ApprovalTask task = (ApprovalTask) taskServices.getCurrentWorkflowManualTask(execution);
		assertThat(task.getDecision()).isNull();
		assertThat(task).is(notAssigned());
		assertThat(task.getAssignCandidates()).containsOnly(aliceId, edouardId, dakotaId, gandalfId);
		assertThat(task.getFinishedBy()).isNull();
		assertThat(task.getFinishedOn()).isNull();
		assertThat(task.getWorkflowId()).isEqualTo(execution.getId());

		workflowExecutor.handleWaitingWorkflows();
		task = (ApprovalTask) taskServices.getCurrentWorkflowManualTask(execution);
		assertThat(task.getDecision()).isNull();
		assertThat(isWorkflowExecutionWithIdExisting(execution.getId())).isTrue();

		givenTimeIs(tockOClock);
		task.approve();
		taskServices.finish(task, users.dakotaLIndienIn(zeCollection));
		assertThat(isRecordLogicallyDeleted(folder)).isFalse();
		assertThat(isWorkflowExecutionWithIdExisting(execution.getId())).isTrue();
		task = (ApprovalTask) taskServices.getCurrentWorkflowManualTask(execution);
		assertThat(task.getDecision()).isEqualTo(ApprovalTask.DECISION_APPROVED);
		assertThat(task).is(notAssigned());
		assertThat(task.getAssignCandidates()).containsOnly(aliceId, edouardId, dakotaId, gandalfId);
		assertThat(task.getFinishedBy()).isEqualTo(dakotaId);
		assertThat(task.getFinishedOn()).isEqualTo(tockOClock);
		assertThat(task.getWorkflowId()).isEqualTo(execution.getId());

		workflowExecutor.handleWaitingWorkflows();
		assertThat(isRecordLogicallyDeleted(folder)).isTrue();
		assertThat(isWorkflowExecutionWithIdExisting(execution.getId())).isFalse();
	}

	@Test
	public void givenApprovalWorkflowsStartedWhenRefusingThenFolderStillActive()
			throws Exception {
		addApprovalRoleAuthorizationToDakotaAndLegendsTo(folder, folder2, folder3);
		TriggeredWorkflowDefinition triggeredWorkflowDefinition = configureWorkflow();

		givenTimeIs(shishOClock);
		WorkflowExecution execution = workflowExecutionService
				.startWorkflow(triggeredWorkflowDefinition, Arrays.asList(folder), users.aliceIn(zeCollection));

		assertThat(taskServices.getCurrentWorkflowManualTask(execution)).isNull();

		workflowExecutor.handleWaitingWorkflows();
		ApprovalTask task = (ApprovalTask) taskServices.getCurrentWorkflowManualTask(execution);
		assertThat(task.getDecision()).isNull();
		assertThat(task).is(notAssigned());
		assertThat(task.getAssignCandidates()).containsOnly(aliceId, edouardId, dakotaId, gandalfId);
		assertThat(task.getFinishedBy()).isNull();
		assertThat(task.getFinishedOn()).isNull();
		assertThat(task.getWorkflowId()).isEqualTo(execution.getId());

		workflowExecutor.handleWaitingWorkflows();
		task = (ApprovalTask) taskServices.getCurrentWorkflowManualTask(execution);
		assertThat(task.getDecision()).isNull();
		assertThat(isWorkflowExecutionWithIdExisting(execution.getId())).isTrue();

		givenTimeIs(tockOClock);
		task.refuse();
		taskServices.finish(task, users.dakotaLIndienIn(zeCollection));
		assertThat(isRecordLogicallyDeleted(folder)).isFalse();
		assertThat(isWorkflowExecutionWithIdExisting(execution.getId())).isTrue();
		task = (ApprovalTask) taskServices.getCurrentWorkflowManualTask(execution);
		assertThat(task.getDecision()).isEqualTo(ApprovalTask.DECISION_REFUSED);
		assertThat(task).is(notAssigned());
		assertThat(task.getAssignCandidates()).containsOnly(aliceId, edouardId, dakotaId, gandalfId);
		assertThat(task.getFinishedBy()).isEqualTo(dakotaId);
		assertThat(task.getFinishedOn()).isEqualTo(tockOClock);
		assertThat(task.getWorkflowId()).isEqualTo(execution.getId());

		workflowExecutor.handleWaitingWorkflows();
		assertThat(isRecordLogicallyDeleted(folder)).isFalse();
		assertThat(isWorkflowExecutionWithIdExisting(execution.getId())).isFalse();
	}

	@Test
	public void givenApprovalWorkflowWithUsersStartedWhenRefusingThenFolderStillActive()
			throws Exception {
		addApprovalRoleAuthorizationToDakotaAndLegendsTo(folder, folder2, folder3);
		TriggeredWorkflowDefinition triggeredWorkflowDefinition = configureWorkflowWithUsers();

		givenTimeIs(shishOClock);
		WorkflowExecution execution = workflowExecutionService
				.startWorkflow(triggeredWorkflowDefinition, Arrays.asList(folder), users.bobIn(zeCollection));

		assertThat(taskServices.getCurrentWorkflowManualTask(execution)).isNull();

		workflowExecutor.handleWaitingWorkflows();
		ApprovalTask task = (ApprovalTask) taskServices.getCurrentWorkflowManualTask(execution);
		assertThat(task.getDecision()).isNull();
		assertThat(task).is(notAssigned());
		assertThat(task.getAssignCandidates()).containsOnly(aliceId, edouardId, bobId, gandalfId);
		assertThat(task.getFinishedBy()).isNull();
		assertThat(task.getFinishedOn()).isNull();
		assertThat(task.getWorkflowId()).isEqualTo(execution.getId());

		workflowExecutor.handleWaitingWorkflows();
		task = (ApprovalTask) taskServices.getCurrentWorkflowManualTask(execution);
		assertThat(task.getDecision()).isNull();
		assertThat(isWorkflowExecutionWithIdExisting(execution.getId())).isTrue();

		givenTimeIs(tockOClock);
		task.refuse();
		taskServices.finish(task, users.bobIn(zeCollection));
		assertThat(isRecordLogicallyDeleted(folder)).isFalse();
		assertThat(isWorkflowExecutionWithIdExisting(execution.getId())).isTrue();
		task = (ApprovalTask) taskServices.getCurrentWorkflowManualTask(execution);
		assertThat(task.getDecision()).isEqualTo(ApprovalTask.DECISION_REFUSED);
		assertThat(task).is(notAssigned());
		assertThat(task.getAssignCandidates()).containsOnly(aliceId, edouardId, bobId, gandalfId);
		assertThat(task.getFinishedBy()).isEqualTo(bobId);
		assertThat(task.getFinishedOn()).isEqualTo(tockOClock);
		assertThat(task.getWorkflowId()).isEqualTo(execution.getId());

		workflowExecutor.handleWaitingWorkflows();
		assertThat(isRecordLogicallyDeleted(folder)).isFalse();
		assertThat(isWorkflowExecutionWithIdExisting(execution.getId())).isFalse();
	}

	@Test
	public void givenApprovalWorkflowsStartedForMultipleRecordsWhenApprovingThenFoldersLogicallyDeleted()
			throws Exception {
		addApprovalRoleAuthorizationToDakotaAndLegendsTo(folder, folder2, folder3);
		TriggeredWorkflowDefinition triggeredWorkflowDefinition = configureWorkflow();

		addApprovalRoleAuthorizationToBobTo(folder, folder2);

		givenTimeIs(shishOClock);
		WorkflowExecution execution = workflowExecutionService
				.startWorkflow(triggeredWorkflowDefinition, Arrays.asList(folder, folder2, folder3), users.aliceIn(zeCollection));

		assertThat(taskServices.getCurrentWorkflowManualTask(execution)).isNull();

		workflowExecutor.handleWaitingWorkflows();
		ApprovalTask task = (ApprovalTask) taskServices.getCurrentWorkflowManualTask(execution);
		assertThat(task.getDecision()).isNull();
		assertThat(task).is(notAssigned());
		assertThat(task.getAssignCandidates()).containsOnly(aliceId, edouardId, dakotaId, gandalfId);
		assertThat(task.getFinishedBy()).isNull();
		assertThat(task.getFinishedOn()).isNull();
		assertThat(task.getWorkflowId()).isEqualTo(execution.getId());

		workflowExecutor.handleWaitingWorkflows();
		task = (ApprovalTask) taskServices.getCurrentWorkflowManualTask(execution);
		assertThat(task.getDecision()).isNull();
		assertThat(isWorkflowExecutionWithIdExisting(execution.getId())).isTrue();

		givenTimeIs(tockOClock);
		task.approve();
		taskServices.finish(task, users.dakotaLIndienIn(zeCollection));
		assertThat(isRecordLogicallyDeleted(folder)).isFalse();
		assertThat(isRecordLogicallyDeleted(folder2)).isFalse();
		assertThat(isRecordLogicallyDeleted(folder3)).isFalse();
		assertThat(isWorkflowExecutionWithIdExisting(execution.getId())).isTrue();
		task = (ApprovalTask) taskServices.getCurrentWorkflowManualTask(execution);
		assertThat(task.getDecision()).isEqualTo(ApprovalTask.DECISION_APPROVED);
		assertThat(task).is(notAssigned());
		assertThat(task.getAssignCandidates()).containsOnly(aliceId, edouardId, dakotaId, gandalfId);
		assertThat(task.getFinishedBy()).isEqualTo(dakotaId);
		assertThat(task.getFinishedOn()).isEqualTo(tockOClock);
		assertThat(task.getWorkflowId()).isEqualTo(execution.getId());

		workflowExecutor.handleWaitingWorkflows();
		assertThat(isRecordLogicallyDeleted(folder)).isTrue();
		assertThat(isRecordLogicallyDeleted(folder2)).isTrue();
		assertThat(isRecordLogicallyDeleted(folder3)).isTrue();
		assertThat(isWorkflowExecutionWithIdExisting(execution.getId())).isFalse();
	}

	@Test
	public void givenApprovalWorkflowsStartedForMultipleRecordsWithMultipleRolesWhenApprovingThenFoldersLogicallyDeleted()
			throws Exception {

		TriggeredWorkflowDefinition triggeredWorkflowDefinition = configureWorkflowWithTwoRoles();

		setupTwoApprovalRoleAuthorizations();

		givenTimeIs(shishOClock);
		WorkflowExecution execution = workflowExecutionService
				.startWorkflow(triggeredWorkflowDefinition, Arrays.asList(folder, folder2, folder3), users.aliceIn(zeCollection));

		assertThat(taskServices.getCurrentWorkflowManualTask(execution)).isNull();

		workflowExecutor.handleWaitingWorkflows();
		ApprovalTask task = (ApprovalTask) taskServices.getCurrentWorkflowManualTask(execution);
		assertThat(task.getDecision()).isNull();
		assertThat(task).is(notAssigned());
		assertThat(task.getAssignCandidates()).containsOnly(bobId);
		assertThat(task.getFinishedBy()).isNull();
		assertThat(task.getFinishedOn()).isNull();
		assertThat(task.getWorkflowId()).isEqualTo(execution.getId());

		workflowExecutor.handleWaitingWorkflows();
		task = (ApprovalTask) taskServices.getCurrentWorkflowManualTask(execution);
		assertThat(task.getDecision()).isNull();
		assertThat(isWorkflowExecutionWithIdExisting(execution.getId())).isTrue();

		givenTimeIs(tockOClock);
		task.approve();
		taskServices.finish(task, users.bobIn(zeCollection));
		assertThat(isRecordLogicallyDeleted(folder)).isFalse();
		assertThat(isRecordLogicallyDeleted(folder2)).isFalse();
		assertThat(isRecordLogicallyDeleted(folder3)).isFalse();
		assertThat(isWorkflowExecutionWithIdExisting(execution.getId())).isTrue();
		task = (ApprovalTask) taskServices.getCurrentWorkflowManualTask(execution);
		assertThat(task.getDecision()).isEqualTo(ApprovalTask.DECISION_APPROVED);
		assertThat(task).is(notAssigned());
		assertThat(task.getAssignCandidates()).containsOnly(bobId);
		assertThat(task.getFinishedBy()).isEqualTo(bobId);
		assertThat(task.getFinishedOn()).isEqualTo(tockOClock);
		assertThat(task.getWorkflowId()).isEqualTo(execution.getId());

		workflowExecutor.handleWaitingWorkflows();
		assertThat(isRecordLogicallyDeleted(folder)).isTrue();
		assertThat(isRecordLogicallyDeleted(folder2)).isTrue();
		assertThat(isRecordLogicallyDeleted(folder3)).isTrue();
		assertThat(isWorkflowExecutionWithIdExisting(execution.getId())).isFalse();
	}

	private Condition<? super Task> notAssigned() {
		return new Condition<Task>() {
			@Override
			public boolean matches(Task task) {
				return task.getAssignedOn() == null && task.getAssignedTo() == null;
			}
		};
	}

	private TriggeredWorkflowDefinition configureWorkflow() {
		Map<String, String> workflowMapping = new HashMap<>();
		workflowMapping.put("role:approverRole", requiredApprovalRole);

		WorkflowConfiguration workflowConfiguration = new WorkflowConfiguration(zeUltimateWorkflow, zeCollection, true,
				workflowMapping, Arrays.asList(Trigger.manual(schemas.folderSchema.code())), "approval.bpmn20.xml");

		workflowsConfigManager.addUpdateWorkflow(workflowConfiguration);

		List<TriggeredWorkflowDefinition> triggeredWorkflowDefinitions = workflowsConfigManager
				.getManualWorflowDefinitionsFor(zeCollection, schemas.folderSchema.code());
		assertThat(triggeredWorkflowDefinitions).hasSize(1);

		return triggeredWorkflowDefinitions.get(0);
	}

	private TriggeredWorkflowDefinition configureWorkflowWithUsers() {
		Map<String, String> workflowMapping = new HashMap<>();

		WorkflowConfiguration workflowConfiguration = new WorkflowConfiguration(zeUltimateWorkflow, zeCollection, true,
				workflowMapping, Arrays.asList(Trigger.manual(schemas.folderSchema.code())), "approvalWithUsers.bpmn20.xml");

		workflowsConfigManager.addUpdateWorkflow(workflowConfiguration);

		List<TriggeredWorkflowDefinition> triggeredWorkflowDefinitions = workflowsConfigManager
				.getManualWorflowDefinitionsFor(zeCollection, schemas.folderSchema.code());
		assertThat(triggeredWorkflowDefinitions).hasSize(1);

		return triggeredWorkflowDefinitions.get(0);
	}

	private TriggeredWorkflowDefinition configureWorkflowWithTwoRoles() {
		Map<String, String> workflowMapping = new HashMap<>();
		workflowMapping.put("role:approverRole", requiredApprovalRole);
		workflowMapping.put("role:otherApproverRole", otherApprovalRole);

		WorkflowConfiguration workflowConfiguration = new WorkflowConfiguration(zeUltimateWorkflow, zeCollection, true,
				workflowMapping, Arrays.asList(Trigger.manual(schemas.folderSchema.code())), "approvalWithRoles.bpmn20.xml");

		workflowsConfigManager.addUpdateWorkflow(workflowConfiguration);

		List<TriggeredWorkflowDefinition> triggeredWorkflowDefinitions = workflowsConfigManager
				.getManualWorflowDefinitionsFor(zeCollection, schemas.folderSchema.code());
		assertThat(triggeredWorkflowDefinitions).hasSize(1);
		WorkflowBPMNDefinitionsService defService = new WorkflowBPMNDefinitionsService(getFoldersLocator(),
				getDataLayerFactory().getIOServicesFactory().newFileService());
		assertThat(defService.getAvailableWorkflowDefinitionMappingKeys("approvalWithRoles.bpmn20.xml"))
				.containsOnly("role:approverRole", "role:otherApproverRole");
		return triggeredWorkflowDefinitions.get(0);
	}

	private boolean isWorkflowExecutionWithIdExisting(String id) {
		try {
			workflowExecutionService.getWorkflow(zeCollection, id);
			return true;
		} catch (WorkflowExecutionIndexRuntimeException_WorkflowExecutionNotFound e) {
			return false;
		}
	}

	private boolean isRecordLogicallyDeleted(Record folder) {
		Record refreshedFolder = refreshedFolder(folder);
		Boolean isLogicallyDeleted = refreshedFolder.get(Schemas.LOGICALLY_DELETED_STATUS);
		if (isLogicallyDeleted != null) {
			return isLogicallyDeleted;
		} else {
			return false;
		}
	}

	private Record refreshedFolder(Record folder) {
		getModelLayerFactory().newRecordServices().refresh(folder);
		return folder;
	}

	private void addApprovalRoleAuthorizationToDakotaAndLegendsTo(Record... folders)
			throws InterruptedException {
		AuthorizationsServices authorizationsServices = getModelLayerFactory().newAuthorizationsServices();

		AuthorizationDetails authorizationDetails = AuthorizationDetails
				.create(zeAuthorization, asList(requiredApprovalRole), zeCollection);

		List<String> principals = asList(users.dakotaLIndienIn(zeCollection).getId(), users.legendsIn(zeCollection).getId());

		Authorization authorization = new Authorization(authorizationDetails, principals, new RecordUtils().toIdList(
				asList(folders)));

		authorizationsServices.add(authorization, CustomizedAuthorizationsBehavior.KEEP_ATTACHED, null);

		waitForBatchProcess();
		getModelLayerFactory().newRecordServices().refresh(folder);
	}

	private void addApprovalRoleAuthorizationToBobTo(Record... folders)
			throws InterruptedException {
		AuthorizationsServices authorizationsServices = getModelLayerFactory().newAuthorizationsServices();

		AuthorizationDetails authorizationDetails = AuthorizationDetails
				.create(zeOtherAuthorization, asList(requiredApprovalRole), zeCollection);

		List<String> principals = asList(users.bobIn(zeCollection).getId());

		Authorization authorization = new Authorization(authorizationDetails, principals,
				new RecordUtils().toIdList(asList(folders)));

		authorizationsServices.add(authorization, CustomizedAuthorizationsBehavior.KEEP_ATTACHED, null);

		waitForBatchProcess();
		getModelLayerFactory().newRecordServices().refresh(folder);
	}

	private void setupTwoApprovalRoleAuthorizations()
			throws InterruptedException {
		AuthorizationsServices authorizationsServices = getModelLayerFactory().newAuthorizationsServices();

		// Give Bob both roles on all folders
		AuthorizationDetails bobBothRolesAuthorizationDetails = AuthorizationDetails
				.create("bobAuth", asList(requiredApprovalRole, otherApprovalRole), zeCollection);
		Authorization bobAuthorization = new Authorization(bobBothRolesAuthorizationDetails, asList(bobId),
				asList(folder.getId(), folder2.getId(), folder3.getId()));
		authorizationsServices.add(bobAuthorization, CustomizedAuthorizationsBehavior.KEEP_ATTACHED, null);

		// Give Gandalf first role on all folders
		AuthorizationDetails gandalfFirstRoleAuthorizationDetails = AuthorizationDetails
				.create("gandalfAuth", asList(requiredApprovalRole), zeCollection);
		Authorization gandalfAuthorization = new Authorization(gandalfFirstRoleAuthorizationDetails, asList(gandalfId),
				asList(folder.getId(), folder2.getId(), folder3.getId()));
		authorizationsServices.add(gandalfAuthorization, CustomizedAuthorizationsBehavior.KEEP_ATTACHED, null);

		// Give Alice second role on all folders
		AuthorizationDetails aliceSecondRoleAuthorizationDetails = AuthorizationDetails
				.create("aliceAuth", asList(otherApprovalRole), zeCollection);
		Authorization aliceAuthorization = new Authorization(aliceSecondRoleAuthorizationDetails, asList(aliceId),
				asList(folder.getId(), folder2.getId(), folder3.getId()));
		authorizationsServices.add(aliceAuthorization, CustomizedAuthorizationsBehavior.KEEP_ATTACHED, null);

		// Give Dakota second role on folder and folder2
		AuthorizationDetails dakotaBothRolesAuthorizationDetails = AuthorizationDetails
				.create("dakotaAuth", asList(otherApprovalRole), zeCollection);
		Authorization dakotaAuthorization = new Authorization(dakotaBothRolesAuthorizationDetails, asList(dakotaId),
				asList(folder.getId(), folder2.getId()));
		authorizationsServices.add(dakotaAuthorization, CustomizedAuthorizationsBehavior.KEEP_ATTACHED, null);

		waitForBatchProcess();
		getModelLayerFactory().newRecordServices().refresh(folder);
	}

	private Record createFolder()
			throws RecordServicesException {
		Record record = new TestRecord(schemas.folderSchema);
		record.set(Schemas.TITLE, "ze ultimate folder");
		getModelLayerFactory().newRecordServices().add(record);
		return record;
	}

}
