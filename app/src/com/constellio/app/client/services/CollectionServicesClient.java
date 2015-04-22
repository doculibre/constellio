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
package com.constellio.app.client.services;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import com.constellio.app.client.AdminServicesConstants;
import com.constellio.app.client.entities.CollectionResource;

public class CollectionServicesClient {

	final String token;
	final String serviceKey;
	final WebTarget target;

	CollectionServicesClient(WebTarget target, String token, String serviceKey) {
		this.target = target;
		this.token = token;
		this.serviceKey = serviceKey;
	}

	public String createCollection(CollectionResource resource) {
		return requestString("createCollection").post(Entity.json(resource), String.class);
	}

	private Builder requestString(String service) {
		return target.path(service).request(MediaType.TEXT_PLAIN).header(AdminServicesConstants.AUTH_TOKEN, token)
				.header(AdminServicesConstants.SERVICE_KEY, serviceKey);
	}
}
