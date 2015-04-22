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
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Authorization {

	AuthorizationDetails detail;

	List<String> grantedToPrincipals = new ArrayList<>();
	List<String> grantedOnRecords = new ArrayList<>();

	public Authorization() {
	}

	public Authorization(AuthorizationDetails detail, List<String> grantedToPrincipals,
			List<String> grantedOnRecords) {
		this.detail = detail;
		this.grantedToPrincipals = grantedToPrincipals;
		this.grantedOnRecords = grantedOnRecords;
	}

	public AuthorizationDetails getDetail() {
		return detail;
	}

	public void setDetail(AuthorizationDetails detail) {
		this.detail = detail;
	}

	public List<String> getGrantedToPrincipals() {
		return grantedToPrincipals;
	}

	public void setGrantedToPrincipals(List<String> grantedToPrincipals) {
		this.grantedToPrincipals = grantedToPrincipals;
	}

	public List<String> getGrantedOnRecords() {
		return grantedOnRecords;
	}

	public void setGrantedOnRecords(List<String> grantedOnRecords) {
		this.grantedOnRecords = grantedOnRecords;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

}
