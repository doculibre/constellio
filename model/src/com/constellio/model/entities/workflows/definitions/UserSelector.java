package com.constellio.model.entities.workflows.definitions;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.constellio.model.entities.workflows.execution.WorkflowExecution;

public class UserSelector {

	List<String> users;

	public UserSelector(List<String> users) {
		this.users = users;
	}

	public List<String> getUsers(WorkflowExecution execution) {
		List<String> processedUsers = new ArrayList<>();
		for (String user : users) {
			if (user.startsWith("${")) {
				processedUsers.add(execution.getVariable(StringUtils.substringBetween(user, "${", "}")));
			} else {
				processedUsers.add(user);
			}
		}
		return processedUsers;
	}

}
