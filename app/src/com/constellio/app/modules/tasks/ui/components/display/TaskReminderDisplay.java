package com.constellio.app.modules.tasks.ui.components.display;

import com.constellio.app.modules.tasks.ui.components.converters.TaskReminderVOToStringConverter;
import com.constellio.app.modules.tasks.ui.entities.TaskReminderVO;
import com.vaadin.ui.Label;

public class TaskReminderDisplay extends Label {
	
	private TaskReminderVOToStringConverter converter = new TaskReminderVOToStringConverter();

	public TaskReminderDisplay(TaskReminderVO taskReminderVO) {
		String label = converter.convertToPresentation(taskReminderVO, String.class, getLocale());
		setValue(label);
	}

}
