package com.constellio.app.modules.tasks.extensions;

import static com.constellio.app.modules.tasks.TasksEmailTemplates.ACTUAL_ASSIGNEE;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.ACTUAL_STATUS;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.COMPLETE_TASK;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.CONSTELLIO_URL;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.DISPLAY_TASK;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.PARENT_TASK_TITLE;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.PREVIOUS_ASSIGNEE;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.PREVIOUS_STATUS;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_ASSIGNED;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_ASSIGNED_BY;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_ASSIGNED_ON;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_ASSIGNED_TO_YOU;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_ASSIGNEE_MODIFIED;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_COMPLETED;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_DELETED;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_DESCRIPTION;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_DUE_DATE;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_STATUS;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_STATUS_MODIFIED;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_SUB_TASKS_MODIFIED;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_TITLE_PARAMETER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.app.modules.tasks.navigation.TasksNavigationConfiguration;
import com.constellio.model.entities.records.Record;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeSaveEvent;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchGroup;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser;

public class TaskRecordExtension extends RecordExtension {
	private static final Logger LOGGER = LogManager.getLogger(TaskRecordExtension.class);
	private final TasksSchemasRecordsServices tasksSchema;
	String collection;

	ModelLayerFactory modelLayerFactory;
	RecordServices recordServices;
	UserServices userServices;
	ConstellioEIMConfigs eimConfigs;

	public TaskRecordExtension(String collection, AppLayerFactory appLayerFactory) {
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.collection = collection;
		tasksSchema = new TasksSchemasRecordsServices(collection, appLayerFactory);
		recordServices = this.modelLayerFactory.newRecordServices();
		userServices = appLayerFactory.getModelLayerFactory().newUserServices();
		eimConfigs = new ConstellioEIMConfigs(appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager());
	}

	@Override
	public void recordLogicallyDeleted(RecordLogicalDeletionEvent event) {
		if (event.getRecord().getSchemaCode().startsWith(Task.SCHEMA_TYPE)) {
			Task task = tasksSchema.wrapTask(event.getRecord());
			sendDeletionEventToFollowers(task);
		}
	}

	@Override
	public void recordInModificationBeforeValidationAndAutomaticValuesCalculation(
			RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent event) {
		if (event.getRecord().getSchemaCode().startsWith(Task.SCHEMA_TYPE)) {
			Task task = tasksSchema.wrapTask(event.getRecord());
			taskInModification(task, event);
		}
	}

	@Override
	public void recordInCreationBeforeValidationAndAutomaticValuesCalculation(
			RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent event) {
		if (event.getRecord().getSchemaCode().startsWith(Task.SCHEMA_TYPE)) {
			Task task = tasksSchema.wrapTask(event.getRecord());
			taskInCreation(task, event);
		}
	}

	private void taskInCreation(Task task, RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent event) {
		sendEmailToAssignee(task);
		TaskStatus currentStatus = (task.getStatus() == null) ? null : tasksSchema.getTaskStatus(task.getStatus());
		updateEndDateAndStartDateIfNecessary(task, currentStatus);
	}

	void sendDeletionEventToFollowers(Task task) {
		Set<String> followersIds = getTaskDeletionFollowers(task);
		if (followersIds.isEmpty()) {
			return;
		}
		EmailToSend emailToSend = prepareEmailToSend(task, followersIds, TASK_DELETED);
		saveEmailToSend(emailToSend, task);
	}

	private void saveEmailToSend(EmailToSend emailToSend, Task task) {

		prepareTaskParameters(emailToSend, task);

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
		EmailToSend emailToSend = tasksSchema.newEmailToSend().setTryingCount(0d);
		List<EmailAddress> followersEmails = getEmails(followersIds);
		emailToSend.setTo(followersEmails);
		emailToSend.setSendOn(TimeProvider.getLocalDateTime());
		emailToSend.setTemplate(templateId);
		return emailToSend;
	}

