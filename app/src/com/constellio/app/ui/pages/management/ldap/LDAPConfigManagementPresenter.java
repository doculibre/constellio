package com.constellio.app.ui.pages.management.ldap;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;
import java.util.Set;

import javax.naming.ldap.LdapContext;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.conf.ldap.EmptyDomainsRuntimeException;
import com.constellio.model.conf.ldap.EmptyUrlsRuntimeException;
import com.constellio.model.conf.ldap.InvalidUrlRuntimeException;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.TooShortDurationRuntimeException;
import com.constellio.model.conf.ldap.services.LDAPConnectionFailure;
import com.constellio.model.conf.ldap.services.LDAPServicesImpl;
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
			LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
		LDAPServicesImpl ldapServices = new LDAPServicesImpl();
		boolean activeDirectory = ldapServerConfiguration.getDirectoryType().equals(LDAPDirectoryType.ACTIVE_DIRECTORY);
		LdapContext ctx = ldapServices.connectToLDAP(ldapServerConfiguration.getDomains(), ldapServerConfiguration.getUrls(),
				ldapUserSyncConfiguration.getUser(), ldapUserSyncConfiguration.getPassword(),
				ldapServerConfiguration.getFollowReferences(), activeDirectory);
		if (ctx == null) {
			return $("ldap.authentication.fail");
		} else {
			return $("ldap.authentication.success");
		}
	}

	public String getSynchResultMessage(LDAPServerConfiguration ldapServerConfiguration,
			LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
		LDAPServicesImpl ldapServices = new LDAPServicesImpl();
		StringBuilder result = new StringBuilder();
		boolean activeDirectory = ldapServerConfiguration.getDirectoryType().equals(LDAPDirectoryType.ACTIVE_DIRECTORY);
		LdapContext ctx = ldapServices.connectToLDAP(ldapServerConfiguration.getDomains(), ldapServerConfiguration.getUrls(),
				ldapUserSyncConfiguration.getUser(), ldapUserSyncConfiguration.getPassword(),
				ldapServerConfiguration.getFollowReferences(), activeDirectory);
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

	public List<String> getAllCollections() {
		return appLayerFactory.getCollectionsManager().getCollectionCodesExcludingSystem();
	}

	public boolean isFollowReferencesVisible(LDAPDirectoryType directoryType) {
		return typeNotAzur(directoryType);
	}

	private boolean typeNotAzur(LDAPDirectoryType directoryType) {
		return directoryType != LDAPDirectoryType.AZUR_AD;
	}

	public boolean isUrlsFieldVisible(LDAPDirectoryType directoryType) {
		return typeNotAzur(directoryType);
	}

	public boolean isDomainsFieldVisible(LDAPDirectoryType directoryType) {
		return typeNotAzur(directoryType);
	}

	public void typeChanged(String previousDirectoryType, String newValue) {
		typeChanged(LDAPDirectoryType.valueOf(previousDirectoryType), LDAPDirectoryType.valueOf(newValue));
	}

	public void typeChanged(LDAPDirectoryType previousDirectoryType, LDAPDirectoryType newValue) {
		switch (previousDirectoryType) {
		case AZUR_AD:
			view.refreshTypeDependantFields(newValue);
			break;
		case ACTIVE_DIRECTORY:
		case E_DIRECTORY:
			if (newValue == LDAPDirectoryType.AZUR_AD) {
				view.refreshTypeDependantFields(newValue);
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
