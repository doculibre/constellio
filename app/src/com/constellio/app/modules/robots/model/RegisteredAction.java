package com.constellio.app.modules.robots.model;

import java.util.List;

public class RegisteredAction {

	String code;

	String parametersSchemaLocalCode;

	List<String> supportedSchemaTypes;

	ActionExecutor executor;

	public RegisteredAction(String code, String parametersSchemaLocalCode,
			ActionExecutor executor, List<String> supportedSchemaTypes) {
		this.code = code;
		this.parametersSchemaLocalCode = parametersSchemaLocalCode;
		this.executor = executor;
		this.supportedSchemaTypes = supportedSchemaTypes;
	}

	public List<String> getSupportedSchemaTypes() {
		return supportedSchemaTypes;
	}

	public String getCode() {
		return code;
	}

	public String getParametersSchemaLocalCode() {
		return parametersSchemaLocalCode;
	}

	public ActionExecutor getExecutor() {
		return executor;
	}
}
