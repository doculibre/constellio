package com.constellio.model.services.security.authentification;

import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.extensions.ModelLayerSystemExtensions;
import com.constellio.model.services.extensions.ModelLayerExtensions;

public class CombinedAuthenticationService implements AuthenticationService {
	private LDAPConfigurationManager ldapConfigurationManager;
	private LDAPAuthenticationService ldapAuthenticationService;
	private PasswordFileAuthenticationService passwordFileAuthenticationService;
	private ModelLayerSystemExtensions modelLayerSystemExtensions;

	public CombinedAuthenticationService(LDAPConfigurationManager ldapConfigurationManager,
			LDAPAuthenticationService ldapAuthenticationService,
			PasswordFileAuthenticationService passwordFileAuthenticationService, ModelLayerExtensions modelLayerExtensions) {
		this.ldapConfigurationManager = ldapConfigurationManager;
		this.ldapAuthenticationService = ldapAuthenticationService;
		this.passwordFileAuthenticationService = passwordFileAuthenticationService;
		this.modelLayerSystemExtensions = modelLayerExtensions.getSystemWideExtensions();
	}

	@Override
	public boolean authenticate(String username, String password) {
		boolean authenticated = false;
		if (ldapConfigurationManager.isLDAPAuthentication()) {
			authenticated = ldapAuthenticationService.authenticate(username, password);
		}
		if (!authenticated && (!ldapConfigurationManager.isLDAPAuthentication()
				|| modelLayerSystemExtensions.canAuthenticateUsingPasswordFileIfLDAPFailed(username))) {
			authenticated = passwordFileAuthenticationService.authenticate(username, password);
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
