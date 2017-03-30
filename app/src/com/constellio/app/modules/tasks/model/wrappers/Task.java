package com.constellio.app.modules.tasks.model.wrappers;

import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.structures.MapStringStringStructure;
import org.joda.time.LocalDate;

import java.util.*;

public class Task extends RecordWrapper {
	public static final String SCHEMA_TYPE = "userTask";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String TYPE = "type";
	public static final String ASSIGNEE = "assignee";
	public static final String ASSIGNER = "assigner";
	public static final String ASSIGNEE_USERS_CANDIDATES = "assigneeUsersCandidates";
	public static final String ASSIGNEE_GROUPS_CANDIDATES = "assigneeGroupsCandidates";
	public static final String ASSIGNED_ON = "assignedOn";
	public static final String FOLLOWERS_IDS = "taskFollowersIds";
	public static final String TASK_FOLLOWERS = "taskFollowers";
	public static final String DESCRIPTION = "description";
	public static final String CONTENTS = "contents";
	public static final String NEXT_REMINDER_ON = "nextReminderOn";
	public static final String REMINDERS = "reminders";
	public static final String START_DATE = "startDate";
	public static final String DUE_DATE = "dueDate";
	public static final String END_DATE = "endDate";
	public static final String STATUS = "status";
	public static final String STATUS_TYPE = "statusType";
	public static final String PROGRESS_PERCENTAGE = "progressPercentage";
	public static final String PARENT_TASK = "parentTask";
	public static final String PARENT_TASK_DUE_DATE = "parentTaskDueDate";
	public static final String COMMENTS = "comments";
	public static final String RELATIVE_DUE_DATE = "relativeDueDate";
	public static final String MODEL_TASK = "modelTask";
	public static final String WORKFLOW = "workflow";
	public static final String WORKFLOW_INSTANCE = "workflowInstance";
	public static final String IS_MODEL = "isModel";
	public static final String WORKFLOW_TASK_SORT = "workflowTaskSort";
	public static final String NEXT_TASKS_DECISIONS = "nextTasksDecisions";
	public static final String NEXT_TASKS = "nextTasks";
	public static final String DECISION = "decision";
	public static final String NEXT_TASK_CREATED = "nextTaskCreated";
	public static final String LINKED_FOLDERS = "linkedFolders";
	public static final String LINKED_DOCUMENTS = "linkedDocuments";
	public static final String LINKED_CONTAINERS = "linkedContainers";

	public static final String DEFAULT_NEXT_TASK = "default";

