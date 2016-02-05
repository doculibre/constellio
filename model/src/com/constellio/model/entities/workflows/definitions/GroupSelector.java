package com.constellio.model.entities.workflows.definitions;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.constellio.model.entities.workflows.execution.WorkflowExecution;

public class GroupSelector {

	List<String> groups;

	public GroupSelector(List<String> groups) {
		this.groups = groups;
	}

	public List<String> getGroups(WorkflowExecution execution) {
		List<String> processedGroups = new ArrayList<>();
		for (String group : groups) {
			if (group.startsWith("${")) {
				processedGroups.add(execution.getVariable(StringUtils.substringBetween(group, "${", "}")));
			} else {
				processedGroups.add(group);
			}
		}
		return processedGroups;
	}
}
