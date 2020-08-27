package com.constellio.model.services.users;

import java.util.ArrayList;
import java.util.List;

public class UserAndGroupsAddUpdateResponse {
	private List<UserAndGroupsResponseError> errors = new ArrayList<>();

	public UserAndGroupsAddUpdateResponse() {
	}

	public List<UserAndGroupsResponseError> getErrors() {
		return errors;
	}

	public UserAndGroupsAddUpdateResponse addError(UserAndGroupsResponseError validationError) {
		this.errors.add(validationError);
		return this;
	}
}
