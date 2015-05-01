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
package com.constellio.app.ui.pages.management.ldap;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Set;

import javax.naming.ldap.LdapContext;

import com.constellio.model.conf.ldap.*;
import com.constellio.model.conf.ldap.services.LDAPConnectionFailure;
import org.apache.commons.lang.StringUtils;

import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.conf.ldap.services.LDAPServices;
import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;

public class LDAPConfigManagementPresenter extends
										   BasePresenter<LDAPConfigManagementView> {

	public LDAPConfigManagementPresenter(LDAPConfigManagementView view) {
		super(view);
	}

	public LDAPServerConfiguration getLDAPServerConfiguration() {
		return view.getConstellioFactories().getModelLayerFactory().getLdapConfigurationManager().getLDAPServerConfiguration();
	}

	public LDAPUserSyncConfiguration getLDAPUserSyncConfiguration() {
		return view.getConstellioFactories().getModelLayerFactory().getLdapConfigurationManager().getLDAPUserSyncConfiguration();
	}

	public void backButtonClick() {
		view.navigateTo().adminModule();
	}

	public void saveConfigurations(LDAPServerConfiguration ldapServerConfigurationVO,
			LDAPUserSyncConfiguration ldapUserSyncConfigurationVO) {
		LDAPConfigurationManager ldapConfigManager = view.getConstellioFactories().getModelLayerFactory()
				.getLdapConfigurationManager();
		try{
			ldapConfigManager.saveLDAPConfiguration(ldapServerConfigurationVO, ldapUserSyncConfigurationVO);
			view.showMessage($("ldap.config.saved"));
		}catch(TooShortDurationRuntimeException e){
			view.showErrorMessage($("ldap.TooShortDurationRuntimeException"));
		}catch(EmptyDomainsRuntimeException e){
			view.showErrorMessage($("ldap.EmptyDomainsRuntimeException"));
		}catch(EmptyUrlsRuntimeException e){
			view.showErrorMessage($("ldap.EmptyUrlsRuntimeException"));
		}catch(LDAPConnectionFailure e){
			view.showErrorMessage($("ldap.LDAPConnectionFailure") + "\n" + e.getUrl()
			 +"\n" + e.getUser() +
			"\n " + StringUtils.join(e.getDomains(), "; "));
		}catch(InvalidUrlRuntimeException e){
			view.showErrorMessage($("ldap.InvalidUrlRuntimeException") + ": " + e.getUrl());
		}
	}

	public String getAuthenticationResultMessage(LDAPServerConfiguration ldapServerConfiguration,
			LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
		LDAPServices ldapServices = new LDAPServices();
		LdapContext ctx = ldapServices.connectToLDAP(ldapServerConfiguration.getDomains(), ldapServerConfiguration.getUrls(),
				ldapUserSyncConfiguration.getUser(), ldapUserSyncConfiguration.getPassword());
		if (ctx == null) {
			return $("ldap.authentication.fail");
		} else {
			return $("ldap.authentication.success");
		}
	}

	public String getSynchResultMessage(LDAPServerConfiguration ldapServerConfiguration,
			LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
		LDAPServices ldapServices = new LDAPServices();
		StringBuilder result = new StringBuilder();
		LdapContext ctx = ldapServices.connectToLDAP(ldapServerConfiguration.getDomains(), ldapServerConfiguration.getUrls(),
				ldapUserSyncConfiguration.getUser(), ldapUserSyncConfiguration.getPassword());
		if (ctx != null) {
			Set<LDAPGroup> groups = ldapServices.getGroupsUsingFilter(ctx, ldapUserSyncConfiguration.getGroupBaseContextList(),
					ldapUserSyncConfiguration.getGroupFilter());
			if (!groups.isEmpty()) {
				result.append($("ldap.imported.groups") + ":");
				for (LDAPGroup group : groups) {
					result.append("\t" + group.getSimpleName());
				}
			}

			Set<String> users = ldapServices.getUsersUsingFilter(ldapServerConfiguration.getDirectoryType(), ctx,
					ldapUserSyncConfiguration.getUsersWithoutGroupsBaseContextList(), ldapUserSyncConfiguration.getUserFilter());
			if (!users.isEmpty()) {
				result.append($("ldap.imported.users") + ":\n\t");
				result.append(StringUtils.join(users, "\n\t"));
			}
		}

		return result.toString();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return userServices().has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_LDAP);
	}

}
