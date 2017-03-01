package com.constellio.model.services.users.sync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.threads.BackgroundThreadConfiguration;
import com.constellio.data.threads.BackgroundThreadExceptionHandling;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.services.LDAPServices;
import com.constellio.model.conf.ldap.services.LDAPServices.LDAPUsersAndGroups;
import com.constellio.model.conf.ldap.services.LDAPServicesFactory;
import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.conf.ldap.user.LDAPUser;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.schemas.validators.EmailValidator;
import com.constellio.model.services.security.authentification.LDAPAuthenticationService;
import com.constellio.model.services.users.GlobalGroupsManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser;
import com.constellio.model.services.users.UserUtils;

public class LDAPUserSyncManager implements StatefulService {
	private final static Logger LOGGER = LoggerFactory.getLogger(LDAPUserSyncManager.class);
	private final LDAPConfigurationManager ldapConfigurationManager;
	UserServices userServices;
	GlobalGroupsManager globalGroupsManager;
	LDAPUserSyncConfiguration userSyncConfiguration;
	LDAPServerConfiguration serverConfiguration;
	BackgroundThreadsManager backgroundThreadsManager;
	boolean processingSynchronizationOfUsers = false;

	public LDAPUserSyncManager(UserServices userServices, GlobalGroupsManager globalGroupsManager,
			LDAPConfigurationManager ldapConfigurationManager, BackgroundThreadsManager backgroundThreadsManager) {
		this.userServices = userServices;
		this.globalGroupsManager = globalGroupsManager;
		this.ldapConfigurationManager = ldapConfigurationManager;
		this.backgroundThreadsManager = backgroundThreadsManager;
	}

	@Override
	public void initialize() {
		this.userSyncConfiguration = ldapConfigurationManager.getLDAPUserSyncConfiguration(false);
		this.serverConfiguration = ldapConfigurationManager.getLDAPServerConfiguration();
		if (userSyncConfiguration != null && userSyncConfiguration.getDurationBetweenExecution() != null) {
			configureBackgroundThread();
		}
	}

	public void reloadLDAPUserSynchConfiguration() {
		this.userSyncConfiguration = ldapConfigurationManager.getLDAPUserSyncConfiguration(false);
		this.serverConfiguration = ldapConfigurationManager.getLDAPServerConfiguration();
	}

	@Override
	public void close() {
	}

	void configureBackgroundThread() {

		Runnable synchronizeAction = new Runnable() {
			@Override
			public void run() {
				synchronizeIfPossible(new LDAPSynchProgressionInfo());
			}
		};

		backgroundThreadsManager.configure(BackgroundThreadConfiguration
				.repeatingAction("UserSyncManager", synchronizeAction)
				.handlingExceptionWith(BackgroundThreadExceptionHandling.CONTINUE)
				.executedEvery(userSyncConfiguration.getDurationBetweenExecution()));
	}

	public synchronized void synchronizeIfPossible(){
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
			LDAPUsersAndGroups importedUsersAndgroups = ldapServices
					.importUsersAndGroups(serverConfiguration, userSyncConfiguration, url);
			if (ldapSynchProgressionInfo != null) {
				ldapSynchProgressionInfo.totalGroupsAndUsers = importedUsersAndgroups.getUsers().size() +
						importedUsersAndgroups.getGroups().size();
			}
			Set<LDAPGroup> ldapGroups = importedUsersAndgroups.getGroups();
			Set<LDAPUser> ldapUsers = importedUsersAndgroups.getUsers();

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
			List<String> selectedCollectionsCodes, LDAPSynchProgressionInfo ldapSynchProgressionInfo) {
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
				UserCredential userCredential = createUserCredentialsFromLdapUser(ldapUser, selectedCollectionsCodes);
				if (userCredential.getUsername() == null) {
					LOGGER.error("Invalid user ignored (missing username). Id: " + ldapUser.getId() + ", Username : " + userCredential.getUsername());
				} else {
					try {
						userServices.addUpdateUserCredential(userCredential);
						updatedUsersAndGroups.addUsername(UserUtils.cleanUsername(ldapUser.getName()));
					} catch (Throwable e) {
						LOGGER.error("User ignored due to error when trying to add it " + userCredential.getUsername(), e);
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
		return userServices.createGlobalGroup(code, name, new ArrayList<>(usersAutomaticallyAddedToCollections), null, GlobalGroupStatus.ACTIVE, false);
	}

	private UserCredential createUserCredentialsFromLdapUser(LDAPUser ldapUser, List<String> selectedCollectionsCodes) {
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
			UserCredential tmpUser = userServices.getUser(username);
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
		UserCredential returnUserCredentials = userServices.createUserCredential(
				username, firstName, lastName, email, globalGroups, new ArrayList<>(collections), userStatus, "",
				msExchDelegateListBL, ldapUser.getId()).withDN(ldapUser.getId());

		try {
			UserCredential currentUserCredential = userServices.getUser(username);
			if (currentUserCredential.isSystemAdmin()) {
				returnUserCredentials = returnUserCredentials.withSystemAdminPermission();
			}
			returnUserCredentials = returnUserCredentials.withAccessTokens(currentUserCredential.getAccessTokens());
		} catch (UserServicesRuntimeException_NoSuchUser e) {
			//OK
		}

		return returnUserCredentials;
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
				if (!userServices.has(userId).globalPermissionInAnyCollection(CorePermissions.MANAGE_SECURITY)) {
					UserCredential userCredential = userServices.getUser(userId);
					userServices.removeUserCredentialAndUser(userCredential);
				}
			}
		}
	}

	private void removeGroups(List<String> removedGroupsIds) {
		UserCredential admin = userServices.getUser(LDAPAuthenticationService.ADMIN_USERNAME);
		for (String groupId : removedGroupsIds) {
			GlobalGroup group = userServices.getGroup(groupId);
			userServices.logicallyRemoveGroupHierarchy(admin, group);
		}
	}

	private List<String> getGroupsIds() {
		List<String> groups = new ArrayList<>();
		List<GlobalGroup> globalGroups = globalGroupsManager.getAllGroups();
		for (GlobalGroup globalGroup : globalGroups) {
			groups.add(globalGroup.getCode());
		}
		return groups;
	}

	private List<String> getAllUsersNames() {
		List<String> usernames = new ArrayList<>();
		List<UserCredential> userCredentials = userServices.getAllUserCredentials();//getUserCredentials();
		for (UserCredential userCredential : userCredentials) {
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
}
