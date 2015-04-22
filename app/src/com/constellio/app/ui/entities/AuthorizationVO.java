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
package com.constellio.app.ui.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;

public class AuthorizationVO implements Serializable {
	String authId;
	List<String> users;
	List<String> groups;
	List<String> records;
	List<String> accessRoles;
	List<String> userRoles;
	List<String> userRolesTitles;
	LocalDate startDate;
	LocalDate endDate;
	boolean synched;

	public static AuthorizationVO forPrincipal(String id) {
		return new AuthorizationVO(
				Arrays.asList(id), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(),
				new ArrayList<String>(), new ArrayList<String>(), null, null, null, false);
	}

	public static AuthorizationVO forContent(String id) {
		return new AuthorizationVO(
				new ArrayList<String>(), new ArrayList<String>(), Arrays.asList(id), new ArrayList<String>(),
				new ArrayList<String>(), new ArrayList<String>(), null, null, null, false);
	}

	public AuthorizationVO(List<String> users, List<String> groups, List<String> records, List<String> accessRoles,
			List<String> userRoles, List<String> userRolesTitles, String authId, LocalDate startDate, LocalDate endDate,
			boolean synched) {
		this.users = users;
		this.records = records;
		this.accessRoles = accessRoles;
		this.userRoles = userRoles;
		this.userRolesTitles = userRolesTitles;
		this.authId = authId;
		this.startDate = startDate;
		this.endDate = endDate;
		this.groups = groups;
		this.synched = synched;
	}

	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}

	public List<String> getGroups() {
		return groups;
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

	public List<String> getRecords() {
		return records;
	}

	public void setRecords(List<String> records) {
		this.records = records;
	}

	public String getRecord() {
		return records.isEmpty() ? null : records.get(0);
	}

	public void setRecord(String record) {
		records = Arrays.asList(record);
	}

	public List<String> getAccessRoles() {
		return accessRoles;
	}

	public void setAccessRoles(List<String> accessRoles) {
		this.accessRoles = accessRoles;
	}

	public List<String> getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(List<String> userRoles) {
		this.userRoles = userRoles;
	}

	public List<String> getUserRolesTitles() {
		return userRolesTitles;
	}

	public void setUserRolesTitles(List<String> userRolesTitles) {
		this.userRolesTitles = userRolesTitles;
	}

	public String getAuthId() {
		return authId;
	}

	public void setAuthId(String authId) {
		this.authId = authId;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public boolean isSynched() {
		return synched;
	}
}

