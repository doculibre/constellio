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
package com.constellio.sdk.load.script.preparators;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.sdk.load.script.UserPreparator;

public class DefaultUsersPreparator implements UserPreparator {

	List<String> collections;

	int usersCount;

	int groupsCount;

	int sequence;

	public DefaultUsersPreparator(List<String> collections, int usersCount, int groupsCount) {
		this.collections = collections;
		this.usersCount = usersCount;
		this.groupsCount = groupsCount;
	}

	@Override
	public List<GlobalGroup> createGroups() {
		List<GlobalGroup> groups = new ArrayList<>();
		for (int i = 0; i < groupsCount; i++) {
			String code = "" + i;
			String name = "Group '" + code + "'";
			groups.add(new GlobalGroup(code, name, collections, null, GlobalGroupStatus.ACTIVE));
		}

		return groups;
	}

	@Override
	public List<UserCredential> createUsers(List<String> groups) {
		List<UserCredential> userCredentials = new ArrayList<>();

		userCredentials.add(newUser("admin", "admin", "admin", groups));
		for (int i = 0; i < usersCount; i++) {
			String code = "user" + i;
			List<String> userGroups = new ArrayList<>();
			userGroups.addAll(groups);
			userCredentials.add(newUser(code, code, code, userGroups));
		}

		return userCredentials;
	}

	private UserCredential newUser(String username, String firstName, String lastName, List<String> groups) {
		String email = firstName + "." + lastName + "@constellio.com";
		return new UserCredential(username, firstName, lastName, email, groups, collections, UserCredentialStatus.ACTIVE);
	}
}
