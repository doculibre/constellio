package com.constellio.model.services.security.authentification;

public interface AuthenticationService {

	boolean authenticate(String username, String password, boolean isAdminInAnyCollection);

	boolean supportPasswordChange();

	void changePassword(String username, String oldPassword, String newPassword);

	void changePassword(String username, String newPassword);

	void reloadServiceConfiguration();

}
