package com.constellio.app.modules.tasks.extensions;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class TaskSchemasExtensionTestUpdateRemindersStatusAcceptanceTest extends ConstellioTest {
	Users users = new Users();
	RecordServices recordServices;
	private LocalDate now = LocalDate.now();
	private LocalDate oldStartDate;
	private LocalDate newStartDate;
	private LocalDate oldDueDate;
	private LocalDate newDueDate;
	private Task zeTask;
	private TasksSchemasRecordsServices tasksSchemas;
	private TaskReminder reminderWithRelativeToStartDateProcessedWithOldStartDateAndProcessedWithNewStartDate_0;
	private TaskReminder reminderWithRelativeToStartDateProcessedWithOldStartDateAndNotProcessedWithNewStartDate_1;
	private TaskReminder reminderWithRelativeToStartDateNotProcessedWithOldStartDateAndNotProcessedWithNewStartDate_2;
	private TaskReminder reminderWithRelativeToDueDateProcessedWithOldDueDateAndProcessedWithNewDueDate_3;
	private TaskReminder reminderWithRelativeToDueDateProcessedWithOldDueDateAndNotProcessedWithNewDueDate_4;
	private TaskReminder reminderWithRelativeToDueDateNotProcessedWithOldDueDateAndNotProcessedWithNewDueDate_5;
	private TaskReminder processedReminderWithFixedDate_6;
	private TaskReminder notProcessedReminderWithFixedDate_7;

	@Before
	public void setUp()
			throws Exception {
		givenTimeIs(now);
		prepareSystem(withZeCollection().withTasksModule().withAllTest(users));

		recordServices = getModelLayerFactory().newRecordServices();
		tasksSchemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		initTestData();
	}

	private void initTestData()
			throws RecordServicesException {
		oldDueDate = now.minusDays(2);
		oldStartDate = now.minusDays(2);
		newStartDate = newDueDate = now.plusDays(2);
		zeTask = tasksSchemas.newTask();
		reminderWithRelativeToStartDateProcessedWithOldStartDateAndProcessedWithNewStartDate_0 = new TaskReminder()
				.setBeforeRelativeDate(true).setRelativeDateMetadataCode(Task.START_DATE).setNumberOfDaysToRelativeDate(3)
				.setProcessed(true);
		reminderWithRelativeToStartDateProcessedWithOldStartDateAndNotProcessedWithNewStartDate_1 = new TaskReminder()
				.setBeforeRelativeDate(true).setRelativeDateMetadataCode(Task.START_DATE).setNumberOfDaysToRelativeDate(1)
				.setProcessed(true);
		reminderWithRelativeToStartDateNotProcessedWithOldStartDateAndNotProcessedWithNewStartDate_2 = new TaskReminder()
				.setBeforeRelativeDate(false).setRelativeDateMetadataCode(Task.START_DATE).setNumberOfDaysToRelativeDate(3)
				.setProcessed(false);
		reminderWithRelativeToDueDateProcessedWithOldDueDateAndProcessedWithNewDueDate_3 = new TaskReminder()
				.setBeforeRelativeDate(true).setRelativeDateMetadataCode(Task.DUE_DATE).setNumberOfDaysToRelativeDate(3)
				.setProcessed(true);
		reminderWithRelativeToDueDateProcessedWithOldDueDateAndNotProcessedWithNewDueDate_4 = new TaskReminder()
				.setBeforeRelativeDate(true).setRelativeDateMetadataCode(Task.DUE_DATE).setNumberOfDaysToRelativeDate(1)
				.setProcessed(true);
		reminderWithRelativeToDueDateNotProcessedWithOldDueDateAndNotProcessedWithNewDueDate_5 = new TaskReminder()
				.setBeforeRelativeDate(false).setRelativeDateMetadataCode(Task.DUE_DATE).setNumberOfDaysToRelativeDate(3)
				.setProcessed(false);

		processedReminderWithFixedDate_6 = new TaskReminder().setFixedDate(now).setProcessed(true);
		//voulu:
		notProcessedReminderWithFixedDate_7 = new TaskReminder().setFixedDate(now.minusDays(1)).setProcessed(false);

		recordServices.add(zeTask.setTitle("zeTaskTilte").setStartDate(oldStartDate).setDueDate(oldDueDate)
				.setReminders(asList(reminderWithRelativeToStartDateProcessedWithOldStartDateAndProcessedWithNewStartDate_0,
						reminderWithRelativeToStartDateProcessedWithOldStartDateAndNotProcessedWithNewStartDate_1,
						reminderWithRelativeToStartDateNotProcessedWithOldStartDateAndNotProcessedWithNewStartDate_2,
						reminderWithRelativeToDueDateProcessedWithOldDueDateAndProcessedWithNewDueDate_3,
						reminderWithRelativeToDueDateProcessedWithOldDueDateAndNotProcessedWithNewDueDate_4,
						reminderWithRelativeToDueDateNotProcessedWithOldDueDateAndNotProcessedWithNewDueDate_5,
						processedReminderWithFixedDate_6,
						notProcessedReminderWithFixedDate_7
				)));
	}

	@Test
	public void whenStartDateSetToNewStartDateThenRemindersSetCorrectly()
			throws RecordServicesException {
		recordServices.add(zeTask.setStartDate(newStartDate));
		zeTask = reloadTask(zeTask.getId());
		reloadRemindersFromTask(zeTask);

		assertThat(reminderWithRelativeToStartDateProcessedWithOldStartDateAndProcessedWithNewStartDate_0.isProcessed()).isTrue();
		assertThat(reminderWithRelativeToStartDateProcessedWithOldStartDateAndNotProcessedWithNewStartDate_1.isProcessed())
				.isFalse();
		assertThat(reminderWithRelativeToStartDateNotProcessedWithOldStartDateAndNotProcessedWithNewStartDate_2.isProcessed())
				.isFalse();
		assertThat(reminderWithRelativeToDueDateProcessedWithOldDueDateAndProcessedWithNewDueDate_3.isProcessed()).isTrue();
		assertThat(reminderWithRelativeToDueDateProcessedWithOldDueDateAndNotProcessedWithNewDueDate_4.isProcessed())
				.isTrue();
		assertThat(reminderWithRelativeToDueDateNotProcessedWithOldDueDateAndNotProcessedWithNewDueDate_5.isProcessed())
				.isFalse();
		assertThat(processedReminderWithFixedDate_6.isProcessed()).isTrue();
		assertThat(notProcessedReminderWithFixedDate_7.isProcessed())
				.isFalse();
	}

	@Test
	public void whenDueDateSetToNewDueDateThenRemindersSetCorrectly()
			throws Exception {
		recordServices.add(zeTask.setDueDate(newDueDate));
		zeTask = reloadTask(zeTask.getId());
		reloadRemindersFromTask(zeTask);
		assertThat(reminderWithRelativeToStartDateProcessedWithOldStartDateAndProcessedWithNewStartDate_0.isProcessed()).isTrue();
		assertThat(reminderWithRelativeToStartDateProcessedWithOldStartDateAndNotProcessedWithNewStartDate_1.isProcessed())
				.isTrue();
		assertThat(reminderWithRelativeToStartDateNotProcessedWithOldStartDateAndNotProcessedWithNewStartDate_2.isProcessed())
				.isFalse();
		assertThat(reminderWithRelativeToDueDateProcessedWithOldDueDateAndProcessedWithNewDueDate_3.isProcessed()).isTrue();
		assertThat(reminderWithRelativeToDueDateProcessedWithOldDueDateAndNotProcessedWithNewDueDate_4.isProcessed())
				.isFalse();
		assertThat(reminderWithRelativeToDueDateNotProcessedWithOldDueDateAndNotProcessedWithNewDueDate_5.isProcessed())
				.isFalse();
		assertThat(processedReminderWithFixedDate_6.isProcessed()).isTrue();
		assertThat(notProcessedReminderWithFixedDate_7.isProcessed())
				.isFalse();
	}

	private void reloadRemindersFromTask(Task zeTask) {
		List<TaskReminder> reminders = zeTask.getReminders();
		reminderWithRelativeToStartDateProcessedWithOldStartDateAndProcessedWithNewStartDate_0 = reminders.get(0);
		reminderWithRelativeToStartDateProcessedWithOldStartDateAndNotProcessedWithNewStartDate_1 = reminders.get(1);
		reminderWithRelativeToStartDateNotProcessedWithOldStartDateAndNotProcessedWithNewStartDate_2 = reminders.get(2);
		reminderWithRelativeToDueDateProcessedWithOldDueDateAndProcessedWithNewDueDate_3 = reminders.get(3);
		reminderWithRelativeToDueDateProcessedWithOldDueDateAndNotProcessedWithNewDueDate_4 = reminders.get(4);
		reminderWithRelativeToDueDateNotProcessedWithOldDueDateAndNotProcessedWithNewDueDate_5 = reminders.get(5);
		processedReminderWithFixedDate_6 = reminders.get(6);
		notProcessedReminderWithFixedDate_7 = reminders.get(7);
	}

	private Task reloadTask(String id) {
		return tasksSchemas.wrapTask(recordServices.getDocumentById(id));
	}
}
