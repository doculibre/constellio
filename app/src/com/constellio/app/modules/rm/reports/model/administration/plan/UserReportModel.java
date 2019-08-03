package com.constellio.app.modules.rm.reports.model.administration.plan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class UserReportModel {
	private List<UserReportModel_User> users = new ArrayList<>();

	public List<UserReportModel_User> getUsers() {
		return users;
	}

	public void setUsers(List<UserReportModel_User> users) {
		this.users = users;
	}

	public List<String> getColumnsTitles() {
		List<String> header = new ArrayList<>();
		header.add($("id"));
		header.add($("UserReport.lastname"));
		header.add($("UserReport.firstname"));
		header.add($("UserReport.username"));
		header.add($("UserReport.roles"));
		header.add($("UserReport.groups"));
		header.add($("UserReport.unit"));
		header.add($("UserReport.status"));
		header.add($("UserReport.createdOn"));
		header.add($("UserReport.modifiedOn"));

		return header;
	}

	public List<List<Object>> getResults() {
		List<List<Object>> result = new ArrayList<>();

		for(UserReportModel_User userReportModel_user : users) {
			List<Object> columnObjectList = new ArrayList<>();
			columnObjectList.add(userReportModel_user.userId);
			columnObjectList.add(userReportModel_user.lastName);
			columnObjectList.add(userReportModel_user.firstName);
			columnObjectList.add(userReportModel_user.userName);
			columnObjectList.add(getRoles(userReportModel_user));
			columnObjectList.add(getGroups(userReportModel_user));
			columnObjectList.add(getAdminUnits(userReportModel_user));
			columnObjectList.add(userReportModel_user.status);
			columnObjectList.add(userReportModel_user.creationDate);
			columnObjectList.add(userReportModel_user.modificationDate);
			result.add(columnObjectList);
		}

		return result;
	}

	private String getGroups(UserReportModel_User user) {
		List<UserReportModel_Group> userGroups = user.getGroups();
		StringBuilder stringBuilder = new StringBuilder();

		if(userGroups != null) {
			for(UserReportModel_Group currentUserReportModel : userGroups) {
				if(stringBuilder.length() > 0) {
					stringBuilder.append("\n");
				}
				stringBuilder.append("*" + currentUserReportModel.getLabel());
				for(String currentTargetAccess : currentUserReportModel.getTargetWithAccessList()) {
					stringBuilder.append("\n\t" + currentTargetAccess);
				}
			}
		}

		return stringBuilder.toString();
	}

	private String getRoles(UserReportModel_User user) {
		Map<UserReportModel_Role,List<UserReportModel_RoleTarget>> roleList = user.getRoles();
		StringBuilder stringBuilder = new StringBuilder();

		if(roleList != null) {
			for(UserReportModel_Role currentKey : roleList.keySet()) {
				List<UserReportModel_RoleTarget> targetRoleList = roleList.get(currentKey);

				if(stringBuilder.length() > 0) {
					stringBuilder.append("\n");
				}

				stringBuilder.append("*" +  currentKey.getLabel());
				for(UserReportModel_RoleTarget currentRoleTarget : targetRoleList) {
					stringBuilder.append("\n\t");
					if(currentRoleTarget != null) {
						stringBuilder.append(currentRoleTarget.getLabel());
					} else {
						stringBuilder.append($("CollectionGroupRolesView.global"));
					}
				}
			}
		}

		return stringBuilder.toString();
	}

	private String getAdminUnits(UserReportModel_User user) {

		StringBuilder administrativeUnitListBuilder = new StringBuilder();


		for (UserReportModel_AdministrativeUnit adminUnit : user.getAdministrativeUnits()) {
			if(administrativeUnitListBuilder.length() != 0) {
				administrativeUnitListBuilder.append(", ");
			}

			administrativeUnitListBuilder.append(adminUnit.getCode());
		}

		return administrativeUnitListBuilder.toString();
	}

	public static class UserReportModel_User {

		private String firstName;
		private String lastName;
		private String userName;
		private String userId;
		private String creationDate;
		private String modificationDate;
		private Map<UserReportModel_Role,List<UserReportModel_RoleTarget>> roles;
		private List<UserReportModel_Group> groups;

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

		public void setCreationDate(String creationDate) {
			this.creationDate = creationDate;
		}

		public void setModificationDate(String modificationDate) {
			this.modificationDate = modificationDate;
		}

		public String getCreationDate() {
			return creationDate;
		}

		public String getModificationDate() {
			return modificationDate;
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

		public Map<UserReportModel_Role,List<UserReportModel_RoleTarget>> getRoles() {
			return roles;
		}

		public List<UserReportModel_Group> getGroups() {
			return groups;
		}

		public void setRoles(Map<UserReportModel_Role,List<UserReportModel_RoleTarget>> roles) {
			this.roles = roles;
		}

		public void setGroups(List<UserReportModel_Group> groups) {
			this.groups = groups;
		}
	}

	public static class UserReportModel_Group {
		private String groupCode;
		private String label;
		List<String> targetWithAccessList;

		public UserReportModel_Group(String groupCode, String label) {
			this.groupCode = groupCode;
			this.label = label;
			targetWithAccessList = new ArrayList<>();
		}

		public String getGroupCode() {
			return groupCode;
		}

		public String getLabel() {
			return label;
		}

		public void addTarget(String targetWithAccess){
			this.targetWithAccessList.add(targetWithAccess);
		}

		public List<String> getTargetWithAccessList() {
			return targetWithAccessList;
		}
	}

	public static class UserReportModel_Role {
		private String code;
		private String label;

		public UserReportModel_Role(String code) {
			this(code, null);
		}

		public UserReportModel_Role(String code, String label) {
			if(code == null) {
				throw new IllegalArgumentException("UserReportModel_Role.code cannot be null");
			}
			this.code = code;
			this.label = label;
		}

		public String getCode() {
			return code;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}

		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof UserReportModel_Role)) {
				return false;
			}
			return code.equals(((UserReportModel_Role) obj).getCode());
		}

		@Override
		public int hashCode() {
			return code.hashCode();
		}
	}

	public static class UserReportModel_RoleTarget {
		private String code;
		private String label;
		private String schema;

		public UserReportModel_RoleTarget(String code, String schema) {
			if(code == null || schema == null) {
				throw new IllegalArgumentException("UserReportModel_RoleTarget.code and UserReportModel_RoleTarget.schema cannote be null");
			}
			this.code = code;
			this.schema = schema;
		}

		public UserReportModel_RoleTarget(String code, String label, String schema) {
			this.code = code;
			this.label = label;
			this.schema = schema;
		}

		public String getCode() {
			return code;
		}

		public String getLabel() {
			return label;
		}

		public String getSchema() {
			return schema;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof UserReportModel_RoleTarget)) {
				return false;
			}

			UserReportModel_RoleTarget userReportModel_roleTarget = (UserReportModel_RoleTarget) obj;
			return (code + schema).equals(userReportModel_roleTarget.getCode() + userReportModel_roleTarget.getSchema());
		}

		@Override
		public int hashCode() {
			return (code + schema).hashCode();
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
