package com.constellio.app.extensions.api.scripts;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScriptParameterValues {

	Map<ScriptParameter, Object> values;

	public ScriptParameterValues(Map<ScriptParameter, Object> values) {
		this.values = values;
	}

	public <T> T get(ScriptParameter parameter) {
		return (T) values.get(parameter);
	}

	public List<String> getCommaSeparatedStringValues(ScriptParameter parameter) {
		List<String> values = new ArrayList<>();

		String strValue = get(parameter);

		if (StringUtils.isNotBlank(strValue)) {

			for (String part : strValue.split(",")) {
				values.add(part.trim());
			}

		}

		return values;
	}

}
