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
package com.constellio.model.entities.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDate;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.security.AuthorizationDetailsRuntimeException.AuthorizationDetailsRuntimeException_RoleRequired;
import com.constellio.model.entities.security.AuthorizationDetailsRuntimeException.AuthorizationDetailsRuntimeException_SameCollectionRequired;

public class AuthorizationDetails {

	private final String id;

	private final List<String> roles;

	private final LocalDate startDate;

	private final LocalDate endDate;

	private final String collection;

	private final boolean synced;

	public AuthorizationDetails(String collection, String id, List<String> roles, LocalDate startDate, LocalDate endDate,
			boolean synced) {
		this.collection = collection;
		this.id = id;
		this.roles = Collections.unmodifiableList(roles);
		this.startDate = startDate;
		this.endDate = endDate;
		this.synced = synced;
	}

	public static AuthorizationDetails create(String id, List<String> roles, String zeCollection) {
		return create(id, roles, null, null, zeCollection);
	}

	public static AuthorizationDetails createSynced(String id, List<String> roles, String zeCollection) {
		return create(id, roles, null, null, zeCollection, true);
	}

	public static AuthorizationDetails create(String id, List<String> roles, LocalDate startDate, LocalDate endDate,
			String zeCollection) {
		return create(id, roles, startDate, endDate, zeCollection, false);
	}

	public static AuthorizationDetails create(String id, List<String> roles, LocalDate startDate, LocalDate endDate,
			String zeCollection, boolean synced) {

		if (roles.isEmpty()) {
			throw new AuthorizationDetailsRuntimeException_RoleRequired();
		}
		Set<String> collections = new HashSet<>();
		boolean read = false;
		boolean write = false;
		boolean delete = false;
		List<String> rolesCodes = new ArrayList<>();
		List<String> operationRolesCodes = new ArrayList<>();
		for (String role : roles) {
			rolesCodes.add(role);
			collections.add(zeCollection);

			boolean readAccess = role.equals(Role.READ);
			boolean writeAccess = role.equals(Role.WRITE) || role.equals(Role.DELETE);
			boolean deleteAccess = role.equals(Role.DELETE);

			read |= readAccess;
			write |= writeAccess;
			delete |= deleteAccess;
			if (!readAccess && !writeAccess && !deleteAccess) {
				operationRolesCodes.add(role);
			}
		}
		if (collections.size() > 1) {
			throw new AuthorizationDetailsRuntimeException_SameCollectionRequired();
		}

		String collection = collections.iterator().next();

		StringBuilder idBuilder = new StringBuilder();
		if (read || write || delete) {
			idBuilder.append("r");
		}
		if (write) {
			idBuilder.append("w");
		}
		if (delete) {
			idBuilder.append("d");
		}
		idBuilder.append("_");
		idBuilder.append(StringUtils.join(operationRolesCodes, ","));
		idBuilder.append("_");
		idBuilder.append(id);
		return new AuthorizationDetails(collection, idBuilder.toString(), rolesCodes, startDate, endDate, synced);
	}

	public List<String> getRoles() {
		return roles;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public String getId() {
		return id;
	}

	public boolean isFutureAuthorization() {
		return startDate != null && TimeProvider.getLocalDate().isBefore(startDate);
	}

	public String getCollection() {
		return collection;
	}

	public AuthorizationDetails withNewEndDate(LocalDate endate) {
		return new AuthorizationDetails(this.collection, this.id, this.roles, this.startDate, endate, synced);
	}

	public boolean isSynced() {
		return synced;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return roles.toString();
	}

}