	private void prepareTaskParameters(EmailToSend emailToSend, Task task) {
		List<String> newParameters = new ArrayList<>();
		List<String> parameters = emailToSend.getParameters();
		newParameters.addAll(parameters);

		String parentTaskTitle = "";
		String assignerFullName = getUserFullNameById(task.getAssigner());
		String assigneeFullName = getUserFullNameById(task.getAssignee());
		if (task.getParentTask() != null) {
			Task parentTask = tasksSchema.getTask(task.getParentTask());
			parentTaskTitle = parentTask.getTitle();
		}
		String status = tasksSchema.getTaskStatus(task.getStatus()).getTitle();

		newParameters.add(TASK_TITLE_PARAMETER + ":" + formatToParameter(task.getTitle()));
		newParameters.add(PARENT_TASK_TITLE + ":" + formatToParameter(parentTaskTitle));
		newParameters.add(TASK_ASSIGNED_BY + ":" + formatToParameter(assignerFullName));
		newParameters.add(TASK_ASSIGNED_ON + ":" + formatToParameter(task.getAssignedOn()));
		newParameters.add(TASK_ASSIGNED + ":" + formatToParameter(assigneeFullName));
		newParameters.add(TASK_DUE_DATE + ":" + formatToParameter(task.getDueDate()));
		newParameters.add(TASK_STATUS + ":" + formatToParameter(status));
		newParameters.add(TASK_DESCRIPTION + ":" + formatToParameter(task.getDescription()));
		String constellioURL = eimConfigs.getConstellioUrl();
		newParameters
				.add(DISPLAY_TASK + ":" + constellioURL + "#!" + TasksNavigationConfiguration.DISPLAY_TASK + "/" + task.getId());
		newParameters.add(COMPLETE_TASK + ":" + constellioURL + "#!" + TasksNavigationConfiguration.EDIT_TASK
				+ "/completeTask%253Dtrue%253Bid%253D" + task.getId());

        newParameters.add(CONSTELLIO_URL + ":" + constellioURL);

		emailToSend.setParameters(newParameters);
	}

	private Object formatToParameter(Object parameter) {
		if(parameter == null) {
			return "";
		}
		return parameter;
	}

	private List<EmailAddress> getEmails(Set<String> usersIds) {
		List<EmailAddress> emailAddressesTo = new ArrayList<>();

		for (String userId : usersIds) {
			User user = tasksSchema.wrapUser(recordServices.getDocumentById(userId));

            emailAddressesTo.addAll(buildEmailAddressList(user.getTitle(), user.getEmail(), user.getPersonalEmails()));
		}

		return emailAddressesTo;
	}

