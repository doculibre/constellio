package com.constellio.app.modules.tasks.model.wrappers.structures;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class TaskReminderTest extends ConstellioTest {
	@Mock
	Task zeTask;
	private LocalDateTime startDate = LocalDateTime.now();

	@Before
	public void setUp()
			throws Exception {
		givenTimeIs(startDate);
		when(zeTask.get(Task.START_DATE)).thenReturn(startDate);
	}

	@Test
	public void givenReminderWithFixedDateWhenComputeDateThenReturnFixedDate()
			throws Exception {
		LocalDate startDatePlus1 = startDate.toLocalDate().plusDays(1);
		TaskReminder taskReminder = new TaskReminder().setFixedDate(startDatePlus1);
		assertThat(taskReminder.computeDate(zeTask)).isEqualTo(startDatePlus1);
	}

	@Test
	public void givenReminderWithRelativeDate2DaysBeforeStartDateWhenComputeDateThenReturn2DaysBeforeStartDate()
			throws Exception {
		TaskReminder taskReminder = new TaskReminder().setBeforeRelativeDate(true).setNumberOfDaysToRelativeDate(2)
				.setRelativeDateMetadataCode(Task.START_DATE);
		assertThat(taskReminder.computeDate(zeTask)).isEqualTo(startDate.toLocalDate().minusDays(2));
	}

	@Test
	public void givenReminderWithRelativeDate0DaysBeforeStartDateWhenComputeDateThenReturnStartDate()
			throws Exception {
		TaskReminder taskReminder = new TaskReminder().setBeforeRelativeDate(true).setNumberOfDaysToRelativeDate(0)
				.setRelativeDateMetadataCode(Task.START_DATE);
		assertThat(taskReminder.computeDate(zeTask)).isEqualTo(startDate.toLocalDate());
	}

	@Test
	public void givenReminderWithRelativeDate2DaysAfterStartDateWhenComputeDateThenReturn2AfterBeforeStartDate()
			throws Exception {
		TaskReminder taskReminder = new TaskReminder().setBeforeRelativeDate(false).setNumberOfDaysToRelativeDate(2)
				.setRelativeDateMetadataCode(Task.START_DATE);
		assertThat(taskReminder.computeDate(zeTask)).isEqualTo(startDate.toLocalDate().plusDays(2));
	}

	@Test
	public void givenReminderWithRelativeDate0DaysAfterStartDateWhenComputeDateThenReturnStartDate()
			throws Exception {
		TaskReminder taskReminder = new TaskReminder().setBeforeRelativeDate(false).setNumberOfDaysToRelativeDate(0)
				.setRelativeDateMetadataCode(Task.START_DATE);
		assertThat(taskReminder.computeDate(zeTask)).isEqualTo(startDate.toLocalDate());
	}

	@Test
	public void givenRelativeDateSetToTaskStartDateLocaleCodeWhenIsRelativeToStartDateThenReturnTrue()
			throws Exception {
		TaskReminder taskReminder = new TaskReminder().setRelativeDateMetadataCode(Task.START_DATE);
		assertThat(taskReminder.isRelativeToStartDate()).isTrue();
		assertThat(taskReminder.isRelativeToDueDate()).isFalse();
	}

	@Test
	public void givenRelativeDateSetToTaskStartDateCodeWhenIsRelativeToStartDateThenReturnTrue()
			throws Exception {
		TaskReminder taskReminder = new TaskReminder().setRelativeDateMetadataCode(Task.DEFAULT_SCHEMA + "_" + Task.START_DATE);
		assertThat(taskReminder.isRelativeToStartDate()).isTrue();
		assertThat(taskReminder.isRelativeToDueDate()).isFalse();
	}

	@Test
	public void givenRelativeDateSetToNullWhenIsRelativeToStartDateThenReturnFalse()
			throws Exception {
		TaskReminder taskReminder = new TaskReminder();
		assertThat(taskReminder.isRelativeToStartDate()).isFalse();
		assertThat(taskReminder.isRelativeToDueDate()).isFalse();
	}

	@Test
	public void givenRelativeDateSetToTaskEndDateLocaleCodeWhenIsRelativeToEndDateThenReturnTrue()
			throws Exception {
		TaskReminder taskReminder = new TaskReminder().setRelativeDateMetadataCode(Task.DUE_DATE);
		assertThat(taskReminder.isRelativeToDueDate()).isTrue();
		assertThat(taskReminder.isRelativeToStartDate()).isFalse();
	}

	@Test
	public void givenRelativeDateSetToTaskEndDateCodeWhenIsRelativeToEndDateThenReturnTrue()
			throws Exception {
		TaskReminder taskReminder = new TaskReminder().setRelativeDateMetadataCode(Task.DEFAULT_SCHEMA + "_" + Task.DUE_DATE);
		assertThat(taskReminder.isRelativeToDueDate()).isTrue();
		assertThat(taskReminder.isRelativeToStartDate()).isFalse();
	}

	@Test
	public void givenRelativeDateSetToNullWhenIsRelativeToEndDateThenReturnFalse()
			throws Exception {
		TaskReminder taskReminder = new TaskReminder();
		assertThat(taskReminder.isRelativeToDueDate()).isFalse();
		assertThat(taskReminder.isRelativeToStartDate()).isFalse();
	}

}
