package com.constellio.model.services.users;

import java.util.Map;

public class UserAndGroupsResponseError {
	private final String code;
	private final Map<String, String> parameters;

	public UserAndGroupsResponseError(Class<?> errorClass, String errorCode, Map<String, String> parameters) {
		this.code = errorClass.getName() + "_" + errorCode;
		this.parameters = parameters;
	}

	public String getCode() {
		return code;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}
}
