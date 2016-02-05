package com.constellio.model.conf.ldap;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Duration;

import com.constellio.model.services.security.authentification.LDAPAuthenticationService;

public class LDAPUserSyncConfiguration {

	String user;

	String password;

	transient RegexFilter userFilter;

	transient RegexFilter groupFilter;

	Duration durationBetweenExecution;

	private final List<String> groupBaseContextList;

	private List<String> usersWithoutGroupsBaseContextList;

	private List<String> selectedCollectionsCodes;

	public LDAPUserSyncConfiguration(String user, String password,
									 RegexFilter userFilter, RegexFilter groupFilter, Duration durationBetweenExecution,
									 List<String> groupBaseContextList, List<String> usersWithoutGroupsBaseContextList) {
		this(user, password, userFilter, groupFilter, durationBetweenExecution, groupBaseContextList, usersWithoutGroupsBaseContextList, new ArrayList<String>());
	}

	public LDAPUserSyncConfiguration(String user, String password,
			RegexFilter userFilter, RegexFilter groupFilter, Duration durationBetweenExecution,
			List<String> groupBaseContextList, List<String> usersWithoutGroupsBaseContextList, List<String> selectedCollectionsCodes) {
		this.user = user;
		this.password = password;
		this.userFilter = userFilter;
		this.groupFilter = groupFilter;
		this.durationBetweenExecution = durationBetweenExecution;
		this.groupBaseContextList = groupBaseContextList;
		this.usersWithoutGroupsBaseContextList = usersWithoutGroupsBaseContextList;
		this.selectedCollectionsCodes = selectedCollectionsCodes;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
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
		return groupBaseContextList;
	}

	public List<String> getUsersWithoutGroupsBaseContextList() {
		return usersWithoutGroupsBaseContextList;
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
}
