package com.constellio.app.modules.tasks.ui.components.display;

import com.constellio.app.modules.tasks.ui.components.converters.TaskReminderFrequencyToHumanReadableStringConverter;
import com.vaadin.ui.Label;

public class TaskReminderFrequencyDisplay extends Label {

	private TaskReminderFrequencyToHumanReadableStringConverter converter = new TaskReminderFrequencyToHumanReadableStringConverter();

	public TaskReminderFrequencyDisplay(String taskReminderFrequency) {
		String label = converter.convertToPresentation(taskReminderFrequency, String.class, getLocale());
		setValue(label);
	}
}
