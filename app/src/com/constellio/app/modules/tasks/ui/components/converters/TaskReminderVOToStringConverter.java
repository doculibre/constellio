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
