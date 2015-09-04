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
package com.constellio.app.modules.tasks.model.calculators;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.CalculatorParametersValidatingDependencies;
import com.constellio.sdk.tests.ConstellioTest;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class TaskNextReminderOnCalculatorAcceptanceTest extends ConstellioTest {
	@Mock
	CalculatorParameters parameters;
	TaskNextReminderOnCalculator calculator;
	List<TaskReminder> reminders;
	private LocalDate now = LocalDate.now();
	LocalDate startDate = now;
	LocalDate endDate = now.plusDays(3);

	@Before
	public void setUp()
			throws Exception {
		calculator = new TaskNextReminderOnCalculator();
		givenTimeIs(now);
	}

	@Test
	public void givenNullParametersOrEmptyRemindersWhenCalculatingThenReturnNull()
			throws Exception {
		startDate = null;
		endDate = now;
		reminders = new ArrayList<>();
		assertThat(calculate()).isNull();

		startDate = now;
		endDate = null;
		reminders = new ArrayList<>();
		assertThat(calculate()).isNull();

		startDate = now;
		endDate = now;
		reminders = null;
		assertThat(calculate()).isNull();

		startDate = now;
		endDate = now;
		reminders = new ArrayList<>();
		assertThat(calculate()).isNull();
	}

	@Test
	public void givenProcessedReminderWhenCalculatingThenReturnNull()
			throws Exception {
		reminders = new ArrayList<>();
		TaskReminder fixedDateReminder = new TaskReminder().setFixedDate(now.plusDays(1))
				.setProcessed(true);
		reminders.add(fixedDateReminder);

		assertThat(calculate()).isNull();
	}

	@Test
	public void givenFixedDateReminderWhenCalculatingThenReturnFixedDate()
			throws Exception {
		reminders = new ArrayList<>();
		TaskReminder fixedDateReminder = new TaskReminder().setFixedDate(now.plusDays(1));
		reminders.add(fixedDateReminder);

		assertThat(calculate()).isEqualTo(fixedDateReminder.getFixedDate());
	}

	@Test
	public void givenReminderWithTodayDateAndReminderWithTomorrowDateWhenCalculatingThenReturnTodayDate()
			throws Exception {
		reminders = new ArrayList<>();
		TaskReminder reminderWithTodayDate = new TaskReminder().setFixedDate(now);
		reminders.add(reminderWithTodayDate);
		TaskReminder reminderWithTomorrowDate = new TaskReminder().setFixedDate(now.plusDays(1));
		reminders.add(reminderWithTomorrowDate);

		assertThat(calculate()).isEqualTo(reminderWithTodayDate.getFixedDate());
	}

	@Test
	public void givenProcessedReminderWithTodayDateAndReminderWithTomorrowDateWhenCalculatingThenReturnTomorrowDate()
			throws Exception {
		reminders = new ArrayList<>();
		TaskReminder reminderWithTodayDate = new TaskReminder().setFixedDate(now)
				.setProcessed(true);
		reminders.add(reminderWithTodayDate);
		TaskReminder reminderWithTomorrowDate = new TaskReminder().setFixedDate(now.plusDays(1));
		reminders.add(reminderWithTomorrowDate);

		assertThat(calculate()).isEqualTo(reminderWithTomorrowDate.getFixedDate());
	}

	@Test
	public void givenReminderWithFlexibleDate2DaysAfterEndDateWhenCalculatingThenReturn2DaysAfterEndDate()
			throws Exception {
		reminders = new ArrayList<>();
		TaskReminder taskReminder = new TaskReminder().setBeforeRelativeDate(false).setNumberOfDaysToRelativeDate(2)
				.setRelativeDateMetadataCode(Task.DUE_DATE);
		reminders.add(taskReminder);

		assertThat(calculate()).isEqualTo(endDate.plusDays(2));
	}

	@Test
	public void givenReminderWithFlexibleDate3BeforeStartDateWhenCalculatingThenReturn3DaysBeforeStartDate()
			throws Exception {
		reminders = new ArrayList<>();
		TaskReminder taskReminder = new TaskReminder().setBeforeRelativeDate(true).setNumberOfDaysToRelativeDate(3)
				.setRelativeDateMetadataCode(Task.START_DATE);
		reminders.add(taskReminder);

		assertThat(calculate()).isEqualTo(startDate.minusDays(3));
	}

	private LocalDate calculate() {
		when(parameters.get(calculator.remindersLocalDependency)).thenReturn(reminders);
		when(parameters.get(calculator.startDateLocalDependency)).thenReturn(startDate);
		when(parameters.get(calculator.endDateLocalDependency)).thenReturn(endDate);

		return calculator.calculate(new CalculatorParametersValidatingDependencies(parameters, calculator));
	}
}
