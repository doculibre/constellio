package com.constellio.model.services.security.authentification;

import com.constellio.model.conf.ldap.LDAPConfigurationManager;

public class CombinedAuthenticationService implements AuthenticationService {
	private LDAPConfigurationManager ldapConfigurationManager;
	private LDAPAuthenticationService ldapAuthenticationService;
	private PasswordFileAuthenticationService passwordFileAuthenticationService;

	public CombinedAuthenticationService(LDAPConfigurationManager ldapConfigurationManager,
			LDAPAuthenticationService ldapAuthenticationService,
			PasswordFileAuthenticationService passwordFileAuthenticationService) {
		this.ldapConfigurationManager = ldapConfigurationManager;
		this.ldapAuthenticationService = ldapAuthenticationService;
		this.passwordFileAuthenticationService = passwordFileAuthenticationService;
	}

	@Override
	public boolean authenticate(String username, String password, boolean isAdminInAnyCollection) {
		boolean authenticated = false;
		if (ldapConfigurationManager.isLDAPAuthentication()) {
			authenticated = ldapAuthenticationService.authenticate(username, password, isAdminInAnyCollection);
		}
		if (!authenticated && (isAdminInAnyCollection || !ldapConfigurationManager.isLDAPAuthentication())) {
			authenticated = passwordFileAuthenticationService.authenticate(username, password, false);
		}
		return authenticated;
	}

	@Override
	public boolean supportPasswordChange() {
		if (ldapConfigurationManager.isLDAPAuthentication()) {
			return ldapAuthenticationService.supportPasswordChange();
		} else {
			return passwordFileAuthenticationService.supportPasswordChange();
		}
	}

	@Override
	public void changePassword(String username, String oldPassword, String newPassword) {
		if (!username.equals("admin") && ldapConfigurationManager.isLDAPAuthentication()) {
			ldapAuthenticationService.changePassword(username, oldPassword, newPassword);
		} else {
			passwordFileAuthenticationService.changePassword(username, oldPassword, newPassword);
		}
	}

	@Override
	public void changePassword(String username, String newPassword) {
		if (!username.equals("admin") && ldapConfigurationManager.isLDAPAuthentication()) {
			ldapAuthenticationService.changePassword(username, newPassword);
		} else {
			passwordFileAuthenticationService.changePassword(username, newPassword);
		}
	}

	@Override
	public void reloadServiceConfiguration() {
		if (ldapConfigurationManager.isLDAPAuthentication()) {
			ldapAuthenticationService.reloadServiceConfiguration();
		} else {
			passwordFileAuthenticationService.reloadServiceConfiguration();
		}
	}
}
