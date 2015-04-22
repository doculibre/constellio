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
