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
package com.constellio.app.modules.tasks.ui.components.fields.list;

import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.app.modules.tasks.ui.components.converters.TaskReminderVOToStringConverter;
import com.constellio.app.modules.tasks.ui.components.fields.TaskReminderFieldImpl;
import com.constellio.app.modules.tasks.ui.entities.TaskReminderVO;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;

@SuppressWarnings("unchecked")
public class ListAddRemoveTaskReminderField extends ListAddRemoveField<TaskReminderVO, TaskReminderFieldImpl> {

	private TaskReminderVOToStringConverter converter = new TaskReminderVOToStringConverter();

	@Override
	protected TaskReminderFieldImpl newAddEditField() {
		return new TaskReminderFieldImpl();
	}

	//FIXME should be always Vo or not
	@Override
	protected String getItemCaption(Object itemId) {
		if (itemId instanceof TaskReminderVO) {
			return converter.convertToPresentation((TaskReminderVO) itemId, String.class, getLocale());
		} else {
			return converter.convertToPresentation(toTaskReminderVO((TaskReminder) itemId), String.class, getLocale());
		}
	}

	private TaskReminderVO toTaskReminderVO(TaskReminder taskReminder) {
		return new TaskReminderVO(taskReminder.getFixedDate(), taskReminder.getNumberOfDaysToRelativeDate(),
				taskReminder.getRelativeDateMetadataCode(), taskReminder.isBeforeRelativeDate());
	}

}
