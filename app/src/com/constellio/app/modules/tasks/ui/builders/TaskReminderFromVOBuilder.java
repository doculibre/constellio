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
package com.constellio.app.modules.tasks.ui.builders;

import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.app.modules.tasks.ui.entities.TaskReminderVO;

public class TaskReminderFromVOBuilder {
	public TaskReminder build(TaskReminderVO reminderVO) {
		return new TaskReminder().setFixedDate(reminderVO.getFixedDate())
				.setRelativeDateMetadataCode(reminderVO.getRelativeDateMetadataCode())
				.setBeforeRelativeDate(reminderVO.getBeforeRelativeDate())
				.setNumberOfDaysToRelativeDate(reminderVO.getNumberOfDaysToRelativeDate());
	}
}
