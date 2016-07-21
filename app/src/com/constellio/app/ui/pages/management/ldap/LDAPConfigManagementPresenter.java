package com.constellio.app.ui.pages.management.ldap;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.conf.ldap.EmptyDomainsRuntimeException;
import com.constellio.model.conf.ldap.EmptyUrlsRuntimeException;
import com.constellio.model.conf.ldap.InvalidUrlRuntimeException;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.TooShortDurationRuntimeException;
import com.constellio.model.conf.ldap.services.LDAPConnectionFailure;
import com.constellio.model.conf.ldap.services.LDAPServices;
import com.constellio.model.conf.ldap.services.LDAPServicesException.CouldNotConnectUserToLDAP;
import com.constellio.model.conf.ldap.services.LDAPServicesFactory;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;

public class LDAPConfigManagementPresenter extends
										   BasePresenter<LDAPConfigManagementView> {
	private static final Logger LOGGER = LoggerFactory.getLogger(LDAPConfigManagementPresenter.class);

	public LDAPConfigManagementPresenter(LDAPConfigManagementView view) {
		super(view);
	}

	public LDAPServerConfiguration getLDAPServerConfiguration() {
		return view.getConstellioFactories().getModelLayerFactory().getLdapConfigurationManager().getLDAPServerConfiguration();
	}

	public LDAPUserSyncConfiguration getLDAPUserSyncConfiguration() {
		return view.getConstellioFactories().getModelLayerFactory().getLdapConfigurationManager()
				.getLDAPUserSyncConfiguration(true);
	}

	public void backButtonClick() {
		view.navigate().to().adminModule();
	}

	public void saveConfigurations(LDAPServerConfiguration ldapServerConfigurationVO,
			LDAPUserSyncConfiguration ldapUserSyncConfigurationVO) {
		LDAPConfigurationManager ldapConfigManager = view.getConstellioFactories().getModelLayerFactory()
				.getLdapConfigurationManager();
		try {
			ldapConfigManager.saveLDAPConfiguration(ldapServerConfigurationVO, ldapUserSyncConfigurationVO);
			view.showMessage($("ldap.config.saved"));
		} catch (TooShortDurationRuntimeException e) {
			view.showErrorMessage($("ldap.TooShortDurationRuntimeException"));
		} catch (EmptyDomainsRuntimeException e) {
			view.showErrorMessage($("ldap.EmptyDomainsRuntimeException"));
		} catch (EmptyUrlsRuntimeException e) {
			view.showErrorMessage($("ldap.EmptyUrlsRuntimeException"));
		} catch (LDAPConnectionFailure e) {
			view.showErrorMessage($("ldap.LDAPConnectionFailure") + "\n" + e.getUrl()
					+ "\n" + e.getUser() +
					"\n " + StringUtils.join(e.getDomains(), "; "));
		} catch (InvalidUrlRuntimeException e) {
			view.showErrorMessage($("ldap.InvalidUrlRuntimeException") + ": " + e.getUrl());
		}
	}

	public String getAuthenticationResultMessage(LDAPServerConfiguration ldapServerConfiguration,
			String user, String password) {
		LDAPServices ldapServices = LDAPServicesFactory.newLDAPServices(ldapServerConfiguration.getDirectoryType());
		try {
			ldapServices.authenticateUser(ldapServerConfiguration, user, password);
			return $("ldap.authentication.success");
		} catch (CouldNotConnectUserToLDAP e) {
			LOGGER.warn("Error when trying to authenticate user " + user);
			return $("ldap.authentication.fail");
		}
	}

	public String getSynchResultMessage(LDAPServerConfiguration ldapServerConfiguration,
			LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
		StringBuilder result = new StringBuilder();
		LDAPServices ldapServices = LDAPServicesFactory.newLDAPServices(ldapServerConfiguration.getDirectoryType());
		List<String> groups = ldapServices.getTestSynchronisationGroups(ldapServerConfiguration, ldapUserSyncConfiguration);
		if (groups != null && !groups.isEmpty()) {
			result.append($("ldap.imported.groups") + ":\n\t");
			result.append(StringUtils.join(groups, "\n\t"));
			result.append("\n");
		}

		List<String> users = ldapServices.getTestSynchronisationUsersNames(ldapServerConfiguration, ldapUserSyncConfiguration);
		if (users != null && !users.isEmpty()) {
			result.append($("ldap.imported.users") + ":\n\t");
			result.append(StringUtils.join(users, "\n\t"));
		}
		return result.toString();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return userServices().has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_LDAP);
	}

	public List<String> getAllCollections() {
		return appLayerFactory.getCollectionsManager().getCollectionCodesExcludingSystem();
	}

	public void typeChanged(String previousDirectoryType, String newValue) {
		typeChanged(LDAPDirectoryType.valueOf(previousDirectoryType), LDAPDirectoryType.valueOf(newValue));
	}

	public void typeChanged(LDAPDirectoryType previousDirectoryType, LDAPDirectoryType newValue) {
		switch (previousDirectoryType) {
		case AZURE_AD:
			view.updateComponents();
			break;
		case ACTIVE_DIRECTORY:
		case E_DIRECTORY:
			if (newValue == LDAPDirectoryType.AZURE_AD) {
				view.updateComponents();
			}
			break;
		default:
			throw new RuntimeException("Unsupported type " + previousDirectoryType);
		}
	}

	public List<String> getSelectedCollections() {
		return modelLayerFactory.getLdapConfigurationManager().getLDAPUserSyncConfiguration().getSelectedCollectionsCodes();
	}
}
