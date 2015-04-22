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
package com.constellio.app.ui.entities;

import java.util.List;

import com.constellio.model.entities.records.wrappers.User;

@SuppressWarnings("serial")
public class UserVO extends RecordVO {

	public UserVO(String id, List<MetadataValueVO> metadataValues, VIEW_MODE viewMode) {
		super(id, metadataValues, viewMode);
	}

	public String getUsername() {
		return get(User.USERNAME);
	}

	public void setUsername(String username) {
		set(User.USERNAME, username);
	}

	public String getFirstName() {
		return get(User.FIRSTNAME);
	}

	public void setFirstName(String firstName) {
		set(User.FIRSTNAME, firstName);
	}

	public String getLastName() {
		return get(User.LASTNAME);
	}

	public void setLastName(String lastName) {
		set(User.LASTNAME, lastName);
	}

	public String getEmail() {
		return get(User.EMAIL);
	}

	public void setEmail(String email) {
		set(User.EMAIL, email);
	}

}
