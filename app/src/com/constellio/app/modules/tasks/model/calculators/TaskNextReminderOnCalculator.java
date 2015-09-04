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

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.model.entities.schemas.MetadataValueType;

import org.joda.time.LocalDate;

import java.util.List;

import static java.util.Arrays.asList;

public class TaskNextReminderOnCalculator implements MetadataValueCalculator<LocalDate> {
	LocalDependency<List<TaskReminder>> remindersLocalDependency = LocalDependency.toAStructure(Task.REMINDERS)
			.whichIsMultivalue().whichIsRequired();
	LocalDependency<LocalDate> startDateLocalDependency = LocalDependency.toADate(Task.START_DATE);
	LocalDependency<LocalDate> endDateLocalDependency = LocalDependency.toADate(Task.DUE_DATE);

	@Override
	public LocalDate calculate(CalculatorParameters parameters) {
		LocalDate oldestReminderDate = null;
		List<TaskReminder> taskReminderList = parameters.get(remindersLocalDependency);
		LocalDate startDate = parameters.get(startDateLocalDependency);
		LocalDate endDate = parameters.get(endDateLocalDependency);

		if (taskReminderList != null) {
			for (TaskReminder currentTaskReminder : taskReminderList) {
				LocalDate currentReminderDate = computeReminders(currentTaskReminder, startDate, endDate);
				if (oldestReminderDate == null) {
					oldestReminderDate = currentReminderDate;
				} else if (currentReminderDate != null && currentReminderDate.isBefore(oldestReminderDate)) {
					oldestReminderDate = currentReminderDate;
				}
			}
		}

		return oldestReminderDate;
	}

	private LocalDate computeReminders(TaskReminder taskReminder, LocalDate startDate, LocalDate endDate) {
		if (taskReminder.isProcessed()) {
			return null;
		}
		return taskReminder.computeDate(startDate, endDate);
	}

	@Override
	public LocalDate getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.DATE;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(remindersLocalDependency, startDateLocalDependency, endDateLocalDependency);
	}

}
