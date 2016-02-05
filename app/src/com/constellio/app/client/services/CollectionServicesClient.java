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
