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
package com.constellio.app.api.admin.services;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;

import com.constellio.app.client.AdminServicesConstants;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.services.factories.ModelLayerFactory;

public class AdminServicesUtils {

	public static ModelLayerFactory modelServicesFactory() {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		return constellioFactories.getModelLayerFactory();
	}

	public static DataLayerFactory daosServicesFactory() {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		return constellioFactories.getDataLayerFactory();
	}

	public static AppLayerFactory appServicesFactory() {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		return constellioFactories.getAppLayerFactory();
	}

	public static String getAuthenticatedUser(HttpHeaders httpHeaders) {
		String authKeys = httpHeaders.getHeaderString(AdminServicesConstants.AUTH_TOKEN);
		List<String> serviceKeys = httpHeaders.getRequestHeader(AdminServicesConstants.SERVICE_KEY);
		return authKeys;

	}

	public static void ensureNotNull(String name, Object object) {
		if (object == null) {
			throw new WebApplicationException("Parameter '" + name + "' is required", Status.BAD_REQUEST);
		}
	}

}
