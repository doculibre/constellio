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
package com.constellio.model.extensions.events.security.authorizations;

import java.util.List;

import com.constellio.data.frameworks.extensions.VaultEventListener;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.utils.Lazy;

public class RemovedAuthorizationOnUserEvent {

	Lazy<User> user;

	Lazy<List<Record>> records;

	Lazy<List<Role>> roles;

	public RemovedAuthorizationOnUserEvent(Lazy<User> user, Lazy<List<Record>> records, Lazy<List<Role>> roles) {
		this.user = user;
		this.records = records;
		this.roles = roles;
	}

	public interface RemovedAuthorizationOnUserEventListener extends VaultEventListener<RemovedAuthorizationOnUserEvent> {
	}
}
