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
	public boolean authenticate(String username, String password) {
		if (ldapConfigurationManager.isLDAPAuthentication()) {
			return ldapAuthenticationService.authenticate(username, password);
		} else {
			return passwordFileAuthenticationService.authenticate(username, password);
		}
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
