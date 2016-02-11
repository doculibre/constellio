package com.constellio.app.modules.tasks.services;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.Workflow;
import com.constellio.app.modules.tasks.model.wrappers.WorkflowInstance;
import com.constellio.app.modules.tasks.model.wrappers.WorkflowInstanceStatus;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.app.modules.tasks.services.WorkflowServicesRuntimeException.WorkflowServicesRuntimeException_UnsupportedAddAtPosition;
import com.constellio.app.modules.tasks.services.WorkflowServicesRuntimeException.WorkflowServicesRuntimeException_UnsupportedMove;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.modules.tasks.ui.entities.WorkflowTaskProgressionVO;
import com.constellio.app.modules.tasks.ui.entities.WorkflowTaskVO;
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
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class WorkflowServices {
	String collection;
	SearchServices searchServices;
	RecordServices recordServices;
	AppLayerFactory appLayerFactory;
	TasksSchemasRecordsServices tasks;

	public WorkflowServices(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.tasks = new TasksSchemasRecordsServices(collection, appLayerFactory);
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
	}

	public List<Workflow> getWorkflows() {
		return tasks.searchWorkflows(getWorkflowsQuery());
	}

	public LogicalSearchQuery getWorkflowsQuery() {
		return new LogicalSearchQuery(from(tasks.workflow.schemaType()).returnAll())
				.sortAsc(Schemas.TITLE).filteredByStatus(StatusFilter.ACTIVES);
	}

	public List<WorkflowInstance> getCurrentWorkflowInstances() {
		return tasks.searchWorkflowInstances(getCurrentWorkflowInstancesQuery());
	}

	public LogicalSearchQuery getCurrentWorkflowInstancesQuery() {
		return new LogicalSearchQuery(from(tasks.workflowInstance.schemaType())
				.where(tasks.workflowInstance.status()).isEqualTo(WorkflowInstanceStatus.IN_PROGRESS))
				.sortAsc(Schemas.TITLE);
	}

	public List<WorkflowTaskProgressionVO> getFlattenModelTaskProgressionVOs(
			WorkflowInstance workflowInstance, SessionContext sessionContext) {
		List<WorkflowTaskProgressionVO> taskProgressionVOs = new ArrayList<>();
		Workflow workflow = tasks.getWorkflow(workflowInstance.getWorkflow());

		for (WorkflowTaskVO taskVO : getFlattenModelTaskVOs(workflow, sessionContext)) {
			taskProgressionVOs.add(getTaskProgression(workflowInstance, taskVO));
		}

		return taskProgressionVOs;
	}

	public List<WorkflowTaskVO> getFlattenModelTaskVOs(Workflow workflow, SessionContext sessionContext) {
		List<WorkflowTaskVO> tasks = new ArrayList<>();

		for (WorkflowTaskVO task : getRootModelTaskVOs(workflow, sessionContext)) {
			tasks.addAll(getAllTasksInHierarchy(task, sessionContext));
		}
		return tasks;
	}

	private WorkflowTaskProgressionVO getTaskProgression(WorkflowInstance workflowInstance, WorkflowTaskVO workflowTaskVO) {
		Task instanceTask = tasks.wrapTask(searchServices.searchSingleResult(from(tasks.userTask.schemaType())
				.where(tasks.userTask.workflowInstance()).isEqualTo(workflowInstance)
				.andWhere(tasks.userTask.modelTask()).isEqualTo(workflowTaskVO.getId())
		));

		WorkflowTaskProgressionVO workflowTaskProgressionVO = new WorkflowTaskProgressionVO();
		workflowTaskProgressionVO.setDecision(workflowTaskVO.getDecision());
		workflowTaskProgressionVO.setWorkflowTaskVO(workflowTaskVO);
		workflowTaskProgressionVO.setTitle(workflowTaskVO.getTitle());

		if (instanceTask != null && workflowTaskVO.getDecision() == null) {
			workflowTaskProgressionVO.setDueDate(instanceTask.getDueDate());
			workflowTaskProgressionVO.setStatus(instanceTask.getStatusType());
		}

		return workflowTaskProgressionVO;

	}

	private List<WorkflowTaskVO> getAllTasksInHierarchy(WorkflowTaskVO task, SessionContext sessionContext) {
		List<WorkflowTaskVO> taskVOs = new ArrayList<>();
		taskVOs.add(task);
		for (WorkflowTaskVO child : getChildModelTasks(task, sessionContext)) {
			taskVOs.addAll(getAllTasksInHierarchy(child, sessionContext));
		}
		return taskVOs;
	}

	public List<WorkflowTaskVO> getRootModelTaskVOs(Workflow workflow, SessionContext sessionContext) {
		List<WorkflowTaskVO> workflows = new ArrayList<>();
		for (Task modelTask : getStartModelTask(workflow)) {
			workflows.addAll(getWorkflowTasksStarting(modelTask.getId(), sessionContext));
		}

		return workflows;
	}

	WorkflowTaskVO newWorkflowTaskVO(Task modelTask, SessionContext sessionContext) {
		RecordToVOBuilder recordToVOBuilder = new RecordToVOBuilder();
		TaskVO taskVO = new TaskVO(recordToVOBuilder.build(modelTask.getWrappedRecord(), VIEW_MODE.TABLE, sessionContext));

		WorkflowTaskVO workflowTaskVO = new WorkflowTaskVO();
		workflowTaskVO.setId(modelTask.getId());
		workflowTaskVO.setTaskVO(taskVO);
		workflowTaskVO.setHasChildren(modelTask.hasDecisions());
		workflowTaskVO.setTitle(modelTask.getTitle());

		return workflowTaskVO;
	}

	WorkflowTaskVO newWorkflowTaskVO(Task modelTask, String decision, SessionContext sessionContext) {
		WorkflowTaskVO workflowTaskVO = newWorkflowTaskVO(modelTask, sessionContext);
		workflowTaskVO.setDecision(decision);
		if (decision != null) {
			workflowTaskVO.setTitle(modelTask.getTitle() + " - " + decision);
		}
		return workflowTaskVO;
	}

	private List<WorkflowTaskVO> getWorkflowTasksStarting(String taskId, SessionContext sessionContext) {
		List<WorkflowTaskVO> workflowTaskVOs = new ArrayList<>();
		if (taskId != null && !taskId.equals("NO_VALUE")) {
			Task task = tasks.getTask(taskId);
			workflowTaskVOs.add(newWorkflowTaskVO(task, sessionContext));

			if (!task.hasDecisions() && task.hasNextTask()) {
				workflowTaskVOs.addAll(getWorkflowTasksStarting(task.getSingleNextTask(), sessionContext));
			}

		}
		return workflowTaskVOs;

	}

	public List<WorkflowTaskProgressionVO> getRootModelTaskProgressionsVOs(WorkflowInstance workflowInstance,
			SessionContext sessionContext) {
		Workflow workflow = tasks.getWorkflow(workflowInstance.getWorkflow());
		List<WorkflowTaskProgressionVO> progressionVOs = new ArrayList<>();

		for (WorkflowTaskVO child : getRootModelTaskVOs(workflow, sessionContext)) {
			progressionVOs.add(getTaskProgression(workflowInstance, child));
		}

		return progressionVOs;
	}

	public List<WorkflowTaskProgressionVO> getChildModelTaskProgressions(WorkflowInstance workflowInstance,
			WorkflowTaskVO workflowTaskVO, SessionContext sessionContext) {
		List<WorkflowTaskProgressionVO> progressionVOs = new ArrayList<>();

		for (WorkflowTaskVO child : getChildModelTasks(workflowTaskVO, sessionContext)) {
			progressionVOs.add(getTaskProgression(workflowInstance, child));
		}

		return progressionVOs;
	}

	public List<WorkflowTaskVO> getChildModelTasks(WorkflowTaskVO workflowTaskVO, SessionContext sessionContext) {

		if (workflowTaskVO.getDecision() == null) {
			List<WorkflowTaskVO> workflows = new ArrayList<>();
			if (workflowTaskVO.hasChildren()) {
				Task task = tasks.getTask(workflowTaskVO.getId());
				for (String decision : task.getNextTasksDecisionsCodes()) {
					workflows.add(newWorkflowTaskVO(task, decision, sessionContext));
				}
			}
			return workflows;
		} else {
			Task task = tasks.getTask(workflowTaskVO.getId());
			String taskId = task.getNextTask(workflowTaskVO.getDecision());
			if (taskId == null) {
				return new ArrayList<>();
			} else {
				return getWorkflowTasksStarting(taskId, sessionContext);
			}
		}

	}

	public boolean canAddTaskIn(WorkflowTaskVO selectedTask, SessionContext sessionContext) {
		Task task = tasks.getTask(selectedTask.getId());
		for (WorkflowTaskVO workflowTaskVO : getAvailableWorkflowTaskVOForNewTask(task.getWorkflow(), sessionContext)) {
			if (selectedTask.hasSameIdDecision(workflowTaskVO)) {
				return true;
			}
		}
		return false;
	}

	public boolean canAddDecisionTaskIn(WorkflowTaskVO selectedTask, SessionContext sessionContext) {
		return canAddTaskIn(selectedTask, sessionContext) && getTaskVOAfter(selectedTask, sessionContext) == null;
	}

	public List<WorkflowTaskVO> getAvailableWorkflowTaskVOForNewTask(String workflowId, SessionContext sessionContext) {
		List<WorkflowTaskVO> tasks = new ArrayList<>();

		for (Task task : getWorkflowModelTasks(workflowId)) {
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
	 * @param taskType TODO
	 */
	public Task createModelTaskAfter(Workflow workflow, WorkflowTaskVO selectedTask, String taskType,
			String title, SessionContext sessionContext) {
		return createDecisionModelTaskAfter(workflow, selectedTask, taskType, title, null, sessionContext);
	}

	/**
	 * Create a task with a true/false decision, and create a task for each decisions
	 * @param taskType TODO
	 */
	public Task createDecisionModelTaskAfter(Workflow workflow, WorkflowTaskVO selectedTask, String taskType, String title,
			List<String> decisions, SessionContext sessionContext) {
		Task newTask;
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
				WorkflowTaskVO newTaskVO = newWorkflowTaskVO(newTask, sessionContext);
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
	public void moveAfter(WorkflowTaskVO selectedWorkflowTaskVO, WorkflowTaskVO moveAfterTask, SessionContext sessionContext) {

		WorkflowTaskVO taskBeforeSelected = getTaskVOBefore(selectedWorkflowTaskVO, sessionContext);
		WorkflowTaskVO taskAfterSelected = getTaskVOAfter(selectedWorkflowTaskVO, sessionContext);

		WorkflowTaskVO newTaskBeforeSelected = moveAfterTask;
		WorkflowTaskVO newTaskAfterSelected = getTaskVOAfter(moveAfterTask, sessionContext);

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

	//TODO Thiago test
	public void addAfter(WorkflowTaskVO existingWorkflowTaskVO, WorkflowTaskVO targetTask, SessionContext sessionContext) {

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

	private void replaceRelationShip(Transaction transaction, WorkflowTaskVO node,
			WorkflowTaskVO newNextNode) {

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

		Task task = tasks.getTask(node.getId());
		if (node.getDecision() == null) {
			task.setNextTask(newNextNodeTaskId);

		} else {
			task.addNextTaskDecision(node.getDecision(), newNextNodeTaskId);
		}
		transaction.add(task);
	}

	//TODO Thiago
	private void addRelationShip(Transaction transaction, WorkflowTaskVO selectNode,
			WorkflowTaskVO targetNode) {

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

		Task task = tasks.getTask(newTargetTaskId);

		if (task.getNextTasks().isEmpty()) {
			task.setNextTask(selectNode.getId());
		} else {
			//TODO Thiago
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

	private WorkflowTaskVO getTaskVOAfter(WorkflowTaskVO node, SessionContext sessionContext) {
		if (node == null) {
			throw new IllegalArgumentException("Node must be not null");
		}
		if (node.getDecision() == null && node.hasChildren()) {
			throw new IllegalArgumentException("Node has multiple nodes after");
		}
		Task task = tasks.getTask(node.getId());
		String nextId = task.getNextTask(node.getDecision());

		if (nextId == null || nextId.equals("NO_VALUE")) {
			return null;
		} else {
			Task nextTask = tasks.getTask(nextId);
			return newWorkflowTaskVO(nextTask, sessionContext);
		}
	}

	private WorkflowTaskVO getTaskVOBefore(WorkflowTaskVO node, SessionContext sessionContext) {

		if (node == null) {
			throw new IllegalArgumentException("Node must be not null");
		}

		if (node.getDecision() != null) {
			throw new IllegalArgumentException("Node must not be a decision node");
		}

		Task task = tasks.getTask(node.getId());
		for (Task modelTask : getWorkflowModelTasks(task.getWorkflow())) {
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
	public void delete(WorkflowTaskVO selectedTask, SessionContext sessionContext) {
		Task task = tasks.getTask(selectedTask.getId());
		WorkflowTaskVO taskBeforeVO = getTaskVOBefore(selectedTask, sessionContext);

		if (!selectedTask.hasChildren()) {
			if (taskBeforeVO != null) {
				Task taskBefore = tasks.getTask(taskBeforeVO.getId());

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
				Task taskBefore = tasks.getTask(taskBeforeVO.getId());
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
			for (WorkflowTaskVO taskVO : getAllTasksInHierarchy(selectedTask, sessionContext)) {
				Task taskInHierarchy = tasks.getTask(taskVO.getId());
				recordServices.logicallyDelete(taskInHierarchy.getWrappedRecord(), User.GOD);
			}
		}
	}

	public List<Task> getWorkflowModelTasks(String workflowId) {
		return tasks.searchTasks(getWorkflowModelTasksQuery(workflowId));
	}

	public LogicalSearchQuery getWorkflowModelTasksQuery(String workflowId) {
		return new LogicalSearchQuery(from(tasks.userTask.schemaType())
				.where(tasks.userTask.workflow()).isEqualTo(workflowId)
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull()
				.andWhere(tasks.userTask.isModel()).isTrue()).sortDesc(tasks.userTask.workflowTaskSort());
	}

	public Task getCurrentWorkflowInstanceTask(WorkflowInstance workflowInstance) {
		return tasks.wrapTask(searchServices.searchSingleResult(from(tasks.userTask.schemaType())
				.where(tasks.userTask.workflow()).isEqualTo(workflowInstance.getWorkflow())
				.andWhere(tasks.userTask.status()).isNotIn(tasks.getFinishedOrClosedStatuses())
				.andWhere(tasks.userTask.workflowInstance()).isEqualTo(workflowInstance.getId())));
	}

	public List<Task> getWorkflowInstanceTasks(WorkflowInstance workflowInstance) {
		return tasks.searchTasks(getWorkflowInstanceTasksQuery(workflowInstance.getWorkflow(), workflowInstance.getId()));
	}

	public List<Task> getWorkflowInstanceTasks(String workflowId, String workflowInstanceId) {
		return tasks.searchTasks(getWorkflowInstanceTasksQuery(workflowId, workflowInstanceId));
	}

	public LogicalSearchQuery getWorkflowInstanceTasksQuery(String workflowId, String workflowInstanceId) {
		return new LogicalSearchQuery(from(tasks.userTask.schemaType())
				.where(tasks.userTask.workflow()).isEqualTo(workflowId)
				.andWhere(tasks.userTask.workflowInstance()).isEqualTo(workflowInstanceId));
	}

	public List<Task> getStartModelTask(Workflow workflow) {

		List<Task> modelTasks = getWorkflowModelTasks(workflow.getId());
		Set<String> referencedTasks = new HashSet<>();

		for (Task modelTask : modelTasks) {
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

	public WorkflowInstance start(Workflow workflow, User user, Map<String, List<String>> extraFields) {
		if (user == null) {
			throw new IllegalArgumentException("User is required");
		}
		WorkflowInstance workflowInstance = tasks.newWorkflowInstance();
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

	public Task createInstanceTask(Task modelTask, WorkflowInstance instance) {
		String typeId = modelTask.getType();

		Task instanceTask;
		if (typeId != null) {
			TaskType type = tasks.getTaskType(typeId);
			instanceTask = tasks.newTaskWithType(type);
		} else {
			instanceTask = tasks.newTask();
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

	public void cancel(WorkflowInstance workflowInstance) {
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
}
