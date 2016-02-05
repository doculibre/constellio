package com.constellio.app.api.admin.services;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import com.constellio.app.client.AdminServicesConstants;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_InvalidToken;

public class AdminServiceAuthenticator {

	private ModelLayerFactory modelLayerFactory;

	public AdminServiceAuthenticator(
			ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	public static String getAuthenticatedUser(HttpHeaders httpHeaders) {
		AdminServiceAuthenticator authenticator = new AdminServiceAuthenticator(AdminServicesUtils.modelServicesFactory());
		String serviceKey = httpHeaders.getHeaderString(AdminServicesConstants.SERVICE_KEY);
		String token = httpHeaders.getHeaderString(AdminServicesConstants.AUTH_TOKEN);
		return authenticator.getTokenUser(serviceKey, token);
	}

	public String getToken(String serviceKey, String username, String password) {
		try {
			return modelLayerFactory.newUserServices().getToken(serviceKey, username, password);
		} catch (Exception e) {
			throw new WebApplicationException("Invalid username/password", Response.Status.UNAUTHORIZED);
		}
	}

	public String getNewToken(String serviceKey, String token) {
		try {
			return modelLayerFactory.newUserServices().getToken(serviceKey, token);
		} catch (Exception e) {
			throw new WebApplicationException("Invalid token/serviceKey", Response.Status.UNAUTHORIZED);
		}

	}

	public String getTokenUser(String serviceKey, String token) {
		try {
			return modelLayerFactory.newUserServices().getTokenUser(serviceKey, token);
		} catch (UserServicesRuntimeException_InvalidToken e) {
			throw new WebApplicationException("Invalid token", Response.Status.UNAUTHORIZED);
		}
	}

	public boolean isServiceKeyValid(String serviceKey) {
		String user = modelLayerFactory.newUserServices().getUserCredentialByServiceKey(serviceKey);
		return user != null;
	}

	public boolean isAuthTokenValid(String serviceKey, String authToken) {
		try {
			getTokenUser(serviceKey, authToken);
			return true;
		} catch (WebApplicationException e) {
			return false;
		}
	}

	public void removeToken(String authToken) {
		modelLayerFactory.newUserServices().removeToken(authToken);
	}
}
