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
package com.constellio.model.frameworks.validation;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ValidationError {

	private final String code;
	private final Map<String, String> parameters;

	public ValidationError(String code, Map<String, String> parameters) {
		this.code = code;
		this.parameters = parameters;
	}

	public String getCode() {
		return code;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	@Override
	public String toString() {
		return "ValidationError [code=" + code + ", parameters=" + parameters + "]";
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public String toErrorSummaryString() {
		if (parameters == null || parameters.isEmpty()) {
			return code;
		} else {

			StringBuilder stringBuilder = new StringBuilder(code + "[");

			boolean first = true;
			for (Map.Entry<String, String> parameter : parameters.entrySet()) {
				if (!first) {
					stringBuilder.append(",");
				}
				stringBuilder.append(parameter.getKey() + "=" + parameter.getValue());
				first = false;
			}

			return stringBuilder.toString() + "]";
		}
	}

	public String toMultilineErrorSummaryString() {
		StringBuilder sb = new StringBuilder();
		sb.append(code);
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			sb.append("\n\t" + entry.getKey() + "=" + entry.getValue());
		}
		return sb.toString();
	}

}
