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
package com.constellio.model.services.users.sync;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.threads.BackgroundThreadConfiguration;
import com.constellio.data.threads.BackgroundThreadExceptionHandling;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.services.LDAPServices;
import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.conf.ldap.user.LDAPUser;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.security.authentification.LDAPAuthenticationService;
import com.constellio.model.services.users.GlobalGroupsManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException;

public class LDAPUserSyncManager implements StatefulService {

	private final LDAPConfigurationManager ldapConfigurationManager;
	UserServices userServices;
	GlobalGroupsManager globalGroupsManager;
	LDAPUserSyncConfiguration userSyncConfiguration;
	LDAPServerConfiguration serverConfiguration;
	BackgroundThreadsManager backgroundThreadsManager;

	public LDAPUserSyncManager(UserServices userServices, GlobalGroupsManager globalGroupsManager,
			LDAPConfigurationManager ldapConfigurationManager, BackgroundThreadsManager backgroundThreadsManager) {
		this.userServices = userServices;
		this.globalGroupsManager = globalGroupsManager;
		this.ldapConfigurationManager = ldapConfigurationManager;
		this.backgroundThreadsManager = backgroundThreadsManager;
	}

	@Override
	public void initialize() {
		this.userSyncConfiguration = ldapConfigurationManager.getLDAPUserSyncConfiguration();
		this.serverConfiguration = ldapConfigurationManager.getLDAPServerConfiguration();
		if (userSyncConfiguration != null && userSyncConfiguration.getDurationBetweenExecution() != null) {
			configureBackgroundThread();
		}
	}

	public void reloadLDAPUserSynchConfiguration() {
		this.userSyncConfiguration = ldapConfigurationManager.getLDAPUserSyncConfiguration();
		this.serverConfiguration = ldapConfigurationManager.getLDAPServerConfiguration();
	}

	@Override
	public void close() {
	}

	void configureBackgroundThread() {

		Runnable synchronizeAction = new Runnable() {
			@Override
			public void run() {
				synchronize();
			}
		};

		backgroundThreadsManager.configure(BackgroundThreadConfiguration
				.repeatingAction("UserSyncManager", synchronizeAction)
				.handlingExceptionWith(BackgroundThreadExceptionHandling.CONTINUE)
				.executedEvery(userSyncConfiguration.getDurationBetweenExecution()));
	}

	public void synchronize() {
		List<String> usersIdsBeforeSynchronisation = getTousNomsUtilisateurs();
		List<String> groupsIdsBeforeSynchronisation = getGroupsIds();

		List<String> usersIdsAfterSynchronisation = new ArrayList<>();
		List<String> groupsIdsAfterSynchronisation = new ArrayList<>();
		LDAPServices ldapServices = new LDAPServices();
		List<String> selectedCollectionsCodes = userSyncConfiguration.getSelectedCollectionsCodes();

		//FIXME cas rare mais possible nom d utilisateur/de groupe non unique (se trouvant dans des urls differentes)
		for (String url : serverConfiguration.getUrls()) {
			LdapContext ldapContext = ldapServices.connectToLDAP(serverConfiguration.getDomains(), url, userSyncConfiguration.getUser(),
					userSyncConfiguration.getPassword());
			Set<LDAPGroup> ldapGroups = ldapServices.getAllGroups(ldapContext, userSyncConfiguration.getGroupBaseContextList());
			ldapGroups = getAcceptedGroups(ldapGroups);

			List<LDAPUser> ldapUsers = getAcceptedUsersFromGroups(ldapGroups, ldapContext);

			List<LDAPUser> usersWithoutGroups = getAcceptedUsersNotLinkedToGroups(ldapContext);

			Set<LDAPGroup> ldapGroupsFromUsers = getGroupsFromUser(usersWithoutGroups);
			ldapGroups.addAll(ldapGroupsFromUsers);

			//groups that are retrieved from users
			ldapGroupsFromUsers = getGroupsFromUser(ldapUsers);
			ldapGroups.addAll(ldapGroupsFromUsers);

			ldapUsers.addAll(usersWithoutGroups);

			UpdatedUsersAndGroups updatedUsersAndGroups = updateUsersAndGroups(ldapUsers, ldapGroups, selectedCollectionsCodes);

			usersIdsAfterSynchronisation.addAll(updatedUsersAndGroups.getUsersNames());
			groupsIdsAfterSynchronisation.addAll(updatedUsersAndGroups.getGroupsCodes());

			try {
				ldapContext.close();
			} catch (NamingException e) {
				e.printStackTrace();
			}

		}

		//remove inexistingUsers
		List<String> removedUsersIds = (List<String>) CollectionUtils
				.subtract(usersIdsBeforeSynchronisation, usersIdsAfterSynchronisation);
		removeUsersExceptAdmin(removedUsersIds);

		//remove inexistingGroups
		List<String> removedGroupsIds = (List<String>) CollectionUtils
				.subtract(groupsIdsBeforeSynchronisation, groupsIdsAfterSynchronisation);
		removeGroups(removedGroupsIds);
	}

