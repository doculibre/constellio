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
import com.constellio.model.conf.ldap.services.LDAPServicesFactory;
import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.conf.ldap.user.LDAPUser;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.GroupAddUpdateRequest;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.UserSyncMode;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.validators.EmailValidator;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserAddUpdateRequest;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser;
import com.constellio.model.services.users.UserUtils;
import com.constellio.model.services.users.sync.model.LDAPUsersAndGroups;
import com.constellio.model.services.users.sync.model.UpdatedUsersAndGroups;
import com.constellio.model.services.users.sync.report.ImportUsersAndGroupsReport;
import com.google.common.base.Joiner;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.ignite.internal.util.lang.GridFunc.asList;

public class LDAPUserSyncManager implements StatefulService {
	private final static Logger LOGGER = LoggerFactory.getLogger(LDAPUserSyncManager.class);
	private final LDAPConfigurationManager ldapConfigurationManager;
	RecordServices recordServices;
	UserServices userServices;
	LDAPUserSyncConfiguration userSyncConfiguration;
	LDAPServerConfiguration serverConfiguration;
	BackgroundThreadsManager backgroundThreadsManager;
	boolean processingSynchronizationOfUsers = false;
	DataLayerFactory dataLayerFactory;
	LDAPServicesFactory ldapServicesFactory;
	ModelLayerFactory modelLayerFactory;

	private ConstellioJobManager constellioJobManager;

	public LDAPUserSyncManager(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.recordServices = modelLayerFactory.newRecordServices();
		this.userServices = modelLayerFactory.newUserServices();
		this.dataLayerFactory = modelLayerFactory.getDataLayerFactory();
		this.ldapConfigurationManager = modelLayerFactory.getLdapConfigurationManager();
		this.constellioJobManager = modelLayerFactory.getDataLayerFactory().getConstellioJobManager();
		this.ldapServicesFactory = new LDAPServicesFactory();
	}

