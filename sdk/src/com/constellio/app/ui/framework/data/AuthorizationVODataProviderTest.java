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
package com.constellio.app.ui.framework.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.framework.builders.AuthorizationToVOBuilder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.records.RecordServicesImpl;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.MockedFactories;

public class AuthorizationVODataProviderTest extends ConstellioTest {

	public static final String RECORD_ID_1 = "recordId1";
	public static final String USER_1 = "user1";
	public static final String USER_2 = "user2";
	public static final String RECORD_ID_2 = "recordId2";
	public static final String ROLE_1 = "role1";
	public static final String ROLE_2 = "role2";
	MockedFactories mockedFactories = new MockedFactories();
	AuthorizationVODataProvider dataProvider;
	@Mock AuthorizationToVOBuilder voBuilder;
	@Mock AuthorizationVO authorizationVO1, authorizationVO2;
	@Mock AuthorizationsServices authorizationsServices;
	@Mock RecordServicesImpl recordServices;
	@Mock Record record1, record2;
	@Mock Authorization authorization1, authorization2;
	@Mock AuthorizationDetails authorizationDetails1;
	@Mock Role role;
	List<Authorization> authorizations1;
	List<String> principals1, records1, roles1;

	@Before
	public void setUp()
			throws Exception {

		authorizations1 = new ArrayList<>();
		authorizations1.add(authorization1);
		authorizations1.add(authorization2);

		principals1 = new ArrayList<>();
		principals1.add(USER_1);
		principals1.add(USER_2);

		records1 = new ArrayList<>();
		records1.add(RECORD_ID_1);
		records1.add(RECORD_ID_1);

		roles1 = new ArrayList<>();
		roles1.add(ROLE_1);
		roles1.add(ROLE_2);

		when(mockedFactories.getModelLayerFactory().newAuthorizationsServices()).thenReturn(authorizationsServices);
		when(mockedFactories.getModelLayerFactory().newRecordServices()).thenReturn(recordServices);

		when(authorization1.getGrantedToPrincipals()).thenReturn(principals1);
		when(authorization1.getGrantedOnRecords()).thenReturn(records1);
		when(authorization1.getDetail()).thenReturn(authorizationDetails1);
		when(authorizationDetails1.getRoles()).thenReturn(roles1);

		when(authorizationsServices.getRecordAuthorizations(record1)).thenReturn(authorizations1);
		when(record1.getId()).thenReturn(RECORD_ID_1);
		when(record2.getId()).thenReturn(RECORD_ID_2);

		when(recordServices.getDocumentById(RECORD_ID_1)).thenReturn(record1);

		when(voBuilder.build(authorization1)).thenReturn(authorizationVO1);
		when(voBuilder.build(authorization2)).thenReturn(authorizationVO2);

		dataProvider = spy(new AuthorizationVODataProvider(voBuilder, mockedFactories.getModelLayerFactory(), record1.getId()));
	}

	@Test
	public void givenDataProviderAndRecordWhenNewInstanceThenListPermissionVOs()
			throws Exception {

		verify(recordServices).getDocumentById(RECORD_ID_1);
		verify(authorizationsServices).getRecordAuthorizations(record1);
		verify(voBuilder).build(authorization1);
		verify(voBuilder).build(authorization2);
		assertThat(dataProvider.authorizationVOs).hasSize(2);
		assertThat(dataProvider.authorizationVOs).containsOnly(authorizationVO1, authorizationVO2);
	}

	@Test
	public void whenListThenReturnIndexes()
			throws Exception {

		List<Integer> indexes = dataProvider.list();

		assertThat(indexes).hasSize(2);
		assertThat(indexes.get(0)).isEqualTo(0);
		assertThat(indexes.get(1)).isEqualTo(1);
	}

	@Test
	public void whenGetPermissionVOByIndexThenOk()
			throws Exception {

		AuthorizationVO retrievedAuthorizationVO1 = dataProvider.getAuthorizationVO(0);
		AuthorizationVO retrievedAuthorizationVO2 = dataProvider.getAuthorizationVO(1);

		assertThat(retrievedAuthorizationVO1).isEqualTo(authorizationVO1);
		assertThat(retrievedAuthorizationVO2).isEqualTo(authorizationVO2);
	}

	@Test
	public void whenSizeThenOk()
			throws Exception {

		assertThat(dataProvider.size()).isEqualTo(2);
	}
}
