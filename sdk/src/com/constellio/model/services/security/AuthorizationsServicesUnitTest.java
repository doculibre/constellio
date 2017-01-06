package com.constellio.model.services.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
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

import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.global.AuthorizationDetails;
import com.constellio.model.entities.security.CustomizedAuthorizationsBehavior;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
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

	@Mock LoggingServices loggingServices;
	@Mock UniqueIdGenerator uniqueIdGenerator;
	@Mock ModelLayerFactory modelLayerFactory;
	@Mock DataLayerFactory dataLayerFactory;

	@Before
	public void setUp()
			throws Exception {

		when(modelLayerFactory.getAuthorizationDetailsManager()).thenReturn(manager);
		when(modelLayerFactory.getRolesManager()).thenReturn(rolesManager);
		when(modelLayerFactory.getTaxonomiesManager()).thenReturn(taxonomiesManager);
		when(modelLayerFactory.newRecordServices()).thenReturn(recordServices);
		when(modelLayerFactory.newSearchServices()).thenReturn(searchServices);
		when(modelLayerFactory.newUserServices()).thenReturn(userServices);
		when(modelLayerFactory.newLoggingServices()).thenReturn(loggingServices);
		when(modelLayerFactory.getDataLayerFactory()).thenReturn(dataLayerFactory);
		when(dataLayerFactory.getSecondaryUniqueIdGenerator()).thenReturn(uniqueIdGenerator);
		when(modelLayerFactory.newLoggingServices()).thenReturn(loggingServices);
		authorizationsServices = new AuthorizationsServices(modelLayerFactory);

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

		authorizationsServices.removeAuthorizationOnRecord("oldAuth2", record1, true);

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

}
