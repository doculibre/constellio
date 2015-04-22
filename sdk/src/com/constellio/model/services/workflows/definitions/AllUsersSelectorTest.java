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
package com.constellio.model.services.workflows.definitions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.workflows.definitions.AllUsersSelector;
import com.constellio.model.entities.workflows.definitions.RoleSelector;
import com.constellio.model.entities.workflows.execution.WorkflowExecution;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.sdk.tests.ConstellioTest;

public class AllUsersSelectorTest extends ConstellioTest {

	@Mock WorkflowExecution execution;
	@Mock ModelLayerFactory modelLayerFactory;
	@Mock RecordServices recordServices;
	@Mock RolesManager rolesManager;
	@Mock AuthorizationsServices authServices;
	@Mock RoleSelector roleSelector;
	@Mock Role role1;
	@Mock Role role2;
	@Mock Record zeRecord;

	@Mock User user1;
	@Mock User user2;
	@Mock User user3;

	private String zeRecordId = "zeRecord";
	private String role1Id = "role1";
	private String role2Id = "role2";
	private String zeCollection = "zeCollection";

	@Before
	public void setUp()
			throws Exception {
		doReturn(recordServices).when(modelLayerFactory).newRecordServices();
		doReturn(rolesManager).when(modelLayerFactory).getRolesManager();
		doReturn(authServices).when(modelLayerFactory).newAuthorizationsServices();

		List<String> recordIds = Arrays.asList(zeRecordId);
		doReturn(recordIds).when(execution).getRecordIds();
		doReturn(Arrays.asList(zeRecord)).when(recordServices).getRecordsById(zeCollection, recordIds);
		doReturn(zeCollection).when(zeRecord).getCollection();
		doReturn(zeCollection).when(execution).getCollection();
		doReturn(role1).when(rolesManager).getRole(zeCollection, role1Id);
		doReturn(role2).when(rolesManager).getRole(zeCollection, role2Id);
		doReturn(Arrays.asList(role1Id, role2Id)).when(roleSelector).getRoles(execution);

		doReturn("user1").when(user1).getId();
		doReturn("user2").when(user2).getId();
		doReturn("user3").when(user3).getId();
		doReturn(Arrays.asList(user1, user2, user3)).when(authServices).getUsersWithRoleForRecord(role1Id, zeRecord);
		doReturn(Arrays.asList(user1, user2, user3)).when(authServices).getUsersWithRoleForRecord(role2Id, zeRecord);
	}

	@Test
	public void givenTwoUsersAndAVariableThenCorrectValuesReturned()
			throws Exception {
		AllUsersSelector allUsersSelector = new AllUsersSelector(roleSelector);

		List<User> allUsers = allUsersSelector.getCandidateUsers(execution, modelLayerFactory);

		assertThat(allUsers).contains(user1, user2, user3);
	}
}
