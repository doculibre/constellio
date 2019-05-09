package com.constellio.app.modules.tasks.model.wrappers;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.structures.MapStringStringStructure;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BetaWorkflowTask extends Task {
	public BetaWorkflowTask(Record record,
							MetadataSchemaTypes types) {
		super(record, types);
	}

	public BetaWorkflowTask(Task task) {
		super(task.getWrappedRecord(), task.getMetadataSchemaTypes());
	}

	public String getNextTask(String decision) {
		if (decision == null) {
			return hasNextTask() ? getSingleNextTask() : null;
		} else {
			return getNextTasksDecisions().get(decision);
		}
	}

	public List<String> getNextTasks() {
		return getList(BETA_NEXT_TASKS);
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
		return get(BETA_NEXT_TASKS_DECISIONS);
	}

	public BetaWorkflowTask addNextTaskDecision(String decision, String reference) {
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

	public BetaWorkflowTask setNextTasksDecisions(MapStringStringStructure decisions) {
		set(BETA_NEXT_TASKS_DECISIONS, decisions);
		return this;
	}

	public BetaWorkflowTask setNextTask(String nextTask) {
		Map<String, String> nextTaskDecisions = new HashMap<>();
		nextTaskDecisions.put(BETA_DEFAULT_NEXT_TASK, nextTask);
		return setNextTasksDecisions(nextTaskDecisions);
	}

	public BetaWorkflowTask setNextTasksDecisions(Map<String, String> decisions) {
		set(BETA_NEXT_TASKS_DECISIONS, new MapStringStringStructure(decisions));
		return this;
	}

	public String getWorkflow() {
		return get(BETA_WORKFLOW);
	}

	public BetaWorkflowTask setWorkflow(String workflow) {
		set(BETA_WORKFLOW, workflow);
		return this;
	}

	public BetaWorkflowTask setWorkflow(Record workflow) {
		set(BETA_WORKFLOW, workflow);
		return this;
	}

	public BetaWorkflowTask setWorkflow(BetaWorkflow workflow) {
		set(BETA_WORKFLOW, workflow);
		return this;
	}

	public String getWorkflowInstance() {
		return get(BETA_WORKFLOW_INSTANCE);
	}

	public BetaWorkflowTask setWorkflowInstance(String workflowInstanceId) {
		set(BETA_WORKFLOW_INSTANCE, workflowInstanceId);
		return this;
	}

	public BetaWorkflowTask setWorkflowInstance(Record workflowInstance) {
		set(BETA_WORKFLOW_INSTANCE, workflowInstance);
		return this;
	}

	public BetaWorkflowTask setWorkflowInstance(BetaWorkflowInstance workflowInstance) {
		set(BETA_WORKFLOW_INSTANCE, workflowInstance);
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

	public boolean isNextTaskCreated() {
		return getBooleanWithDefaultValue(BETA_NEXT_TASK_CREATED, false);
	}

	public BetaWorkflowTask setNextTaskCreated(boolean nextTaskCreated) {
		set(BETA_NEXT_TASK_CREATED, nextTaskCreated);
		return this;
	}

	@Override
	public BetaWorkflowTask setType(String type) {
		return (BetaWorkflowTask) super.setType(type);
	}

	@Override
	public BetaWorkflowTask setType(TaskType type) {
		return (BetaWorkflowTask) super.setType(type);
	}

	@Override
	public BetaWorkflowTask setAssignee(String assignedTo) {
		return (BetaWorkflowTask) super.setAssignee(assignedTo);
	}

	@Override
	public BetaWorkflowTask setAssigner(String assignedBy) {
		return (BetaWorkflowTask) super.setAssigner(assignedBy);
	}

	@Override
	public BetaWorkflowTask setAssigneeGroupsCandidates(List<String> groups) {
		return (BetaWorkflowTask) super.setAssigneeGroupsCandidates(groups);
	}

	@Override
	public BetaWorkflowTask setAssigneeUsersCandidates(List<?> users) {
		return (BetaWorkflowTask) super.setAssigneeUsersCandidates(users);
	}

	@Override
	public BetaWorkflowTask setAssignedOn(LocalDate assignedOn) {
		return (BetaWorkflowTask) super.setAssignedOn(assignedOn);
	}

	@Override
	public BetaWorkflowTask setTaskFollowers(List<TaskFollower> taskFollowers) {
		return (BetaWorkflowTask) super.setTaskFollowers(taskFollowers);
	}

	@Override
	public BetaWorkflowTask setRelativeDueDate(Integer relativeDueDate) {
		return (BetaWorkflowTask) super.setRelativeDueDate(relativeDueDate);
	}

	@Override
	public BetaWorkflowTask setComments(List<Comment> comments) {
		return (BetaWorkflowTask) super.setComments(comments);
	}

	@Override
	public BetaWorkflowTask setDecision(String decision) {
		return (BetaWorkflowTask) super.setDecision(decision);
	}

	@Override
	public BetaWorkflowTask setModelTask(String modelTaskId) {
		return (BetaWorkflowTask) super.setModelTask(modelTaskId);
	}

	@Override
	public BetaWorkflowTask setModelTask(Record modelTask) {
		return (BetaWorkflowTask) super.setModelTask(modelTask);
	}

	@Override
	public BetaWorkflowTask setModelTask(Task modelTask) {
		return (BetaWorkflowTask) super.setModelTask(modelTask);
	}

	@Override
	public BetaWorkflowTask setModel(boolean isModel) {
		return (BetaWorkflowTask) super.setModel(isModel);
	}

	@Override
	public BetaWorkflowTask setDescription(String description) {
		return (BetaWorkflowTask) super.setDescription(description);
	}

	@Override
	public BetaWorkflowTask setContent(List<Content> contents) {
		return (BetaWorkflowTask) super.setContent(contents);
	}

	@Override
	public BetaWorkflowTask setReminders(List<TaskReminder> reminders) {
		return (BetaWorkflowTask) super.setReminders(reminders);
	}

	@Override
	public BetaWorkflowTask setStartDate(LocalDate startDate) {
		return (BetaWorkflowTask) super.setStartDate(startDate);
	}

	@Override
	public BetaWorkflowTask setDueDate(LocalDate dueDate) {
		return (BetaWorkflowTask) super.setDueDate(dueDate);
	}

	@Override
	public BetaWorkflowTask setEndDate(LocalDate endDate) {
		return (BetaWorkflowTask) super.setEndDate(endDate);
	}

	@Override
	public BetaWorkflowTask setStatus(String status) {
		return (BetaWorkflowTask) super.setStatus(status);
	}

	@Override
	public BetaWorkflowTask setProgressPercentage(Double progress) {
		return (BetaWorkflowTask) super.setProgressPercentage(progress);
	}

	@Override
	public BetaWorkflowTask setParentTask(Task task) {
		return (BetaWorkflowTask) super.setParentTask(task);
	}

	@Override
	public BetaWorkflowTask setParentTask(Record task) {
		return (BetaWorkflowTask) super.setParentTask(task);
	}

	@Override
	public BetaWorkflowTask setAssignationDate(LocalDate date) {
		return (BetaWorkflowTask) super.setAssignationDate(date);
	}

	@Override
	public BetaWorkflowTask setTitle(String title) {
		return (BetaWorkflowTask) super.setTitle(title);
	}

	@Override
	public BetaWorkflowTask setLinkedFolders(List<?> folderIds) {
		return (BetaWorkflowTask) super.setLinkedFolders(folderIds);
	}

	@Override
	public BetaWorkflowTask setLinkedContainers(List<?> containerIds) {
		return (BetaWorkflowTask) super.setLinkedContainers(containerIds);
	}

	@Override
	public BetaWorkflowTask setLinkedDocuments(List<?> documentIds) {
		return (BetaWorkflowTask) super.setLinkedDocuments(documentIds);
	}

}