	private Set<LDAPGroup> getGroupsFromUser(List<LDAPUser> users) {
		Set<LDAPGroup> returnSet = new HashSet<>();
		for (LDAPUser user : users) {
			returnSet.addAll(user.getUserGroups());
		}
		return returnSet;
	}

	private UpdatedUsersAndGroups updateUsersAndGroups(List<LDAPUser> ldapUsers, Set<LDAPGroup> ldapGroups, List<String> selectedCollectionsCodes) {
		UpdatedUsersAndGroups updatedUsersAndGroups = new UpdatedUsersAndGroups();
		for (LDAPGroup ldapGroup : ldapGroups) {
			GlobalGroup group = createGlobalGroupFromLdapGroup(ldapGroup, selectedCollectionsCodes);
			userServices.addUpdateGlobalGroup(group);
			updatedUsersAndGroups.addGroupCode(group.getCode());
		}

		for (LDAPUser ldapUser : ldapUsers) {
			if (!ldapUser.getName().toLowerCase().equals("admin")) {
				UserCredential userCredential = createUserCredentialsFromLdapUser(ldapUser, selectedCollectionsCodes);
				userServices.addUpdateUserCredential(userCredential);
				updatedUsersAndGroups.addUsername(ldapUser.getName());
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
		return new GlobalGroup(code, name, new ArrayList<>(usersAutomaticallyAddedToCollections), null, GlobalGroupStatus.ACTIVE);
	}

	private UserCredential createUserCredentialsFromLdapUser(LDAPUser ldapUser, List<String> selectedCollectionsCodes) {
		String username = ldapUser.getName();
		String firstName = notNull(ldapUser.getGivenName());
		String lastName = notNull(ldapUser.getFamilyName());
		String email = notNull(ldapUser.getEmail());
		List<String> globalGroups = new ArrayList<>();
		for (LDAPGroup ldapGroup : ldapUser.getUserGroups()) {
			String groupSimpleName = ldapGroup.getSimpleName();
			if (userSyncConfiguration.isGroupAccepted(groupSimpleName)) {
				globalGroups.add(ldapGroup.getDistinguishedName());
			}
		}
		Set<String> collections;
		try {
			UserCredential tmpUser = userServices.getUser(username);
			collections = new HashSet<>(tmpUser.getCollections());
			collections.addAll(selectedCollectionsCodes);
		} catch (UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser e) {
			collections = new HashSet<>();
		}

		UserCredentialStatus userStatus;
		if (ldapUser.getEnabled()) {
			userStatus = UserCredentialStatus.ACTIVE;
		} else {
			userStatus = UserCredentialStatus.DELETED;
		}
		UserCredential returnUserCredentials = new UserCredential(username, firstName, lastName, email, globalGroups,
				new ArrayList<>(collections), userStatus);
		return returnUserCredentials;
	}

	private String notNull(String value) {
		return (value == null) ? "" : value;
	}

	public List<LDAPUser> getAcceptedUsersFromGroups(Set<LDAPGroup> ldapGroups, LdapContext ldapContext) {
		List<LDAPUser> returnUsers = new ArrayList<>();
		Set<String> groupsMembersIds = new HashSet<>();
		LDAPServices ldapServices = new LDAPServices();
		for (LDAPGroup group : ldapGroups) {
			List<String> usersToAdd = group.getMembers();
			groupsMembersIds.addAll(usersToAdd);
		}
		LDAPDirectoryType directoryType = serverConfiguration.getDirectoryType();
		for (String memberId : groupsMembersIds) {
			if (ldapServices.isUser(directoryType, memberId, ldapContext)) {
				LDAPUser ldapUser = ldapServices.getUser(directoryType, memberId,
						ldapContext);
				String userName = ldapUser.getName();
				if (userSyncConfiguration.isUserAccepted(userName)) {
					returnUsers.add(ldapUser);
				}
				removeNonAcceptedGroups(ldapUser);
			}
		}
		return returnUsers;
	}

	private List<LDAPUser> getAcceptedUsersNotLinkedToGroups(LdapContext ldapContext) {
		List<LDAPUser> returnUsers = new ArrayList<>();
		if (userSyncConfiguration.getUsersWithoutGroupsBaseContextList() == null || userSyncConfiguration
				.getUsersWithoutGroupsBaseContextList().isEmpty()) {
			return returnUsers;
		}
		Set<String> usersIds = new HashSet<>();
		LDAPServices ldapServices = new LDAPServices();
		for (String baseContextName : userSyncConfiguration.getUsersWithoutGroupsBaseContextList()) {
			List<String> currentUsersIds;
			try {
				currentUsersIds = ldapServices
						.searchUsersIdsFromContext(serverConfiguration.getDirectoryType(), ldapContext, baseContextName);
			} catch (NamingException e) {
				throw new RuntimeException(e);
			}
			usersIds.addAll(currentUsersIds);
		}
		//Accepted users:
		for (String userId : usersIds) {
			String userName = ldapServices.extractUsername(userId);
			if (userSyncConfiguration.isUserAccepted(userName)) {
				LDAPUser ldapUser = ldapServices.getUser(serverConfiguration.getDirectoryType(), userId,
						ldapContext);
				removeNonAcceptedGroups(ldapUser);
				returnUsers.add(ldapUser);
			}
		}
		return returnUsers;
	}

	private void removeNonAcceptedGroups(LDAPUser ldapUser) {
		CollectionUtils.filter(ldapUser.getUserGroups(), new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				LDAPGroup group = (LDAPGroup) object;
				return userSyncConfiguration.isGroupAccepted(group.getSimpleName());
			}
		});
	}

	private void removeUsersExceptAdmin(List<String> removedUsersIds) {
		for (String userId : removedUsersIds) {
			if (!userId.equals(LDAPAuthenticationService.ADMIN_USERNAME)) {
				UserCredential userCredential = userServices.getUser(userId);
				userServices.removeUserCredentialAndUser(userCredential);
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

	private List<String> getTousNomsUtilisateurs() {
		List<String> usernames = new ArrayList<>();
		List<UserCredential> userCredentials = userServices.getAllUserCredentials();//getUserCredentials();
		for (UserCredential userCredential : userCredentials) {
			usernames.add(userCredential.getUsername());
		}
		return usernames;
	}

	private Set<LDAPGroup> getAcceptedGroups(Set<LDAPGroup> ldapGroups) {
		Set<LDAPGroup> returnList = new HashSet<>();
		for (LDAPGroup ldapGroup : ldapGroups) {
			String groupName = ldapGroup.getSimpleName();
			if (userSyncConfiguration.isGroupAccepted(groupName)) {
				if (!ldapGroup.getMembers().isEmpty()) {
					returnList.add(ldapGroup);
				}
			}
		}
		return returnList;
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
}
