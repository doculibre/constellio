package com.constellio.model.conf.ldap.config;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Duration;

import com.constellio.model.conf.ldap.RegexFilter;
import com.constellio.model.services.security.authentification.LDAPAuthenticationService;

public class LDAPUserSyncConfiguration {
	transient RegexFilter userFilter;

	transient RegexFilter groupFilter;

	Duration durationBetweenExecution;

	private List<String> selectedCollectionsCodes;

	AzureADUserSynchConfig azurUserSynchConfig = new AzureADUserSynchConfig();
	NonAzureADUserSynchConfig nonAzureADUserSynchConfig = new NonAzureADUserSynchConfig();

	public LDAPUserSyncConfiguration(String user, String password,
			RegexFilter userFilter, RegexFilter groupFilter, Duration durationBetweenExecution,
			List<String> groupBaseContextList, List<String> usersWithoutGroupsBaseContextList) {
		this(user, password, userFilter, groupFilter, durationBetweenExecution, groupBaseContextList,
				usersWithoutGroupsBaseContextList, new ArrayList<String>());
	}

	public LDAPUserSyncConfiguration(String user, String password,
			RegexFilter userFilter, RegexFilter groupFilter, Duration durationBetweenExecution,
			List<String> groupBaseContextList, List<String> usersWithoutGroupsBaseContextList,
			List<String> selectedCollectionsCodes) {
		this.nonAzureADUserSynchConfig.user = user;
		this.nonAzureADUserSynchConfig.password = password;
		this.userFilter = userFilter;
		this.groupFilter = groupFilter;
		this.durationBetweenExecution = durationBetweenExecution;
		this.nonAzureADUserSynchConfig.groupBaseContextList = groupBaseContextList;
		this.nonAzureADUserSynchConfig.usersWithoutGroupsBaseContextList = usersWithoutGroupsBaseContextList;
		this.selectedCollectionsCodes = selectedCollectionsCodes;
	}

	public LDAPUserSyncConfiguration(AzureADUserSynchConfig azurUserSynchConfig,
			RegexFilter userFilter, RegexFilter groupFilter, Duration durationBetweenExecution,
			List<String> selectedCollectionsCodes) {
		this.azurUserSynchConfig.applicationKey = azurUserSynchConfig.applicationKey;
		this.azurUserSynchConfig.setClientId(azurUserSynchConfig.getClientId());
        this.azurUserSynchConfig.setGroupsFilter(azurUserSynchConfig.getGroupsFilter());
        this.azurUserSynchConfig.setUsersFilter(azurUserSynchConfig.getUsersFilter());
        this.azurUserSynchConfig.setUserGroups(azurUserSynchConfig.getUserGroups());
		this.userFilter = userFilter;
		this.groupFilter = groupFilter;
		this.durationBetweenExecution = durationBetweenExecution;
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

	public String getClientSecret(){
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

    public List<String> getUserGroups() {
        return this.azurUserSynchConfig.getUserGroups();
    }
}
