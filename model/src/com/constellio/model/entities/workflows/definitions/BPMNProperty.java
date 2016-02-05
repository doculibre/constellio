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
