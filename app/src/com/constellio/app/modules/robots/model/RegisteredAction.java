package com.constellio.app.modules.robots.model;

import java.util.Collection;

public class RegisteredAction {

	String code;

	String parametersSchemaLocalCode;

	Collection<String> supportedSchemaTypes;

	ActionExecutor executor;

	public RegisteredAction(String code, String parametersSchemaLocalCode,
			ActionExecutor executor, Collection<String> supportedSchemaTypes) {
		this.code = code;
		this.parametersSchemaLocalCode = parametersSchemaLocalCode;
		this.executor = executor;
		this.supportedSchemaTypes = supportedSchemaTypes;
	}

	public Collection<String> getSupportedSchemaTypes() {
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
