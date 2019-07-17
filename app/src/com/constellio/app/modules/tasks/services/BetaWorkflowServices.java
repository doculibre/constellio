package com.constellio.app.modules.tasks.services;

import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflow;
import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflowInstance;
import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflowTask;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.WorkflowInstanceStatus;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.app.modules.tasks.services.BetaWorkflowServicesRuntimeException.WorkflowServicesRuntimeException_UnsupportedAddAtPosition;
import com.constellio.app.modules.tasks.services.BetaWorkflowServicesRuntimeException.WorkflowServicesRuntimeException_UnsupportedMove;
import com.constellio.app.modules.tasks.ui.entities.BetaWorkflowTaskProgressionVO;
import com.constellio.app.modules.tasks.ui.entities.BetaWorkflowTaskVO;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.VisibilityStatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class BetaWorkflowServices {
	String collection;
	SearchServices searchServices;
	RecordServices recordServices;
	AppLayerFactory appLayerFactory;
	TasksSchemasRecordsServices tasks;

	public BetaWorkflowServices(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.tasks = new TasksSchemasRecordsServices(collection, appLayerFactory);
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
	}

	public List<BetaWorkflow> getWorkflows() {
		return tasks.searchBetaWorkflows(getWorkflowsQuery());
	}

	public LogicalSearchQuery getWorkflowsQuery() {
		return new LogicalSearchQuery(from(tasks.betaWorkflow.schemaType()).returnAll())
				.sortAsc(Schemas.TITLE).filteredByStatus(StatusFilter.ACTIVES);
	}

	public List<BetaWorkflowInstance> getCurrentWorkflowInstances() {
		return tasks.searchBetaWorkflowInstances(getCurrentWorkflowInstancesQuery());
	}

	public LogicalSearchQuery getCurrentWorkflowInstancesQuery() {
		return new LogicalSearchQuery(from(tasks.betaWorkflowInstance.schemaType())
				.where(tasks.betaWorkflowInstance.status()).isEqualTo(WorkflowInstanceStatus.IN_PROGRESS))
				.sortAsc(Schemas.TITLE);
	}

	public List<BetaWorkflowTaskProgressionVO> getFlattenModelTaskProgressionVOs(
			BetaWorkflowInstance workflowInstance, SessionContext sessionContext) {
		List<BetaWorkflowTaskProgressionVO> taskProgressionVOs = new ArrayList<>();
		BetaWorkflow workflow = tasks.getBetaWorkflow(workflowInstance.getWorkflow());

		for (BetaWorkflowTaskVO taskVO : getFlattenModelTaskVOs(workflow, sessionContext)) {
			taskProgressionVOs.add(getTaskProgression(workflowInstance, taskVO));
		}

		return taskProgressionVOs;
	}

	public List<BetaWorkflowTaskVO> getFlattenModelTaskVOs(BetaWorkflow workflow, SessionContext sessionContext) {
		List<BetaWorkflowTaskVO> tasks = new ArrayList<>();

		for (BetaWorkflowTaskVO task : getRootModelTaskVOs(workflow, sessionContext)) {
			tasks.addAll(getAllTasksInHierarchy(task, sessionContext));
		}
		return tasks;
	}

	private BetaWorkflowTaskProgressionVO getTaskProgression(BetaWorkflowInstance workflowInstance,
															 BetaWorkflowTaskVO workflowTaskVO) {
		Task instanceTask = tasks.wrapTask(searchServices.searchSingleResult(from(tasks.userTask.schemaType())
				.where(tasks.userTask.betaWorkflowInstance()).isEqualTo(workflowInstance)
				.andWhere(tasks.userTask.modelTask()).isEqualTo(workflowTaskVO.getId())
		));

		BetaWorkflowTaskProgressionVO workflowTaskProgressionVO = new BetaWorkflowTaskProgressionVO();
		workflowTaskProgressionVO.setDecision(workflowTaskVO.getDecision());
		workflowTaskProgressionVO.setWorkflowTaskVO(workflowTaskVO);
		workflowTaskProgressionVO.setTitle(workflowTaskVO.getTitle());

		if (instanceTask != null && workflowTaskVO.getDecision() == null) {
			workflowTaskProgressionVO.setDueDate(instanceTask.getDueDate());
			workflowTaskProgressionVO.setStatus(instanceTask.getStatusType());
		}

		return workflowTaskProgressionVO;

	}

	private List<BetaWorkflowTaskVO> getAllTasksInHierarchy(BetaWorkflowTaskVO task, SessionContext sessionContext) {
		List<BetaWorkflowTaskVO> taskVOs = new ArrayList<>();
		taskVOs.add(task);
		for (BetaWorkflowTaskVO child : getChildModelTasks(task, sessionContext)) {
			taskVOs.addAll(getAllTasksInHierarchy(child, sessionContext));
		}
		return taskVOs;
	}

	public List<BetaWorkflowTaskVO> getRootModelTaskVOs(BetaWorkflow workflow, SessionContext sessionContext) {
		List<BetaWorkflowTaskVO> workflows = new ArrayList<>();
		for (Task modelTask : getStartModelTask(workflow)) {
			workflows.addAll(getWorkflowTasksStarting(modelTask.getId(), sessionContext));
		}

		return workflows;
	}

	BetaWorkflowTaskVO newWorkflowTaskVO(Task modelTask, SessionContext sessionContext) {
		BetaWorkflowTask betaModelTask = new BetaWorkflowTask(modelTask);
		RecordToVOBuilder recordToVOBuilder = new RecordToVOBuilder();
		TaskVO taskVO = new TaskVO(recordToVOBuilder.build(modelTask.getWrappedRecord(), VIEW_MODE.TABLE, sessionContext));

		BetaWorkflowTaskVO workflowTaskVO = new BetaWorkflowTaskVO();
		workflowTaskVO.setId(modelTask.getId());
		workflowTaskVO.setTaskVO(taskVO);
		workflowTaskVO.setHasChildren(betaModelTask.hasDecisions());
		workflowTaskVO.setTitle(modelTask.getTitle());

		return workflowTaskVO;
	}

	BetaWorkflowTaskVO newWorkflowTaskVO(Task modelTask, String decision, SessionContext sessionContext) {
		BetaWorkflowTaskVO workflowTaskVO = newWorkflowTaskVO(modelTask, sessionContext);
		workflowTaskVO.setDecision(decision);
		if (decision != null) {
			workflowTaskVO.setTitle(modelTask.getTitle() + " - " + decision);
		}
		return workflowTaskVO;
	}

	private List<BetaWorkflowTaskVO> getWorkflowTasksStarting(String taskId, SessionContext sessionContext) {
		List<BetaWorkflowTaskVO> workflowTaskVOs = new ArrayList<>();
		if (taskId != null && !taskId.equals("NO_VALUE")) {
			BetaWorkflowTask task = tasks.getBetaWorkflowTask(taskId);
			workflowTaskVOs.add(newWorkflowTaskVO(task, sessionContext));

			if (!task.hasDecisions() && task.hasNextTask()) {
				workflowTaskVOs.addAll(getWorkflowTasksStarting(task.getSingleNextTask(), sessionContext));
			}

		}
		return workflowTaskVOs;

	}

	public List<BetaWorkflowTaskProgressionVO> getRootModelTaskProgressionsVOs(BetaWorkflowInstance workflowInstance,
																			   SessionContext sessionContext) {
		BetaWorkflow workflow = tasks.getBetaWorkflow(workflowInstance.getWorkflow());
		List<BetaWorkflowTaskProgressionVO> progressionVOs = new ArrayList<>();

		for (BetaWorkflowTaskVO child : getRootModelTaskVOs(workflow, sessionContext)) {
			progressionVOs.add(getTaskProgression(workflowInstance, child));
		}

		return progressionVOs;
	}

	public List<BetaWorkflowTaskProgressionVO> getChildModelTaskProgressions(BetaWorkflowInstance workflowInstance,
																			 BetaWorkflowTaskVO workflowTaskVO,
																			 SessionContext sessionContext) {
		List<BetaWorkflowTaskProgressionVO> progressionVOs = new ArrayList<>();

		for (BetaWorkflowTaskVO child : getChildModelTasks(workflowTaskVO, sessionContext)) {
			progressionVOs.add(getTaskProgression(workflowInstance, child));
		}

		return progressionVOs;
	}

	public List<BetaWorkflowTaskVO> getChildModelTasks(BetaWorkflowTaskVO workflowTaskVO,
													   SessionContext sessionContext) {

		if (workflowTaskVO.getDecision() == null) {
			List<BetaWorkflowTaskVO> workflows = new ArrayList<>();
			if (workflowTaskVO.hasChildren()) {
				BetaWorkflowTask task = tasks.getBetaWorkflowTask(workflowTaskVO.getId());
				for (String decision : task.getNextTasksDecisionsCodes()) {
					workflows.add(newWorkflowTaskVO(task, decision, sessionContext));
				}
			}
			return workflows;
		} else {
			BetaWorkflowTask task = tasks.getBetaWorkflowTask(workflowTaskVO.getId());
			String taskId = task.getNextTask(workflowTaskVO.getDecision());
			if (taskId == null) {
				return new ArrayList<>();
			} else {
				return getWorkflowTasksStarting(taskId, sessionContext);
			}
		}

	}

	public boolean canAddTaskIn(BetaWorkflowTaskVO selectedTask, SessionContext sessionContext) {
		BetaWorkflowTask task = tasks.getBetaWorkflowTask(selectedTask.getId());
		for (BetaWorkflowTaskVO workflowTaskVO : getAvailableWorkflowTaskVOForNewTask(task.getWorkflow(), sessionContext)) {
			if (selectedTask.hasSameIdDecision(workflowTaskVO)) {
				return true;
			}
		}
		return false;
	}

	public boolean canAddDecisionTaskIn(BetaWorkflowTaskVO selectedTask, SessionContext sessionContext) {
		return canAddTaskIn(selectedTask, sessionContext) && getTaskVOAfter(selectedTask, sessionContext) == null;
	}

	public List<BetaWorkflowTaskVO> getAvailableWorkflowTaskVOForNewTask(String workflowId,
																		 SessionContext sessionContext) {
		List<BetaWorkflowTaskVO> tasks = new ArrayList<>();

		for (BetaWorkflowTask task : getWorkflowModelTasks(workflowId)) {
			if (task.hasDecisions()) {
				for (String decision : task.getNextTasksDecisionsCodes()) {
					tasks.add(newWorkflowTaskVO(task, decision, sessionContext));
				}
			} else {
				tasks.add(newWorkflowTaskVO(task, sessionContext));
			}
		}

		return tasks;
	}

	/**
	 * Create a task, which will be the next task of the current task
	 *
	 * @param taskType TODO
	 */
	public BetaWorkflowTask createModelTaskAfter(BetaWorkflow workflow, BetaWorkflowTaskVO selectedTask,
												 String taskType,
												 String title, SessionContext sessionContext) {
		return createDecisionModelTaskAfter(workflow, selectedTask, taskType, title, null, sessionContext);
	}

	/**
	 * Create a task with a true/false decision, and create a task for each decisions
	 *
	 * @param taskType TODO
	 */
	public BetaWorkflowTask createDecisionModelTaskAfter(BetaWorkflow workflow, BetaWorkflowTaskVO selectedTask,
														 String taskType,
														 String title,
														 List<String> decisions, SessionContext sessionContext) {
		BetaWorkflowTask newTask;
		if (taskType != null) {
			newTask = tasks.newWorkflowModelTaskWithType(workflow, taskType);
		} else {
			newTask = tasks.newWorkflowModelTask(workflow);
		}
		newTask.setTitle(title);
		newTask.setModel(true);
		newTask.setAssignee(sessionContext.getCurrentUser().getId());
		newTask.setAssigner(sessionContext.getCurrentUser().getId());
		newTask.setAssignedOn(TimeProvider.getLocalDate());
		newTask.setAssignationDate(TimeProvider.getLocalDate());

		try {
			recordServices.add(newTask);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		try {
			if (selectedTask != null) {
				BetaWorkflowTaskVO newTaskVO = newWorkflowTaskVO(newTask, sessionContext);
				moveAfter(newTaskVO, selectedTask, sessionContext);
			}

			recordServices.refresh(newTask);

			if (decisions != null) {
				Map<String, String> decisionsMap = new HashMap<>();
				for (String decision : decisions) {
					decisionsMap.put(decision, "NO_VALUE");
				}
				newTask.setNextTasksDecisions(decisionsMap);
			}
			try {
				recordServices.update(newTask);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			recordServices.logicallyDelete(newTask.getWrappedRecord(), User.GOD);
			recordServices.physicallyDelete(newTask.getWrappedRecord(), User.GOD);

			throw new WorkflowServicesRuntimeException_UnsupportedAddAtPosition(selectedTask, t);
		}

		return newTask;
	}

	/**
	 * Create a task, which will be the next task of the current task
	 */
	public void moveAfter(BetaWorkflowTaskVO selectedWorkflowTaskVO, BetaWorkflowTaskVO moveAfterTask,
						  SessionContext sessionContext) {

		BetaWorkflowTaskVO taskBeforeSelected = getTaskVOBefore(selectedWorkflowTaskVO, sessionContext);
		BetaWorkflowTaskVO taskAfterSelected = getTaskVOAfter(selectedWorkflowTaskVO, sessionContext);

		BetaWorkflowTaskVO newTaskBeforeSelected = moveAfterTask;
		BetaWorkflowTaskVO newTaskAfterSelected = getTaskVOAfter(moveAfterTask, sessionContext);

		Transaction transaction = new Transaction();

		replaceRelationShip(transaction, taskBeforeSelected, taskAfterSelected);
		replaceRelationShip(transaction, newTaskBeforeSelected, selectedWorkflowTaskVO);
		replaceRelationShip(transaction, selectedWorkflowTaskVO, newTaskAfterSelected);

		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	public void addAfter(BetaWorkflowTaskVO existingWorkflowTaskVO, BetaWorkflowTaskVO targetTask,
						 SessionContext sessionContext) {

		//		WorkflowTaskVO taskBeforeSelected = getTaskVOBefore(selectedWorkflowTaskVO, sessionContext);
		//		WorkflowTaskVO taskAfterSelected = getTaskVOAfter(selectedWorkflowTaskVO, sessionContext);

		//		WorkflowTaskVO newTaskBeforeSelected = moveAfterTask;
		//		WorkflowTaskVO newTaskAfterSelected = getTaskVOAfter(moveAfterTask, sessionContext);

		Transaction transaction = new Transaction();

		//		replaceRelationShip(transaction, taskBeforeSelected, taskAfterSelected);
		//		replaceRelationShip(transaction, newTaskBeforeSelected, selectedWorkflowTaskVO);
		addRelationShip(transaction, existingWorkflowTaskVO, targetTask);

		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	private void replaceRelationShip(Transaction transaction, BetaWorkflowTaskVO node,
									 BetaWorkflowTaskVO newNextNode) {

		if (node == null) {
			return;
		}

		if (node.getDecision() == null && node.hasChildren()) {
			throw new WorkflowServicesRuntimeException_UnsupportedMove("Cannot move something to a node with decisions");
		}

		if (newNextNode != null && newNextNode.getDecision() != null && newNextNode.hasChildren()) {
			throw new WorkflowServicesRuntimeException_UnsupportedMove("Cannot move a decision node");
		}

		if (newNextNode != null && node.getId().equals(newNextNode.getId())) {
			newNextNode = null;
		}

		String newNextNodeTaskId = newNextNode == null ? null : newNextNode.getId();

		BetaWorkflowTask task = tasks.wrapBetaWorkflowTask(transaction.getRecord(node.getId()));

		if (task == null) {
			task = tasks.getBetaWorkflowTask(node.getId());
		}

		if (node.getDecision() == null) {
			task.setNextTask(newNextNodeTaskId);

		} else {
			task.addNextTaskDecision(node.getDecision(), newNextNodeTaskId);
		}
		transaction.add(task);
	}

	private void addRelationShip(Transaction transaction, BetaWorkflowTaskVO selectNode,
								 BetaWorkflowTaskVO targetNode) {

		if (selectNode == null) {
			return;
		}

		//		if (targetNode.getDecision() == null && targetNode.hasChildren()) {
		//			throw new WorkflowServicesRuntimeException_UnsupportedMove("Cannot move something to a node with decisions");
		//		}
		//
		//		if (targetNode != null && targetNode.getDecision() != null && targetNode.hasChildren()) {
		//			throw new WorkflowServicesRuntimeException_UnsupportedMove("Cannot move a decision node");
		//		}

		if (targetNode != null && selectNode.getId().equals(targetNode.getId())) {
			targetNode = null;
		}

		String newTargetTaskId = targetNode == null ? null : targetNode.getId();

		BetaWorkflowTask task = tasks.getBetaWorkflowTask(newTargetTaskId);

		if (task.getNextTasks().isEmpty()) {
			task.setNextTask(selectNode.getId());
		} else {
			task.addNextTaskDecision(targetNode.getTitle(), selectNode.getId());
		}
		//		if (node.getDecision() == null) {
		//			task.setNextTask(newNextNodeTaskId);
		//
		//		} else {
		//			task.addNextTaskDecision(node.getDecision(), newNextNodeTaskId);
		//		}
		transaction.add(task);
	}

	private BetaWorkflowTaskVO getTaskVOAfter(BetaWorkflowTaskVO node, SessionContext sessionContext) {
		if (node == null) {
			throw new IllegalArgumentException("Node must be not null");
		}
		if (node.getDecision() == null && node.hasChildren()) {
			throw new IllegalArgumentException("Node has multiple nodes after");
		}
		BetaWorkflowTask task = tasks.getBetaWorkflowTask(node.getId());
		String nextId = task.getNextTask(node.getDecision());

		if (nextId == null || nextId.equals("NO_VALUE")) {
			return null;
		} else {
			Task nextTask = tasks.getTask(nextId);
			return newWorkflowTaskVO(nextTask, sessionContext);
		}
	}

	private BetaWorkflowTaskVO getTaskVOBefore(BetaWorkflowTaskVO node, SessionContext sessionContext) {

		if (node == null) {
			throw new IllegalArgumentException("Node must be not null");
		}

		if (node.getDecision() != null) {
			throw new IllegalArgumentException("Node must not be a decision node");
		}

		BetaWorkflowTask task = tasks.getBetaWorkflowTask(node.getId());
		for (BetaWorkflowTask modelTask : getWorkflowModelTasks(task.getWorkflow())) {
			if (modelTask.hasDecisions()) {
				for (String decision : modelTask.getNextTasksDecisionsCodes()) {
					if (task.getId().equals(modelTask.getNextTask(decision))) {
						return newWorkflowTaskVO(modelTask, decision, sessionContext);
					}
				}
			} else if (task.getId().equals(modelTask.getNextTask(null))) {
				return newWorkflowTaskVO(modelTask, sessionContext);
			}
		}

		return null;
	}

	/**
	 * Delete the task (and all its sub tasks)
	 */
	public void delete(BetaWorkflowTaskVO selectedTask, SessionContext sessionContext) {
		BetaWorkflowTask task = tasks.getBetaWorkflowTask(selectedTask.getId());
		BetaWorkflowTaskVO taskBeforeVO = getTaskVOBefore(selectedTask, sessionContext);

		if (!selectedTask.hasChildren()) {
			if (taskBeforeVO != null) {
				BetaWorkflowTask taskBefore = tasks.getBetaWorkflowTask(taskBeforeVO.getId());

				String nextTaskId = task.hasNextTask() ? task.getSingleNextTask() : "NO_VALUE";

				if (taskBeforeVO.getDecision() == null) {
					taskBefore.setNextTask(nextTaskId);
				} else {
					taskBefore.addNextTaskDecision(taskBeforeVO.getDecision(), nextTaskId);
				}
				try {
					recordServices.update(taskBefore);
				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				}

			}
			recordServices.logicallyDelete(task.getWrappedRecord(), User.GOD);
		} else {
			if (taskBeforeVO != null) {
				BetaWorkflowTask taskBefore = tasks.getBetaWorkflowTask(taskBeforeVO.getId());
				if (taskBeforeVO.getDecision() == null) {
					taskBefore.setNextTask(null);
				} else {
					taskBefore.addNextTaskDecision(taskBefore.getDecision(), "NO_VALUE");
				}
				try {
					recordServices.update(taskBefore);
				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				}
			}
			for (BetaWorkflowTaskVO taskVO : getAllTasksInHierarchy(selectedTask, sessionContext)) {
				Task taskInHierarchy = tasks.getTask(taskVO.getId());
				recordServices.logicallyDelete(taskInHierarchy.getWrappedRecord(), User.GOD);
			}
		}
	}

	public List<BetaWorkflowTask> getWorkflowModelTasks(String workflowId) {
		return tasks.searchBetaWorkflowTasks(getWorkflowModelTasksQuery(workflowId));
	}

	public LogicalSearchQuery getWorkflowModelTasksQuery(String workflowId) {
		return new LogicalSearchQuery(from(tasks.userTask.schemaType())
				.where(tasks.userTask.betaWorkflow()).isEqualTo(workflowId)
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull()
				.andWhere(tasks.userTask.isModel()).isTrue()).sortDesc(tasks.userTask.betaWorkflowTaskSort())
				.filteredByVisibilityStatus(VisibilityStatusFilter.ALL).setForceExecutionInSolr(true);
	}

	public Task getCurrentWorkflowInstanceTask(BetaWorkflowInstance workflowInstance) {
		return tasks.wrapTask(searchServices.searchSingleResult(from(tasks.userTask.schemaType())
				.where(tasks.userTask.betaWorkflow()).isEqualTo(workflowInstance.getWorkflow())
				.andWhere(tasks.userTask.status()).isNotIn(tasks.getFinishedOrClosedStatuses())
				.andWhere(tasks.userTask.betaWorkflowInstance()).isEqualTo(workflowInstance.getId())));
	}

	public List<Task> getWorkflowInstanceTasks(BetaWorkflowInstance workflowInstance) {
		return tasks.searchTasks(getWorkflowInstanceTasksQuery(workflowInstance.getWorkflow(), workflowInstance.getId()));
	}

	public List<Task> getWorkflowInstanceTasks(String workflowId, String workflowInstanceId) {
		return tasks.searchTasks(getWorkflowInstanceTasksQuery(workflowId, workflowInstanceId));
	}

	public LogicalSearchQuery getWorkflowInstanceTasksQuery(String workflowId, String workflowInstanceId) {
		return new LogicalSearchQuery(from(tasks.userTask.schemaType())
				.where(tasks.userTask.betaWorkflow()).isEqualTo(workflowId)
				.andWhere(tasks.userTask.betaWorkflowInstance()).isEqualTo(workflowInstanceId));
	}

	public List<Task> getStartModelTask(BetaWorkflow workflow) {

		List<BetaWorkflowTask> modelTasks = getWorkflowModelTasks(workflow.getId());
		Set<String> referencedTasks = new HashSet<>();

		for (BetaWorkflowTask modelTask : modelTasks) {
			referencedTasks.addAll(modelTask.getNextTasks());
		}

		List<Task> startTasks = new ArrayList<>();
		for (Task modelTask : modelTasks) {
			if (!referencedTasks.contains(modelTask.getId())) {
				startTasks.add(modelTask);
			}
		}

		return startTasks;
	}

	public BetaWorkflowInstance start(BetaWorkflow workflow, User user, Map<String, List<String>> extraFields) {
		if (user == null) {
			throw new IllegalArgumentException("User is required");
		}
		BetaWorkflowInstance workflowInstance = tasks.newBetaWorkflowInstance();
		workflowInstance.setTitle(workflow.getTitle());
		workflowInstance.setWorkflow(workflow);
		workflowInstance.setStartedOn(TimeProvider.getLocalDateTime());
		workflowInstance.setStartedBy(user);
		workflowInstance.setExtraFields(extraFields);
		workflowInstance.setWorkflowStatus(WorkflowInstanceStatus.IN_PROGRESS);

		Record event = tasks.newEvent()
				.setUsername(user.getUsername())
				.setUserRoles(StringUtils.join(user.getAllRoles().toArray(), "; "))
				.setIp(workflow.getTitle())        // Looky looky, the nice trick!
				.setRecordId(workflowInstance.getId())
				.setType(EventType.WORKFLOW_STARTED)
				.setTitle(workflow.getTitle())
				.setCreatedOn(TimeProvider.getLocalDateTime())
				.getWrappedRecord();

		Transaction transaction = new Transaction();
		transaction.add(workflowInstance);
		transaction.add(event);
		for (Task modelTask : getStartModelTask(workflow)) {
			Task instanceTask = createInstanceTask(modelTask, workflowInstance);
			transaction.add(instanceTask);
		}

		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		return workflowInstance;
	}

	public Task createInstanceTask(Task modelTask, BetaWorkflowInstance instance) {
		String typeId = modelTask.getType();

		BetaWorkflowTask instanceTask;
		if (typeId != null) {
			TaskType type = tasks.getTaskType(typeId);
			instanceTask = new BetaWorkflowTask(tasks.newTaskWithType(type));
		} else {
			instanceTask = tasks.newBetaWorkflowTask();
		}

		for (Metadata metadata : tasks.userTask.schema().getMetadatas().onlyManuals().onlyNotSystemReserved().onlyEnabled()) {
			if (!metadata.getLocalCode().equals(Schemas.IDENTIFIER)) {
				instanceTask.set(metadata.getLocalCode(), modelTask.get(metadata.getLocalCode()));
			}
		}
		instanceTask.setModel(false);
		instanceTask.setModelTask(modelTask);
		instanceTask.setAssigner(instance.getStartedBy());
		instanceTask.setAssignedOn(TimeProvider.getLocalDate());
		instanceTask.setAssignationDate(TimeProvider.getLocalDate());
		instanceTask.setRelativeDueDate(modelTask.getRelativeDueDate());
		instanceTask.setWorkflowInstance(instance);
		if (instanceTask.getAssignee() == null) {
			instanceTask.setAssignee(instance.getStartedBy());
		}
		if (instance.getExtraFields() != null) {
			for (Map.Entry<String, List<String>> entry : instance.getExtraFields().entrySet()) {
				Metadata metadata = tasks.userTask.schema().get(entry.getKey());
				if (metadata.isMultivalue()) {
					instanceTask.set(metadata.getLocalCode(), entry.getValue());
				} else {
					if (!entry.getValue().isEmpty()) {
						instanceTask.set(metadata.getLocalCode(), entry.getValue().get(0));
					}
				}
			}
		}

		return instanceTask;
	}

	public void cancel(BetaWorkflowInstance workflowInstance) {
		try {
			recordServices.update(workflowInstance.setWorkflowStatus(WorkflowInstanceStatus.CANCELLED));
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		Task currentTask = getCurrentWorkflowInstanceTask(workflowInstance);
		if (currentTask != null) {
			recordServices.logicallyDelete(currentTask.getWrappedRecord(), User.GOD);
			recordServices.physicallyDelete(currentTask.getWrappedRecord(), User.GOD);
		}
	}

	public BetaWorkflowTaskVO getTaskVO(Task task, SessionContext context) {
		return newWorkflowTaskVO(task, context);
	}

	public BetaWorkflowTaskVO getTaskVO(Task task, String decision, SessionContext context) {
		return newWorkflowTaskVO(task, decision, context);
	}
}
