package com.constellio.model.conf.ldap;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.PropertiesAlteration;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.model.conf.PropertiesModelLayerConfigurationRuntimeException;
import com.constellio.model.conf.ldap.config.AzureADServerConfig;
import com.constellio.model.conf.ldap.config.AzureADUserSynchConfig;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.services.LDAPServicesImpl;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.sync.LDAPFastBind;
import com.constellio.model.services.users.sync.RuntimeNamingException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

public class LDAPConfigurationManager implements StatefulService {
	private static final Logger LOGGER = LoggerFactory.getLogger(LDAPConfigurationManager.class);

	private static final String CACHE_KEY = "configs";
	private static final String LDAP_CONFIGS = "ldapConfigs.properties";
	public static final long MIN_DURATION = 1000 * 60 * 10;//10mns
	private final ModelLayerFactory modelLayerFactory;
	LDAPUserSyncConfiguration userSyncConfiguration;
	LDAPServerConfiguration serverConfiguration;
	EncryptionServices encryptionServices;
	ConfigManager configManager;
	Date nextUsersSyncFireTime;

	public LDAPConfigurationManager(ModelLayerFactory modelLayerFactory, ConfigManager configManager) {
		this.configManager = configManager;
		this.modelLayerFactory = modelLayerFactory;
		configManager.keepInCache(LDAP_CONFIGS);
		configManager.createPropertiesDocumentIfInexistent(LDAP_CONFIGS, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				//Default values
			}
		});
	}

	@Override
	public void initialize() {

	}

	@Override
	public void close() {

	}

	public void saveLDAPConfiguration(final LDAPServerConfiguration ldapServerConfiguration,
									  final LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
		saveLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration, true);
	}

	public void saveLDAPConfiguration(final LDAPServerConfiguration ldapServerConfiguration,
									  final LDAPUserSyncConfiguration ldapUserSyncConfiguration,
									  boolean validateBeforeSave) {
		if (validateBeforeSave) {
			validateLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);
		}

		configManager.createPropertiesDocumentIfInexistent(LDAP_CONFIGS, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				//Default values
			}
		});

		configManager.updateProperties(LDAP_CONFIGS, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				if (ldapServerConfiguration.getLdapAuthenticationActive()) {
					properties.put("ldap.authentication.active", "" + ldapServerConfiguration.getLdapAuthenticationActive());
				} else {
					properties.put("ldap.authentication.active", "" + false);
				}
				LDAPDirectoryType directoryType;
				if (ldapServerConfiguration.getDirectoryType() != null) {
					properties
							.put("ldap.serverConfiguration.directoryType", ldapServerConfiguration.getDirectoryType().getCode());
					directoryType = ldapServerConfiguration.getDirectoryType();
				} else {
					directoryType = LDAPDirectoryType.ACTIVE_DIRECTORY;
				}
				if (directoryType == LDAPDirectoryType.AZURE_AD) {
					properties.put("ldap.serverConfiguration.authorityTenantId", ldapServerConfiguration.getTenantName());
					properties.put("ldap.serverConfiguration.clientId", ldapServerConfiguration.getClientId());
					properties.put("ldap.syncConfiguration.clientId", ldapUserSyncConfiguration.getClientId());
					properties.put("ldap.syncConfiguration.applicationKey", ldapUserSyncConfiguration.getClientSecret());
					if (ldapUserSyncConfiguration.getGroupsFilter() != null) {
						properties.put("ldap.syncConfiguration.groupsFilter", ldapUserSyncConfiguration.getGroupsFilter());
					}
					if (ldapUserSyncConfiguration.getUsersFilter() != null) {
						properties.put("ldap.syncConfiguration.usersFilter", ldapUserSyncConfiguration.getUsersFilter());
					}
					if (!CollectionUtils.isEmpty(ldapUserSyncConfiguration.getUserGroups())) {
						properties.put("ldap.syncConfiguration.userGroups.sharpSV",
								joinWithSharp(ldapUserSyncConfiguration.getUserGroups()));
					}
				} else {
					properties.put("ldap.serverConfiguration.urls.sharpSV", joinWithSharp(ldapServerConfiguration.getUrls()));
					properties
							.put("ldap.serverConfiguration.domains.sharpSV", joinWithSharp(ldapServerConfiguration.getDomains()));
					if (ldapServerConfiguration.getFollowReferences() != null && ldapServerConfiguration.getFollowReferences()) {
						properties.put("ldap.serverConfiguration.followReferences", "" + true);
					} else {
						properties.put("ldap.serverConfiguration.followReferences", "" + false);
					}

					if (ldapUserSyncConfiguration.getUser() != null) {
						properties.put("ldap.syncConfiguration.user.login", ldapUserSyncConfiguration.getUser());
					}

					String password = ldapUserSyncConfiguration.getPassword();
					if (password != null) {
						String encryptedPassword = password;
						if (StringUtils.isNotBlank(password)) {
							encryptedPassword = (String) modelLayerFactory.newEncryptionServices().encryptWithAppKey(password);
						}
						properties.put("ldap.syncConfiguration.user.password", encryptedPassword);
					}
					if (ldapUserSyncConfiguration.getGroupBaseContextList() != null) {
						properties.put("ldap.syncConfiguration.groupsBaseContextList.sharpSV",
								joinWithSharp(ldapUserSyncConfiguration.getGroupBaseContextList()));
					}
					if (ldapUserSyncConfiguration.getUsersWithoutGroupsBaseContextList() != null) {
						properties.put("ldap.syncConfiguration.usersWithoutGroupsBaseContextList.sharpSV",
								joinWithSharp(ldapUserSyncConfiguration.getUsersWithoutGroupsBaseContextList()));
					}
					if (ldapUserSyncConfiguration.getUserFilterGroupsList() != null) {
						properties.put("ldap.syncConfiguration.userFilterGroupsList.sharpSV",
								joinWithSharp(ldapUserSyncConfiguration.getUserFilterGroupsList()));
					}
					properties.put("ldap.syncConfiguration.membershipAutomaticDerivationActivated",
							Boolean.toString(ldapUserSyncConfiguration.isMembershipAutomaticDerivationActivated()));
				}

				properties.put("ldap.syncConfiguration.selectedCollectionsCodes.sharpSV",
						joinWithSharp(ldapUserSyncConfiguration.getSelectedCollectionsCodes()));

				if (ldapUserSyncConfiguration.getUsersFilterAcceptanceRegex() != null) {
					properties.put("ldap.syncConfiguration.userFilter.acceptedRegex",
							ldapUserSyncConfiguration.getUsersFilterAcceptanceRegex());
				}
				if (ldapUserSyncConfiguration.getUsersFilterRejectionRegex() != null) {
					properties.put("ldap.syncConfiguration.userFilter.rejectedRegex",
							ldapUserSyncConfiguration.getUsersFilterRejectionRegex());
				}
				if (ldapUserSyncConfiguration.getGroupsFilterAcceptanceRegex() != null) {
					properties.put("ldap.syncConfiguration.groupFilter.acceptedRegex",
							ldapUserSyncConfiguration.getGroupsFilterAcceptanceRegex());
				}
				if (ldapUserSyncConfiguration.getGroupsFilterRejectionRegex() != null) {
					properties.put("ldap.syncConfiguration.groupFilter.rejectedRegex",
							ldapUserSyncConfiguration.getGroupsFilterRejectionRegex());
				}

				if (ldapUserSyncConfiguration.getScheduleTime() == null || ldapUserSyncConfiguration.getScheduleTime()
						.isEmpty()) {
					properties.remove("ldap.syncConfiguration.schedule.time.sharpSV");
				} else {
					properties.put("ldap.syncConfiguration.schedule.time.sharpSV",
							joinWithSharp(ldapUserSyncConfiguration.getScheduleTime()));
				}

				if (ldapUserSyncConfiguration.getDurationBetweenExecution() != null) {
					long durationInMilli = ldapUserSyncConfiguration.getDurationBetweenExecution().getMillis();
					if (durationInMilli >= MIN_DURATION) {
						properties.put("ldap.syncConfiguration.durationBetweenExecution",
								format(durationInMilli) + "");
					} else if (durationInMilli == 0L) {
						properties.remove("ldap.syncConfiguration.durationBetweenExecution");
					} else {
						throw new TooShortDurationRuntimeException(ldapUserSyncConfiguration.getDurationBetweenExecution());
					}
				}
			}
		});
		modelLayerFactory.getLdapAuthenticationService().reloadServiceConfiguration();
		modelLayerFactory.getLdapUserSyncManager().reloadLDAPUserSynchConfiguration();
	}

	/*private void init(boolean decryptPassword) {
		if (!decryptPassword) {
			this.userSyncConfiguration = getLDAPUserSyncConfiguration(false);
			this.serverConfiguration = getLDAPServerConfiguration();
		}
		if (this.encryptionServices != null) {
			return;
		}
		try {
			this.encryptionServices = modelLayerFactory.newEncryptionServices();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
		this.userSyncConfiguration = getLDAPUserSyncConfiguration(true);
		this.serverConfiguration = getLDAPServerConfiguration();
	}*/

	private String format(long millis) {
		//format (\\d*d)(\\d*h)(\\d*mn) (d == day)
		long seconds = millis / 1000;
		long mns = seconds / 60;
		long hours = mns / 60;
		mns = mns % 60;
		long days = hours / 24;
		hours = hours % 24;
		return days + "d" + hours + "h" + mns + "mn";
	}

	private String joinWithSharp(List<String> list) {
		if (list == null) {
			return "";
		}
		return StringUtils.join(list, "#");
	}

	private void validateLDAPConfiguration(LDAPServerConfiguration configs,
										   LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
		Boolean authenticationActive = configs.getLdapAuthenticationActive();
		if (authenticationActive) {
			LDAPDirectoryType directoryType = configs.getDirectoryType();
			if (directoryType == LDAPDirectoryType.AZURE_AD) {
				validateAzurConfig(configs, ldapUserSyncConfiguration);
			} else {
				validateADAndEDirectoryConfiguration(configs, ldapUserSyncConfiguration);
			}

			if (ldapUserSyncConfiguration.getDurationBetweenExecution() != null
				&& ldapUserSyncConfiguration.getDurationBetweenExecution().getMillis() != 0L) {
				if (ldapUserSyncConfiguration.getDurationBetweenExecution().getMillis() < MIN_DURATION) {
					throw new TooShortDurationRuntimeException(ldapUserSyncConfiguration.getDurationBetweenExecution());
				}
			}
		}
	}

	private void validateADAndEDirectoryConfiguration(LDAPServerConfiguration configs,
													  LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
		boolean activeDirectory = configs.getDirectoryType().equals(LDAPDirectoryType.ACTIVE_DIRECTORY);
		for (String url : configs.getUrls()) {
			try {
				LDAPFastBind fastBind = new LDAPFastBind(url, configs.getFollowReferences(), activeDirectory);
				fastBind.close();
			} catch (RuntimeNamingException e) {
				throw new InvalidUrlRuntimeException(url, e.getMessage());
			}
		}
		if (configs.getDomains() == null || configs.getDomains().isEmpty()) {
			throw new EmptyDomainsRuntimeException();
		}
		if (configs.getUrls() == null || configs.getUrls().isEmpty()) {
			throw new EmptyUrlsRuntimeException();
		}
		LDAPServicesImpl ldapServices = new LDAPServicesImpl();
		for (String url : configs.getUrls()) {
			LdapContext ctx = null;
			try {
				ctx = ldapServices.connectToLDAP(configs.getDomains(), url, ldapUserSyncConfiguration.getUser(),
						ldapUserSyncConfiguration.getPassword(), configs.getFollowReferences(), activeDirectory);
			} finally {
				if (ctx != null) {
					try {
						ctx.close();
					} catch (NamingException e) {
						LOGGER.warn("Naming exception", e);
					}
				}
			}
		}
	}

	private void validateAzurConfig(LDAPServerConfiguration configs,
									LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
		//TODO
	}

	public LDAPServerConfiguration getLDAPServerConfiguration() {
		PropertiesConfiguration configuration = getConfigs();
		Map<String, String> configs = configuration == null ? new HashMap<String, String>() : configuration.getProperties();

		LDAPDirectoryType directoryType = getLDAPDirectoryType(configs);
		Boolean active = getBooleanValue(configs, "ldap.authentication.active", false);

		if (directoryType == LDAPDirectoryType.AZURE_AD) {
			String authorityTanentId = getString(configs, "ldap.serverConfiguration.authorityTenantId", null);
			String clientId = getString(configs, "ldap.serverConfiguration.clientId", null);
			AzureADServerConfig serverConf = new AzureADServerConfig().setAuthorityTenantId(authorityTanentId)
					.setClientId(clientId);
			return new LDAPServerConfiguration(serverConf, active);
		} else {
			List<String> urls = getSharpSeparatedValuesWithoutBlanks(configs, "ldap.serverConfiguration.urls.sharpSV",
					new ArrayList<String>());
			List<String> domains = getSharpSeparatedValuesWithoutBlanks(configs, "ldap.serverConfiguration.domains.sharpSV",
					new ArrayList<String>());

			Boolean followReferences = getBooleanValue(configs, "ldap.serverConfiguration.followReferences", false);
			return new LDAPServerConfiguration(urls, domains, directoryType, active, followReferences);
		}
	}

	public LDAPUserSyncConfiguration getLDAPUserSyncConfiguration() {
		return getLDAPUserSyncConfiguration(true);
	}

	public LDAPUserSyncConfiguration getLDAPUserSyncConfiguration(boolean decryptPassword) {
		PropertiesConfiguration configuration = getConfigs();
		Map<String, String> configs = configuration == null ? new HashMap<String, String>() : configuration.getProperties();
		RegexFilter userFilter = newRegexFilter(configs, "ldap.syncConfiguration.userFilter.acceptedRegex",
				"ldap.syncConfiguration.userFilter.rejectedRegex");
		RegexFilter groupFilter = newRegexFilter(configs, "ldap.syncConfiguration.groupFilter.acceptedRegex",
				"ldap.syncConfiguration.groupFilter.rejectedRegex");
		Duration durationBetweenExecution = newDuration(configs, "ldap.syncConfiguration.durationBetweenExecution");
		List<String> scheduleTimeList = getSharpSeparatedValuesWithoutBlanks(configs,
				"ldap.syncConfiguration.schedule.time.sharpSV", new ArrayList<String>());

		List<String> selectedCollections = getSharpSeparatedValuesWithoutBlanks(configs,
				"ldap.syncConfiguration.selectedCollectionsCodes.sharpSV", new ArrayList<String>());
		boolean membershipAutomaticDerivationActivated = getBooleanValue(configs,
				"ldap.syncConfiguration.membershipAutomaticDerivationActivated", true);
		LDAPDirectoryType directoryType = getLDAPDirectoryType(configs);
		if (directoryType == LDAPDirectoryType.AZURE_AD) {
			String applicationKey = getString(configs, "ldap.syncConfiguration.applicationKey", null);
			String synchClientId = getString(configs, "ldap.syncConfiguration.clientId", null);
			String groupsFilter = getString(configs, "ldap.syncConfiguration.groupsFilter", null);
			String usersFilter = getString(configs, "ldap.syncConfiguration.usersFilter", null);
			List<String> userGroups = getSharpSeparatedValuesWithoutBlanks(configs, "ldap.syncConfiguration.userGroups.sharpSV",
					null);
			AzureADUserSynchConfig azurConf = new AzureADUserSynchConfig()
					.setApplicationKey(applicationKey)
					.setClientId(synchClientId)
					.setGroupsFilter(groupsFilter)
					.setUsersFilter(usersFilter)
					.setUserGroups(userGroups);
			return new LDAPUserSyncConfiguration(azurConf, userFilter, groupFilter, durationBetweenExecution, scheduleTimeList,
					selectedCollections);
		} else {
			String user = getString(configs, "ldap.syncConfiguration.user.login", null);
			List<String> groupBaseContextList = getSharpSeparatedValuesWithoutBlanks(configs,
					"ldap.syncConfiguration.groupsBaseContextList.sharpSV",
					new ArrayList<String>());
			List<String> usersWithoutGroupsBaseContextList = getSharpSeparatedValuesWithoutBlanks(configs,
					"ldap.syncConfiguration.usersWithoutGroupsBaseContextList.sharpSV", new ArrayList<String>());
			List<String> userFilterGroupsList = getSharpSeparatedValuesWithoutBlanks(configs,
					"ldap.syncConfiguration.userFilterGroupsList.sharpSV", new ArrayList<String>());
			String password = getString(configs, "ldap.syncConfiguration.user.password", "");
			if (decryptPassword) {
				if (encryptionServices == null) {
					encryptionServices = modelLayerFactory.newEncryptionServices();

				}
				password = (String) encryptionServices.decryptWithAppKey(password);
			}
			return new LDAPUserSyncConfiguration(user, password, userFilter, groupFilter, durationBetweenExecution,
					scheduleTimeList,
					groupBaseContextList, usersWithoutGroupsBaseContextList, userFilterGroupsList,
					membershipAutomaticDerivationActivated, selectedCollections);
		}
	}

	private PropertiesConfiguration getConfigs() {
		return configManager.getProperties(LDAP_CONFIGS);
	}

	public boolean isLDAPAuthentication() {
		PropertiesConfiguration properties = getConfigs();
		if (properties == null) {
			return false;
		} else {
			Map<String, String> configs = properties.getProperties();
			return getBooleanValue(configs, "ldap.authentication.active", false);
		}
	}

	private boolean getBooleanValue(Map<String, String> configs, String key, boolean defaultValue) {
		String returnValueAsString = getString(configs, key, defaultValue + "");
		if (!returnValueAsString.toLowerCase().matches("true|false")) {
			throw new PropertiesModelLayerConfigurationRuntimeException.PropertiesModelLayerConfigurationRuntimeException_NotABooleanValue(
					key,
					returnValueAsString);
		}
		return Boolean.valueOf(returnValueAsString);
	}

	private String getString(Map<String, String> configs, String key, String defaultValue) {
		String value = configs.get(key);
		return value == null ? defaultValue : value;
	}

	private LDAPDirectoryType getLDAPDirectoryType(Map<String, String> configs) {
		String directoryTypeString = getString(configs, "ldap.serverConfiguration.directoryType",
				LDAPDirectoryType.ACTIVE_DIRECTORY.getCode()).toLowerCase();
		if (StringUtils.isBlank(directoryTypeString) ||
			directoryTypeString.equals(LDAPDirectoryType.ACTIVE_DIRECTORY.getCode().toLowerCase())) {
			return LDAPDirectoryType.ACTIVE_DIRECTORY;
		} else if (directoryTypeString.equals(LDAPDirectoryType.E_DIRECTORY.getCode().toLowerCase())) {
			return LDAPDirectoryType.E_DIRECTORY;
		} else if (directoryTypeString.equals(LDAPDirectoryType.AZURE_AD.getCode().toLowerCase())) {
			return LDAPDirectoryType.AZURE_AD;
		} else {
			throw new PropertiesModelLayerConfigurationRuntimeException.PropertiesModelLayerConfigurationRuntimeException_InvalidLdapType(
					directoryTypeString);
		}
	}

	private List<String> getSharpSeparatedValuesWithoutBlanks(Map<String, String> configs, String key,
															  ArrayList<String> defaultValues) {
		String allValuesString = getString(configs, key, "");
		String[] allVales = StringUtils.split(allValuesString, "#");
		if (allVales.length == 0) {
			return defaultValues;
		} else {
			List<String> returnValues = new ArrayList<>();
			for (String currentValue : allVales) {
				returnValues.add(currentValue.trim());
			}
			return returnValues;
		}
	}

	private Duration newDuration(Map<String, String> configs, String key) {
		String durationString = getString(configs, key, null);
		if (durationString != null) {
			//format (\\d*d)(\\d*h)(\\d*mn) (d == day)
			PeriodFormatter formatter = new PeriodFormatterBuilder()
					.appendDays()
					.appendLiteral("d")
					.appendHours()
					.appendLiteral("h")
					.appendMinutes()
					.appendLiteral("mn")
					.toFormatter();
			Duration duration = formatter.parsePeriod(durationString).toStandardDuration();
			return duration;
		}
		return null;
	}

	private RegexFilter newRegexFilter(Map<String, String> configs, String acceptedRegexKey, String rejectedRegexKey) {
		String acceptedRegex = getString(configs, acceptedRegexKey, null);
		String rejectedRegex = getString(configs, rejectedRegexKey, null);

		try {
			return new RegexFilter(acceptedRegex, rejectedRegex);
		} catch (PatternSyntaxException e) {
			throw new PropertiesModelLayerConfigurationRuntimeException.PropertiesModelLayerConfigurationRuntimeException_InvalidRegex(
					acceptedRegex + " or " + rejectedRegex);
		}
	}

	public Boolean idUsersSynchActivated() {
		LDAPUserSyncConfiguration config = getLDAPUserSyncConfiguration(false);
		return config != null && !(config.getDurationBetweenExecution() == null && CollectionUtils
				.isEmpty(config.getScheduleTime()));
	}

	public Date getNextUsersSyncFireTime() {
		return nextUsersSyncFireTime;
	}

	public void setNextUsersSyncFireTime(Date nextUsersSyncFireTime) {
		this.nextUsersSyncFireTime = nextUsersSyncFireTime;
	}
}
