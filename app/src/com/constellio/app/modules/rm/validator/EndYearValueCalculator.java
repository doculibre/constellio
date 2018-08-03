package com.constellio.app.modules.rm.validator;

import com.constellio.model.entities.configs.AbstractSystemConfigurationScript;
import com.constellio.model.frameworks.validation.ValidationErrors;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class EndYearValueCalculator extends AbstractSystemConfigurationScript<String> {
	public static final Pattern MM_DD = Pattern.compile("^((0[1-9]|1[0-2])\\/([01][1-9]|10|2[0-8]))|((0[13-9]|1[0-2])\\/(29|30))|((0[13578]|1[0-2])\\/31)|02\\/29$");

	@Override
	public void validate(String newValue, ValidationErrors errors) {
		if (newValue != null && !MM_DD.matcher(newValue).matches()) {
			Map<String, Object> parameters = new HashMap<>();
			//parameters.put("message", $("com.constellio.app.modules.rm.RMConfigs.EndYearValueCalculator"));
			errors.add(EndYearValueCalculator.class, "InvalideEndYear"/*, parameters*/);
		}
	}
}