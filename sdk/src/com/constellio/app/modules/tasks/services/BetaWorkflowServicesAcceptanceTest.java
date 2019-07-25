package com.constellio.app.modules.tasks.services;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflow;
import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflowInstance;
import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflowTask;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.TaskStatusType;
import com.constellio.app.modules.tasks.model.wrappers.WorkflowInstanceStatus;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.services.BetaWorkflowServicesRuntimeException.WorkflowServicesRuntimeException_UnsupportedAddAtPosition;
import com.constellio.app.modules.tasks.ui.entities.BetaWorkflowTaskProgressionVO;
import com.constellio.app.modules.tasks.ui.entities.BetaWorkflowTaskVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;
import org.assertj.core.api.ListAssert;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.modules.rm.wrappers.RMTask.LINKED_FOLDERS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static java.util.Arrays.asList;
import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class BetaWorkflowServicesAcceptanceTest extends ConstellioTest {
	TasksSchemasRecordsServices tasks;
	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();
	BetaWorkflowServices services;
	SearchServices searchServices;
	RecordServices recordServices;
	Map<String, List<String>> noExtraFields = Collections.emptyMap();

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withTasksModule().withAllTest(users).withConstellioRMModule().withRMTest(records)
				.withFoldersAndContainersOfEveryStatus());
		services = new BetaWorkflowServices(zeCollection, getAppLayerFactory());
		searchServices = getModelLayerFactory().newSearchServices();
		tasks = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		this.recordServices = getModelLayerFactory().newRecordServices();

		givenApprovalWorkflow();
		givenSimpleWorkflow();

		givenConfig(RMConfigs.WORKFLOWS_ENABLED, true);
	}


	@Test
	public void whenStartingAWorkflowThenCreateFirstTasks()
			throws Exception {

		long initialTasksCount = getAllTasksCount();

		BetaWorkflow approvalWorkflow = tasks.getBetaWorkflowWithCode("approval");
		BetaWorkflow simpleWorkflow = tasks.getBetaWorkflowWithCode("simple");
		BetaWorkflowInstance approvalWorkflowInstance = services
				.start(approvalWorkflow, users.edouardIn(zeCollection), noExtraFields);

		assertThat(services.getCurrentWorkflowInstances()).containsOnly(approvalWorkflowInstance);
		assertThatRecord(approvalWorkflowInstance)
				.hasMetadata(tasks.betaWorkflowInstance.title(), "Un workflow d'approbation")
				.hasMetadata(tasks.betaWorkflowInstance.startedBy(), users.edouardIn(zeCollection).getId())
				.hasMetadata(tasks.betaWorkflowInstance.status(), WorkflowInstanceStatus.IN_PROGRESS);
		assertThat(getAllTasksCount() - initialTasksCount).isEqualTo(1);
		assertThat(services.getWorkflowInstanceTasks(approvalWorkflowInstance)).extracting("title").isEqualTo(asList(
				"Détails"));

		BetaWorkflowInstance simpleWorkflowInstance = services
				.start(simpleWorkflow, users.dakotaLIndienIn(zeCollection), noExtraFields);

		assertThat(services.getWorkflowInstanceTasks(simpleWorkflowInstance)).extracting("title").containsOnly(
				"Task 1"
		);
		assertThat(getAllTasksCount() - initialTasksCount).isEqualTo(2);

	}

	@Test
	public void whenGetAvailableWorkflowTaskVOForNewTaskThenReturnEligibleTasks()
			throws Exception {

		SessionContext sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		BetaWorkflow approvalWorkflow = tasks.getBetaWorkflowWithCode("approval");

		assertThat(services.getAvailableWorkflowTaskVOForNewTask(approvalWorkflow.getId(), sessionContext))
				.extracting("id", "decision").containsOnly(
				tuple("details", null),
				tuple("approval", "true"),
				tuple("approval", "false"),
				tuple("approvedFirstTask", null),
				tuple("approvedSecondTask", null),
				tuple("refusalFirstTask", null)

		);

		List<BetaWorkflowTaskVO> approvalWorkflowRoots = services.getRootModelTaskVOs(approvalWorkflow, sessionContext);
		List<BetaWorkflowTaskVO> approvalNodes = services.getChildModelTasks(approvalWorkflowRoots.get(1), sessionContext);
		List<BetaWorkflowTaskVO> approvedNodes = services.getChildModelTasks(approvalNodes.get(1), sessionContext);
		List<BetaWorkflowTaskVO> refusedNodes = services.getChildModelTasks(approvalNodes.get(0), sessionContext);

		assertThat(services.canAddTaskIn(approvalWorkflowRoots.get(0), sessionContext)).isTrue();
		assertThat(services.canAddDecisionTaskIn(approvalWorkflowRoots.get(0), sessionContext)).isFalse();
		assertThat(services.canAddTaskIn(approvalWorkflowRoots.get(1), sessionContext)).isFalse();
		assertThat(services.canAddDecisionTaskIn(approvalWorkflowRoots.get(1), sessionContext)).isFalse();

		assertThat(services.canAddTaskIn(approvalNodes.get(0), sessionContext)).isTrue();
		assertThat(services.canAddDecisionTaskIn(approvalNodes.get(0), sessionContext)).isFalse();
		assertThat(services.canAddTaskIn(approvalNodes.get(1), sessionContext)).isTrue();
		assertThat(services.canAddDecisionTaskIn(approvalNodes.get(1), sessionContext)).isFalse();

		assertThat(services.canAddTaskIn(approvedNodes.get(0), sessionContext)).isTrue();
		assertThat(services.canAddDecisionTaskIn(approvedNodes.get(0), sessionContext)).isFalse();
		assertThat(services.canAddTaskIn(approvedNodes.get(1), sessionContext)).isTrue();
		assertThat(services.canAddDecisionTaskIn(approvedNodes.get(1), sessionContext)).isTrue();

		assertThat(services.canAddTaskIn(refusedNodes.get(0), sessionContext)).isTrue();
		assertThat(services.canAddDecisionTaskIn(refusedNodes.get(0), sessionContext)).isTrue();
	}

	@Test
	public void whenCreateATaskAtTheEndThenCorrectlyAdded()
			throws Exception {

		SessionContext sessionContext = FakeSessionContext.forRealUserIncollection(users.gandalfLeblancIn(zeCollection));
		BetaWorkflow approvalWorkflow = tasks.getBetaWorkflowWithCode("approval");

		BetaWorkflowTaskVO approvalSecondTask = services.newWorkflowTaskVO(tasks.getTask("approvedSecondTask"), sessionContext);

		BetaWorkflowTask task = services
				.createModelTaskAfter(approvalWorkflow, approvalSecondTask, null, "ze new task", sessionContext);
		assertThat(task.getTitle()).isEqualTo("ze new task");
		assertThat(task.getNextTasks()).isEmpty();

		BetaWorkflowTask previousTask = tasks.getBetaWorkflowTask(approvalSecondTask.getId());
		assertThat(previousTask.getNextTasks()).containsOnly(task.getId());

		assertThat(services.getRootModelTaskVOs(approvalWorkflow, sessionContext)).extracting("id").containsOnly(
				"details", "approval"
		);

	}

	@Test
	public void whenCreateATaskWithDecisionAtTheEndThenCorrectlyAdded()
			throws Exception {

		SessionContext sessionContext = FakeSessionContext.forRealUserIncollection(users.gandalfLeblancIn(zeCollection));
		BetaWorkflow approvalWorkflow = tasks.getBetaWorkflowWithCode("approval");

		BetaWorkflowTaskVO approvalSecondTask = services.newWorkflowTaskVO(tasks.getTask("approvedSecondTask"), sessionContext);

		BetaWorkflowTask task = services.createDecisionModelTaskAfter(approvalWorkflow, approvalSecondTask, null,
				"ze new task", asList("decision1", "decision2"), sessionContext);
		assertThat(task.getTitle()).isEqualTo("ze new task");
		assertThat(task.getNextTasksDecisionsCodes()).containsOnly("decision1", "decision2");
		assertThat(task.getNextTasks()).isEmpty();

		BetaWorkflowTask previousTask = tasks.getBetaWorkflowTask(approvalSecondTask.getId());
		assertThat(previousTask.getNextTasks()).containsOnly(task.getId());

		assertThat(services.getRootModelTaskVOs(approvalWorkflow, sessionContext)).extracting("id").containsOnly(
				"details", "approval"
		);

		BetaWorkflowTaskVO zeNewTaskDecision1 = services.newWorkflowTaskVO(previousTask, "decision1", sessionContext);
		BetaWorkflowTask zeNewTaskInDecision1 = services.createDecisionModelTaskAfter(approvalWorkflow, zeNewTaskDecision1,
				null, "ze new task decision 1 sub decisions", asList("decision3", "decision4"), sessionContext);
		assertThat(zeNewTaskInDecision1.getTitle()).isEqualTo("ze new task decision 1 sub decisions");
		assertThat(zeNewTaskInDecision1.getNextTasksDecisionsCodes()).containsOnly("decision3", "decision4");
		assertThat(zeNewTaskInDecision1.getNextTasks()).isEmpty();

		BetaWorkflowTaskVO zeNewTaskDecision2 = services.newWorkflowTaskVO(previousTask, "decision2", sessionContext);
		BetaWorkflowTask zeNewTaskInDecision2 = services.createModelTaskAfter(approvalWorkflow, zeNewTaskDecision2,
				null, "ze new task decision 2 sub decisions", sessionContext);
		assertThat(zeNewTaskInDecision2.getTitle()).isEqualTo("ze new task decision 2 sub decisions");
		assertThat(zeNewTaskInDecision2.getNextTasks()).isEmpty();

		assertThat(services.getChildModelTasks(zeNewTaskDecision1, sessionContext)).extracting("id", "decision").containsOnly(
				tuple(zeNewTaskInDecision1.getId(), null)
		);

		BetaWorkflowTaskVO zeNewTaskInDecision1VO = services.newWorkflowTaskVO(zeNewTaskInDecision1, sessionContext);
		assertThat(services.getChildModelTasks(zeNewTaskInDecision1VO, sessionContext)).extracting("id", "decision").containsOnly(
				tuple(zeNewTaskInDecision1.getId(), "decision3"),
				tuple(zeNewTaskInDecision1.getId(), "decision4")
		);

		assertThat(services.getChildModelTasks(zeNewTaskDecision2, sessionContext)).extracting("id", "decision").containsOnly(
				tuple(zeNewTaskInDecision2.getId(), null)
		);

	}

	@Test
	public void whenCreateATaskInAtAnInvalidLocationThenExceptionAndNoCreatedTask()
			throws Exception {

		SessionContext sessionContext = FakeSessionContext.forRealUserIncollection(users.gandalfLeblancIn(zeCollection));
		BetaWorkflow approvalWorkflow = tasks.getBetaWorkflowWithCode("approval");

		assertThat(services.getWorkflowModelTasks(approvalWorkflow.getId())).extracting("id").containsOnly(
				"details", "approval", "approval", "approvedFirstTask", "approvedSecondTask", "refusalFirstTask"
		);

		BetaWorkflowTaskVO approvalTask = services.getRootModelTaskVOs(approvalWorkflow, sessionContext).get(1);

		try {

			services.createModelTaskAfter(approvalWorkflow, approvalTask, null, "ze new task", sessionContext);
			fail("exception expected");
		} catch (WorkflowServicesRuntimeException_UnsupportedAddAtPosition e) {
			//OK
		}

		assertThat(services.getWorkflowModelTasks(approvalWorkflow.getId())).extracting("id").containsOnly(
				"details", "approval", "approval", "approvedFirstTask", "approvedSecondTask", "refusalFirstTask"
		);

	}

	@Test
	public void whenCompleteATaskWithoutSpecifyingADecisionThenValidationError()
			throws Exception {

		String finishedStatus = searchServices.searchSingleResult(
				from(tasks.ddvTaskStatus.schemaType()).where(tasks.ddvTaskStatus.statusType()).isEqualTo(
						TaskStatusType.FINISHED)).getId();
		String closedStatus = tasks.getTaskStatusWithCode(TaskStatus.CLOSED_CODE).getId();

		BetaWorkflow approvalWorkflow = tasks.getBetaWorkflowWithCode("approval");
		BetaWorkflowInstance approvalWorkflowInstance = services
				.start(approvalWorkflow, users.edouardIn(zeCollection), noExtraFields);

		recordServices.update(services.getCurrentWorkflowInstanceTask(approvalWorkflowInstance).setStatus(closedStatus));

		Task currentTask = services.getCurrentWorkflowInstanceTask(approvalWorkflowInstance);

		try {
			recordServices.update(currentTask.setStatus(finishedStatus));
			fail("validation exception expected");
		} catch (ValidationException e) {
			//OK
		}
	}

	@Test
	public void givenAWorkflowIsCreatedWithAttachedRecordsThenAttachedToAllCreatedTasks()
			throws Exception {

		String closedStatus = tasks.getTaskStatusWithCode(TaskStatus.CLOSED_CODE).getId();

		BetaWorkflow approvalWorkflow = tasks.getBetaWorkflowWithCode("approval");

		Map<String, List<String>> extraFields = new HashMap<>();
		extraFields.put(LINKED_FOLDERS, asList(records.folder_A04, records.folder_A08));

		BetaWorkflowInstance approvalWorkflowInstance = services
				.start(approvalWorkflow, users.edouardIn(zeCollection), extraFields);

		assertThat(services.getCurrentWorkflowInstances()).containsOnly(approvalWorkflowInstance);
		assertThatRecord(approvalWorkflowInstance)
				.hasMetadata(tasks.betaWorkflowInstance.status(), WorkflowInstanceStatus.IN_PROGRESS);

		List<Task> tasks = services.getWorkflowInstanceTasks(approvalWorkflowInstance);
		assertThat(tasks).hasSize(1);
		assertThat(tasks.get(0).<List<String>>get(RMTask.LINKED_FOLDERS)).isEqualTo(asList(records.folder_A04, records.folder_A08));

		recordServices.update(services.getCurrentWorkflowInstanceTask(approvalWorkflowInstance).setStatus(closedStatus));

		tasks = services.getWorkflowInstanceTasks(approvalWorkflowInstance);
		assertThat(tasks).hasSize(2);
		assertThat(tasks.get(0).<List<String>>get(RMTask.LINKED_FOLDERS)).isEqualTo(asList(records.folder_A04, records.folder_A08));
		assertThat(tasks.get(1).<List<String>>get(RMTask.LINKED_FOLDERS)).isEqualTo(asList(records.folder_A04, records.folder_A08));

	}

	@Test
	public void whenCompleteATaskThenNextOneIsCreatedOrWorkflowFinished()
			throws Exception {

		String standbyStatus = tasks.getTaskStatusWithCode(TaskStatus.STANDBY_CODE).getId();
		String finishedStatus = searchServices.searchSingleResult(
				from(tasks.ddvTaskStatus.schemaType()).where(tasks.ddvTaskStatus.statusType()).isEqualTo(
						TaskStatusType.FINISHED)).getId();
		String closedStatus = tasks.getTaskStatusWithCode(TaskStatus.CLOSED_CODE).getId();

		BetaWorkflow approvalWorkflow = tasks.getBetaWorkflowWithCode("approval");
		BetaWorkflowInstance approvalWorkflowInstance = services
				.start(approvalWorkflow, users.edouardIn(zeCollection), noExtraFields);

		assertThat(services.getCurrentWorkflowInstances()).containsOnly(approvalWorkflowInstance);
		assertThatRecord(approvalWorkflowInstance)
				.hasMetadata(tasks.betaWorkflowInstance.status(), WorkflowInstanceStatus.IN_PROGRESS);

		assertThat(services.getWorkflowInstanceTasks(approvalWorkflowInstance)).extracting("title", "status").containsOnly(
				tuple("Détails", standbyStatus));
		assertThatWorkflowProgression(approvalWorkflowInstance).extracting("title", "decision", "status", "dueDate").containsOnly(
				tuple("Détails", null, TaskStatusType.STANDBY, null),
				tuple("Demande d'approbation", null, null, null),
				tuple("Demande d'approbation - true", "true", null, null),
				tuple("Demande d'approbation - false", "false", null, null),
				tuple("Approuvée - Première tâche", null, null, null),
				tuple("Approuvée - Deuxième tâche", null, null, null),
				tuple("Refusées - Première tâche", null, null, null)
		);

		recordServices.update(services.getCurrentWorkflowInstanceTask(approvalWorkflowInstance).setStatus(closedStatus));

		assertThat(services.getWorkflowInstanceTasks(approvalWorkflowInstance)).extracting("title", "status").containsOnly(
				tuple("Demande d'approbation", standbyStatus),
				tuple("Détails", closedStatus));
		assertThatWorkflowProgression(approvalWorkflowInstance).extracting("title", "decision", "status", "dueDate").containsOnly(
				tuple("Détails", null, TaskStatusType.CLOSED, null),
				tuple("Demande d'approbation", null, TaskStatusType.STANDBY, null),
				tuple("Demande d'approbation - true", "true", null, null),
				tuple("Demande d'approbation - false", "false", null, null),
				tuple("Approuvée - Première tâche", null, null, null),
				tuple("Approuvée - Deuxième tâche", null, null, null),
				tuple("Refusées - Première tâche", null, null, null)
		);

		Task currentTask = services.getCurrentWorkflowInstanceTask(approvalWorkflowInstance);

		recordServices.update(currentTask.setStatus(finishedStatus).setDecision("true"));

		assertThat(services.getWorkflowInstanceTasks(approvalWorkflowInstance)).extracting("title", "status").containsOnly(
				tuple("Demande d'approbation", finishedStatus),
				tuple("Détails", closedStatus),
				tuple("Approuvée - Première tâche", standbyStatus));

		recordServices.update(currentTask.setStatus(standbyStatus).setDecision("true"));
		recordServices.update(currentTask.setStatus(closedStatus).setDecision("true"));

		recordServices.update(services.getCurrentWorkflowInstanceTask(approvalWorkflowInstance)
				.setStatus(closedStatus));

		assertThat(services.getWorkflowInstanceTasks(approvalWorkflowInstance)).extracting("title", "status").containsOnly(
				tuple("Demande d'approbation", closedStatus),
				tuple("Détails", closedStatus),
				tuple("Approuvée - Première tâche", closedStatus),
				tuple("Approuvée - Deuxième tâche", standbyStatus));
		assertThatRecord(tasks.getBetaWorkflowInstance(approvalWorkflowInstance.getId()))
				.hasMetadata(tasks.betaWorkflowInstance.status(), WorkflowInstanceStatus.IN_PROGRESS);

		recordServices.update(services.getCurrentWorkflowInstanceTask(approvalWorkflowInstance)
				.setStatus(closedStatus));

		assertThat(services.getWorkflowInstanceTasks(approvalWorkflowInstance)).extracting("title", "status").containsOnly(
				tuple("Demande d'approbation", closedStatus),
				tuple("Détails", closedStatus),
				tuple("Approuvée - Première tâche", closedStatus),
				tuple("Approuvée - Deuxième tâche", closedStatus));

		assertThatRecord(tasks.getBetaWorkflowInstance(approvalWorkflowInstance.getId()))
				.hasMetadata(tasks.betaWorkflowInstance.status(), WorkflowInstanceStatus.FINISHED);
	}

	private ListAssert<BetaWorkflowTaskProgressionVO> assertThatWorkflowProgression(
			BetaWorkflowInstance workflowInstance) {
		SessionContext sessionContext = FakeSessionContext.forRealUserIncollection(users.adminIn(zeCollection));
		return assertThat(services.getFlattenModelTaskProgressionVOs(workflowInstance, sessionContext));
	}

	@Test
	public void whenCancellingAWorkflowThen()
			throws Exception {

		String standbyStatus = tasks.getTaskStatusWithCode(TaskStatus.STANDBY_CODE).getId();
		String finishedStatus = searchServices.searchSingleResult(
				from(tasks.ddvTaskStatus.schemaType()).where(tasks.ddvTaskStatus.statusType()).isEqualTo(
						TaskStatusType.FINISHED)).getId();
		String closedStatus = tasks.getTaskStatusWithCode(TaskStatus.CLOSED_CODE).getId();

		BetaWorkflow approvalWorkflow = tasks.getBetaWorkflowWithCode("approval");
		BetaWorkflowInstance approvalWorkflowInstance = services
				.start(approvalWorkflow, users.edouardIn(zeCollection), noExtraFields);

		assertThat(services.getCurrentWorkflowInstances()).containsOnly(approvalWorkflowInstance);
		assertThatRecord(approvalWorkflowInstance)
				.hasMetadata(tasks.betaWorkflowInstance.status(), WorkflowInstanceStatus.IN_PROGRESS);

		assertThat(services.getWorkflowInstanceTasks(approvalWorkflowInstance)).extracting("title", "status").containsOnly(
				tuple("Détails", standbyStatus));
		recordServices.update(services.getCurrentWorkflowInstanceTask(approvalWorkflowInstance).setStatus(closedStatus));

		assertThat(services.getWorkflowInstanceTasks(approvalWorkflowInstance)).extracting("title", "status").containsOnly(
				tuple("Demande d'approbation", standbyStatus),
				tuple("Détails", closedStatus));

		Task currentTask = services.getCurrentWorkflowInstanceTask(approvalWorkflowInstance);

		recordServices.update(currentTask.setStatus(finishedStatus).setDecision("true"));

		assertThatRecord(tasks.getBetaWorkflowInstance(approvalWorkflowInstance.getId()))
				.hasMetadata(tasks.betaWorkflowInstance.status(), WorkflowInstanceStatus.IN_PROGRESS);
		assertThat(services.getWorkflowInstanceTasks(approvalWorkflowInstance)).extracting("title", "status").containsOnly(
				tuple("Demande d'approbation", finishedStatus),
				tuple("Détails", closedStatus),
				tuple("Approuvée - Première tâche", standbyStatus));

		services.cancel(approvalWorkflowInstance);

		assertThatRecord(tasks.getBetaWorkflowInstance(approvalWorkflowInstance.getId()))
				.hasMetadata(tasks.betaWorkflowInstance.status(), WorkflowInstanceStatus.CANCELLED);
		assertThat(services.getWorkflowInstanceTasks(approvalWorkflowInstance)).extracting("title", "status").containsOnly(
				tuple("Demande d'approbation", finishedStatus),
				tuple("Détails", closedStatus));
	}

	@Test
	public void whenDisplayingModelTaskHasATreeThenValidTasks()
			throws Exception {

		SessionContext sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		BetaWorkflow approvalWorkflow = tasks.getBetaWorkflowWithCode("approval");
		BetaWorkflow simpleWorkflow = tasks.getBetaWorkflowWithCode("simple");
		services.start(approvalWorkflow, users.edouardIn(zeCollection), noExtraFields);
		services.start(simpleWorkflow, users.edouardIn(zeCollection), noExtraFields);

		//--
		// Approval workflow
		//--

		List<BetaWorkflowTaskVO> approvalWorkflowRoots = services.getRootModelTaskVOs(approvalWorkflow, sessionContext);
		assertThat(approvalWorkflowRoots).extracting("id", "decision").isEqualTo(asList(
				tuple("details", null),
				tuple("approval", null)));

		//details
		assertThat(services.getChildModelTasks(approvalWorkflowRoots.get(0), sessionContext)).isEmpty();

		//approval
		List<BetaWorkflowTaskVO> approvalNodes = services.getChildModelTasks(approvalWorkflowRoots.get(1), sessionContext);
		assertThat(approvalNodes).extracting("id", "decision").isEqualTo(asList(
				tuple("approval", "false"),
				tuple("approval", "true")));

		//approved nodes
		List<BetaWorkflowTaskVO> approvedNodes = services.getChildModelTasks(approvalNodes.get(1), sessionContext);
		assertThat(approvedNodes).extracting("id", "decision").isEqualTo(asList(
				tuple("approvedFirstTask", null),
				tuple("approvedSecondTask", null)));

		//refused nodes
		List<BetaWorkflowTaskVO> refusedNodes = services.getChildModelTasks(approvalNodes.get(0), sessionContext);
		assertThat(refusedNodes).extracting("id", "decision").isEqualTo(asList(
				tuple("refusalFirstTask", null)));

		//--
		// Simple workflow
		//--

		List<BetaWorkflowTaskVO> simpleWorkflowRoots = services.getRootModelTaskVOs(simpleWorkflow, sessionContext);
		assertThat(simpleWorkflowRoots).extracting("id", "decision").isEqualTo(asList(
				tuple("task1", null),
				tuple("task2", null),
				tuple("task3", null),
				tuple("task4", null),
				tuple("taskZ", null)));

		//--
		// Move tasks
		//--

	}

	@Test
	public void whenDeletingTaskWithDecisionThenAllHierarchyIsDeleted()
			throws Exception {

		SessionContext sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		BetaWorkflow approvalWorkflow = tasks.getBetaWorkflowWithCode("approval");
		BetaWorkflow simpleWorkflow = tasks.getBetaWorkflowWithCode("simple");
		services.start(approvalWorkflow, users.edouardIn(zeCollection), noExtraFields);
		services.start(simpleWorkflow, users.edouardIn(zeCollection), noExtraFields);

		//--
		// Approval workflow
		//--

		List<BetaWorkflowTaskVO> approvalWorkflowRoots = services.getRootModelTaskVOs(approvalWorkflow, sessionContext);
		assertThat(approvalWorkflowRoots).extracting("id", "decision").isEqualTo(asList(
				tuple("details", null),
				tuple("approval", null)));

		services.delete(approvalWorkflowRoots.get(1), sessionContext);

		//details
		assertThat(services.getChildModelTasks(approvalWorkflowRoots.get(0), sessionContext)).isEmpty();

		approvalWorkflowRoots = services.getRootModelTaskVOs(approvalWorkflow, sessionContext);
		assertThat(approvalWorkflowRoots).extracting("id", "decision").isEqualTo(asList(
				tuple("details", null)));

	}

	@Test
	public void whenDeletingTaskDecisionNodeThenAllHierarchyIsDeleted()
			throws Exception {

		SessionContext sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		BetaWorkflow approvalWorkflow = tasks.getBetaWorkflowWithCode("approval");
		BetaWorkflow simpleWorkflow = tasks.getBetaWorkflowWithCode("simple");
		services.start(approvalWorkflow, users.edouardIn(zeCollection), noExtraFields);
		services.start(simpleWorkflow, users.edouardIn(zeCollection), noExtraFields);

		//--
		// Approval workflow
		//--

		List<BetaWorkflowTaskVO> approvalWorkflowRoots = services.getRootModelTaskVOs(approvalWorkflow, sessionContext);
		assertThat(approvalWorkflowRoots).extracting("id", "decision").isEqualTo(asList(
				tuple("details", null),
				tuple("approval", null)));

		List<BetaWorkflowTaskVO> approvalNodes = services.getChildModelTasks(approvalWorkflowRoots.get(1), sessionContext);
		assertThat(approvalNodes).extracting("id", "decision").isEqualTo(asList(
				tuple("approval", "false"),
				tuple("approval", "true")));

		//approved nodes
		List<BetaWorkflowTaskVO> approvedNodes = services.getChildModelTasks(approvalNodes.get(1), sessionContext);
		assertThat(approvedNodes).extracting("id", "decision").isEqualTo(asList(
				tuple("approvedFirstTask", null),
				tuple("approvedSecondTask", null)));

		services.delete(approvedNodes.get(0), sessionContext);

		approvalWorkflowRoots = services.getRootModelTaskVOs(approvalWorkflow, sessionContext);
		assertThat(approvalWorkflowRoots).extracting("id", "decision").isEqualTo(asList(
				tuple("details", null),
				tuple("approval", null)));

		approvalNodes = services.getChildModelTasks(approvalWorkflowRoots.get(1), sessionContext);
		assertThat(approvalNodes).extracting("id", "decision").isEqualTo(asList(
				tuple("approval", "false"),
				tuple("approval", "true")));

		//approved nodes
		approvedNodes = services.getChildModelTasks(approvalNodes.get(1), sessionContext);
		assertThat(approvedNodes).extracting("id", "decision").isEqualTo(asList(
				tuple("approvedSecondTask", null)));

		List<BetaWorkflowTaskVO> simpleWorkflowRoots = services.getRootModelTaskVOs(simpleWorkflow, sessionContext);
		assertThat(simpleWorkflowRoots).extracting("id", "decision").isEqualTo(asList(
				tuple("task1", null),
				tuple("task2", null),
				tuple("task3", null),
				tuple("task4", null),
				tuple("taskZ", null)));

		services.delete(simpleWorkflowRoots.get(2), sessionContext);

		simpleWorkflowRoots = services.getRootModelTaskVOs(simpleWorkflow, sessionContext);
		assertThat(simpleWorkflowRoots).extracting("id", "decision").isEqualTo(asList(
				tuple("task1", null),
				tuple("task2", null),
				tuple("task4", null),
				tuple("taskZ", null)));

		services.delete(simpleWorkflowRoots.get(3), sessionContext);

		simpleWorkflowRoots = services.getRootModelTaskVOs(simpleWorkflow, sessionContext);
		assertThat(simpleWorkflowRoots).extracting("id", "decision").isEqualTo(asList(
				tuple("task1", null),
				tuple("task2", null),
				tuple("task4", null)));

		services.delete(simpleWorkflowRoots.get(0), sessionContext);

		simpleWorkflowRoots = services.getRootModelTaskVOs(simpleWorkflow, sessionContext);
		assertThat(simpleWorkflowRoots).extracting("id", "decision").isEqualTo(asList(
				tuple("task2", null),
				tuple("task4", null)));

	}

	@Test
	public void whenCreateTaskAfterAnotherOneThenCorrect()
			throws Exception {

		SessionContext sessionContext = FakeSessionContext.forRealUserIncollection(users.dakotaLIndienIn(zeCollection));
		BetaWorkflow simpleWorkflow = tasks.getBetaWorkflowWithCode("simple");

		List<BetaWorkflowTaskVO> simpleWorkflowRoots = services.getRootModelTaskVOs(simpleWorkflow, sessionContext);
		assertThat(simpleWorkflowRoots).extracting("title", "decision").isEqualTo(asList(
				tuple("Task 1", null),
				tuple("Task 2", null),
				tuple("Task 3", null),
				tuple("Task 4", null),
				tuple("Task Z", null)));

		services.createModelTaskAfter(simpleWorkflow, simpleWorkflowRoots.get(1), null, "New task", sessionContext);

		simpleWorkflowRoots = services.getRootModelTaskVOs(simpleWorkflow, sessionContext);
		assertThat(simpleWorkflowRoots).extracting("title", "decision").isEqualTo(asList(
				tuple("Task 1", null),
				tuple("Task 2", null),
				tuple("New task", null),
				tuple("Task 3", null),
				tuple("Task 4", null),
				tuple("Task Z", null)));

		services.createDecisionModelTaskAfter(simpleWorkflow, simpleWorkflowRoots.get(5), null,
				"New decision task", asList("decision1", "decision2"), sessionContext);

		simpleWorkflowRoots = services.getRootModelTaskVOs(simpleWorkflow, sessionContext);
		assertThat(simpleWorkflowRoots).extracting("title", "decision").isEqualTo(asList(
				tuple("Task 1", null),
				tuple("Task 2", null),
				tuple("New task", null),
				tuple("Task 3", null),
				tuple("Task 4", null),
				tuple("Task Z", null),
				tuple("New decision task", null)));

		assertThat(services.getChildModelTasks(simpleWorkflowRoots.get(2), sessionContext)).isEmpty();

		assertThat(services.getChildModelTasks(simpleWorkflowRoots.get(6), sessionContext)).extracting("title")
				.containsOnly("New decision task - decision1", "New decision task - decision2");
	}

	@Test
	public void whenMovingAfterThenCorrectlyMoved()
			throws Exception {

		SessionContext sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		BetaWorkflow approvalWorkflow = tasks.getBetaWorkflowWithCode("approval");
		BetaWorkflow simpleWorkflow = tasks.getBetaWorkflowWithCode("simple");

		List<BetaWorkflowTaskVO> simpleWorkflowRoots;
		List<BetaWorkflowTaskVO> approvalWorkflowRoots = services.getRootModelTaskVOs(approvalWorkflow, sessionContext);

		//approval
		List<BetaWorkflowTaskVO> approvalNodes = services.getChildModelTasks(approvalWorkflowRoots.get(1), sessionContext);

		//approved nodes
		List<BetaWorkflowTaskVO> approvedNodes;

		//refused nodes
		List<BetaWorkflowTaskVO> refusedNodes = services.getChildModelTasks(approvalNodes.get(0), sessionContext);

		//--
		// Simple workflow
		//--
		simpleWorkflowRoots = services.getRootModelTaskVOs(simpleWorkflow, sessionContext);

		services.moveAfter(refusedNodes.get(0), approvalNodes.get(0), sessionContext);
		//Change nothing

		//approval
		approvalNodes = services.getChildModelTasks(approvalWorkflowRoots.get(1), sessionContext);
		assertThat(approvalNodes).extracting("id", "decision").isEqualTo(asList(
				tuple("approval", "false"),
				tuple("approval", "true")));

		//approved nodes
		approvedNodes = services.getChildModelTasks(approvalNodes.get(1), sessionContext);
		assertThat(approvedNodes).extracting("id", "decision").isEqualTo(asList(
				tuple("approvedFirstTask", null),
				tuple("approvedSecondTask", null)));

		//refused nodes
		refusedNodes = services.getChildModelTasks(approvalNodes.get(0), sessionContext);
		assertThat(refusedNodes).extracting("id", "decision").isEqualTo(asList(
				tuple("refusalFirstTask", null)));

		services.moveAfter(refusedNodes.get(0), approvalNodes.get(1), sessionContext);

		refusedNodes = services.getChildModelTasks(approvalNodes.get(0), sessionContext);
		assertThat(refusedNodes).extracting("id", "decision").isEmpty();

		//approved nodes
		approvedNodes = services.getChildModelTasks(approvalNodes.get(1), sessionContext);
		assertThat(approvedNodes).extracting("id", "decision").isEqualTo(asList(
				tuple("refusalFirstTask", null),
				tuple("approvedFirstTask", null),
				tuple("approvedSecondTask", null)));

		services.moveAfter(simpleWorkflowRoots.get(4), simpleWorkflowRoots.get(1), sessionContext);

		simpleWorkflowRoots = services.getRootModelTaskVOs(simpleWorkflow, sessionContext);
		assertThat(simpleWorkflowRoots).extracting("id", "decision").isEqualTo(asList(
				tuple("task1", null),
				tuple("task2", null),
				tuple("taskZ", null),
				tuple("task3", null),
				tuple("task4", null)
		));

	}

	private long getAllTasksCount() {
		return getModelLayerFactory().newSearchServices().getResultsCount(from(tasks.userTask.schemaType()).returnAll());
	}

	private void givenApprovalWorkflow()
			throws Exception {

		TasksSchemasRecordsServices tasks = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		String legends = users.legendsIn(zeCollection).getId();
		String heroes = users.heroesIn(zeCollection).getId();

		Transaction transaction = new Transaction();

		BetaWorkflow workflow = transaction.add(tasks.newBetaWorkflow());
		workflow.setCode("approval");
		workflow.setTitle("Un workflow d'approbation");

		BetaWorkflowTask details = transaction.add(tasks.newWorkflowModelTaskWithId("details", workflow));
		BetaWorkflowTask approval = transaction.add(tasks.newWorkflowModelTaskWithId("approval", workflow));
		BetaWorkflowTask approvedSecondTask = transaction.add(tasks.newWorkflowModelTaskWithId("approvedSecondTask", workflow));
		BetaWorkflowTask refusalFirstTask = transaction.add(tasks.newWorkflowModelTaskWithId("refusalFirstTask", workflow));
		BetaWorkflowTask approvedFirstTask = transaction.add(tasks.newWorkflowModelTaskWithId("approvedFirstTask", workflow));

		details.setTitle("Détails").setAssigneeGroupsCandidates(asList(legends));
		approval.setTitle("Demande d'approbation").setAssigneeGroupsCandidates(asList(legends));
		approvedFirstTask.setTitle("Approuvée - Première tâche").setAssigneeGroupsCandidates(asList(heroes));
		approvedSecondTask.setTitle("Approuvée - Deuxième tâche").setAssigneeGroupsCandidates(asList(heroes));
		refusalFirstTask.setTitle("Refusées - Première tâche").setAssigneeGroupsCandidates(asList(heroes));

		getModelLayerFactory().newRecordServices().execute(transaction);

		recordServices.update(approval.addNextTaskDecision("true", approvedFirstTask.getId())
				.addNextTaskDecision("false", refusalFirstTask.getId()));
		recordServices.update(approvedFirstTask.setNextTask(approvedSecondTask.getId()));
		//recordServices.update(refusalFirstTask.setNextTask(approval.getId()));
		recordServices.update(details.setNextTask(approval.getId()));

	}

	private void givenSimpleWorkflow()
			throws Exception {

		TasksSchemasRecordsServices tasks = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		String legends = users.legendsIn(zeCollection).getId();
		String heroes = users.heroesIn(zeCollection).getId();
		String dakota = users.dakotaLIndienIn(zeCollection).getId();
		String gandalf = users.gandalfLeblancIn(zeCollection).getId();
		String edouard = users.edouardLechatIn(zeCollection).getId();
		String chuckNorris = users.chuckNorrisIn(zeCollection).getId();

		Transaction transaction = new Transaction();

		BetaWorkflow workflow = tasks.newBetaWorkflow();
		workflow.setCode("simple");
		workflow.setTitle("Un simple workflow");
		recordServices.add(workflow);

		BetaWorkflowTask taskZ = transaction.add(tasks.newWorkflowModelTaskWithId("taskZ", workflow));
		BetaWorkflowTask task4 = transaction.add(tasks.newWorkflowModelTaskWithId("task4", workflow));
		BetaWorkflowTask task1 = transaction.add(tasks.newWorkflowModelTaskWithId("task1", workflow));
		BetaWorkflowTask task3 = transaction.add(tasks.newWorkflowModelTaskWithId("task3", workflow));
		BetaWorkflowTask task2 = transaction.add(tasks.newWorkflowModelTaskWithId("task2", workflow));

		task1.setTitle("Task 1").setAssigneeGroupsCandidates(asList(legends));
		task2.setTitle("Task 2").setAssigneeUsersCandidates(asList(dakota, gandalf));
		task3.setTitle("Task 3").setAssigneeUsersCandidates(asList(dakota, edouard));
		task4.setTitle("Task 4").setAssignee(chuckNorris).setAssignationDate(new LocalDate()).setAssignedOn(new LocalDate())
				.setAssigner(chuckNorris);
		taskZ.setTitle("Task Z").setAssigneeGroupsCandidates(asList(heroes));
		recordServices.execute(transaction);

		recordServices.update(task1.setNextTask(task2.getId()));
		recordServices.update(task2.setNextTask(task3.getId()));
		recordServices.update(task3.setNextTask(task4.getId()));
		recordServices.update(task4.setNextTask(taskZ.getId()));

	}

	private void givenSimpleWorkflow2()
			throws Exception {

		TasksSchemasRecordsServices tasks = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		String legends = users.legendsIn(zeCollection).getId();
		String dakota = users.dakotaLIndienIn(zeCollection).getId();
		String gandalf = users.gandalfLeblancIn(zeCollection).getId();
		String edouard = users.edouardLechatIn(zeCollection).getId();

		Transaction transaction = new Transaction();

		BetaWorkflow workflow = tasks.newBetaWorkflow();
		workflow.setCode("simple2");
		workflow.setTitle("Un simple workflow2");
		recordServices.add(workflow);

		BetaWorkflowTask task1 = transaction.add(tasks.newWorkflowModelTaskWithId("task1", workflow));
		BetaWorkflowTask task2 = transaction.add(tasks.newWorkflowModelTaskWithId("task2", workflow));
		BetaWorkflowTask task3 = transaction.add(tasks.newWorkflowModelTaskWithId("task3", workflow));
		BetaWorkflowTask task4 = transaction.add(tasks.newWorkflowModelTaskWithId("task4", workflow));
		BetaWorkflowTask task5 = transaction.add(tasks.newWorkflowModelTaskWithId("task5", workflow));
		BetaWorkflowTask task6 = transaction.add(tasks.newWorkflowModelTaskWithId("task6", workflow));
		BetaWorkflowTask task7 = transaction.add(tasks.newWorkflowModelTaskWithId("task7", workflow));

		task1.setTitle("Task 1").setAssigneeGroupsCandidates(asList(legends));
		task2.setTitle("Task 2").setAssigneeUsersCandidates(asList(dakota, gandalf));
		task3.setTitle("Task 3").setAssigneeUsersCandidates(asList(dakota, edouard));
		task4.setTitle("Task 4").setAssigneeUsersCandidates(asList(dakota, gandalf));
		task5.setTitle("Task 5").setAssigneeUsersCandidates(asList(dakota, gandalf));
		task6.setTitle("Task 6").setAssigneeUsersCandidates(asList(dakota, edouard));
		task7.setTitle("Task 7").setAssigneeUsersCandidates(asList(dakota, edouard));

		recordServices.execute(transaction);

		recordServices.update(task1.setNextTask(task2.getId()));
		recordServices.update(task2.addNextTaskDecision("T3", task3.getId()).addNextTaskDecision("T5", task5.getId()));
		recordServices.update(task3.setNextTask(task4.getId()));
		recordServices.update(task4.addNextTaskDecision("T2", task2.getId()).addNextTaskDecision("T6", task6.getId()));
		recordServices.update(task5.setNextTask(task6.getId()));
		recordServices.update(task6.setNextTask(task7.getId()));

	}
}
