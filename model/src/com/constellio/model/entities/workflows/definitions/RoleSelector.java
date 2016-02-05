package com.constellio.model.entities.workflows.definitions;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.constellio.model.entities.workflows.execution.WorkflowExecution;

public class RoleSelector {

	List<String> roles;

	public RoleSelector(List<String> roles) {
		this.roles = roles;
	}

	public List<String> getRoles(WorkflowExecution execution) {
		List<String> processedRoles = new ArrayList<>();
		for (String role : roles) {
			if (role.startsWith("${")) {
				processedRoles.add(execution.getVariable(StringUtils.substringBetween(role, "${", "}")));
			} else {
				processedRoles.add(role);
			}
		}
		return processedRoles;
	}

}
