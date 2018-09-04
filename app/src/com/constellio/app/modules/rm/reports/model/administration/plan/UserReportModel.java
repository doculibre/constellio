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

		//		public List<UserReportModel_FilingSpace> getFilingSpaces() {
		//			return filingSpaces;
		//		}
		//
		//		public UserReportModel_User setFilingSpaces(List<UserReportModel_FilingSpace> filingSpaces) {
		//			this.filingSpaces = filingSpaces;
		//			return this;
		//		}

		public List<UserReportModel_AdministrativeUnit> getAdministrativeUnits() {
			return administrativeUnits;
		}

		public UserReportModel_User setAdministrativeUnits(
				List<UserReportModel_AdministrativeUnit> administrativeUnits) {
			this.administrativeUnits = administrativeUnits;
			return this;
		}

		public String getUnit() {
			return unit;
		}

		public UserReportModel_User setUnit(String unit) {
			this.unit = unit;
			return this;
		}

		public String getStatus() {
			return status;
		}

		public UserReportModel_User setStatus(String status) {
			this.status = status;
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
