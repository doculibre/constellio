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
