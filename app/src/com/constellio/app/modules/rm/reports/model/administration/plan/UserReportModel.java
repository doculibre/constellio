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
package com.constellio.app.modules.rm.reports.model.administration.plan;

import java.util.ArrayList;
import java.util.List;

public class UserReportModel {
	private List<UserReportModel_User> users = new ArrayList<>();
	
	public List<UserReportModel_User> getUsers() {
		return users;
	}
	
	public void setUsers(List<UserReportModel_User> users) {
		this.users = users;
	}
	
	public static class UserReportModel_User {
		
		private String firstName;
		private String lastName;
		private String userName;
		private String userId;
		
		private List<UserReportModel_FilingSpace> filingSpaces = new ArrayList<>();
		private List<UserReportModel_AdministrativeUnit> administrativeUnits = new ArrayList<>();
		
		private String unit;
		private String status;
		
		public String getFirstName() {
			return firstName;
		}

		public UserReportModel_User setFirstName(String firstName) {
			this.firstName = firstName;
			return this;
		}

		public String getLastName() {
			return lastName;
		}

		public UserReportModel_User setLastName(String lastName) {
			this.lastName = lastName;
			return this;
		}

		public String getUserName() {
			return userName;
		}

		public UserReportModel_User setUserName(String userName) {
			this.userName = userName;
			return this;
		}

		public String getUserId() {
			return userId;
		}

		public UserReportModel_User setUserId(String userId) {
			this.userId = userId;
			return this;
		}

		public List<UserReportModel_FilingSpace> getFilingSpaces() {
			return filingSpaces;
		}

		public UserReportModel_User setFilingSpaces(List<UserReportModel_FilingSpace> filingSpaces) {
			this.filingSpaces = filingSpaces;
			return this;
		}

		public List<UserReportModel_AdministrativeUnit> getAdministrativeUnits() {
			return administrativeUnits;
		}

		public UserReportModel_User setAdministrativeUnits(List<UserReportModel_AdministrativeUnit> administrativeUnits) {
			this.administrativeUnits = administrativeUnits;
			return this;
		}
		
		public String getUnit(){
			return unit;
		}
		
		public UserReportModel_User setUnit(String unit){
			this.unit = unit;
			return this;
		}
		
		public String getStatus(){
			return status;
		}
		
		public UserReportModel_User setStatus(String status){
			this.status = status;
			return this;
		}
	}

	public static class UserReportModel_FilingSpace {
		
		private String code;
		private String label;
		private String description;

		public String getCode() {
			return code;
		}

		public UserReportModel_FilingSpace setCode(String code) {
			this.code = code;
			return this;
		}

		public String getLabel() {
			return label;
		}

		public UserReportModel_FilingSpace setLabel(String label) {
			this.label = label;
			return this;
		}

		public String getDescription() {
			return description;
		}

		public UserReportModel_FilingSpace setDescription(String description) {
			this.description = description;
			return this;
		}
	}

	public static class UserReportModel_AdministrativeUnit {
		private String code;

		private String label;

		private String description;

		public String getCode() {
			return code;
		}

		public UserReportModel_AdministrativeUnit setCode(String code) {
			this.code = code;
			return this;
		}

		public String getLabel() {
			return label;
		}

		public UserReportModel_AdministrativeUnit setLabel(String label) {
			this.label = label;
			return this;
		}

		public String getDescription() {
			return description;
		}

		public UserReportModel_AdministrativeUnit setDescription(String description) {
			this.description = description;
			return this;
		}

	}
}
