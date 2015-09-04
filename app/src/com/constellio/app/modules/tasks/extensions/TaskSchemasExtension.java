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
package com.constellio.app.modules.tasks.extensions;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.TaskStatusType;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInCreationEvent;
import com.constellio.model.extensions.events.records.RecordInModificationEvent;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.app.modules.tasks.TasksEmailTemplates.*;
import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.STANDBY;

public class TaskSchemasExtension extends RecordExtension {
	private final TasksSchemasRecordsServices tasksSchema;
	String collection;

	ModelLayerFactory modelLayerFactory;
	RecordServices recordServices;

	public TaskSchemasExtension(String collection, AppLayerFactory appLayerFactory) {
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.collection = collection;
		tasksSchema = new TasksSchemasRecordsServices(collection, appLayerFactory);
		recordServices = this.modelLayerFactory.newRecordServices();
	}

	@Override
	public void recordLogicallyDeleted(RecordLogicalDeletionEvent event) {
		if (event.getRecord().getSchemaCode().startsWith(Task.SCHEMA_TYPE)) {
			Task task = tasksSchema.wrapTask(event.getRecord());
			sendDeletionEventToFollowers(task);
		}
	}

	@Override
	public void recordInModification(RecordInModificationEvent event) {
		if (event.getRecord().getSchemaCode().startsWith(Task.SCHEMA_TYPE)) {
			Task task = tasksSchema.wrapTask(event.getRecord());
			taskInModification(task, event);
		}
	}

	@Override
	public void recordInCreation(RecordInCreationEvent event) {
		if (event.getRecord().getSchemaCode().startsWith(Task.SCHEMA_TYPE)) {
			Task task = tasksSchema.wrapTask(event.getRecord());
			taskInCreation(task, event);
		}
	}

	private void taskInCreation(Task task, RecordInCreationEvent event) {
		TaskStatus currentStatus = (task.getStatus() == null) ? null : tasksSchema.getTaskStatus(task.getStatus());

		updateEndDateAndStartDateIfNecessary(task, currentStatus);
	}

	void sendDeletionEventToFollowers(Task task) {
		Set<String> followersIds = getTaskDeletionFollowers(task);
		if (followersIds.isEmpty()) {
			return;
		}
		saveEmailToSend(prepareEmailToSend(task, followersIds, TASK_DELETED));
	}

	private void saveEmailToSend(EmailToSend emailToSend) {
		Transaction transaction = new Transaction();
		transaction.setRecordFlushing(RecordsFlushing.LATER());
		transaction.add(emailToSend);
		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

	}

	private EmailToSend prepareEmailToSend(Task task, Set<String> followersIds, String templateId) {
		EmailToSend emailToSend = tasksSchema.newEmailToSend();
		List<EmailAddress> followersEmails = getEmails(followersIds);
		emailToSend.setTo(followersEmails);
		emailToSend.setSendOn(TimeProvider.getLocalDateTime());
		emailToSend.setTemplate(templateId);

		List<String> parameters = prepareTaskParameters(task);
		emailToSend.setParameters(parameters);
		return emailToSend;
	}

	private List<String> prepareTaskParameters(Task task) {
		List<String> parameters = new ArrayList<>();
		parameters.add(TASK_TITLE_PARAMETER + ":" + task.getTitle());
		return parameters;
	}

	private List<EmailAddress> getEmails(Set<String> usersIds) {
		List<EmailAddress> emailAddressesTo = new ArrayList<>();

		for (String userId : usersIds) {
			User user = tasksSchema.wrapUser(recordServices.getDocumentById(userId));
			EmailAddress toAddress = new EmailAddress(user.getTitle(), user.getEmail());
			emailAddressesTo.add(toAddress);
		}
		return emailAddressesTo;
	}

