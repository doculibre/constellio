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

public class AdministrativeUnitReportModel {

	private boolean detailed = false;

	private List<AdministrativeUnitReportModel_AdministrativeUnit> administrativeUnits = new ArrayList<>();

	public List<AdministrativeUnitReportModel_AdministrativeUnit> getAdministrativeUnits() {
		return administrativeUnits;
	}

	public AdministrativeUnitReportModel setAdministrativeUnits(
			List<AdministrativeUnitReportModel_AdministrativeUnit> administrativeUnits) {
		this.administrativeUnits = administrativeUnits;
		return this;
	}

	public boolean isDetailed() {
		return detailed;
	}

	public void setDetailed(boolean detailed) {
		this.detailed = detailed;
	}

	public static class AdministrativeUnitReportModel_AdministrativeUnit {
		private String unitId;

		private String code;

		private String label;

		private String description;

		private List<AdministrativeUnitReportModel_AdministrativeUnit> childAdministrativeUnits = new ArrayList<>();

		private List<AdministrativeUnitReportModel_User> users = new ArrayList<>();

		public String getUnitId() {
			return unitId;
		}

		public void setUnitId(String unitId) {
			this.unitId = unitId;
		}

		public String getCode() {
			return code;
		}

		public AdministrativeUnitReportModel_AdministrativeUnit setCode(String code) {
			this.code = code;
			return this;
		}

		public String getLabel() {
			return label;
		}

		public AdministrativeUnitReportModel_AdministrativeUnit setLabel(String label) {
			this.label = label;
			return this;
		}

		public String getDescription() {
			return description;
		}

		public AdministrativeUnitReportModel_AdministrativeUnit setDescription(String description) {
			this.description = description;
			return this;
		}

		public List<AdministrativeUnitReportModel_User> getUsers() {
			return users;
		}

		public AdministrativeUnitReportModel_AdministrativeUnit setUsers(List<AdministrativeUnitReportModel_User> users) {
			this.users = users;
			return this;
		}

		public List<AdministrativeUnitReportModel_AdministrativeUnit> getChildAdministrativeUnits() {
			return childAdministrativeUnits;
		}

		public AdministrativeUnitReportModel_AdministrativeUnit setChildAdministrativeUnits(
				List<AdministrativeUnitReportModel_AdministrativeUnit> childAdministrativeUnits) {
			this.childAdministrativeUnits = childAdministrativeUnits;
			return this;
		}
	}

	public static class AdministrativeUnitReportModel_User {
		private String firstName;
		private String lastName;
		private String userName;
		private String email;

		public String getFirstName() {
			return firstName;
		}

		public AdministrativeUnitReportModel_User setFirstName(String firstName) {
			this.firstName = firstName;
			return this;
		}

		public String getLastName() {
			return lastName;
		}

		public AdministrativeUnitReportModel_User setLastName(String lastName) {
			this.lastName = lastName;
			return this;
		}

		public String getUserName() {
			return userName;
		}

		public AdministrativeUnitReportModel_User setUserName(String userName) {
			this.userName = userName;
			return this;
		}

		public String getEmail() {
			return email;
		}

		public AdministrativeUnitReportModel_User setEmail(String email) {
			this.email = email;
			return this;
		}

	}
}
