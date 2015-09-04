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
package com.constellio.app.modules.tasks.model.wrappers.structures;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.sdk.tests.ConstellioTest;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class TaskReminderTest extends ConstellioTest {
	@Mock
	Task zeTask;
	private LocalDate startDate = LocalDate.now();

	@Before
	public void setUp()
			throws Exception {
		givenTimeIs(startDate);
		when(zeTask.get(Task.START_DATE)).thenReturn(startDate);
	}

	@Test
	public void givenReminderWithFixedDateWhenComputeDateThenReturnFixedDate()
			throws Exception {
		LocalDate startDatePlus1 = startDate.plusDays(1);
		TaskReminder taskReminder = new TaskReminder().setFixedDate(startDatePlus1);
		assertThat(taskReminder.computeDate(zeTask)).isEqualTo(startDatePlus1);
	}

	@Test
	public void givenReminderWithRelativeDate2DaysBeforeStartDateWhenComputeDateThenReturn2DaysBeforeStartDate()
			throws Exception {
		TaskReminder taskReminder = new TaskReminder().setBeforeRelativeDate(true).setNumberOfDaysToRelativeDate(2)
				.setRelativeDateMetadataCode(Task.START_DATE);
		assertThat(taskReminder.computeDate(zeTask)).isEqualTo(startDate.minusDays(2));
	}

	@Test
	public void givenReminderWithRelativeDate0DaysBeforeStartDateWhenComputeDateThenReturnStartDate()
			throws Exception {
		TaskReminder taskReminder = new TaskReminder().setBeforeRelativeDate(true).setNumberOfDaysToRelativeDate(0)
				.setRelativeDateMetadataCode(Task.START_DATE);
		assertThat(taskReminder.computeDate(zeTask)).isEqualTo(startDate);
	}

	@Test
	public void givenReminderWithRelativeDate2DaysAfterStartDateWhenComputeDateThenReturn2AfterBeforeStartDate()
			throws Exception {
		TaskReminder taskReminder = new TaskReminder().setBeforeRelativeDate(false).setNumberOfDaysToRelativeDate(2)
				.setRelativeDateMetadataCode(Task.START_DATE);
		assertThat(taskReminder.computeDate(zeTask)).isEqualTo(startDate.plusDays(2));
	}

	@Test
	public void givenReminderWithRelativeDate0DaysAfterStartDateWhenComputeDateThenReturnStartDate()
			throws Exception {
		TaskReminder taskReminder = new TaskReminder().setBeforeRelativeDate(false).setNumberOfDaysToRelativeDate(0)
				.setRelativeDateMetadataCode(Task.START_DATE);
		assertThat(taskReminder.computeDate(zeTask)).isEqualTo(startDate);
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
