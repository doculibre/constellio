package com.constellio.model.entities.workflows.definitions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.workflows.execution.WorkflowExecution;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;

public class AllUsersSelector {

	RoleSelector roleSelector;
	GroupSelector groupSelector;
	UserSelector userSelector;

	public AllUsersSelector(RoleSelector roles, GroupSelector groups, UserSelector users) {
		this.roleSelector = roles;
		this.groupSelector = groups;
		this.userSelector = users;
	}

	public AllUsersSelector(RoleSelector roles) {
		this(roles, new GroupSelector(new ArrayList<String>()), new UserSelector(new ArrayList<String>()));
	}

	public RoleSelector getRoleSelector() {
		return roleSelector;
	}

	public GroupSelector getGroupSelector() {
		return groupSelector;
	}

	public UserSelector getUserSelector() {
		return userSelector;
	}

	public List<User> getCandidateUsers(WorkflowExecution execution, ModelLayerFactory modelLayerFactory) {
		List<User> allUsers = new ArrayList<>();
		allUsers.addAll(getUsersFromRoles(execution, modelLayerFactory));
		getUsersFromGroups(execution, modelLayerFactory, allUsers);
		for (String userCode : userSelector.getUsers(execution)) {
			allUsers.add(modelLayerFactory.newUserServices().getUserInCollection(userCode, execution.getCollection()));
		}
		return noDuplicates(allUsers);
	}

	private List<User> noDuplicates(List<User> allUsers) {
		Map<String, User> filteredUsers = new HashMap<>();
		for (User user : allUsers) {
			filteredUsers.put(user.getId(), user);
		}
		return new ArrayList<>(filteredUsers.values());
	}

	private void getUsersFromGroups(WorkflowExecution execution, ModelLayerFactory modelLayerFactory, List<User> allUsers) {

		SchemasRecordsServices schemas = new SchemasRecordsServices(execution.getCollection(), modelLayerFactory);
		for (String groupCode : groupSelector.getGroups(execution)) {
			Record groupRecord = modelLayerFactory.newUserServices().getGroupInCollection(groupCode, execution.getCollection())
					.getWrappedRecord();
			List<Record> userRecords = modelLayerFactory.newAuthorizationsServices().getUserRecordsInGroup(groupRecord);
			for (Record userRecord : userRecords) {
				allUsers.add(schemas.wrapUser(userRecord));
			}
		}
	}

	private List<User> getUsersFromRoles(WorkflowExecution execution, ModelLayerFactory modelLayerFactory) {
		List<List<User>> usersForRoleLists = new ArrayList<>();
		for (Record record : modelLayerFactory.newRecordServices().getRecordsById(execution.getCollection(),
				execution.getRecordIds())) {
			for (String roleCode : roleSelector.getRoles(execution)) {
				List<User> usersForRoles = new ArrayList<>();
				usersForRoles.addAll(modelLayerFactory.newAuthorizationsServices().getUsersWithRoleForRecord(roleCode, record));
				usersForRoleLists.add(usersForRoles);
			}
		}
		return getCommonUsers(usersForRoleLists);
	}

	public List<User> getCommonUsers(List<List<User>> collections) {
		List<User> commonUsers = new ArrayList<>();
		if (!collections.isEmpty()) {
			Iterator<List<User>> iterator = collections.iterator();
			commonUsers.addAll(iterator.next());
			while (iterator.hasNext()) {
				commonUsers.retainAll(iterator.next());
			}
		}
		return commonUsers;
	}
}
