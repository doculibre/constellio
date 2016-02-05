package com.constellio.app.client.services;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.jackson.JacksonFeature;

import com.constellio.app.client.AdminServicesConstants;
import com.constellio.app.client.entities.UserResource;

public class AdminServicesSession {

	String serviceKey;

	String token;

	WebTarget target;

	private AdminServicesSession(WebTarget target, String token, String serviceKey) {
		this.target = target;
		this.token = token;
		this.serviceKey = serviceKey;
	}

	public static AdminServicesSession connect(String url, String serviceKey, String user, String password) {

		javax.ws.rs.client.Client client = ClientBuilder.newClient();
		WebTarget target = client.register(JacksonFeature.class).target(url);

		Form form = new Form();
		form.param("username", user);
		form.param("password", password);

		String token = target.path("session").path("getToken").request(MediaType.TEXT_PLAIN)
				.header(AdminServicesConstants.SERVICE_KEY, serviceKey)
				.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);

		return new AdminServicesSession(target, token, serviceKey);
	}

	public String getNewToken() {
		return requestString("getNewToken").get(String.class);
	}

	public UserResource schema() {
		return requestJson("schema").get(UserResource.class);
	}

	public void removeToken() {
		requestJson("removeToken").method("POST");
	}

	public CollectionServicesClient newCollectionServices() {
		return new CollectionServicesClient(target.path("collections"), token, serviceKey);
	}

	public UserServicesClient newUserServices() {
		return new UserServicesClient(target.path("users"), token, serviceKey);
	}

	public SecurityManagementDriver manageCollectionSecurity(String collection) {
		return new SecurityManagementDriver(target.path("security"), token, serviceKey, collection);
	}

	public SchemaServicesClient newSchemaServicesForCollection(String collection) {
		return new SchemaServicesClient(target.path("schemas"), token, serviceKey, collection);
	}

	public String getToken() {
		return token;
	}

	public String getServiceKey() {
		return serviceKey;
	}

	public WebTarget getTarget() {
		return target;
	}

	private Builder requestJson(String service) {
		return target.path("session").path(service).request(MediaType.APPLICATION_JSON_TYPE)
				.header(AdminServicesConstants.AUTH_TOKEN, token).header(AdminServicesConstants.SERVICE_KEY, serviceKey);
	}

	private Builder requestString(String service) {
		return target.path("session").path(service).request(MediaType.TEXT_PLAIN)
				.header(AdminServicesConstants.AUTH_TOKEN, token).header(AdminServicesConstants.SERVICE_KEY, serviceKey);
	}
}
