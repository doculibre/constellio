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
package com.constellio.app.ui.framework.builders;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDateTime;

import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;

@SuppressWarnings("serial")
public class UserCredentialToVOBuilder implements Serializable {

	public UserCredentialVO build(UserCredential userCredential) {
		String username = userCredential.getUsername();
		String firstName = userCredential.getFirstName();
		String lastName = userCredential.getLastName();
		String email = userCredential.getEmail();
		Map<String, LocalDateTime> tokens = userCredential.getTokens();
		String serviceKey = userCredential.getServiceKey();
		boolean systemAdmin = userCredential.isSystemAdmin();
		List<String> globalGroups = userCredential.getGlobalGroups();
		Set<String> collections = new HashSet<>();
		collections.addAll(userCredential.getCollections());
		UserCredentialStatus status = userCredential.getStatus();

		return new UserCredentialVO(username, firstName, lastName, email, serviceKey, systemAdmin, globalGroups, collections,
				tokens, null, null, status);

	}
}