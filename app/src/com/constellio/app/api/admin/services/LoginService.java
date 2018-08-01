package com.constellio.app.api.admin.services;

import com.constellio.app.client.AdminServicesConstants;
import com.constellio.app.client.entities.UserResource;
import com.constellio.model.entities.security.global.UserCredential;
import org.joda.time.LocalDateTime;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@Path("session")
public class LoginService {

	private static final long serialVersionUID = -6663599014192066936L;

	@POST
	@Path("getToken")
	@Produces(MediaType.TEXT_PLAIN)
	public String getToken(
			@Context HttpHeaders httpHeaders,
			@FormParam("username") String username,
			@FormParam("password") String password) {

		String serviceKey = httpHeaders.getHeaderString(AdminServicesConstants.SERVICE_KEY);
		return new AdminServiceAuthenticator(AdminServicesUtils.modelServicesFactory()).getToken(serviceKey, username, password);

	}

	@POST
	@Path("getNewToken")
	@Produces(MediaType.TEXT_PLAIN)
	public String getNewToken(@Context HttpHeaders httpHeaders) {
		String serviceKey = httpHeaders.getHeaderString(AdminServicesConstants.SERVICE_KEY);
		String token = httpHeaders.getHeaderString(AdminServicesConstants.AUTH_TOKEN);
		return new AdminServiceAuthenticator(AdminServicesUtils.modelServicesFactory()).getNewToken(serviceKey, token);
	}

	@GET
	@Path("schema")
	@Produces(MediaType.APPLICATION_JSON)
	public UserResource schema(
			@Context HttpHeaders httpHeaders) {
		String user = AdminServiceAuthenticator.getAuthenticatedUser(httpHeaders);
		return toUserData(AdminServicesUtils.modelServicesFactory().newUserServices().getUser(user));
	}

	@POST
	@Path("removeToken")
	public Response removeToken(
			@Context HttpHeaders httpHeaders) {
		AdminServiceAuthenticator adminServiceAuthenticator = new AdminServiceAuthenticator(
				AdminServicesUtils.modelServicesFactory());
		String authToken = httpHeaders.getHeaderString(AdminServicesConstants.AUTH_TOKEN);

		adminServiceAuthenticator.removeToken(authToken);

		return getNoCacheResponseBuilder(Response.Status.NO_CONTENT).build();
	}

	private Response.ResponseBuilder getNoCacheResponseBuilder(Response.Status status) {
		CacheControl cc = new CacheControl();
		cc.setNoCache(true);
		cc.setMaxAge(-1);
		cc.setMustRevalidate(true);

		return Response.status(status).cacheControl(cc);
	}

	private UserResource toUserData(UserCredential userCredential) {
		UserResource userResource = new UserResource();
		userResource.setUsername(userCredential.getUsername());
		userResource.setFirstName(userCredential.getFirstName());
		userResource.setLastName(userCredential.getLastName());
		userResource.setEmail(userCredential.getEmail());
		userResource.setGlobalGroups(userCredential.getGlobalGroups());
		userResource.setCollections(userCredential.getCollections());
		userResource.setServiceKey(userCredential.getServiceKey());
		userResource.setSystemAdmin(userCredential.isSystemAdmin());
		Map<String, String> tokens = new HashMap<String, String>();
		for (Entry<String, LocalDateTime> token : userCredential.getAccessTokens().entrySet()) {
			tokens.put(token.getKey(), token.getValue().toString());
		}
		userResource.setTokens(tokens);
		return userResource;
	}
}
