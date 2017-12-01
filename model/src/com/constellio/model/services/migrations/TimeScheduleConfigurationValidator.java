package com.constellio.model.services.migrations;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.configs.SystemConfigurationScript;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;

public class TimeScheduleConfigurationValidator implements SystemConfigurationScript<String> {

	public static final String BAD_SCHEDULE = "badSchedule";

	@Override
	public void onNewCollection(String newValue, String collection, ModelLayerFactory modelLayerFactory) {

	}

	@Override
	public void validate(String newValue, ValidationErrors errors) {
		if (!isValid(newValue)) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("schedule", newValue);
			errors.add(TimeScheduleConfigurationValidator.class, BAD_SCHEDULE, parameters);
		}
	}

	@Override
	public void onValueChanged(String previousValue, String newValue, ModelLayerFactory modelLayerFactory) {

	}

	@Override
	public void onValueChanged(String previousValue, String newValue, ModelLayerFactory modelLayerFactory, String collection) {

	}

	public static boolean isValid(String timeSchedule) {

		if (timeSchedule == null || timeSchedule.isEmpty()) {
			return true;
		}

		return Pattern.compile("[0-9][0-9]-[0-9][0-9]").matcher(timeSchedule).find();
	}

	public static boolean isCurrentlyInSchedule(String timeSchedule) {

		if (timeSchedule == null || timeSchedule.isEmpty()) {
			return true;
		}

		int from = Integer.parseInt(timeSchedule.substring(0, 2));
		int to = Integer.parseInt(timeSchedule.substring(3, 5));

		int hour = TimeProvider.getLocalDateTime().getHourOfDay();

		if (from == to) {
			return true;
		} else if (from < to) {
			return from <= hour && hour < to;
		} else {
			return (from <= hour && hour < 24) || (0 <= hour && hour < to);
		}
	}
}
