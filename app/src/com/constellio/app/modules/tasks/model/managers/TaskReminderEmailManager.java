package com.constellio.app.modules.tasks.model.managers;

import static com.constellio.app.modules.tasks.TasksEmailTemplates.COMPLETE_TASK;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.DISPLAY_TASK;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.PARENT_TASK_TITLE;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_ASSIGNED;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_ASSIGNED_BY;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_ASSIGNED_ON;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_DESCRIPTION;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_DUE_DATE;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_STATUS;
import static com.constellio.app.modules.tasks.TasksEmailTemplates.TASK_TITLE_PARAMETER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.app.modules.tasks.navigation.TasksNavigationConfiguration;

import org.apache.commons.lang.StringUtils;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.tasks.TasksEmailTemplates;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.threads.BackgroundThreadConfiguration;
import com.constellio.data.threads.BackgroundThreadExceptionHandling;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class TaskReminderEmailManager implements StatefulService {
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskReminderEmailManager.class);
	static int RECORDS_BATCH = 1000;
	private static final long TWENTY_SECONDS = 60 * 1000l;
	private static final Duration DURATION_BETWEEN_EXECUTION = new Duration(TWENTY_SECONDS);
	public static final String ID = "taskReminderEmailManager";
	private final BackgroundThreadsManager backgroundThreadsManager;
	private final TasksSchemasRecordsServices taskSchemas;
	private final AppLayerFactory appLayerFactory;
	private final RecordServices recordServices;
	SearchServices searchServices;
	ConstellioEIMConfigs eimConfigs;

	public TaskReminderEmailManager(AppLayerFactory appLayerFactory, String collection) {
		this.appLayerFactory = appLayerFactory;
		this.backgroundThreadsManager = appLayerFactory.getModelLayerFactory().getDataLayerFactory()
				.getBackgroundThreadsManager();
		taskSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		eimConfigs = new ConstellioEIMConfigs(appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager());
	}

	@Override
	public void initialize() {
		configureBackgroundThread();
	}

	void configureBackgroundThread() {

		Runnable sendEmailsAction = new Runnable() {
			@Override
			public void run() {
				generateReminderEmails();
			}
		};

		backgroundThreadsManager.configure(BackgroundThreadConfiguration
				.repeatingAction("EmailQueueManager", sendEmailsAction)
				.handlingExceptionWith(BackgroundThreadExceptionHandling.CONTINUE)
				.executedEvery(DURATION_BETWEEN_EXECUTION));
	}

	void generateReminderEmails() {

		LogicalSearchQuery query = new LogicalSearchQuery(
				from(taskSchemas.userTask.schema()).where(taskSchemas.userTask.nextReminderOn())
						.isLessOrEqualThan(TimeProvider.getLocalDate()));
		do {
			query.setNumberOfRows(RECORDS_BATCH);
			Transaction transaction = new Transaction();
			List<Task> readyToSendTasks = taskSchemas.searchTasks(query);
			for (Task task : readyToSendTasks) {
				generateReminderEmail(task, transaction);
			}
			try {
				recordServices.execute(transaction);
			} catch (RecordServicesException e) {
				LOGGER.warn("Batch not processed", e);
			}
		} while (searchServices.hasResults(query));
	}

	private void generateReminderEmail(Task task, Transaction transaction) {
		List<String> assigneeCandidates = getAssigneeCandidates(task);
		List<EmailAddress> validEmailAddresses = getValidEmailAddresses(assigneeCandidates);
		if (!validEmailAddresses.isEmpty()) {
			EmailToSend emailToSend = createEmailToSend(task, validEmailAddresses);
			transaction.add(emailToSend);
		} else {
			LOGGER.warn("Task reminder not sent because no assignee candidate with valid email " + task.getTitle());
		}
		task = updateTaskReminders(task);
		transaction.add(task);
	}

	private Task updateTaskReminders(Task task) {
		List<TaskReminder> newReminders = new ArrayList<>();
		for (TaskReminder taskReminder : task.getReminders()) {
			if (taskReminder.computeDate(task).isBefore(TimeProvider.getLocalDate()) || taskReminder.computeDate(task)
					.isEqual(TimeProvider.getLocalDate())) {
				taskReminder.setProcessed(true);
			}
			newReminders.add(taskReminder);
		}
		return task.setReminders(newReminders);
	}

	private EmailToSend createEmailToSend(Task task, List<EmailAddress> validEmailAddresses) {
		List<String> parameters = new ArrayList<>();
		EmailToSend emailToSend = taskSchemas.newEmailToSend().setTryingCount(0d)
				.setTemplate(TasksEmailTemplates.TASK_REMINDER)
				.setTo(validEmailAddresses)
				.setParameters(parameters)
				.setSendOn(TimeProvider.getLocalDateTime());
		prepareTaskParameters(emailToSend, task);
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
			Task parentTask = taskSchemas.getTask(task.getParentTask());
			parentTaskTitle = parentTask.getTitle();
		}
		String status = taskSchemas.getTaskStatus(task.getStatus()).getTitle();

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

		emailToSend.setParameters(newParameters);
	}

	private Object formatToParameter(Object parameter) {
		if(parameter == null) {
			return "";
		}
		return parameter;
	}

	private String getUserNameById(String userId) {
		if (org.apache.commons.lang3.StringUtils.isBlank(userId)) {
			return "";
		}
		return taskSchemas.wrapUser(recordServices.getDocumentById(userId)).getUsername();
	}

	private String getUserFullNameById(String userId) {
		if (org.apache.commons.lang3.StringUtils.isBlank(userId)) {
			return "";
		}
		return taskSchemas.wrapUser(recordServices.getDocumentById(userId)).getFirstName() + " " +
				taskSchemas.wrapUser(recordServices.getDocumentById(userId)).getLastName();
	}

	private List<EmailAddress> getValidEmailAddresses(List<String> usersIds) {
		List<EmailAddress> returnList = new ArrayList<>();
		LogicalSearchCondition condition = from(taskSchemas.userSchema()).where(Schemas.IDENTIFIER).isIn(usersIds);
		Metadata userEmailMetadata = taskSchemas.userSchema().get(User.EMAIL);
		List<Record> usersFromGroups = searchServices.search(new LogicalSearchQuery(condition).
				setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(asList(Schemas.TITLE, userEmailMetadata))));
		for (Record userRecord : usersFromGroups) {
			String userEmail = userRecord.get(userEmailMetadata);
			String userTitle = userRecord.get(Schemas.TITLE);
			if (!StringUtils.isBlank(userEmail)) {
				returnList.add(new EmailAddress(userTitle, userEmail));
			} else {
				LOGGER.warn("User with blank email " + userTitle);
			}
		}
		return returnList;
	}

	private List<String> getAssigneeCandidates(Task task) {
		Set<String> returnSet = new HashSet<>();
		String assignee = task.getAssignee();
		if (assignee != null) {
			returnSet.add(assignee);
		}
		List<String> taskAssignationUsersCandidates = task.getAssigneeUsersCandidates();
		if (taskAssignationUsersCandidates != null) {
			returnSet.addAll(taskAssignationUsersCandidates);
		}
		List<String> taskAssigneeGroupsCandidates = task.getAssigneeGroupsCandidates();
		if (taskAssigneeGroupsCandidates != null && !taskAssigneeGroupsCandidates.isEmpty()) {
			Metadata userGroups = taskSchemas.userSchema().getMetadata(User.GROUPS);
			LogicalSearchCondition condition = from(taskSchemas.userSchema()).where(userGroups)
					.isContaining(taskAssigneeGroupsCandidates);
			List<Record> usersFromGroups = searchServices.search(new LogicalSearchQuery(condition)
					.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchema()));
			for (Record userRecord : usersFromGroups) {
				returnSet.add(userRecord.getId());
			}
		}
		return new ArrayList<>(returnSet);
	}

	@Override
	public void close() {

	}
}
