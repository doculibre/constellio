package com.constellio.app.ui.pages.management.ldap;

import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.conf.ldap.EmptyDomainsRuntimeException;
import com.constellio.model.conf.ldap.EmptyUrlsRuntimeException;
import com.constellio.model.conf.ldap.InvalidUrlRuntimeException;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.TooShortDurationRuntimeException;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.services.LDAPConnectionFailure;
import com.constellio.model.conf.ldap.services.LDAPServices;
import com.constellio.model.conf.ldap.services.LDAPServicesException.CouldNotConnectUserToLDAP;
import com.constellio.model.conf.ldap.services.LDAPServicesFactory;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.users.sync.LDAPUserSyncManager.LDAPSynchProgressionInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class LDAPConfigManagementPresenter extends
		BasePresenter<LDAPConfigManagementView> {
	private static final Logger LOGGER = LoggerFactory.getLogger(LDAPConfigManagementPresenter.class);

	private boolean isLDAPActive = false;

	public LDAPConfigManagementPresenter(LDAPConfigManagementView view) {
		super(view);
		this.setLDAPActive(getLDAPServerConfiguration().getLdapAuthenticationActive());
	}

	public LDAPServerConfiguration getLDAPServerConfiguration() {
		return modelLayerFactory.getLdapConfigurationManager().getLDAPServerConfiguration();
	}

	public LDAPUserSyncConfiguration getLDAPUserSyncConfiguration() {
		return modelLayerFactory.getLdapConfigurationManager()
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
		} catch (Throwable e) {
			LOGGER.warn("Error when trying to save ldap config", e);
			view.showErrorMessage($("ldap.save.error") + ": " + e.getMessage());
		}
	}

	public String getAuthenticationResultMessage(LDAPServerConfiguration ldapServerConfiguration,
												 String user, String password) {
		LDAPServices ldapServices = new LDAPServicesFactory().newLDAPServices(ldapServerConfiguration.getDirectoryType());
		if (StringUtils.isBlank(user) || StringUtils.isBlank(password)) {
			return $("ldap.authentication.fail");
		}
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
		LDAPServices ldapServices = new LDAPServicesFactory().newLDAPServices(ldapServerConfiguration.getDirectoryType());
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

	public LDAPConfigManagementPresenter activateLDAPActive(boolean active) {

		String notConfigureMessage = getLDAPUserSyncConfiguration().isMinimumConfiguredMessage();
		if (!this.isLDAPActive && notConfigureMessage != null) {
			view.showMessage($("ldap.config.missingConfiguration", $(notConfigureMessage)));
		} else {
			view.showMessage($("ldap.config.activated"));
			this.isLDAPActive = active;
		}
		return this;
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

	public LDAPSynchProgressionInfo forceSynchronization() {
		if (!modelLayerFactory.getLdapUserSyncManager().isSynchronizing()) {
			LDAPSynchProgressionInfo info = new LDAPSynchProgressionInfo();
			new Thread(new ForceSynchThread(info)).start();
			return info;
		} else {
			view.showMessage($("ldap.processingSynchronization"));
			return new LDAPSynchProgressionInfo();
		}
	}

	public boolean isForceSynchVisible() {
		return !modelLayerFactory.getLdapUserSyncManager().isSynchronizing() && getLDAPServerConfiguration()
				.getLdapAuthenticationActive()
			   && getLDAPUserSyncConfiguration().getDurationBetweenExecution() != null || CollectionUtils.isNotEmpty(getLDAPUserSyncConfiguration().getScheduleTime());
	}

	public LDAPConfigManagementPresenter setLDAPActive(boolean active) {
		this.isLDAPActive = active;
		return this;
	}

	public boolean isLDAPActive() {
		return isLDAPActive;
	}

	private class ForceSynchThread implements Runnable {
		private final LDAPSynchProgressionInfo info;

		public ForceSynchThread(LDAPSynchProgressionInfo info) {
			this.info = info;
		}

		@Override
		public void run() {
			modelLayerFactory.getLdapUserSyncManager().synchronizeIfPossible(info);
		}
	}
}
