package com.constellio.model.services.users.sync.report;

import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.conf.ldap.user.LDAPUser;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.users.SystemWideUserInfos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ImportUsersAndGroupsReport {

	private final List<String> groupsFoundList;
	private final List<String> usersFoundList;
	private final List<String> newUsersImportedList;
	private final List<String> unsyncedUsersList;
	private final List<String> usersRemovedList;
	private final List<String> groupsRemovedList;
	private final List<String> assignationRelationships;

	public ImportUsersAndGroupsReport() {
		groupsFoundList = new ArrayList<>();
		usersFoundList = new ArrayList<>();
		newUsersImportedList = new ArrayList<>();
		unsyncedUsersList = new ArrayList<>();
		usersRemovedList = new ArrayList<>();
		groupsRemovedList = new ArrayList<>();
		assignationRelationships = new ArrayList<>();
	}

	public void addGroupsFoundList(Collection<LDAPGroup> groups) {
		groups.stream().forEach(group ->
				groupsFoundList.add("Found group " + group.getDistinguishedName() + " with name " + group.getSimpleName() + "."));
	}

	public void addUsersFoundList(Collection<LDAPUser> users) {
		users.stream().forEach(user ->
				usersFoundList.add("Found user " + user.getGivenName() + " with email " + user.getEmail() + "with last name " + user.getFamilyName() + "."));
	}

	public void addGroupsFoundList(LDAPGroup group) {
		groupsFoundList.add("Found group " + group.getDistinguishedName() + " with name " + group.getSimpleName() + ".");
	}

	public void addUsersFoundList(LDAPUser user) {
		usersFoundList.add("Found user " + user.getGivenName() + " with email " + user.getEmail() + "with last name " + user.getFamilyName() + ".");
	}

	public void addNewUsersImportedList(LDAPUser user) {
		newUsersImportedList.add("Added new user " + user.getGivenName() + " with email " + user.getEmail() + " to Constellio.");
	}

	public void addUnsyncedUsersImportedList(Collection<UserCredential> users) {
		users.stream().forEach(user ->
				unsyncedUsersList.add("No changes for unsynced user " + user.getUsername() + " with email " + user.getEmail() + " to Constellio."));
	}

	public void addUsersRemovedList(User user) {
		usersRemovedList.add("Removed user " + user.getUsername() + " with record ID " + user.getId() + ".");
	}

	public void addUsersRemovedList(SystemWideUserInfos user) {
		usersRemovedList.add("Removed user " + user.getUsername() + " with record ID " + user.getId() + ".");
	}

	public void addGroupsRemovedList(Group group) {
		groupsRemovedList.add("Removed group " + group.getCode() + " with record ID " + group.getId() + ".");
	}

	public void addAssignationRelationships(String groupCode, String username) {
		assignationRelationships.add("Assigned user " + username + " to group " + groupCode + ".");
	}

	public void addAssignationRelationships(List<LDAPGroup> groups, String username) {
		assignationRelationships.add("Assigned user " + username + " to group(s) " +
									 groups.stream().map(group -> group.getDistinguishedName()).collect(Collectors.joining(", ")) + ".");
	}

	public void removeAssignationRelationship(String groupCode, String username) {
		assignationRelationships.add("Removed user " + username + " from group " + groupCode + ".");
	}

	public String reportImport(List<String> colletions) {
		StringBuilder builder = new StringBuilder();
		builder.append("Synchronization in the following collections: " + colletions.stream().collect(Collectors.joining(", ")) + "\n");
		builder.append("Number of groups found: " + groupsFoundList.size() + "\n");
		builder.append(groupsFoundList.stream().collect(Collectors.joining("\n")));
		builder.append("\nNumber of users found: " + usersFoundList.size() + "\n");
		builder.append(usersFoundList.stream().collect(Collectors.joining("\n")));
		builder.append("\nNumber of users added: " + newUsersImportedList.size() + "\n");
		builder.append(newUsersImportedList.stream().collect(Collectors.joining("\n")));
		builder.append("\nNumber of users unsynced : " + unsyncedUsersList.size() + "\n");
		builder.append(unsyncedUsersList.stream().collect(Collectors.joining("\n")));
		builder.append("\nNumber of groups removed: " + usersFoundList.size() + "\n");
		builder.append(groupsRemovedList.stream().collect(Collectors.joining("\n")));
		builder.append("\nNumber of users removed: " + usersFoundList.size() + "\n");
		builder.append(usersRemovedList.stream().collect(Collectors.joining("\n")));
		builder.append("\n");
		builder.append(assignationRelationships.stream().collect(Collectors.joining("\n")));
		return builder.toString();
	}

}
