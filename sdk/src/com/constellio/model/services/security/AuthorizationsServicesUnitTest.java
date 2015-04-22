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
package com.constellio.model.services.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.CustomizedAuthorizationsBehavior;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;

public class AuthorizationsServicesUnitTest extends ConstellioTest {

	AuthorizationsServices authorizationsServices;

	@Mock AuthorizationDetailsManager manager;
	@Mock MetadataSchemasManager schemasManager;
	@Mock RolesManager rolesManager;
	@Mock TaxonomiesManager taxonomiesManager;
	@Mock RecordServices recordServices;
	@Mock SearchServices searchServices;
	@Mock UserServices userServices;
	@Mock Metadata title;

	List<String> recordIds;
	String record1id = "record1";
	String record2id = "record2";
	List<Record> records;
	@Mock Record record1;
	@Mock Record record2;

	List<String> userIds;
	String user1id = "user1";
	String user2id = "user2";
	List<Record> users;
	@Mock Record user1;
	@Mock Record user2;

	@Mock User bob;
	@Mock Group rightGroup;
	@Mock Group rightSubGroup;
	@Mock Authorization rightAuthorization;
	@Mock AuthorizationDetails rightAuthorizationDetails;
	@Mock Record rightRecord;
	@Mock Role rightRole;
	List<String> rightAuthorizationList;
	List<Role> rightRoleList;
	String rightAuthorizationCode = "rightAuthorization";
	String rightGroupCode = "rightGroupCode";
	String rightSubGroupCode = "rightSubGroupCode";

	@Before
	public void setUp()
			throws Exception {

		authorizationsServices = spy(new AuthorizationsServices(manager, rolesManager, taxonomiesManager, recordServices,
				searchServices, userServices, schemasManager, mock(LoggingServices.class)));

		recordIds = new ArrayList<>();
		recordIds.add(record1id);
		recordIds.add(record2id);
		records = new ArrayList<>();
		records.add(record1);
		records.add(record2);

		userIds = new ArrayList<>();
		userIds.add(user1id);
		userIds.add(user2id);
		users = new ArrayList<>();
		users.add(user1);
		users.add(user2);

		when(bob.getId()).thenReturn("zeBobId");
		when(rightRecord.getId()).thenReturn("zeRightId");
		when(rightRole.getCode()).thenReturn("zeRightRoleCode");

		rightAuthorizationList = new ArrayList<>();
		rightAuthorizationList.add(rightAuthorizationCode);

		when(authorizationsServices.getRecordAuthorizations(rightRecord)).thenReturn(Arrays.asList(rightAuthorization));
	}

	private void configureDummySchemasForValidation() {
		SchemaUtils schemaUtils = mock(SchemaUtils.class, "SchemaUtils");
		doReturn(schemaUtils).when(authorizationsServices).newSchemaUtils();
		doReturn(aString()).when(schemaUtils).getSchemaTypeCode(anyString());
		List<String> secondaryTaxonomiesTypes = mock(List.class, "types");
		doReturn(secondaryTaxonomiesTypes).when(taxonomiesManager).getSecondaryTaxonomySchemaTypes(anyString());
		doReturn(false).when(secondaryTaxonomiesTypes).contains(any());
	}

	@Test
	public void whenAddingAuthToRecordThenAuthAdded()
			throws Exception {
		List<String> auths = new ArrayList<>();
		auths.add("oldAuth1");
		auths.add("oldAuth2");

		doReturn(auths).when(record1).getList(Schemas.AUTHORIZATIONS);

		ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);

		authorizationsServices.addAuthorizationToRecord("newAuth", record1);

