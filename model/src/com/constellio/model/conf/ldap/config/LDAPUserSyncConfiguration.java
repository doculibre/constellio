package com.constellio.model.conf.ldap.config;

import com.constellio.model.conf.ldap.RegexFilter;
import com.constellio.model.services.security.authentification.LDAPAuthenticationService;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.List;

public class LDAPUserSyncConfiguration {

	public static final String TIME_PATTERN = "HH:mm";

	transient RegexFilter userFilter;

	transient RegexFilter groupFilter;

	Duration durationBetweenExecution;

	private List<String> scheduleTime;

	private List<String> selectedCollectionsCodes;

	AzureADUserSynchConfig azurUserSynchConfig = new AzureADUserSynchConfig();
	NonAzureADUserSynchConfig nonAzureADUserSynchConfig = new NonAzureADUserSynchConfig();

	boolean membershipAutomaticDerivationActivated = true;

	public LDAPUserSyncConfiguration(String user, String password,
									 RegexFilter userFilter, RegexFilter groupFilter, Duration durationBetweenExecution,
									 List<String> scheduleTime,
									 List<String> groupBaseContextList, List<String> usersWithoutGroupsBaseContextList,
									 List<String> userFilterGroupsList,
									 boolean membershipAutomaticDerivationActivated) {
		this(user, password, userFilter, groupFilter, durationBetweenExecution, scheduleTime, groupBaseContextList,
				usersWithoutGroupsBaseContextList, userFilterGroupsList, membershipAutomaticDerivationActivated, new ArrayList<String>());
	}

	public LDAPUserSyncConfiguration(String user, String password,
									 RegexFilter userFilter, RegexFilter groupFilter, Duration durationBetweenExecution,
									 List<String> scheduleTime,
									 List<String> groupBaseContextList, List<String> usersWithoutGroupsBaseContextList,
									 List<String> userFilterGroupsList, boolean membershipAutomaticDerivationActivated,
									 List<String> selectedCollectionsCodes) {
		this.nonAzureADUserSynchConfig.user = user;
		this.nonAzureADUserSynchConfig.password = password;
		this.userFilter = userFilter;
		this.groupFilter = groupFilter;
		this.durationBetweenExecution = durationBetweenExecution;
		this.scheduleTime = scheduleTime;
		this.nonAzureADUserSynchConfig.groupBaseContextList = groupBaseContextList;
		this.nonAzureADUserSynchConfig.usersWithoutGroupsBaseContextList = usersWithoutGroupsBaseContextList;
		this.selectedCollectionsCodes = selectedCollectionsCodes;
		this.nonAzureADUserSynchConfig.userFilterGroupsList = userFilterGroupsList;
		this.membershipAutomaticDerivationActivated = membershipAutomaticDerivationActivated;
	}

	public LDAPUserSyncConfiguration(AzureADUserSynchConfig azurUserSynchConfig,
									 RegexFilter userFilter, RegexFilter groupFilter, Duration durationBetweenExecution,
									 List<String> scheduleTime,
									 List<String> selectedCollectionsCodes) {
		this.azurUserSynchConfig.applicationKey = azurUserSynchConfig.applicationKey;
		this.azurUserSynchConfig.setClientId(azurUserSynchConfig.getClientId());
		this.azurUserSynchConfig.setGroupsFilter(azurUserSynchConfig.getGroupsFilter());
		this.azurUserSynchConfig.setUsersFilter(azurUserSynchConfig.getUsersFilter());
		this.azurUserSynchConfig.setUserGroups(azurUserSynchConfig.getUserGroups());
		this.userFilter = userFilter;
		this.groupFilter = groupFilter;
		this.durationBetweenExecution = durationBetweenExecution;
		this.scheduleTime = scheduleTime;
		this.selectedCollectionsCodes = selectedCollectionsCodes;
	}

	public String getUser() {
		return nonAzureADUserSynchConfig.user;
	}

	public String getPassword() {
		return nonAzureADUserSynchConfig.password;
	}

	public boolean isUserAccepted(String userName) {
		if (userName == null) {
			return false;
		}
		if (userName.equals(LDAPAuthenticationService.ADMIN_USERNAME)) {
			return false;
		}
		if (this.userFilter == null) {
			return true;
		}
		return this.userFilter.isAccepted(userName);
	}

	public boolean isGroupAccepted(String groupName) {
		if (groupName == null) {
			return false;
		}
		if (this.groupFilter == null) {
			return true;
		}
		return this.groupFilter.isAccepted(groupName);
	}

	public Duration getDurationBetweenExecution() {
		return durationBetweenExecution;
	}

	public void setDurationBetweenExecution(Duration durationBetweenExecution) {
		this.durationBetweenExecution = durationBetweenExecution;
	}

	public List<String> getScheduleTime() {
		return scheduleTime;
	}

	public void setScheduleTime(List<String> scheduleTime) {
		this.scheduleTime = scheduleTime;
	}

	public List<String> getGroupBaseContextList() {
		return nonAzureADUserSynchConfig.groupBaseContextList;
	}

	public List<String> getUsersWithoutGroupsBaseContextList() {
		return nonAzureADUserSynchConfig.usersWithoutGroupsBaseContextList;
	}

	public String getUsersFilterAcceptanceRegex() {
		if (this.userFilter == null) {
			return "";
		}
		return this.userFilter.getAcceptedRegex();
	}

	public String getUsersFilterRejectionRegex() {
		if (this.userFilter == null) {
			return "";
		}
		return this.userFilter.getRejectedRegex();
	}

	public String getGroupsFilterAcceptanceRegex() {
		if (this.groupFilter == null) {
			return "";
		}
		return this.groupFilter.getAcceptedRegex();
	}

	public String getGroupsFilterRejectionRegex() {
		if (this.groupFilter == null) {
			return "";
		}
		return this.groupFilter.getRejectedRegex();
	}

	public RegexFilter getUserFilter() {
		return userFilter;
	}

	public RegexFilter getGroupFilter() {
		return groupFilter;
	}

	public List<String> getSelectedCollectionsCodes() {
		return selectedCollectionsCodes;
	}

	public String getClientSecret() {
		return this.azurUserSynchConfig.getApplicationKey();
	}

	public String getClientId() {
		return this.azurUserSynchConfig.getClientId();
	}

	public String getGroupsFilter() {
		return this.azurUserSynchConfig.getGroupsFilter();
	}

	public String getUsersFilter() {
		return this.azurUserSynchConfig.getUsersFilter();
	}

	public boolean isMembershipAutomaticDerivationActivated() {
		return membershipAutomaticDerivationActivated;
	}

	public List<String> getUserGroups() {
		return this.azurUserSynchConfig.getUserGroups();
	}

	public List<String> getUserFilterGroupsList() {
		return nonAzureADUserSynchConfig.userFilterGroupsList;
	}

	public String isMinimumConfiguredMessage() {
		String notConfigured = null;
		if(this.getUser() == null || this.getUser().isEmpty()){
			notConfigured = "ldap.syncConfiguration.user.login";
		}
		else if (this.getPassword() == null || this.getPassword().isEmpty()){
			notConfigured = "ldap.syncConfiguration.user.password";
		}
		return notConfigured;
	}
}
