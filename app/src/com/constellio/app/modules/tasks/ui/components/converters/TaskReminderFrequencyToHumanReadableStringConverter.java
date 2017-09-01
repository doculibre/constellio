package com.constellio.app.modules.tasks.ui.components.converters;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.ui.entities.TaskReminderVO;
import com.constellio.app.ui.framework.components.converters.JodaDateToStringConverter;
import com.vaadin.data.util.converter.Converter;
import org.joda.time.LocalDate;

import java.util.Locale;

import static com.constellio.app.modules.tasks.services.background.AlertOverdueTasksBackgroundAction.PARAMETER_SEPARATOR;
import static com.constellio.app.ui.i18n.i18n.$;

public class TaskReminderFrequencyToHumanReadableStringConverter implements Converter<String, String> {

	private JodaDateToStringConverter jodaDateToStringConverter = new JodaDateToStringConverter();

	@Override
	public String convertToModel(String value, Class<? extends String> targetType, Locale locale) throws ConversionException {
		return null;
	}

	@Override
	public String convertToPresentation(String value, Class<? extends String> targetType, Locale locale) throws ConversionException {
		StringBuilder convertedValue = new StringBuilder("");
		if(value != null) {
			String[] parameters = (value).split(PARAMETER_SEPARATOR);
			if(parameters.length >= 2) {
				convertedValue.append($("TaskReminderFrequencyDisplay.every_"+parameters[0], parameters[1]));
				if(parameters.length == 4) {
					convertedValue.append(" ");
					String durationType = parameters[2];
					String durationValue = parameters[3];
					if("Date".equals(durationType)) {
						convertedValue.append($("TaskReminderFrequencyDisplay.dateLimit",
								jodaDateToStringConverter.convertToPresentation(LocalDate.parse(durationValue), targetType, locale)));
					} else {
						convertedValue.append($("TaskReminderFrequencyDisplay.timesLimit", durationValue));
					}
				}
			}
		}
		return convertedValue.toString();
	}

	@Override
	public Class<String> getModelType() {
		return String.class;
	}

	@Override
	public Class<String> getPresentationType() {
		return String.class;
	}
}
