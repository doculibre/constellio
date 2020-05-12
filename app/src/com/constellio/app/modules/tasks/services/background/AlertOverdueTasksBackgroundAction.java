package com.constellio.app.modules.tasks.services.background;

import com.constellio.app.modules.tasks.model.managers.TaskReminderEmailManager;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class AlertOverdueTasksBackgroundAction implements Runnable {
	public final static String PARAMETER_SEPARATOR = ";";

	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private SearchServices searchServices;
	private RecordServices recordServices;
	private CollectionsListManager collectionsListManager;
	private String collection;

	public AlertOverdueTasksBackgroundAction(AppLayerFactory appLayerFactory, String collection) {
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
		this.appLayerFactory = appLayerFactory;
		this.collection = collection;
	}

	@Override
	public synchronized void run() {
		TasksSchemasRecordsServices tasksSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		LogicalSearchQuery query = new LogicalSearchQuery().setCondition(from(tasksSchemas.userTask.schemaType())
				.where(tasksSchemas.userTask.dueDate()).isLessThan(getCurrentDate())
				.andWhere(tasksSchemas.userTask.status()).isNotIn(tasksSchemas.getFinishedOrClosedStatuses())
				.andWhere(tasksSchemas.userTask.reminderFrequency()).isNotNull()
				.andWhere(Schemas.COLLECTION).isEqualTo(collection));

		SearchResponseIterator<Record> overdueTaskIterator = searchServices.recordsIterator(query, 1000);
		while (overdueTaskIterator.hasNext()) {
			Task task = tasksSchemas.wrapTask(overdueTaskIterator.next());
			LocalDate dueDate = task.getDueDate();
			String reminderFrequency = task.getReminderFrequency();
			LocalDateTime lastReminder = task.getLastReminder();
			LocalDateTime reminderDate = addFrequencyToDueDate(dueDate.toLocalDateTime(LocalTime.MIDNIGHT), reminderFrequency, lastReminder);
			if (reminderDate.isBefore(getCurrentDateTime())) {
				String userIdToSendEmailTo = task.getAssignee();
				int numberOfRemindersAlreadySent = task.getNumberOfReminders();

				if (numberOfRemindersAlreadySent != -1) {
					if (isLimitAttained(reminderFrequency, numberOfRemindersAlreadySent)) {
						userIdToSendEmailTo = task.getEscalationAssignee();
						numberOfRemindersAlreadySent = -1;
					} else {
						numberOfRemindersAlreadySent++;
					}
					if (userIdToSendEmailTo != null) {
						sendEmail(task, userIdToSendEmailTo);
						try {
							recordServices.update(task.setLastReminder(getCurrentDateTime()).setNumberOfReminders(numberOfRemindersAlreadySent));
						} catch (RecordServicesException e) {
							e.printStackTrace();
						}

					} else {
						try {
							recordServices.update(task.setLastReminder(getCurrentDateTime()).setNumberOfReminders(numberOfRemindersAlreadySent));
						} catch (RecordServicesException e) {
							e.printStackTrace();
						}
					}

				}
			}
		}
	}

	protected void sendEmail(Task task, String userId) {
		TaskReminderEmailManager taskReminderEmailManager = new TaskReminderEmailManager(appLayerFactory, collection);
		User userToSendMessageTo = new SchemasRecordsServices(collection, modelLayerFactory).getUser(userId);
		String email = userToSendMessageTo.getEmail();
		if (!StringUtils.isBlank(email)) {
			EmailAddress emailAddress = new EmailAddress(userToSendMessageTo.getTitle(), email);
			EmailToSend emailToSend = taskReminderEmailManager.createEmailToSend(task, asList(emailAddress));
			try {
				recordServices.add(emailToSend);
			} catch (RecordServicesException e) {
				e.printStackTrace();
			}
		}
	}

	private LocalDateTime addFrequencyToDueDate(LocalDateTime dueDate, String reminderFrequency,
												LocalDateTime lastReminder) {
		String[] parameters = reminderFrequency.split(PARAMETER_SEPARATOR);
		LocalDateTime returnedValue = null;
		LocalDateTime reminderDate = lastReminder == null ? dueDate : lastReminder;
		if (parameters.length >= 2) {
			String reminderFrequencyType = parameters[0];
			String reminderFrequencyValue = parameters[1];

			switch (reminderFrequencyType) {
				case "mm":
					returnedValue = reminderDate.plusMinutes(Integer.parseInt(reminderFrequencyValue));
					break;
				case "hh":
					returnedValue = reminderDate.plusHours(Integer.parseInt(reminderFrequencyValue));
					break;
				case "DD":
					returnedValue = reminderDate.plusDays(Integer.parseInt(reminderFrequencyValue));
					break;
			}
		}
		return returnedValue;
	}

	public boolean isLimitAttained(String reminderFrequency, int numberOfReminders) {
		boolean isLimitAttained = false;
		if (numberOfReminders == -1) {
			isLimitAttained = true;
		} else {
			String[] parameters = reminderFrequency.split(PARAMETER_SEPARATOR);
			if (parameters.length == 4) {
				String reminderDurationType = parameters[2];
				String reminderDurationValue = parameters[3];

				switch (reminderDurationType) {
					case "Times":
						isLimitAttained = numberOfReminders >= Integer.parseInt(reminderDurationValue);
						break;
					case "Date":
						isLimitAttained = getCurrentDate().isAfter(LocalDate.parse(reminderDurationValue));
						break;
				}
			}
		}
		return isLimitAttained;
	}

	public LocalDateTime getCurrentDateTime() {
		return LocalDateTime.now();
	}

	public LocalDate getCurrentDate() {
		return LocalDate.now();
	}
}
