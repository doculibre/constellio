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
