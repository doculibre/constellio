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
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import com.constellio.app.client.AdminServicesConstants;

@Provider
@PreMatching
public class AdminServicesResponseFilter implements ContainerResponseFilter {

	private final static Logger log = Logger.getLogger(AdminServicesResponseFilter.class.getName());

	@Override
	public void filter(ContainerRequestContext requestCtx, ContainerResponseContext responseCtx)
			throws IOException {

		log.info("Filtering REST Response");

		responseCtx.getHeaders().add("Access-Control-Allow-Origin",
				"*");    // You may further limit certain client IPs with Access-Control-Allow-Origin instead of '*'
		responseCtx.getHeaders().add("Access-Control-Allow-Credentials", "true");
		responseCtx.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
		responseCtx.getHeaders().add("Access-Control-Allow-Headers",
				AdminServicesConstants.SERVICE_KEY + ", " + AdminServicesConstants.AUTH_TOKEN);
	}
}
