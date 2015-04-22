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
package com.constellio.model.entities.workflows.definitions;

import org.apache.commons.lang.StringUtils;

import com.constellio.model.entities.workflows.execution.WorkflowExecution;

public class BPMNProperty {

	private String fieldId;
	private String expressionValue;
	private String variableCode;

	public BPMNProperty(String fieldId, String expressionValue, String variableCode) {
		this.fieldId = fieldId;
		this.expressionValue = expressionValue;
		this.variableCode = variableCode;
	}

	public String getFieldId() {
		return fieldId;
	}

	public String getExpressionValue() {
		return expressionValue;
	}

	public String getVariableCode() {
		return variableCode;
	}

	public String getParsedExpression(WorkflowExecution execution) {
		String[] variableCodes = StringUtils.substringsBetween(expressionValue, "${", "}");
		String[] variableValues = new String[variableCodes.length];
		for (int i = 0; i < variableCodes.length; i++) {
			variableValues[i] = execution.getVariable(variableCodes[i]);
		}
		String parsedExpression = StringUtils.replaceEach(expressionValue, variableCodes, variableValues);
		return StringUtils.remove(StringUtils.remove(parsedExpression, "${"), "}");
	}
}