		verify(record1).set(eq(Schemas.AUTHORIZATIONS), valueCaptor.capture());
		assertThat((List) valueCaptor.getValue()).containsOnly("oldAuth1", "oldAuth2", "newAuth");
	}

	@Test
	public void whenRemovingAuthOnRecordByIdThenAuthRemoved()
			throws Exception {
		List<String> auths = new ArrayList<>();
		auths.add("oldAuth1");
		auths.add("oldAuth2");

		doReturn(auths).when(record1).getList(Schemas.AUTHORIZATIONS);
		ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);

		authorizationsServices.removeAuthorizationOnRecord("oldAuth2", record1);

		verify(record1).set(eq(Schemas.AUTHORIZATIONS), valueCaptor.capture());
		assertThat((List) valueCaptor.getValue()).containsOnly("oldAuth1");
	}

	@Test
	public void whenRemovingRemovedAuthOnRecordByIdThenAuthRemoved()
			throws Exception {
		List<String> auths = new ArrayList<>();
		auths.add("oldAuth1");
		auths.add("oldAuth2");

		doReturn(auths).when(record1).getList(Schemas.REMOVED_AUTHORIZATIONS);
		ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);

		authorizationsServices.removeRemovedAuthorizationOnRecord("oldAuth2", record1);

		verify(record1).set(eq(Schemas.REMOVED_AUTHORIZATIONS), valueCaptor.capture());
		assertThat((List) valueCaptor.getValue()).containsOnly("oldAuth1");
	}

	@Test
	public void whenRemovingInheritedAuthOnRecordByIdThenAuthAddedToRemovedAuths()
			throws Exception {
		List<String> removedAuths = new ArrayList<>();

		doReturn(removedAuths).when(record1).getList(Schemas.REMOVED_AUTHORIZATIONS);
		ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);

		authorizationsServices.removeInheritedAuthorizationOnRecord("inheritedAuth2", record1);

		verify(record1).set(eq(Schemas.REMOVED_AUTHORIZATIONS), valueCaptor.capture());
		assertThat((List) valueCaptor.getValue()).containsOnly("inheritedAuth2");
	}

	@Test
	public void whenRemovingAuthOnRecordThenAuthRemovedAndBehaviorSet()
			throws Exception {
		List<String> auths = new ArrayList<>();
		auths.add("oldAuth1");
		auths.add("oldAuth2");

		Authorization auth = mock(Authorization.class, "oldAuth2");
		AuthorizationDetails authDetails = mock(AuthorizationDetails.class, "authDetails");
		when(authDetails.getId()).thenReturn("oldAuth2");
		when(auth.getDetail()).thenReturn(authDetails);

		doReturn(auths).when(record1).getList(Schemas.AUTHORIZATIONS);

		authorizationsServices.removeAuthorizationOnRecord(auth, record1, CustomizedAuthorizationsBehavior.KEEP_ATTACHED);

		verify(authorizationsServices).removeAuthorizationOnRecord("oldAuth2", record1);
		verify(authorizationsServices).setAuthorizationBehaviorToRecord(CustomizedAuthorizationsBehavior.KEEP_ATTACHED, record1);
	}

	@Test
	public void whenRemovingInheritedAuthOnRecordThenAuthRemovedAndBehaviorSet()
			throws Exception {
		List<String> inheritedAuths = new ArrayList<>();
		inheritedAuths.add("inheritedAuth1");
		inheritedAuths.add("inheritedAuth2");

		Authorization auth = mock(Authorization.class, "inheritedAuth2");
		AuthorizationDetails authDetails = mock(AuthorizationDetails.class, "authDetails");
		when(authDetails.getId()).thenReturn("inheritedAuth2");
		when(auth.getDetail()).thenReturn(authDetails);

		doReturn(inheritedAuths).when(record1).getList(Schemas.INHERITED_AUTHORIZATIONS);

		authorizationsServices.removeAuthorizationOnRecord(auth, record1, CustomizedAuthorizationsBehavior.KEEP_ATTACHED);

		verify(authorizationsServices).removeInheritedAuthorizationOnRecord("inheritedAuth2", record1);
		verify(authorizationsServices).setAuthorizationBehaviorToRecord(CustomizedAuthorizationsBehavior.KEEP_ATTACHED, record1);
	}

	@Test
	public void whenSettingAuthBehaviorToRecordThenBehaviorSetAndAuthsAdjusted()
			throws Exception {
		authorizationsServices.setAuthorizationBehaviorToRecord(CustomizedAuthorizationsBehavior.DETACH, record1);
		verify(authorizationsServices).setupAuthorizationsForDetachedRecord(record1);
		verify(record1).set(Schemas.IS_DETACHED_AUTHORIZATIONS, true);

		authorizationsServices.setAuthorizationBehaviorToRecord(CustomizedAuthorizationsBehavior.KEEP_ATTACHED, record2);
		verify(record2).set(Schemas.IS_DETACHED_AUTHORIZATIONS, false);
	}

	@Test
	public void whenDeletingAuthThenAuthRemovedOnAllRecords()
			throws Exception {
		AuthorizationDetails authDetails = mock(AuthorizationDetails.class, "authDetails");
		when(authDetails.getId()).thenReturn("theAuth");

		records.addAll(users);
		doReturn(records).when(searchServices).search(any(LogicalSearchQuery.class));
		doReturn(null).when(authorizationsServices).getAuthorization(anyString(), anyString());

		authorizationsServices.delete(authDetails, null);

		verify(authorizationsServices).removeAuthorizationOnRecord("theAuth", record1);
		verify(authorizationsServices).removeRemovedAuthorizationOnRecord("theAuth", record1);
		verify(authorizationsServices).removeAuthorizationOnRecord("theAuth", record2);
		verify(authorizationsServices).removeRemovedAuthorizationOnRecord("theAuth", record2);
		verify(authorizationsServices).removeAuthorizationOnRecord("theAuth", user1);
		verify(authorizationsServices).removeRemovedAuthorizationOnRecord("theAuth", user1);
		verify(authorizationsServices).removeAuthorizationOnRecord("theAuth", user2);
		verify(authorizationsServices).removeRemovedAuthorizationOnRecord("theAuth", user2);

		verify(manager).remove(authDetails);
	}

	@Test
	public void whenResettingAuthsForRecordThenAuthsResetCorrectly()
			throws Exception {
		ArgumentCaptor<Metadata> metadataCaptor = new ArgumentCaptor<>();
		ArgumentCaptor<Object> valueCaptor = new ArgumentCaptor<>();

		authorizationsServices.reset(record1);

		verify(record1, times(3)).set(metadataCaptor.capture(), valueCaptor.capture());
		assertThat(metadataCaptor.getAllValues().get(0)).isEqualTo(Schemas.AUTHORIZATIONS);
		assertThat(valueCaptor.getAllValues().get(0)).isNull();
		assertThat(metadataCaptor.getAllValues().get(1)).isEqualTo(Schemas.REMOVED_AUTHORIZATIONS);
		assertThat(valueCaptor.getAllValues().get(1)).isNull();
		assertThat(metadataCaptor.getAllValues().get(2)).isEqualTo(Schemas.IS_DETACHED_AUTHORIZATIONS);
		assertThat(valueCaptor.getAllValues().get(2)).isEqualTo(false);
	}

	@Test
	public void whenChangingAuthCodeThenCodeChanged()
			throws Exception {
		AuthorizationDetails authDetails = mock(AuthorizationDetails.class, "authDetails");
		when(authDetails.getId()).thenReturn("oldAuth");
		doReturn(records).when(authorizationsServices).getRecordsWithAuth(anyString(), anyString());
		ArgumentCaptor<AuthorizationDetails> valueCaptor = new ArgumentCaptor<>();

		authorizationsServices.changeAuthorizationCode(authDetails, "newCode");

		verify(authorizationsServices).removeAuthorizationOnRecord("oldAuth", record1);
		verify(authorizationsServices).removeAuthorizationOnRecord("oldAuth", record2);
		verify(authorizationsServices).addAuthorizationToRecord("newCode", record1);
		verify(authorizationsServices).addAuthorizationToRecord("newCode", record2);
		verify(manager).remove(authDetails);
		verify(manager).add(valueCaptor.capture());
		assertThat(valueCaptor.getValue().getId()).isEqualTo("newCode");
	}

	@Test
	public void whenActivatingAllAuthsWithActiveDatesThenAllAuthsRefreshed()
			throws Exception {
		List<String> collections = Arrays.asList("zeCollection");
		AuthorizationDetails auth1 = mock(AuthorizationDetails.class);
		AuthorizationDetails auth2 = mock(AuthorizationDetails.class);
		Map<String, AuthorizationDetails> auths = new HashMap<>();
		auths.put("auth1", auth1);
		auths.put("auth2", auth2);
		doReturn(auths).when(manager).getAuthorizationsDetails("zeCollection");

		authorizationsServices.refreshActivationForAllAuths(collections);

		verify(authorizationsServices).refreshAuthorizationBasedOnDates(auth1);
		verify(authorizationsServices).refreshAuthorizationBasedOnDates(auth2);
	}

	@Test
	public void whenRemovingMultipleAuthsThenAllAuthsRemoved()
			throws Exception {
		Authorization auth1 = mock(Authorization.class);
		Authorization auth2 = mock(Authorization.class);

		doNothing().when(authorizationsServices).removeAuthorizationOnRecord(any(Authorization.class), any(Record.class),
				any(CustomizedAuthorizationsBehavior.class));
		authorizationsServices.removeMultipleAuthorizationsOnRecord(Arrays.asList(auth1, auth2), record1,
				CustomizedAuthorizationsBehavior.KEEP_ATTACHED);

		verify(authorizationsServices)
				.removeAuthorizationOnRecord(auth1, record1, CustomizedAuthorizationsBehavior.KEEP_ATTACHED);
		verify(authorizationsServices)
				.removeAuthorizationOnRecord(auth2, record1, CustomizedAuthorizationsBehavior.KEEP_ATTACHED);

	}

}