	void taskInModification(Task task, RecordInModificationEvent event) {
		if (event.hasModifiedMetadata(Task.STATUS)) {
			TaskStatus currentStatus = (task.getStatus() == null) ? null : tasksSchema.getTaskStatus(task.getStatus());

			updateEndDateAndStartDateIfNecessary(task, currentStatus);
			if (currentStatus != null && currentStatus.isFinished()) {
				sendTaskCompletedEmail(task, event);
			} else {
				sendStatusModificationToFollowers(task, event);
			}
		}
		//FIXME Francis plusieurs courriels envoyés si plusieurs sous taches modifiees
		if (event.hasModifiedMetadata(Task.PARENT_TASK)) {
			String parentId = task.getParentTask();
			if (StringUtils.isNotBlank(parentId)) {
				Task parent = tasksSchema.getTask(parentId);
				sendSubTasksModification(parent);
			}
			String previousParent = event.getPreviousValue(Task.PARENT_TASK);
			if (previousParent != null) {
				sendSubTasksModification(tasksSchema.getTask(previousParent));
			}
		}
		if (event.hasModifiedMetadata(Task.ASSIGNEE)) {
			sendAssigneeModificationEvent(task, event);
		}
		boolean startDateModified = event.hasModifiedMetadata(Task.START_DATE);
		boolean endDateModified = event.hasModifiedMetadata(Task.DUE_DATE);
		if (startDateModified || endDateModified) {
			updateRemindersStatus(task, event, startDateModified, endDateModified);
		}
	}

	private void updateEndDateAndStartDateIfNecessary(Task task, TaskStatus currentStatus) {
		if (currentStatus == null) {
			task.setEndDate(null);
			task.setStartDate(null);
		} else {
			switch (currentStatus.getStatusType()) {
			case STANDBY:
				task.setEndDate(null);
				task.setStartDate(null);
				break;
			case IN_PROGRESS:
				updateStartDateIfNotNull(task);
				task.setEndDate(null);
				break;
			case FINISHED:
			case CLOSED:
				updateStartDateIfNotNull(task);
				updateEndDateIfNotNull(task);
				break;
			}
		}
	}

	private void updateStartDateIfNotNull(Task task) {
		if (task.getStartDate() == null) {
			task.setStartDate(TimeProvider.getLocalDate());
		}
	}

	private void updateEndDateIfNotNull(Task task) {
		if (task.getEndDate() == null) {
			task.setEndDate(TimeProvider.getLocalDate());
		}
	}

	private List<TaskReminder> updateRemindersStatus(Task task, RecordInModificationEvent event, boolean startDateModified,
			boolean endDateModified) {
		List<TaskReminder> reminders = task.getReminders();
		if (reminders == null || reminders.isEmpty()) {
			return reminders;
		}
		for (TaskReminder taskReminder : reminders) {
			if (taskReminder.getFixedDate() == null && taskReminder.isProcessed()) {
				if ((startDateModified && taskReminder.isRelativeToStartDate())) {
					LocalDate newRelativeDate = taskReminder.computeDate(task);
					if (newRelativeDate.isAfter(task.getStartDate())) {
						taskReminder.setProcessed(false);
					}
				} else if ((endDateModified && taskReminder.isRelativeToDueDate())) {
					LocalDate newRelativeDate = taskReminder.computeDate(task);
					if (newRelativeDate.isAfter(task.getDueDate())) {
						taskReminder.setProcessed(false);
					}
				}
			}
		}
		return reminders;
	}

	private void sendAssigneeModificationEvent(Task task, RecordInModificationEvent event) {
		Set<String> followersIds = getTaskAssigneeModificationFollowers(task);
		if (followersIds.isEmpty()) {
			return;
		}
		EmailToSend emailToSend = prepareEmailToSend(task, followersIds, TASK_ASSIGNEE_MODIFIED);
		List<String> parameters = new ArrayList<>(emailToSend.getParameters());
		parameters.add(PREVIOUS_ASSIGNEE + ":" + getAssigneeUserName((String) event.getPreviousValue(Task.ASSIGNEE)));
		parameters.add(ACTUAL_ASSIGNEE + ":" + getAssigneeUserName(task.getAssignee()));
		emailToSend.setParameters(parameters);
		saveEmailToSend(emailToSend);
	}

