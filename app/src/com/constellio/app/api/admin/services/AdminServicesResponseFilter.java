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