	void taskInModification(Task task, RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent event) {
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
				sendSubTasksModification(parent, task);
			}
			String previousParent = event.getPreviousValue(Task.PARENT_TASK);
			if (previousParent != null) {
				sendSubTasksModification(tasksSchema.getTask(previousParent), task);
			}
		}
		if (event.hasModifiedMetadata(Task.ASSIGNEE)) {
			sendAssigneeModificationEvent(task, event);
		} else if (event.hasModifiedMetadata(Task.ASSIGNEE_GROUPS_CANDIDATES) || event
				.hasModifiedMetadata(Task.ASSIGNEE_USERS_CANDIDATES)) {
			sendEmailToAssignee(task);
		}
		boolean startDateModified = event.hasModifiedMetadata(Task.START_DATE);
		boolean dueDateModified = event.hasModifiedMetadata(Task.DUE_DATE);
		if (startDateModified || dueDateModified) {
			updateRemindersStatus(task, startDateModified, dueDateModified);
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

	List<TaskReminder> updateRemindersStatus(Task task, boolean startDateModified,
			boolean dueDate) {
		List<TaskReminder> reminders = task.getReminders();
		if (reminders == null || reminders.isEmpty()) {
			return reminders;
		}
		for (TaskReminder taskReminder : reminders) {
			if (taskReminder.getFixedDate() == null && taskReminder.isProcessed()) {
				if (startDateModified && taskReminder.isRelativeToStartDate()) {
					LocalDate newRelativeDate = taskReminder.computeDate(task);
					if (newRelativeDate.isAfter(TimeProvider.getLocalDate())) {
						taskReminder.setProcessed(false);
					}
				} else if (dueDate && taskReminder.isRelativeToDueDate()) {
					LocalDate newRelativeDate = taskReminder.computeDate(task);
					if (newRelativeDate.isAfter(TimeProvider.getLocalDate())) {
						taskReminder.setProcessed(false);
					}
				}
			}
		}
		return reminders;
	}

	private void sendAssigneeModificationEvent(Task task,
			RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent event) {
		sendEmailToAssignee(task);
		Set<String> followersIds = getTaskAssigneeModificationFollowers(task);
		if (followersIds.isEmpty()) {
			return;
		}
		EmailToSend emailToSend = prepareEmailToSend(task, followersIds, TASK_ASSIGNEE_MODIFIED);
		List<String> parameters = new ArrayList<>(emailToSend.getParameters());
		parameters.add(PREVIOUS_ASSIGNEE + ":" + getUserNameById((String) event.getPreviousValue(Task.ASSIGNEE)));
		parameters.add(ACTUAL_ASSIGNEE + ":" + getUserNameById(task.getAssignee()));
		emailToSend.setParameters(parameters);
		saveEmailToSend(emailToSend, task);
	}

	private void sendEmailToAssignee(Task task) {
		Set<EmailAddress> assigneeEmails = getTaskAssigneesEmails(task);
		if (!assigneeEmails.isEmpty()) {
			EmailToSend emailToSend = tasksSchema.newEmailToSend().setTryingCount(0d);
			emailToSend.setTo(new ArrayList<>(assigneeEmails));
			emailToSend.setSendOn(TimeProvider.getLocalDateTime());
			emailToSend.setTemplate(TASK_ASSIGNED_TO_YOU);

			List<String> parameters = new ArrayList<>(emailToSend.getParameters());
			emailToSend.setParameters(parameters);
			saveEmailToSend(emailToSend, task);
		}
	}

	private Set<EmailAddress> getTaskAssigneesEmails(Task task) {
		Set<EmailAddress> assigneeEmails = new HashSet<>();

		if (task.getAssignee() != null) {
            assigneeEmails.addAll(emailAddress(task.getAssignee()));
		}

		if (task.getAssigneeUsersCandidates() != null) {
			for (String userId : task.getAssigneeUsersCandidates()) {
                assigneeEmails.addAll(emailAddress(userId));
			}
		}

		if (task.getAssigneeGroupsCandidates() != null) {
			for (String groupId : task.getAssigneeGroupsCandidates()) {
				UserServices userServices = modelLayerFactory.newUserServices();
				try {
					Group group = tasksSchema.getGroup(groupId);
					List<UserCredential> groupUsers = userServices.getGlobalGroupActifUsers(group.getCode());
					for (UserCredential user : groupUsers) {
                        assigneeEmails.addAll(buildEmailAddressList(user.getTitle(), user.getEmail(), user.getPersonalEmails()));
					}
				} catch (UserServicesRuntimeException_NoSuchGroup e) {
					LOGGER.warn("Group assigned in task " + task.getTitle() + " does not exist " + groupId);
				}
			}
		}

		return assigneeEmails;
	}

	private List<EmailAddress> emailAddress(String userId) {
		List<EmailAddress> emailAddressList = new ArrayList<>();

		try {
			final User user = tasksSchema.getUser(userId);

            emailAddressList = buildEmailAddressList(user.getTitle(), user.getEmail(), user.getPersonalEmails());
		} catch (UserServicesRuntimeException_NoSuchUser e) {
			LOGGER.warn("User assignee does not exists " + userId);
		}

		return emailAddressList;
	}

    private List<EmailAddress> buildEmailAddressList(final String title, final String principalEmail, final List<String> personalEmails) {
        final List<EmailAddress> emailAddressList = new ArrayList<>();

        if (StringUtils.isNotBlank(principalEmail)) {
            emailAddressList.add(new EmailAddress(title, principalEmail));
        }

        if (!CollectionUtils.isEmpty(personalEmails)) {
            for (final String personalEmail : personalEmails) {
                emailAddressList.add(new EmailAddress(title, personalEmail));
            }
        }

        return emailAddressList;
    }

	private String getUserNameById(String userId) {
		if (StringUtils.isBlank(userId)) {
			return "";
		}
		return tasksSchema.wrapUser(recordServices.getDocumentById(userId)).getUsername();
	}

	private String getUserFullNameById(String userId) {
		if (StringUtils.isBlank(userId)) {
			return "";
		}
		return tasksSchema.wrapUser(recordServices.getDocumentById(userId)).getFirstName() + " " +
				tasksSchema.wrapUser(recordServices.getDocumentById(userId)).getLastName();
	}

	private void sendSubTasksModification(Task parentTask, Task task) {
		Set<String> followersIds = getTaskSubTasksModificationFollowers(parentTask);
		if (followersIds.isEmpty()) {
			return;
		}
		EmailToSend emailToSend = prepareEmailToSend(task, followersIds, TASK_SUB_TASKS_MODIFIED);
		saveEmailToSend(emailToSend, task);
	}

	private void sendStatusModificationToFollowers(Task task,
			RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent event) {
		Set<String> followersIds = getTaskStatusModificationFollowers(task);
		if (followersIds.isEmpty()) {
			return;
		}
		EmailToSend emailToSend = prepareEmailToSend(task, followersIds, TASK_STATUS_MODIFIED);
		List<String> parameters = new ArrayList<>(emailToSend.getParameters());
		parameters.add(PREVIOUS_STATUS + ":" + getStatusLabel((String) event.getPreviousValue(Task.STATUS)));
		parameters.add(ACTUAL_STATUS + ":" + getStatusLabel(task.getStatus()));
		emailToSend.setParameters(parameters);
		saveEmailToSend(emailToSend, task);
	}

	private String getStatusLabel(String statusId) {
		if (StringUtils.isBlank(statusId)) {
			return "";
		}
		return tasksSchema.getTaskStatus(statusId).getTitle();
	}

	private void sendTaskCompletedEmail(Task task, RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent event) {
		Set<String> followersIds = getTaskCompetedFollowers(task);
		if (followersIds.isEmpty()) {
			return;
		}
		EmailToSend emailToSend = prepareEmailToSend(task, followersIds, TASK_COMPLETED);
		saveEmailToSend(emailToSend, task);
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

	@Override
	public void recordInCreationBeforeSave(final RecordInCreationBeforeSaveEvent event) {
        final Record record = event.getRecord();

        if (record.getSchemaCode().startsWith(Task.SCHEMA_TYPE)) {
            final Task task = tasksSchema.wrapTask(record);

            //
            addAssignerAsCompletionEventFollower(task);
        }
    }

    private void addAssignerAsCompletionEventFollower(final Task task) {
        final List<TaskFollower> currentFollowersList = task.getTaskFollowers();

        if (!(task.isModel() || task.getAssigner() == null)) {
            final TaskFollower assignerAsCompletionEventFollower = new TaskFollower().setFollowerId(task.getAssigner()).setFollowTaskCompleted(true).setDirty(true);

            if (!currentFollowersList.contains(assignerAsCompletionEventFollower)) {
                final List<TaskFollower> newFollowersList = new ArrayList<>(task.getTaskFollowers());
                newFollowersList.add(assignerAsCompletionEventFollower);
                task.setTaskFollowers(Collections.unmodifiableList(newFollowersList));
            }
        }
    }
}