	public Task(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public String getType() {
		return get(TYPE);
	}

	public Task setType(String type) {
		set(TYPE, type);
		return this;
	}

	public Task setType(TaskType type) {
		set(TYPE, type);
		return this;
	}

	public String getAssignee() {
		return get(ASSIGNEE);
	}

	public String getAssigner() {
		return get(ASSIGNER);
	}

	public Task setAssignee(String assignedTo) {
		set(ASSIGNEE, assignedTo);
		return this;
	}

	public Task setAssigner(String assignedBy) {
		set(ASSIGNER, assignedBy);
		return this;
	}

	public List<String> getAssigneeUsersCandidates() {
		return get(ASSIGNEE_USERS_CANDIDATES);
	}

	public Task setAssigneeUsersCandidates(List<String> users) {
		set(ASSIGNEE_USERS_CANDIDATES, users);
		return this;
	}

	public List<String> getAssigneeGroupsCandidates() {
		return get(ASSIGNEE_GROUPS_CANDIDATES);
	}

	public Task setAssigneeGroupsCandidates(List<String> groups) {
		set(ASSIGNEE_GROUPS_CANDIDATES, groups);
		return this;
	}

	public LocalDate getAssignedOn() {
		return get(ASSIGNED_ON);
	}

	public Task setAssignedOn(LocalDate assignedOn) {
		set(ASSIGNED_ON, assignedOn);
		return this;
	}

	public List<String> getFollowersIds() {
		return get(FOLLOWERS_IDS);
	}

	public List<TaskFollower> getTaskFollowers() {
		return get(TASK_FOLLOWERS);
	}

	public Task setTaskFollowers(List<TaskFollower> taskFollowers) {
		set(TASK_FOLLOWERS, taskFollowers);
		return this;
	}

	public Integer getRelativeDueDate() {
		return getInteger(RELATIVE_DUE_DATE);
	}

	public Task setRelativeDueDate(Integer relativeDueDate) {
		set(RELATIVE_DUE_DATE, relativeDueDate);
		return this;
	}

	public String getComments() {
		return get(COMMENTS);
	}

	public Task setComments(String comments) {
		set(COMMENTS, comments);
		return this;
	}

	public String getNextTask(String decision) {
		if (decision == null) {
			return hasNextTask() ? getSingleNextTask() : null;
		} else {
			return getNextTasksDecisions().get(decision);
		}
	}

	public List<String> getNextTasks() {
		return getList(NEXT_TASKS);
	}

	public List<String> getNextTasksDecisionsCodes() {
		List<String> decisionCodes = new ArrayList<>();
		MapStringStringStructure nextTasks = getNextTasksDecisions();
		if (nextTasks != null) {
			decisionCodes.addAll(nextTasks.keySet());
		}
		Collections.sort(decisionCodes);
		return decisionCodes;
	}

	public boolean hasDecisions() {
		List<String> decisions = getNextTasksDecisionsCodes();
		return decisions.size() > 1;
	}

	public MapStringStringStructure getNextTasksDecisions() {
		return get(NEXT_TASKS_DECISIONS);
	}

	public Task addNextTaskDecision(String decision, String reference) {
		MapStringStringStructure values = getNextTasksDecisions();
		if (values == null) {
			setNextTasksDecisions(values = new MapStringStringStructure());
		}
		//if (reference == null || reference.equals("null")) {
		//	values.remove(decision);
		//} else {
		values.put(decision, reference);
		//}
		return this;
	}

	public Task setNextTasksDecisions(MapStringStringStructure decisions) {
		set(NEXT_TASKS_DECISIONS, decisions);
		return this;
	}

	public Task setNextTask(String nextTask) {
		Map<String, String> nextTaskDecisions = new HashMap<>();
		nextTaskDecisions.put(DEFAULT_NEXT_TASK, nextTask);
		return setNextTasksDecisions(nextTaskDecisions);
	}

	public Task setNextTasksDecisions(Map<String, String> decisions) {
		set(NEXT_TASKS_DECISIONS, new MapStringStringStructure(decisions));
		return this;
	}

	public String getDecision() {
		return get(DECISION);
	}

	public Task setDecision(String decision) {
		set(DECISION, decision);
		return this;
	}

	public String getWorkflow() {
		return get(WORKFLOW);
	}

	public Task setWorkflow(String workflow) {
		set(WORKFLOW, workflow);
		return this;
	}

	public Task setWorkflow(Record workflow) {
		set(WORKFLOW, workflow);
		return this;
	}

	public Task setWorkflow(Workflow workflow) {
		set(WORKFLOW, workflow);
		return this;
	}

	public String getModelTask() {
		return get(MODEL_TASK);
	}

	public Task setModelTask(String modelTaskId) {
		set(MODEL_TASK, modelTaskId);
		return this;
	}

	public Task setModelTask(Record modelTask) {
		set(MODEL_TASK, modelTask);
		return this;
	}

	public Task setModelTask(Task modelTask) {
		set(MODEL_TASK, modelTask);
		return this;
	}

	public String getWorkflowInstance() {
		return get(WORKFLOW_INSTANCE);
	}

	public Task setWorkflowInstance(String workflowInstanceId) {
		set(WORKFLOW_INSTANCE, workflowInstanceId);
		return this;
	}

	public Task setWorkflowInstance(Record workflowInstance) {
		set(WORKFLOW_INSTANCE, workflowInstance);
		return this;
	}

	public Task setWorkflowInstance(WorkflowInstance workflowInstance) {
		set(WORKFLOW_INSTANCE, workflowInstance);
		return this;
	}

	public boolean isModel() {
		return getBooleanWithDefaultValue(IS_MODEL, false);
	}

	public Task setModel(boolean isModel) {
		set(IS_MODEL, isModel);
		return this;
	}

	public int getWorkflowTaskSort() {
		return getInteger(WORKFLOW_TASK_SORT);
	}

	public String getDescription() {
		return get(DESCRIPTION);
	}

	public Task setDescription(String description) {
		set(DESCRIPTION, description);
		return this;
	}

	public List<Content> getContent() {
		return get(CONTENTS);
	}

	public Task setContent(List<Content> contents) {
		set(CONTENTS, contents);
		return this;
	}

	public LocalDate getNextReminderOn() {
		return get(NEXT_REMINDER_ON);
	}

	public List<TaskReminder> getReminders() {
		return get(REMINDERS);
	}

	public Task setReminders(List<TaskReminder> reminders) {
		set(REMINDERS, reminders);
		return this;
	}

	public LocalDate getStartDate() {
		return get(START_DATE);
	}

	public Task setStartDate(LocalDate startDate) {
		set(START_DATE, startDate);
		return this;
	}

	public LocalDate getParentTaskDueDate() {
		return get(PARENT_TASK_DUE_DATE);
	}

	public LocalDate getDueDate() {
		return get(DUE_DATE);
	}

	public LocalDate getEndDate() {
		return get(END_DATE);
	}

	public Task setDueDate(LocalDate dueDate) {
		set(DUE_DATE, dueDate);
		return this;
	}

	public Task setEndDate(LocalDate endDate) {
		set(END_DATE, endDate);
		return this;
	}

	public boolean isNextTaskCreated() {
		return getBooleanWithDefaultValue(NEXT_TASK_CREATED, false);
	}

	public Task setNextTaskCreated(boolean nextTaskCreated) {
		set(NEXT_TASK_CREATED, nextTaskCreated);
		return this;
	}

	public TaskStatusType getStatusType() {
		return get(STATUS_TYPE);
	}

	public String getStatus() {
		return get(STATUS);
	}

	public Task setStatus(String status) {
		set(STATUS, status);
		return this;
	}

	public Double getProgressPercentage() {
		return get(PROGRESS_PERCENTAGE);
	}

	public Task setProgressPercentage(Double progress) {
		set(PROGRESS_PERCENTAGE, progress);
		return this;
	}

	public String getParentTask() {
		return get(PARENT_TASK);
	}

	public Task setParentTask(Task task) {
		set(PARENT_TASK, task);
		return this;
	}

	public Task setParentTask(Record task) {
		set(PARENT_TASK, task);
		return this;
	}

	public Task setParentTask(String task) {
		set(PARENT_TASK, task);
		return this;
	}

	public Task setAssignationDate(LocalDate date) {
		set(ASSIGNED_ON, date);
		return this;
	}

	public Task setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public boolean hasNextTask() {
		return !getNextTasks().isEmpty();
	}

	public String getSingleNextTask() {
		List<String> nextTasks = getNextTasksDecisionsCodes();
		if (nextTasks.size() != 1) {
			throw new RuntimeException("Has no single next task");
		}
		return getNextTasksDecisions().get(nextTasks.get(0));
	}

	public Task setLinkedFolders(List<?> folderIds) {
		set(LINKED_FOLDERS, folderIds);
		return this;
	}

	public Task setLinkedContainers(List<?> containerIds) {
		set(LINKED_CONTAINERS, containerIds);
		return this;
	}

	public Task setLinkedDocuments(List<?> documentIds) {
		set(LINKED_DOCUMENTS, documentIds);
		return this;
	}
}
