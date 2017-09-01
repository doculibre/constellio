package com.constellio.app.modules.tasks.ui.components.display;

import com.constellio.app.modules.tasks.ui.components.converters.TaskReminderFrequencyToHumanReadableStringConverter;
import com.constellio.app.modules.tasks.ui.components.converters.TaskReminderVOToStringConverter;
import com.constellio.app.modules.tasks.ui.entities.TaskReminderVO;
import com.vaadin.ui.Label;
import org.joda.time.LocalDate;

import static com.constellio.app.modules.tasks.services.background.AlertOverdueTasksBackgroundAction.PARAMETER_SEPARATOR;
import static com.constellio.app.ui.i18n.i18n.$;

public class TaskReminderFrequencyDisplay extends Label {

	private TaskReminderFrequencyToHumanReadableStringConverter converter = new TaskReminderFrequencyToHumanReadableStringConverter();

	public TaskReminderFrequencyDisplay(String taskReminderFrequency) {
		String label = converter.convertToPresentation(taskReminderFrequency, String.class, getLocale());
		setValue(label);
	}
}
