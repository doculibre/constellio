package com.constellio.model.conf.ldap.config;

import java.util.List;

public class AzureADUserSynchConfig {
	String applicationKey;
	private String clientId;
    private String groupsFilter;
    private String usersFilter;
    private List<String> userGroups;

	public String getApplicationKey() {
		return applicationKey;
	}

	public AzureADUserSynchConfig setApplicationKey(String applicationKey) {
		this.applicationKey = applicationKey;
		return this;
	}

	public String getClientId() {
		return clientId;
	}

	public AzureADUserSynchConfig setClientId(String clientId) {
		this.clientId = clientId;
		return this;
	}

    public String getGroupsFilter() {
        return groupsFilter;
    }

    public AzureADUserSynchConfig setGroupsFilter(String groupsFilter) {
        this.groupsFilter = groupsFilter;
        return this;
    }

    public String getUsersFilter() {
        return usersFilter;
    }

    public AzureADUserSynchConfig setUsersFilter(String usersFilter) {
        this.usersFilter = usersFilter;
        return this;
    }

    public List<String> getUserGroups() {
        return userGroups;
    }

    public AzureADUserSynchConfig setUserGroups(List<String> userGroups) {
        this.userGroups = userGroups;
        return this;
    }
}
