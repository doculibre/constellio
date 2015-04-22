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

import java.io.IOException;
import java.util.logging.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import com.constellio.app.client.AdminServicesConstants;

@Provider
@PreMatching
public class AdminServicesRequestFilter implements ContainerRequestFilter {

	private final static Logger log = Logger.getLogger(AdminServicesRequestFilter.class.getName());

	@Override
	public void filter(ContainerRequestContext requestCtx)
			throws IOException {

		String path = requestCtx.getUriInfo().getPath();
		if (path.startsWith("document")) {
			return;
		}

		log.info("Filtering request path: " + path);

		// IMPORTANT!!! First, Acknowledge any pre-flight test from browsers for this case before validating the headers (CORS stuff)
		if (requestCtx.getRequest().getMethod().equals("OPTIONS")) {
			requestCtx.abortWith(Response.status(Response.Status.OK).build());

			return;
		}

		// Then check is the service key exists and is valid.
		String serviceKey = requestCtx.getHeaderString(AdminServicesConstants.SERVICE_KEY);
		AdminServicesUtils.ensureNotNull(AdminServicesConstants.SERVICE_KEY, serviceKey);
		AdminServiceAuthenticator adminServiceAuthenticator = new AdminServiceAuthenticator(
				AdminServicesUtils.modelServicesFactory());//AdminServiceAuthenticator.getInstance();

		if (!adminServiceAuthenticator.isServiceKeyValid(serviceKey)) {
			// Kick anyone without a valid service key
			requestCtx.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
			return;
		}

		// For any pther methods besides login, the authToken must be verified
		if (!path.equals("session/getToken")) {
			String authToken = requestCtx.getHeaderString(AdminServicesConstants.AUTH_TOKEN);

			// if it isn't valid, just kick them out.
			if (!adminServiceAuthenticator.isAuthTokenValid(serviceKey, authToken)) {
				requestCtx.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
			}
		}

	}
}

