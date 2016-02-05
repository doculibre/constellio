package com.constellio.app.modules.tasks.ui.components.converters;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Locale;

import org.joda.time.LocalDate;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.ui.entities.TaskReminderVO;
import com.constellio.app.ui.framework.components.converters.JodaDateToStringConverter;
import com.vaadin.data.util.converter.Converter;

public class TaskReminderVOToStringConverter implements Converter<String, TaskReminderVO> {
	
	private JodaDateToStringConverter jodaDateToStringConverter = new JodaDateToStringConverter();
	
	@Override
	public TaskReminderVO convertToModel(String value, Class<? extends TaskReminderVO> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		return null;
	}

	@Override
	public String convertToPresentation(TaskReminderVO value, Class<? extends String> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		String presentation;
		if (value != null) {
			LocalDate fixedDate = value.getFixedDate();
			String relativeDateMetadataCode = value.getRelativeDateMetadataCode();
			int numberOfDaysToRelativeDate = value.getNumberOfDaysToRelativeDate();
			Boolean beforeRelativeDate = value.getBeforeRelativeDate();
			
			if (fixedDate != null) {
				presentation = jodaDateToStringConverter.convertToPresentation(fixedDate, String.class, locale);
			} else {
				String relativeDateLabel;
				if (Task.START_DATE.equals(relativeDateMetadataCode)) {
					relativeDateLabel = $("TaskReminder.display.relativeDateMetadataCode.startDate");
				} else if (Task.DUE_DATE.equals(relativeDateMetadataCode)) {
					relativeDateLabel = $("TaskReminder.display.relativeDateMetadataCode.dueDate");
				} else if (Task.END_DATE.equals(relativeDateMetadataCode)) {
					relativeDateLabel = $("TaskReminder.display.relativeDateMetadataCode.endDate");
				} else {
					// Should never happen
					throw new RuntimeException("Invalid metadata code : " + relativeDateMetadataCode);
				}
				if (numberOfDaysToRelativeDate == 0) {
					presentation = relativeDateLabel;
				} else {
					String beforeAfter;
					if (Boolean.TRUE.equals(beforeRelativeDate)) {
						beforeAfter = $("TaskReminder.display.beforeRelativeDate.before");
					} else {
						beforeAfter = $("TaskReminder.display.beforeRelativeDate.after");
					}
					presentation = $("TaskReminder.display.numberOfDaysToRelativeDate", numberOfDaysToRelativeDate, beforeAfter, relativeDateLabel);
				}
			}
		} else {
			presentation = null;
		}
		return presentation;
	}

	@Override
	public Class<TaskReminderVO> getModelType() {
		return TaskReminderVO.class;
	}

	@Override
	public Class<String> getPresentationType() {
		return String.class;
	}
}
