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
package com.constellio.model.conf.ldap;

import java.io.Serializable;
import java.util.List;

import com.constellio.model.services.security.authentification.LDAPAuthenticationService;
import org.joda.time.Duration;

public class LDAPUserSyncConfiguration{

	String user;

	String password;

	transient RegexFilter userFilter;

	transient RegexFilter groupFilter;

	Duration durationBetweenExecution;

	private final List<String> groupBaseContextList;

	private List<String> usersWithoutGroupsBaseContextList;

	public LDAPUserSyncConfiguration(String user, String password,
			RegexFilter userFilter, RegexFilter groupFilter, Duration durationBetweenExecution,
			List<String> groupBaseContextList, List<String> usersWithoutGroupsBaseContextList) {
		this.user = user;
		this.password = password;
		this.userFilter = userFilter;
		this.groupFilter = groupFilter;
		this.durationBetweenExecution = durationBetweenExecution;
		this.groupBaseContextList = groupBaseContextList;
		this.usersWithoutGroupsBaseContextList = usersWithoutGroupsBaseContextList;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public boolean isUserAccepted(String userName) {
		if (userName == null){
			return false;
		}
		if(userName.equals(LDAPAuthenticationService.ADMIN_USERNAME)){
			return false;
		}
		if (this.userFilter == null){
			return true;
		}
		return this.userFilter.isAccepted(userName);
	}

	public boolean isGroupAccepted(String groupName) {
		if (this.groupFilter == null){
			return true;
		}
		return this.groupFilter.isAccepted(groupName);
	}

	public Duration getDurationBetweenExecution() {
		return durationBetweenExecution;
	}

	public List<String> getGroupBaseContextList() {
		return groupBaseContextList;
	}

	public List<String> getUsersWithoutGroupsBaseContextList() {
		return usersWithoutGroupsBaseContextList;
	}

	public String getUsersFilterAcceptanceRegex() {
		if (this.userFilter == null){
			return "";
		}
		return this.userFilter.getAcceptedRegex();
	}

	public String getUsersFilterRejectionRegex() {
		if (this.userFilter == null){
			return "";
		}
		return this.userFilter.getRejectedRegex();
	}

	public String getGroupsFilterAcceptanceRegex() {
		if (this.groupFilter == null){
			return "";
		}
		return this.groupFilter.getAcceptedRegex();
	}

	public String getGroupsFilterRejectionRegex() {
		if (this.groupFilter == null){
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
}
