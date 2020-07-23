package com.constellio.model.services.users.sync;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.data.threads.ConstellioJob;
import com.constellio.data.threads.ConstellioJobManager;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.services.LDAPServices;
import com.constellio.model.conf.ldap.services.LDAPServices.LDAPUsersAndGroups;
import com.constellio.model.conf.ldap.services.LDAPServicesFactory;
import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.conf.ldap.user.LDAPUser;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.validators.EmailValidator;
import com.constellio.model.services.security.authentification.LDAPAuthenticationService;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserAddUpdateRequest;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser;
import com.constellio.model.services.users.UserUtils;
import com.google.common.base.Joiner;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LDAPUserSyncManager implements StatefulService {
	private final static Logger LOGGER = LoggerFactory.getLogger(LDAPUserSyncManager.class);
	private final LDAPConfigurationManager ldapConfigurationManager;
	UserServices userServices;
	LDAPUserSyncConfiguration userSyncConfiguration;
	LDAPServerConfiguration serverConfiguration;
	BackgroundThreadsManager backgroundThreadsManager;
	boolean processingSynchronizationOfUsers = false;
	DataLayerFactory dataLayerFactory;

	private ConstellioJobManager constellioJobManager;

	public LDAPUserSyncManager(ModelLayerFactory modelLayerFactory) {
		this.userServices = modelLayerFactory.newUserServices();
		this.dataLayerFactory = modelLayerFactory.getDataLayerFactory();
		this.ldapConfigurationManager = modelLayerFactory.getLdapConfigurationManager();
		this.constellioJobManager = modelLayerFactory.getDataLayerFactory().getConstellioJobManager();
	}

	@Override
	public void initialize() {
		this.serverConfiguration = ldapConfigurationManager.getLDAPServerConfiguration();
		this.userSyncConfiguration = ldapConfigurationManager.getLDAPUserSyncConfiguration(false);
		if (!(userSyncConfiguration == null || (userSyncConfiguration.getDurationBetweenExecution() == null
												&& userSyncConfiguration.getScheduleTime() == null))) {
			configureAndScheduleJob();
		}
	}

	public void reloadLDAPUserSynchConfiguration() {
		this.userSyncConfiguration = ldapConfigurationManager.getLDAPUserSyncConfiguration(false);
		this.serverConfiguration = ldapConfigurationManager.getLDAPServerConfiguration();
	}

	@Override
	public void close() {
	}

	private void configureAndScheduleJob() {
		//
		LDAPUserSyncManagerJob.action = new Runnable() {
			@Override
			public void run() {
				synchronizeIfPossible(new LDAPSynchProgressionInfo());
			}
		};

		//
		LDAPUserSyncManagerJob.intervals = new HashSet<Integer>();
		if (userSyncConfiguration.getDurationBetweenExecution() != null) {
			LDAPUserSyncManagerJob.intervals.add(
					new Long(userSyncConfiguration.getDurationBetweenExecution().getStandardSeconds()).intValue()
			);
		}

		//
		if (userSyncConfiguration.getScheduleTime() != null) {
			//
			final List<LocalTime> scheduleTimeList = new ArrayList(
					CollectionUtils.collect(
							userSyncConfiguration.getScheduleTime(),
							new Transformer() {
								@Override
								public Object transform(Object input) {
									return DateTimeFormat.forPattern(LDAPUserSyncConfiguration.TIME_PATTERN)
											.parseLocalTime((String) input);
								}
							}
					)
			);

			final Map<Integer, List<Integer>> minuteHoursMap = new HashMap<>();
			for (final LocalTime time : scheduleTimeList) {
				if (!minuteHoursMap.containsKey(time.getMinuteOfHour())) {
					minuteHoursMap.put(time.getMinuteOfHour(), new ArrayList<Integer>());
				}
				minuteHoursMap.get(time.getMinuteOfHour()).add(time.getHourOfDay());
			}

			LDAPUserSyncManagerJob.cronExpressions = new HashSet<>(minuteHoursMap.entrySet().size());
			for (final Map.Entry<Integer, List<Integer>> minuteHours : minuteHoursMap.entrySet()) {
				LDAPUserSyncManagerJob.cronExpressions
						.add(String.format("0 %d %s ? * *", minuteHours.getKey(), Joiner.on(",").join(minuteHours.getValue())));
			}
		}

		//
		ldapConfigurationManager
				.setNextUsersSyncFireTime(constellioJobManager.addJob(new LDAPUserSyncManagerJob(), true));
	}

	public synchronized void synchronizeIfPossible() {
		synchronizeIfPossible(null);
	}

	public synchronized void synchronizeIfPossible(LDAPSynchProgressionInfo ldapSynchProgressionInfo) {
		if (!processingSynchronizationOfUsers) {
			processingSynchronizationOfUsers = true;
			try {
				synchronize(ldapSynchProgressionInfo);
			} finally {
				processingSynchronizationOfUsers = false;
			}
		}
	}

	private synchronized void synchronize(LDAPSynchProgressionInfo ldapSynchProgressionInfo) {

		while (ReindexingServices.getReindexingInfos() != null) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		this.userSyncConfiguration = ldapConfigurationManager.getLDAPUserSyncConfiguration(true);
		this.serverConfiguration = ldapConfigurationManager.getLDAPServerConfiguration();

		List<String> usersIdsBeforeSynchronisation = getAllUsersNames();
		List<String> groupsIdsBeforeSynchronisation = getGroupsIds();

		List<String> usersIdsAfterSynchronisation = new ArrayList<>();
		List<String> groupsIdsAfterSynchronisation = new ArrayList<>();
		LDAPServices ldapServices = LDAPServicesFactory.newLDAPServices(serverConfiguration.getDirectoryType());
		List<String> selectedCollectionsCodes = userSyncConfiguration.getSelectedCollectionsCodes();

		//FIXME cas rare mais possible nom d utilisateur/de groupe non unique (se trouvant dans des urls differentes)
		for (String url : getNonEmptyUrls(serverConfiguration)) {
			LDAPUsersAndGroups importedUsersAndGroups = ldapServices
					.importUsersAndGroups(serverConfiguration, userSyncConfiguration, url);
			if (ldapSynchProgressionInfo != null) {
				ldapSynchProgressionInfo.totalGroupsAndUsers = importedUsersAndGroups.getUsers().size() +
															   importedUsersAndGroups.getGroups().size();
			}
			Set<LDAPGroup> ldapGroups = importedUsersAndGroups.getGroups();
			Set<LDAPUser> ldapUsers = importedUsersAndGroups.getUsers();

			UpdatedUsersAndGroups updatedUsersAndGroups = updateUsersAndGroups(ldapUsers, ldapGroups, selectedCollectionsCodes,
					ldapSynchProgressionInfo);

			usersIdsAfterSynchronisation.addAll(updatedUsersAndGroups.getUsersNames());
			groupsIdsAfterSynchronisation.addAll(updatedUsersAndGroups.getGroupsCodes());
		}

		//remove inexistingUsers
		List<String> removedUsersIds = (List<String>) CollectionUtils
				.subtract(usersIdsBeforeSynchronisation, usersIdsAfterSynchronisation);
		removeUsersExceptAdmins(removedUsersIds);

		//remove inexistingGroups
		List<String> removedGroupsIds = (List<String>) CollectionUtils
				.subtract(groupsIdsBeforeSynchronisation, groupsIdsAfterSynchronisation);
		removeGroups(removedGroupsIds);
	}

	private List<String> getNonEmptyUrls(LDAPServerConfiguration serverConfiguration) {
		if (serverConfiguration.getDirectoryType() == LDAPDirectoryType.AZURE_AD) {
			return Arrays.asList(new String[]{null});
		} else {
			return serverConfiguration.getUrls();
		}
	}

	private UpdatedUsersAndGroups updateUsersAndGroups(Set<LDAPUser> ldapUsers, Set<LDAPGroup> ldapGroups,
													   List<String> selectedCollectionsCodes,
													   LDAPSynchProgressionInfo ldapSynchProgressionInfo) {
		UpdatedUsersAndGroups updatedUsersAndGroups = new UpdatedUsersAndGroups();
		for (LDAPGroup ldapGroup : ldapGroups) {
			GlobalGroup group = createGlobalGroupFromLdapGroup(ldapGroup, selectedCollectionsCodes);
			try {
				userServices.addUpdateGlobalGroup(group);
				updatedUsersAndGroups.addGroupCode(group.getCode());
			} catch (Throwable e) {
				LOGGER.error("Group ignored due to error when trying to add it " + group.getCode(), e);
			}
			if (ldapSynchProgressionInfo != null) {
				ldapSynchProgressionInfo.processedGroupsAndUsers++;
			}
		}

		for (LDAPUser ldapUser : ldapUsers) {
			if (!ldapUser.getName().toLowerCase().equals("admin")) {
				UserAddUpdateRequest request = createUserCredentialsFromLdapUser(ldapUser, selectedCollectionsCodes);
				if (request.getUsername() == null) {
					LOGGER.error(
							"Invalid user ignored (missing username). Id: " + ldapUser.getId() + ", Username : " + request.getUsername());
				} else {
					try {
						// Keep locally created groups of existing users
						final List<String> newUserGlobalGroups = new ArrayList<>(request.getGlobalGroups());
						SystemWideUserInfos previousUserCredential = userServices
								.getUserInfos(request.getUsername());
						SystemWideUserInfos userCredentialByDn = userServices.getUserCredentialByDN(ldapUser.getId());
						if (previousUserCredential == null) {
							previousUserCredential = userServices.getUserCredentialByDN(ldapUser.getId());
						}
						if (previousUserCredential != null && userCredentialByDn != null
							&& !previousUserCredential.getId().equals(userCredentialByDn.getId())) {
							LOGGER.info("Two users with same DN but different username. Id: " + ldapUser.getId() + ", Usernames : " + previousUserCredential.getUsername() + " and " + userCredentialByDn.getUsername());
							try {
								LOGGER.info(
										"Attempting to delete username " + userCredentialByDn.getUsername());
								userServices.physicallyRemoveUserCredentialAndUsers(userCredentialByDn.getUsername());
							} catch (Throwable t) {
								LOGGER.info(
										"Could not delete username " + userCredentialByDn.getUsername() + ", attempting to delete " + previousUserCredential.getUsername() + " instead");
								userServices.physicallyRemoveUserCredentialAndUsers(previousUserCredential.getUsername());
								previousUserCredential = userCredentialByDn;
							}
						}
						if (previousUserCredential != null) {
							request.setServiceKey(previousUserCredential.getServiceKey());
							request.setAccessTokens(previousUserCredential.getAccessTokens());
							for (final String userGlobalGroup : previousUserCredential.getGlobalGroups()) {
								final GlobalGroup previousGlobalGroup = userServices.getNullableGroup(userGlobalGroup);
								if (previousGlobalGroup != null && previousGlobalGroup.isLocallyCreated()) {
									newUserGlobalGroups.add(previousGlobalGroup.getCode());
								}
							}
						}
						request.setGlobalGroups(newUserGlobalGroups);

						userServices.addUpdateUserCredential(request);
						updatedUsersAndGroups.addUsername(UserUtils.cleanUsername(ldapUser.getName()));
					} catch (Throwable e) {
						LOGGER.error("User ignored due to error when trying to add it " + request.getUsername(), e);
					}
				}
			}
			if (ldapSynchProgressionInfo != null) {
				ldapSynchProgressionInfo.processedGroupsAndUsers++;
			}
		}

		return updatedUsersAndGroups;
	}

	private GlobalGroup createGlobalGroupFromLdapGroup(LDAPGroup ldapGroup, List<String> selectedCollectionsCodes) {
		String code = ldapGroup.getDistinguishedName();
		String name = ldapGroup.getSimpleName();
		Set<String> usersAutomaticallyAddedToCollections;
		try {
			GlobalGroup group = userServices.getGroup(code);
			usersAutomaticallyAddedToCollections = new HashSet<>(group.getUsersAutomaticallyAddedToCollections());
			usersAutomaticallyAddedToCollections.addAll(selectedCollectionsCodes);
		} catch (UserServicesRuntimeException.UserServicesRuntimeException_NoSuchGroup e) {
			usersAutomaticallyAddedToCollections = new HashSet<>();
		}
		return userServices.createGlobalGroup(code, name, new ArrayList<>(usersAutomaticallyAddedToCollections), null,
				GlobalGroupStatus.ACTIVE, false);
	}

	private UserAddUpdateRequest createUserCredentialsFromLdapUser(LDAPUser ldapUser,
																   List<String> selectedCollectionsCodes) {
		String username = ldapUser.getName();
		String firstName = notNull(ldapUser.getGivenName());
		String lastName = notNull(ldapUser.getFamilyName());
		String email = notNull(validateEmail(ldapUser.getEmail()));
		List<String> globalGroups = new ArrayList<>();
		for (LDAPGroup ldapGroup : ldapUser.getUserGroups()) {
			String groupSimpleName = ldapGroup.getSimpleName();
			if (userSyncConfiguration.isGroupAccepted(groupSimpleName)) {
				globalGroups.add(ldapGroup.getDistinguishedName());
			}
		}
		List<String> msExchDelegateListBL = new ArrayList<>();
		if (ldapUser.getMsExchDelegateListBL() != null) {
			msExchDelegateListBL.addAll(ldapUser.getMsExchDelegateListBL());
		}
		Set<String> collections;
		try {
			SystemWideUserInfos tmpUser = userServices.getUserInfos(username);
			collections = new HashSet<>(tmpUser.getCollections());
			collections.addAll(selectedCollectionsCodes);
		} catch (UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser e) {
			collections = new HashSet<>(selectedCollectionsCodes);
		}

		UserCredentialStatus userStatus;
		if (ldapUser.getEnabled()) {
			userStatus = UserCredentialStatus.ACTIVE;
		} else {
			userStatus = UserCredentialStatus.DELETED;
		}
		UserAddUpdateRequest request = userServices.addEditRequest(username)
				.setFirstName(firstName)
				.setLastName(lastName)
				.setEmail(email)
				.setServiceKey(null)
				.setSystemAdmin(false)
				.setGlobalGroups(globalGroups)
				.setCollections(new ArrayList<>(collections))
				.setAccessTokens(Collections.<String, LocalDateTime>emptyMap())
				.setStatus(userStatus)
				.setDomain("")
				.setMsExchDelegateListBL(msExchDelegateListBL)
				.setDn(ldapUser.getId());

		try {
			SystemWideUserInfos currentUserCredential = userServices.getUserInfos(username);
			if (currentUserCredential.isSystemAdmin()) {
				request = request.setSystemAdmin(true);
			}
			request = request.setAccessTokens(currentUserCredential.getAccessTokens());
		} catch (UserServicesRuntimeException_NoSuchUser e) {
			//OK
		}

		return request;
	}

	private String validateEmail(String email) {
		if (StringUtils.isBlank(email)) {
			return email;
		} else if (!EmailValidator.isValid(email)) {
			LOGGER.warn("Invalid email set to null : " + email);
			return null;
		} else {
			return email;
		}
	}

	private String notNull(String value) {
		return (value == null) ? "" : value;
	}

	private void removeUsersExceptAdmins(List<String> removedUsersIds) {
		for (String userId : removedUsersIds) {
			if (!userId.equals(LDAPAuthenticationService.ADMIN_USERNAME)) {
				try {
					userServices.getUserConfigs(userId);

					if (!userServices.isAdminInAnyCollection(userId)) {
						SystemWideUserInfos userCredential = userServices.getUserInfos(userId);
						userServices.removeUserCredentialAndUser(userCredential);
					}
				} catch (UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser e) {
					// User no longer exists
				}
			}
		}
	}

	private void removeGroups(List<String> removedGroupsIds) {
		SystemWideUserInfos admin = userServices.getUserInfos(LDAPAuthenticationService.ADMIN_USERNAME);
		for (String groupId : removedGroupsIds) {
			GlobalGroup group = userServices.getGroup(groupId);
			userServices.logicallyRemoveGroupHierarchy(admin, group);
		}
	}

	private List<String> getGroupsIds() {
		List<String> groups = new ArrayList<>();
		List<GlobalGroup> globalGroups = userServices.getAllGroups();
		for (GlobalGroup globalGroup : globalGroups) {
			if (!globalGroup.isLocallyCreated()) {
				groups.add(globalGroup.getCode());
			}
		}
		return groups;
	}

	private List<String> getAllUsersNames() {
		List<String> usernames = new ArrayList<>();
		List<SystemWideUserInfos> userCredentials = userServices.getAllUserCredentials();//getUserCredentials();
		for (SystemWideUserInfos userCredential : userCredentials) {
			usernames.add(userCredential.getUsername());
		}
		return usernames;
	}

	public boolean isSynchronizing() {
		return this.processingSynchronizationOfUsers;
	}

	private class UpdatedUsersAndGroups {

		private Set<String> usersNames = new HashSet<>();
		private Set<String> groupsCodes = new HashSet<>();

		public void addUsername(String username) {
			usersNames.add(username);
		}

		public Set<String> getUsersNames() {
			return usersNames;
		}

		public Set<String> getGroupsCodes() {
			return groupsCodes;
		}

		public void addGroupCode(String groupCode) {
			groupsCodes.add(groupCode);
		}
	}

	public static class LDAPSynchProgressionInfo {
		int totalGroupsAndUsers = 0;
		int processedGroupsAndUsers = 0;

		public int getProgressPercentage() {
			if (totalGroupsAndUsers == 0) {
				return 0;
			} else {
				return processedGroupsAndUsers / totalGroupsAndUsers;
			}
		}
	}

	public final static class LDAPUserSyncManagerJob extends ConstellioJob {

		private static Runnable action;

		private static Set<Integer> intervals;

		private static Set<String> cronExpressions;

		private static Date startTime;

		@Override
		protected String name() {
			return LDAPUserSyncManagerJob.class.getSimpleName();
		}

		@Override
		protected Runnable action() {
			return action;
		}

		@Override
		protected boolean unscheduleOnException() {
			return false;
		}

		@Override
		protected Set<Integer> intervals() {
			return intervals;
		}

		@Override
		protected Set<String> cronExpressions() {
			return cronExpressions;
		}

	}
}