	private String getAssigneeUserName(String userId) {
		if (StringUtils.isBlank(userId)) {
			return "";
		}
		return tasksSchema.wrapUser(recordServices.getDocumentById(userId)).getUsername();
	}

	private void sendSubTasksModification(Task task) {
		Set<String> followersIds = getTaskSubTasksModificationFollowers(task);
		if (followersIds.isEmpty()) {
			return;
		}
		saveEmailToSend(prepareEmailToSend(task, followersIds, TASK_SUB_TASKS_MODIFIED));
	}

	private void sendStatusModificationToFollowers(Task task, RecordInModificationEvent event) {
		Set<String> followersIds = getTaskStatusModificationFollowers(task);
		if (followersIds.isEmpty()) {
			return;
		}
		EmailToSend emailToSend = prepareEmailToSend(task, followersIds, TASK_STATUS_MODIFIED);
		List<String> parameters = new ArrayList<>(emailToSend.getParameters());
		parameters.add(PREVIOUS_STATUS + ":" + getStatusLabel((String) event.getPreviousValue(Task.STATUS)));
		parameters.add(ACTUAL_STATUS + ":" + getStatusLabel(task.getStatus()));
		emailToSend.setParameters(parameters);
		saveEmailToSend(emailToSend);
	}

	private String getStatusLabel(String statusId) {
		if (StringUtils.isBlank(statusId)) {
			return "";
		}
		return tasksSchema.getTaskStatus(statusId).getTitle();
	}

	private void sendTaskCompletedEmail(Task task, RecordInModificationEvent event) {
		Set<String> followersIds = getTaskCompetedFollowers(task);
		if (followersIds.isEmpty()) {
			return;
		}
		saveEmailToSend(prepareEmailToSend(task, followersIds, TASK_COMPLETED));
	}

	private Set<String> getTaskAssigneeModificationFollowers(Task task) {
		Set<String> followersIds = new HashSet<>();
		List<TaskFollower> taskFollowers = task.getTaskFollowers();
		if (taskFollowers != null) {
			for (TaskFollower taskFollower : taskFollowers) {
				if (taskFollower.getFollowTaskAssigneeModified()) {
					followersIds.add(taskFollower.getFollowerId());
				}
			}
		}
		return followersIds;
	}

	private Set<String> getTaskSubTasksModificationFollowers(Task task) {
		Set<String> followersIds = new HashSet<>();
		List<TaskFollower> taskFollowers = task.getTaskFollowers();
		if (taskFollowers != null) {
			for (TaskFollower taskFollower : taskFollowers) {
				if (taskFollower.getFollowSubTasksModified()) {
					followersIds.add(taskFollower.getFollowerId());
				}
			}
		}
		return followersIds;
	}

	private Set<String> getTaskStatusModificationFollowers(Task task) {
		Set<String> followersIds = new HashSet<>();
		List<TaskFollower> taskFollowers = task.getTaskFollowers();
		if (taskFollowers != null) {
			for (TaskFollower taskFollower : taskFollowers) {
				if (taskFollower.getFollowTaskStatusModified()) {
					followersIds.add(taskFollower.getFollowerId());
				}
			}
		}
		return followersIds;
	}

	private Set<String> getTaskCompetedFollowers(Task task) {
		Set<String> followersIds = new HashSet<>();
		List<TaskFollower> taskFollowers = task.getTaskFollowers();
		if (taskFollowers != null) {
			for (TaskFollower taskFollower : taskFollowers) {
				if (taskFollower.getFollowTaskCompleted()) {
					followersIds.add(taskFollower.getFollowerId());
				}
			}
		}
		return followersIds;
	}

	private Set<String> getTaskDeletionFollowers(Task task) {
		List<TaskFollower> taskFollowers = task.getTaskFollowers();
		Set<String> followersIds = new HashSet<>();
		if (taskFollowers != null) {
			for (TaskFollower taskFollower : taskFollowers) {
				if (taskFollower.getFollowTaskDeleted()) {
					followersIds.add(taskFollower.getFollowerId());
				}
			}
		}
		return followersIds;
	}

}