	public LDAPUserSyncManager(LDAPConfigurationManager ldapConfigurationManager,
							   RecordServices recordServices, DataLayerFactory dataLayerFactory,
							   UserServices userServices, ConstellioJobManager constellioJobManager,
							   LDAPServicesFactory ldapServicesFactory) {
		this.recordServices = recordServices;
		this.userServices = userServices;
		this.dataLayerFactory = dataLayerFactory;
		this.ldapConfigurationManager = ldapConfigurationManager;
		this.constellioJobManager = constellioJobManager;
		this.ldapServicesFactory = ldapServicesFactory;
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

		ImportUsersAndGroupsReport report = new ImportUsersAndGroupsReport();

		while (ReindexingServices.getReindexingInfos() != null) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		this.userSyncConfiguration = ldapConfigurationManager.getLDAPUserSyncConfiguration(true);
		this.serverConfiguration = ldapConfigurationManager.getLDAPServerConfiguration();

		List<String> selectedCollectionsCodes = userSyncConfiguration.getSelectedCollectionsCodes();
		//Get all users of collection

		List<String> usersIdsAfterSynchronisation = new ArrayList<>();
		List<String> groupsIdsAfterSynchronisation = new ArrayList<>();

		LDAPServices ldapServices = this.ldapServicesFactory.newLDAPServices(serverConfiguration.getDirectoryType());

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

			report.addGroupsFoundList(ldapGroups);
			report.addUsersFoundList(ldapUsers);

			UpdatedUsersAndGroups updatedUsersAndGroups = updateUsersAndGroups(ldapUsers, ldapGroups, selectedCollectionsCodes,
					ldapSynchProgressionInfo, report);
		}

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
													   LDAPSynchProgressionInfo ldapSynchProgressionInfo,
													   ImportUsersAndGroupsReport report) {
		UpdatedUsersAndGroups updatedUsersAndGroups = new UpdatedUsersAndGroups();
		removeGroupOnMissingSync(ldapGroups, selectedCollectionsCodes, report);
		Map<String, String> parentRelationships = groupParenting(ldapGroups);
		for (LDAPGroup ldapGroup : ldapGroups) {
			GroupAddUpdateRequest group = createGroupFromLdapGroup(ldapGroup, selectedCollectionsCodes);
			//
			try {
				userServices.execute(group);
				updatedUsersAndGroups.addGroupCode(group.getCode());
			} catch (Throwable e) {
				LOGGER.error("Group ignored due to error when trying to add it " + group.getCode(), e);
			}
			if (ldapSynchProgressionInfo != null) {
				ldapSynchProgressionInfo.processedGroupsAndUsers++;
			}
		}
		for (LDAPGroup ldapGroup : ldapGroups) {
			if (parentRelationships.containsKey(ldapGroup.getDistinguishedName())
				&& parentRelationships.get(ldapGroup.getDistinguishedName()) != null) {
				GroupAddUpdateRequest group = userServices.request(ldapGroup.getDistinguishedName());
				group.setParent(parentRelationships.get(ldapGroup.getDistinguishedName()));
				try {
					userServices.execute(group);
				} catch (Throwable e) {
					LOGGER.error("Group parent ignored due to error when trying to assign it " + group.getCode(), e);
				}
			}
		}

		List<UserCredential> notSyncedUsers = userServices.getUsersNotSynced();

		//filter out not synced
		Set<LDAPUser> syncLdapUsers = ldapUsers
				.stream()
				.filter(ldapUser ->
						notSyncedUsers
								.stream()
								.noneMatch(notSyncedUser ->
										notSyncedUser.getUsername() != null
										&& notSyncedUser.getUsername().equals(UserUtils.cleanUsername(ldapUser.getGivenName()))))
				.collect(Collectors.toSet());

		notSyncedUsers.stream().forEach(notSyncedUser -> updatedUsersAndGroups.addUsername(notSyncedUser.getUsername()));
		report.addUnsyncedUsersImportedList(notSyncedUsers);

		//update list of synchro users
		for (LDAPUser ldapUser : syncLdapUsers) {
			if (!ldapUser.getName().toLowerCase().equals("admin")) {
				com.constellio.model.services.users.UserAddUpdateRequest request = createUserCredentialsFromLdapUser(ldapUser, selectedCollectionsCodes);
				try {
					// Keep locally created groups of existing users
					SystemWideUserInfos previousUserCredential = null;
					SystemWideUserInfos userCredentialByDn = null;
					try {
						previousUserCredential = userServices
								.getUserInfos(request.getUsername());
						userCredentialByDn = userServices.getUserCredentialByDN(ldapUser.getId());
					} catch (UserServicesRuntimeException_NoSuchUser noSuchUser) {
						previousUserCredential = userServices.getUserCredentialByDN(ldapUser.getId());
					}
					if (previousUserCredential != null && userCredentialByDn != null
						&& !previousUserCredential.getId().equals(userCredentialByDn.getId())) {
						LOGGER.info("Two users with same DN but different username. Id: " + ldapUser.getId() + ", Usernames : " + previousUserCredential.getUsername() + " and " + userCredentialByDn.getUsername());
						try {
							LOGGER.info(
									"Attempting to delete username " + userCredentialByDn.getUsername());
							userServices.execute(userCredentialByDn.getUsername(), req -> req.removeFromAllCollections());
							report.addUsersRemovedList(userCredentialByDn);
						} catch (Throwable t) {
							try {
								LOGGER.info(
										"Could not delete username " + userCredentialByDn.getUsername() + ", attempting to delete " + previousUserCredential.getUsername() + " instead");
								userServices.execute(userCredentialByDn.getUsername(), req -> req.removeFromAllCollections());
								previousUserCredential = userCredentialByDn;
							} catch (Throwable t2) {
								com.constellio.model.services.users.UserAddUpdateRequest invalidUserCredential;
								if (previousUserCredential.getUsername().equalsIgnoreCase(ldapUser.getName())) {
									invalidUserCredential = userServices.addUpdate(userCredentialByDn.getUsername());
								} else {
									invalidUserCredential = userServices.addUpdate(previousUserCredential.getUsername());
									previousUserCredential = userCredentialByDn;
								}
								try {
									LOGGER.info(
											"Could not delete username " + invalidUserCredential.getUsername() + ", attempting to change DN for " + invalidUserCredential.getUsername() + " instead");
									invalidUserCredential.setDn(ldapUser.getId() + "-duplicate");
									request.setDnUnicityValidationCheck(false);
									userServices.execute(request);
								} catch (Throwable t3) {
									LOGGER.error("Unable to change DN for username " + invalidUserCredential.getUsername(), t3);
								}
							}
						}
					}
					if (previousUserCredential != null) { //User exists and is synced
						request.setCollections(selectedCollectionsCodes);
						removeUserFromGroupsMissingOnSync(selectedCollectionsCodes, request, previousUserCredential, report);
					} else {
						report.addNewUsersImportedList(ldapUser);
					}
					report.addAssignationRelationships(ldapUser.getUserGroups(), ldapUser.getGivenName());
					userServices.execute(request);
					updatedUsersAndGroups.addUsername(UserUtils.cleanUsername(ldapUser.getName()));

				} catch (Throwable e) {
					LOGGER.error("User ignored due to error when trying to add it " + request.getUsername(), e);
				}
			}
		}

		if (ldapSynchProgressionInfo != null) {
			ldapSynchProgressionInfo.processedGroupsAndUsers++;
		}

		LOGGER.info(report.reportImport(selectedCollectionsCodes));
		return updatedUsersAndGroups;
	}

	private void removeGroupOnMissingSync(Set<LDAPGroup> ldapGroups, List<String> selectedCollectionsCodes,
										  ImportUsersAndGroupsReport report) {

		for (String collection :
				selectedCollectionsCodes) {
			List<Group> syncGroups = userServices.getAllGroupsInCollections(collection);
			if (syncGroups != null) {
				List<Group> syncGroupsCodes = syncGroups.stream().filter(x -> !x.isLocallyCreated())
						.collect(Collectors.toList());
				for (Group syncGroup : syncGroupsCodes) {
					if (ldapGroups.stream().noneMatch(x -> x.getDistinguishedName().equals(syncGroup.getCode()))) {
						GroupAddUpdateRequest request = new GroupAddUpdateRequest(syncGroup.getCode());
						request.markForDeletionInCollections(asList(collection));
						userServices.execute(request);
						report.addGroupsRemovedListFromCollection(syncGroup, collection);
					}
				}
			}
		}

	}

	private void removeUserFromGroupsMissingOnSync(List<String> selectedCollectionsCodes,
												   final UserAddUpdateRequest request,
												   SystemWideUserInfos previousUserCredential,
												   ImportUsersAndGroupsReport report) {
		for (String collection : selectedCollectionsCodes) {
			previousUserCredential.getGroupCodes(collection);
			for (String groupCode : previousUserCredential.getGroupCodes(collection)) {
				if (!isGroupUnsynced(groupCode, collection)) {
					if (request.getAddToGroup() != null && !request.getAddToGroup().contains(groupCode)) {
						request.removeFromGroupOfCollection(groupCode, collection);
						report.removeAssignationRelationship(groupCode, previousUserCredential.getUsername());
					}
					if (request.getAddToGroupInCollection() != null
						&& request.getAddToGroupInCollection().containsKey(collection)
						&& !request.getAddToGroupInCollection().get(collection).contains(groupCode)) {
						request.removeFromGroupOfCollection(groupCode, collection);
						report.removeAssignationRelationship(groupCode, previousUserCredential.getUsername());
					}
				}
			}
		}
	}

	private boolean isGroupUnsynced(String groupCode, String collection) {
		Group group = userServices.getGroupInCollection(groupCode, collection);
		boolean locallyCreated = group.isLocallyCreated();
		return locallyCreated;
	}

	private GroupAddUpdateRequest createGroupFromLdapGroup(LDAPGroup ldapGroup,
														   List<String> selectedCollectionsCodes) {
		String code = ldapGroup.getDistinguishedName();
		String name = ldapGroup.getSimpleName();
		Set<String> usersAutomaticallyAddedToCollections;
		try {
			usersAutomaticallyAddedToCollections = new HashSet<>();
			usersAutomaticallyAddedToCollections.addAll(selectedCollectionsCodes);
		} catch (UserServicesRuntimeException.UserServicesRuntimeException_NoSuchGroup e) {
			usersAutomaticallyAddedToCollections = new HashSet<>();
		}

		return userServices.createGlobalGroup(code, name, new ArrayList<>(usersAutomaticallyAddedToCollections), null,
				GlobalGroupStatus.ACTIVE, false);
	}

	private Map<String, String> groupParenting(Collection<LDAPGroup> ldapGroups) {
		Map<String, String> parenting = new HashMap<>();
		for (LDAPGroup group :
				ldapGroups) {
			String parent = null;
			if (group.getMemberOf() != null && group.getMemberOf().size() > 0) {
				parent = group.getMemberOf().get(0);
			}
			parenting.put(group.getDistinguishedName(), parent);
		}
		return parenting;
	}

	private com.constellio.model.services.users.UserAddUpdateRequest createUserCredentialsFromLdapUser(
			LDAPUser ldapUser,
			List<String> selectedCollectionsCodes) {
		String username = ldapUser.getName() != null ? ldapUser.getName().toLowerCase() : null;
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
		List<String> currentCollections;
		try {
			SystemWideUserInfos tmpUser = userServices.getUserInfos(username);
			currentCollections = tmpUser.getCollections();
		} catch (UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser e) {
			currentCollections = Collections.emptyList();
		}

		UserCredentialStatus userStatus;
		if (ldapUser.getEnabled()) {
			userStatus = UserCredentialStatus.ACTIVE;
		} else {
			userStatus = UserCredentialStatus.DISABLED;
		}
		com.constellio.model.services.users.UserAddUpdateRequest request = userServices.addUpdate(username)
				.setFirstName(firstName)
				.setLastName(lastName)
				.setEmail(email)
				.setServiceKey(null)
				.setSystemAdmin(false)
				.setStatusForAllCollections(userStatus)
				.setSyncMode(UserSyncMode.SYNCED)
				.setDomain("")
				.setMsExchDelegateListBL(msExchDelegateListBL)
				.setDn(ldapUser.getId());

		for (String selectedCollectionsCode : selectedCollectionsCodes) {
			if (!currentCollections.contains(selectedCollectionsCode)) {
				request.addToCollection(selectedCollectionsCode);
			}
		}
		request.addToGroupsInCollections(globalGroups, selectedCollectionsCodes);

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

	public boolean isSynchronizing() {
		return this.processingSynchronizationOfUsers;
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