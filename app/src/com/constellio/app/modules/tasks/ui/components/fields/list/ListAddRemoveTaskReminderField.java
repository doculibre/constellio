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
