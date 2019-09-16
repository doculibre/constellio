package com.constellio.app.modules.tasks.model.wrappers;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Task extends RecordWrapper {
	public static final String SCHEMA_TYPE = "userTask";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String TYPE = "type";
	public static final String ASSIGNEE = "assignee";
	public static final String ASSIGNER = "assigner";
	public static final String ASSIGNEE_USERS_CANDIDATES = "assigneeUsersCandidates";
	public static final String ASSIGNEE_GROUPS_CANDIDATES = "assigneeGroupsCandidates";
	public static final String TASK_COLLABORATORS = "taskCollaborators";
	public static final String TASK_COLLABORATORS_WRITE_AUTHORIZATIONS = "taskCollaboratorsWriteAuthorizations";
	public static final String ASSIGNED_ON = "assignedOn";
	public static final String FOLLOWERS_IDS = "taskFollowersIds";
	public static final String TASK_FOLLOWERS = "taskFollowers";
	public static final String DESCRIPTION = "description";
	public static final String CONTENTS = "contents";
	public static final String NEXT_REMINDER_ON = "nextReminderOn";
	public static final String REMINDER_FREQUENCY = "reminderFrequency";
	public static final String LAST_REMINDER = "lastReminder";
	public static final String ESCALATION_ASSIGNEE = "escalationAssignee";
	public static final String NUMBER_OF_REMINDERS = "numberOfReminders";
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
	public static final String LINKED_FOLDERS = "linkedFolders";
	public static final String LINKED_DOCUMENTS = "linkedDocuments";
	public static final String LINKED_CONTAINERS = "linkedContainers";
	public static final String REASON = "reason";
	public static final String STARRED_BY_USERS = "starredByUsers";
	public static final String READ_BY_USER = "readByUser";
	public static final String IS_LATE = "isLate";
	public static final String WORK_HOURS = "workHours";
	public static final String ESTIMATED_HOURS = "estimatedHours";

	/**
	 * Fields used by second and third version of the workflow feature
	 */
	public static final String IS_MODEL = "isModel";
	public static final String MODEL_TASK = "modelTask";
	public static final String DECISION = "decision";
	public static final String QUESTION = "question";

	public static final String RELATIVE_DUE_DATE = "relativeDueDate";

	/**
	 * Fields used by the second version of workflow features
	 */

	public static final String BETA_WORKFLOW = "workflow";
	public static final String BETA_WORKFLOW_INSTANCE = "workflowInstance";
	public static final String BETA_NEXT_TASKS_DECISIONS = "nextTasksDecisions";
	public static final String BETA_NEXT_TASKS = "nextTasks";
	public static final String BETA_NEXT_TASK_CREATED = "nextTaskCreated";
	public static final String BETA_DEFAULT_NEXT_TASK = "default";

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

	public Task setAssigneeUsersCandidates(List<?> users) {
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


	public List<String> getTaskCollaborators() {
		return Collections.unmodifiableList(this.<String>getList(TASK_COLLABORATORS));
	}

	public List<Boolean> getTaskCollaboratorsWriteAuthorizations() {
		return Collections.unmodifiableList(this.<Boolean>getList(TASK_COLLABORATORS_WRITE_AUTHORIZATIONS));
	}

	public Task addTaskCollaborator(String taskCollaborator, Boolean taskCollaboratorWriteAuthorization) {
		if (getCollaboratorIndex(taskCollaborator, taskCollaboratorWriteAuthorization) == -1) {
			List<String> taskCollaborators = new ArrayList<>(getTaskCollaborators());
			List<Boolean> taskCollaboratorWriteAuthorizations = new ArrayList<>(getTaskCollaboratorsWriteAuthorizations());

			taskCollaborators.add(taskCollaborator);
			taskCollaboratorWriteAuthorizations.add(taskCollaboratorWriteAuthorization);

			set(TASK_COLLABORATORS, taskCollaborator);
			set(TASK_COLLABORATORS_WRITE_AUTHORIZATIONS, taskCollaboratorWriteAuthorization);
		}
		return this;
	}

	public Task removeTaskCollaborator(String taskCollaborator, Boolean taskCollaboratorWriteAuthorization) {
		int index;
		while ((index = getCollaboratorIndex(taskCollaborator, taskCollaboratorWriteAuthorization)) != -1) {
			List<String> taskCollaborators = new ArrayList<>(getTaskCollaborators());
			List<Boolean> taskCollaboratorWriteAuthorizations = new ArrayList<>(getTaskCollaboratorsWriteAuthorizations());

			taskCollaborators.remove(index);
			taskCollaboratorWriteAuthorizations.remove(index);

			set(TASK_COLLABORATORS, taskCollaborator);
			set(TASK_COLLABORATORS_WRITE_AUTHORIZATIONS, taskCollaboratorWriteAuthorization);
		}
		return this;
	}

	private int getCollaboratorIndex(String collaboratorId, Boolean writeAuthorization) {
		List<String> collaborators = getTaskCollaborators();
		List<Boolean> writeAuthorizations = getTaskCollaboratorsWriteAuthorizations();

		for (int i = 0; i < collaborators.size(); i++) {
			if (collaboratorId.equals(collaborators.get(i)) && writeAuthorization.equals(writeAuthorizations.get(i))) {
				return i;
			}
		}

		return -1;
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

	public List<Comment> getComments() {
		return get(COMMENTS);
	}

	public Task setComments(List<Comment> comments) {
		set(COMMENTS, comments);
		return this;
	}

	public String getDecision() {
		return get(DECISION);
	}

	public Task setDecision(String decision) {
		set(DECISION, decision);
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

	public boolean isModel() {
		return getBooleanWithDefaultValue(IS_MODEL, false);
	}

	public Task setModel(boolean isModel) {
		set(IS_MODEL, isModel);
		return this;
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

	public String getReason() {
		return (String) get(REASON);
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

	public String getReminderFrequency() {
		return get(REMINDER_FREQUENCY);
	}

	public Task setReminderFrequency(String reminderFrequency) {
		set(REMINDER_FREQUENCY, reminderFrequency);
		return this;
	}

	public LocalDateTime getLastReminder() {
		return get(LAST_REMINDER);
	}

	public Task setLastReminder(LocalDateTime datetime) {
		set(LAST_REMINDER, datetime);
		return this;
	}

	public int getNumberOfReminders() {
		Double number = get(NUMBER_OF_REMINDERS);
		return number == null ? 0 : number.intValue();
	}

	public Task setNumberOfReminders(int numberOfReminder) {
		set(NUMBER_OF_REMINDERS, numberOfReminder);
		return this;
	}

	public Double getWorkHours() {
		return get(WORK_HOURS);
	}

	public Task setWorkHours(Double workHours) {
		set(WORK_HOURS, workHours);
		return this;
	}


	public Double getEstimatedHours() {
		return get(ESTIMATED_HOURS);
	}

	public Task setEstimatedHours(Double workHours) {
		set(ESTIMATED_HOURS, workHours);
		return this;
	}

	public String getEscalationAssignee() {
		return get(ESCALATION_ASSIGNEE);
	}

	public Task setEscalationAssignee(String userId) {
		set(ESCALATION_ASSIGNEE, userId);
		return this;
	}

	public void addStarredBy(String userId) {
		ArrayList<Object> list = new ArrayList<>(getList(STARRED_BY_USERS));
		if (!list.contains(userId)) {
			list.add(userId);
			set(STARRED_BY_USERS, list);
		}
	}

	public void removeStarredBy(String userId) {
		ArrayList<Object> list = new ArrayList<>(getList(STARRED_BY_USERS));
		if (list.contains(userId)) {
			list.remove(userId);
			if (list.isEmpty()) {
				set(STARRED_BY_USERS, null);
			} else {
				set(STARRED_BY_USERS, list);
			}
		}
	}

	public Task setQuestion(String question) {
		set(QUESTION, question);
		return this;
	}

	public String getQuestion() {
		return get(QUESTION);
	}

	public static boolean isExpressionLanguage(String text) {
		String value = StringUtils.trimToEmpty(text);
		if (StringUtils.startsWith(value, "${") && StringUtils.endsWith(value, "}")) {
			return true;
		}
		return false;
	}

	public Boolean getReadByUser() {
		return get(READ_BY_USER);
	}

	public Task setReadByUser(Boolean readByUser) {
		set(READ_BY_USER, readByUser);
		return this;
	}

	@Override
	public Task setCreatedBy(String createdBy) {
		super.setCreatedBy(createdBy);
		return this;
	}

	@Override
	public Task setCreatedBy(User createdBy) {
		super.setCreatedBy(createdBy);
		return this;
	}

	public boolean isLate() {
		return getBooleanWithDefaultValue(IS_LATE, false);
	}
}
