package com.constellio.app.api;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.ui.pages.base.VaadinSessionContext;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_UserIsNotInCollection;

public class HttpServletRequestAuthenticator {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpServletRequestAuthenticator.class);
	public static final String USER_SERVICE_KEY = "serviceKey";
	public static final String USER_TOKEN = "token";
	public static final String COLLECTION = "collection";

	UserServices userServices;
	ModelLayerFactory modelLayerFactory;

	public HttpServletRequestAuthenticator(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.userServices = modelLayerFactory.newUserServices();
	}

	public UserCredential authenticate(HttpServletRequest request) {

		String userServiceKey = request.getHeader(USER_SERVICE_KEY);
		String userToken = request.getHeader(USER_TOKEN);

		if (userServiceKey == null) {
			userServiceKey = request.getParameter(USER_SERVICE_KEY);
		}
		if (userToken == null) {
			userToken = request.getParameter(USER_TOKEN);
		}

		if (userServiceKey != null) {
			userServiceKey = userServiceKey.trim();
		}

		if (userToken != null) {
			userToken = userToken.trim();
		}

		UserServices userServices = modelLayerFactory.newUserServices();

		if (userServices.isAuthenticated(userServiceKey, userToken)) {
			String username = userServices.getUserCredentialByServiceKey(userServiceKey);
			return userServices.getUser(username);
		} else {
			return ssoLogin(request);
		}
	}
	
	private UserCredential ssoLogin(HttpServletRequest request) {
		Principal userPrincipal = (Principal) request.getSession().getAttribute(VaadinSessionContext.USER_PRINCIPAL_ATTRIBUTE);
		if (userPrincipal != null) {
			String username = userPrincipal.getName();
			UserCredential userCredential = userServices.getUserCredential(username);
			if (userCredential.getStatus() == UserCredentialStatus.ACTIVE) {
				return userServices.getUser(username);
			}
		}
		return null;
	}

	public UserCredential authenticateSystemAdmin(HttpServletRequest request) {
		UserCredential user = authenticate(request);
		if (user == null) {
			return null;
		} else {
			if (user.isSystemAdmin()) {
				return user;
			} else {
				LOGGER.warn("Cannot authenticate : User '" + user.getUsername() + "' is not a system admin");
				return null;
			}
		}

	}

	public User authenticateInCollection(HttpServletRequest request) {
		UserCredential userCredential = authenticate(request);
		if (userCredential == null) {
			return null;
		} else {
			String collection = getCollection(request);
			return userServices.getUserInCollection(userCredential.getUsername(), collection);
		}
	}

	public String getCollection(HttpServletRequest request) {
		String collection = request.getHeader(COLLECTION);
		if (collection == null) {
			collection = request.getParameter(COLLECTION);
		}
		return collection;
	}

	public User authenticateSystemAdminInCollection(HttpServletRequest request) {
		UserCredential userCredential = authenticateSystemAdmin(request);
		if (userCredential == null) {
			return null;
		} else {
			String collection = getCollection(request);
			if (collection == null) {
				LOGGER.warn("Cannot authenticate : Missing 'collection' parameter");
				return null;
			} else {
				try {
					return userServices.getUserInCollection(userCredential.getUsername(), collection);
				} catch (UserServicesRuntimeException_UserIsNotInCollection e) {
					LOGGER.warn("Cannot authenticate : User '" + userCredential.getUsername() + "' is not in collection '"
							+ collection + "'");
					return null;
				}
			}
		}
	}
}
