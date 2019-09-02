package com.constellio.model.services.users;

import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.security.roles.Roles;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_UserIsNotInCollection;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class UserServicesUnitTest extends ConstellioTest {

	@Mock MetadataSchemasManager metadataSchemasManager;

	@Mock MetadataSchemaTypes collection1Types;
	@Mock MetadataSchema collection1UserSchema;
	@Mock MetadataSchema collection1GroupSchema;
	@Mock Metadata collection1UsernameMetadata;
	@Mock Metadata collection1UserGroupsMetadata;
	@Mock Metadata collection1GroupCodeMetadata;

	@Mock MetadataSchemaType collection1UserType;
	@Mock MetadataSchemaType collection1GroupType;

	@Mock MetadataSchemaTypes collection2Types;
	@Mock MetadataSchema collection2UserSchema;
	@Mock MetadataSchema collection2GroupSchema;
	@Mock Metadata collection2UsernameMetadata;
	@Mock Metadata collection2UserGroupsMetadata;
	@Mock Metadata collection2GroupCodeMetadata;

	@Mock SolrUserCredentialsManager userCredentialsManager;
	@Mock SolrGlobalGroupsManager globalGroupsManager;
	@Mock CollectionsListManager collectionsListManager;
	@Mock RecordServices recordServices;
	@Mock SearchServices searchServices;
	@Mock AuthenticationService authenticationService;
	@Mock ModelLayerConfiguration modelLayerConfiguration;
	@Mock LDAPConfigurationManager configurationManager;
	UserServices userServices;

	@Mock UserCredential admin, alice, bob;

	@Mock GlobalGroup legends, heroes;

	@Mock Record userRecord, groupRecord;

	@Mock User bobRecordInCollection1, bobRecordInCollection2;
	@Mock Record bobRecordInCollection1Record, bobRecordInCollection2Record;

	@Mock ModelLayerFactory modelLayerFactory;
	@Mock DataLayerFactory dataLayerFactory;
	UniqueIdGenerator uniqueIdGenerator = new UUIDV1Generator();
	@Mock Group legendsInCollection1, legendsInCollection2;
	@Mock Record legendsInCollection1Record, legendsInCollection2Record;
	@Mock RolesManager rolesManager;
	@Mock Roles roles;

	@Before
	public void setUp()
			throws Exception {

		when(rolesManager.getCollectionRoles(zeCollection)).thenReturn(roles);
		when(modelLayerFactory.getDataLayerFactory()).thenReturn(dataLayerFactory);
		when(dataLayerFactory.getSecondaryUniqueIdGenerator()).thenReturn(uniqueIdGenerator);
		when(modelLayerFactory.getUserCredentialsManager()).thenReturn(userCredentialsManager);
		when(modelLayerFactory.getGlobalGroupsManager()).thenReturn(globalGroupsManager);
		when(modelLayerFactory.getCollectionsListManager()).thenReturn(collectionsListManager);
		when(modelLayerFactory.newRecordServices()).thenReturn(recordServices);
		when(modelLayerFactory.newSearchServices()).thenReturn(searchServices);
		when(modelLayerFactory.getMetadataSchemasManager()).thenReturn(metadataSchemasManager);
		when(modelLayerFactory.newAuthenticationService()).thenReturn(authenticationService);
		when(modelLayerFactory.getRolesManager()).thenReturn(rolesManager);
		when(modelLayerFactory.getConfiguration()).thenReturn(modelLayerConfiguration);
		when(modelLayerFactory.getLdapConfigurationManager()).thenReturn(configurationManager);

		userServices = spy(new UserServices(modelLayerFactory));

		doReturn("group1Collection1Id").when(userServices).getGroupIdInCollection("group1", "collection1");
		doReturn("group2Collection1Id").when(userServices).getGroupIdInCollection("group2", "collection1");
		doReturn("group1Collection2Id").when(userServices).getGroupIdInCollection("group1", "collection2");
		doReturn("group2Collection2Id").when(userServices).getGroupIdInCollection("group2", "collection2");

		when(admin.getUsername()).thenReturn("admin");
		when(admin.getStatus()).thenReturn(UserCredentialStatus.ACTIVE);
		when(alice.getUsername()).thenReturn("alice");
		when(alice.getStatus()).thenReturn(UserCredentialStatus.ACTIVE);
		when(bob.getUsername()).thenReturn("bob");
		when(bob.getStatus()).thenReturn(UserCredentialStatus.ACTIVE);
		when(legends.getCode()).thenReturn("legends");
		when(heroes.getCode()).thenReturn("heroes");

		when(userRecord.getSchemaCode()).thenReturn("user_default");
		when(bobRecordInCollection1.getSchemaCode()).thenReturn("user_default");
		when(bobRecordInCollection2.getSchemaCode()).thenReturn("user_default");
		when(bobRecordInCollection1.getCollection()).thenReturn("collection1");
		when(bobRecordInCollection2.getCollection()).thenReturn("collection2");
		when(legendsInCollection1.getSchemaCode()).thenReturn("group_default");
		when(legendsInCollection2.getSchemaCode()).thenReturn("group_default");
		when(legendsInCollection1.getCollection()).thenReturn("collection1");
		when(legendsInCollection2.getCollection()).thenReturn("collection2");
		when(groupRecord.getSchemaCode()).thenReturn("group_default");

		when(metadataSchemasManager.getSchemaTypes("collection1")).thenReturn(collection1Types);
		when(collection1Types.getSchema("user_default")).thenReturn(collection1UserSchema);
		when(collection1Types.getSchema("group_default")).thenReturn(collection1GroupSchema);
		when(collection1UserSchema.getCode()).thenReturn("user_default");
		when(collection1GroupSchema.getCode()).thenReturn("group_default");
		when(collection1UserSchema.getCollection()).thenReturn("collection1");
		when(collection1GroupSchema.getCollection()).thenReturn("collection1");
		when(collection1UserSchema.getMetadata(User.USERNAME)).thenReturn(collection1UsernameMetadata);
		when(collection1UserSchema.getMetadata(User.GROUPS)).thenReturn(collection1UserGroupsMetadata);
		when(collection1GroupSchema.getMetadata(Group.CODE)).thenReturn(collection1GroupCodeMetadata);

		when(metadataSchemasManager.getSchemaTypes("collection2")).thenReturn(collection2Types);
		when(collection2Types.getSchema("user_default")).thenReturn(collection2UserSchema);
		when(collection2Types.getSchema("group_default")).thenReturn(collection2GroupSchema);
		when(collection2UserSchema.getCode()).thenReturn("user_default");
		when(collection2GroupSchema.getCode()).thenReturn("group_default");
		when(collection2UserSchema.getCollection()).thenReturn("collection2");
		when(collection2GroupSchema.getCollection()).thenReturn("collection2");
		when(collection2UserSchema.getMetadata(User.USERNAME)).thenReturn(collection2UsernameMetadata);
		when(collection2UserSchema.getMetadata(User.GROUPS)).thenReturn(collection2UserGroupsMetadata);
		when(collection2GroupSchema.getMetadata(Group.CODE)).thenReturn(collection2GroupCodeMetadata);

		when(bobRecordInCollection1.getCollection()).thenReturn("collection1");
		when(bobRecordInCollection2.getCollection()).thenReturn("collection2");
		when(bobRecordInCollection1.getWrappedRecord()).thenReturn(bobRecordInCollection1Record);
		when(bobRecordInCollection2.getWrappedRecord()).thenReturn(bobRecordInCollection2Record);
		when(bobRecordInCollection1Record.getCollection()).thenReturn("collection1");
		when(bobRecordInCollection2Record.getCollection()).thenReturn("collection2");

		when(legendsInCollection1.getCollection()).thenReturn("collection1");
		when(legendsInCollection2.getCollection()).thenReturn("collection2");
		when(legendsInCollection1.getWrappedRecord()).thenReturn(legendsInCollection1Record);
		when(legendsInCollection2.getWrappedRecord()).thenReturn(legendsInCollection2Record);
		when(legendsInCollection1Record.getCollection()).thenReturn("collection1");
		when(legendsInCollection2Record.getCollection()).thenReturn("collection2");

		doReturn(collection1GroupSchema).when(userServices).groupSchema("collection1");
		doReturn(collection1UserSchema).when(userServices).userSchema("collection1");
		doReturn(collection2GroupSchema).when(userServices).groupSchema("collection2");
		doReturn(collection2UserSchema).when(userServices).userSchema("collection2");

		when(modelLayerConfiguration.getMainDataLanguage()).thenReturn("fr");
	}

	@Test
	public void whenGetUserThenReturnHim()
			throws Exception {

		when(userCredentialsManager.getUserCredential("bob")).thenReturn(bob);

		assertThat(userServices.getUser("bob")).isSameAs(bob);

	}

	@Test
	public void whenGetGroupThenReturnIt()
			throws Exception {

		when(globalGroupsManager.getGlobalGroupWithCode("legends")).thenReturn(legends);

		assertThat(userServices.getGroup("legends")).isSameAs(legends);

	}

	@Test
	public void whenGeActiveGroupThenOk()
			throws Exception {

		when(globalGroupsManager.getGlobalGroupWithCode("legends")).thenReturn(legends);
		when(globalGroupsManager.getGlobalGroupWithCode("heroes")).thenReturn(heroes);
		when(globalGroupsManager.getActiveGlobalGroupWithCode("heroes")).thenReturn(null);
		when(globalGroupsManager.getActiveGlobalGroupWithCode("legends")).thenReturn(legends);
		when(legends.getStatus()).thenReturn(GlobalGroupStatus.ACTIVE);
		when(heroes.getStatus()).thenReturn(GlobalGroupStatus.INACTIVE);

		assertThat(userServices.getGroup("legends")).isSameAs(legends);
		assertThat(userServices.getActiveGroup("legends")).isSameAs(legends);
		try {
			userServices.getActiveGroup("heroes");
		} catch (Exception e) {
			assertThat(e.getMessage()).isEqualTo("No such group 'heroes'");
		}
	}

	@Test
	public void whenGetUserInInvalidCollectionThenException()
			throws Exception {

		when(bob.getCollections()).thenReturn(Arrays.asList("collection2"));
		when(userCredentialsManager.getUserCredential("bob")).thenReturn(bob);

		try {
			userServices.getUserInCollection("bob", "collection1");
			fail("UserServicesRuntimeException_UserIsNotInCollection expected");
		} catch (UserServicesRuntimeException_UserIsNotInCollection e) {
			// OK
		}
		verifyZeroInteractions(searchServices);

	}

	@Test
	public void whenAddUpdateUserThenAddUpdateInManagerAndSync()
			throws Exception {

		doNothing().when(userServices).sync(bob);

		userServices.addUpdateUserCredential(bob);

		InOrder inOrder = inOrder(userServices, userCredentialsManager);
		inOrder.verify(userCredentialsManager).addUpdate(bob);
		inOrder.verify(userServices).sync(bob);

	}

	@Test
	public void whenAddUpdateGroupThenAddUpdateInManagerAndSync()
			throws Exception {

		doNothing().when(userServices).sync(legends);

		userServices.addUpdateGlobalGroup(legends);

		InOrder inOrder = inOrder(userServices, globalGroupsManager);
		inOrder.verify(globalGroupsManager).addUpdate(legends);
		inOrder.verify(userServices).sync(legends);

	}

	//@Test
	public void RecordwhenSyncUserThenAddUserToHisNewCollections()
			throws Exception {

		when(bob.getStatus()).thenReturn(UserCredentialStatus.ACTIVE);
		when(bob.getCollections()).thenReturn(Arrays.asList("collection1", "collection2"));
		when(bob.getGlobalGroups()).thenReturn(Arrays.asList("group1", "group2"));

		doReturn(bobRecordInCollection1).when(userServices).getUserInCollection("bob", "collection1");
		doReturn(null).when(userServices).getUserInCollection("bob", "collection2");
		when(bobRecordInCollection1.getUserGroups()).thenReturn(Arrays.asList("group2Collection1Id", "group1Collection1Id"));
		doReturn(bobRecordInCollection2).when(userServices).newUserInCollection("collection2");
		when(bobRecordInCollection1.isDirty()).thenReturn(false);
		when(bobRecordInCollection2.isDirty()).thenReturn(true);

		userServices.sync(bob);

		ArgumentCaptor<Transaction> transactionArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);
		verify(bobRecordInCollection1, never()).setUserGroups(anyList());

		InOrder inOrder = inOrder(bobRecordInCollection2, recordServices);
		inOrder.verify(recordServices, times(2)).execute(transactionArgumentCaptor.capture());

		Transaction transaction = transactionArgumentCaptor.getValue();
		assertThat(transaction.getRecords()).hasSize(1);
		assertThat(transaction.getRecords().get(0)).isSameAs(bobRecordInCollection2Record);

	}

	//@Test
	public void whenSyncUserThenAddUserToHisNewGroups()
			throws Exception {

		when(bob.getCollections()).thenReturn(Arrays.asList("collection1", "collection2"));
		when(bob.getGlobalGroups()).thenReturn(Arrays.asList("group1", "group2"));
		when(bob.getStatus()).thenReturn(UserCredentialStatus.ACTIVE);

		doReturn(bobRecordInCollection1).when(userServices).getUserInCollection("bob", "collection1");
		doReturn(bobRecordInCollection2).when(userServices).getUserInCollection("bob", "collection2");
		when(bobRecordInCollection1.getUserGroups()).thenReturn(Arrays.asList("group2Collection1Id", "group1Collection1Id"));
		when(bobRecordInCollection2.getUserGroups()).thenReturn(Arrays.asList("group1Collection2Id"));
		when(bobRecordInCollection1.isDirty()).thenReturn(false);
		when(bobRecordInCollection2.isDirty()).thenReturn(true);

		userServices.sync(bob);

		ArgumentCaptor<Transaction> transactionArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);
		verify(bobRecordInCollection1, never()).setUserGroups(anyList());

		InOrder inOrder = inOrder(bobRecordInCollection2, recordServices);
		inOrder.verify(bobRecordInCollection2).setUserGroups(Arrays.asList("group1Collection2Id", "group2Collection2Id"));
		inOrder.verify(recordServices, times(1)).execute(transactionArgumentCaptor.capture());

		Transaction transaction = transactionArgumentCaptor.getValue();
		assertThat(transaction.getRecords()).hasSize(1);
		assertThat(transaction.getRecords().get(0)).isSameAs(bobRecordInCollection2Record);

	}

	@Test
	public void whenSyncGroupThenAddToHisNewCollections()
			throws Exception {

		when(collectionsListManager.getCollections()).thenReturn(Arrays.asList("collection1", "collection2"));
		doReturn(legendsInCollection1).when(userServices).getGroupInCollection("legends", "collection1");
		doReturn(null).when(userServices).getGroupInCollection("legends", "collection2");
		doReturn(legendsInCollection2).when(userServices).newGroupInCollection("collection2");

		when(legendsInCollection1.isDirty()).thenReturn(false);
		when(legendsInCollection2.isDirty()).thenReturn(true);

		userServices.sync(legends);

		ArgumentCaptor<Transaction> transactionArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);

		verify(recordServices, times(2)).execute(transactionArgumentCaptor.capture());

		Transaction transaction = transactionArgumentCaptor.getValue();
		assertThat(transaction.getRecords()).hasSize(1);
		assertThat(transaction.getRecords().get(0)).isSameAs(legendsInCollection2Record);

	}

	@Test
	public void whenAddGroupsToCollectionsThenSyncAllOfThem()
			throws Exception {

		when(globalGroupsManager.getActiveGroups()).thenReturn(Arrays.asList(legends, heroes));
		doNothing().when(userServices).sync(any(GlobalGroup.class));

		userServices.addGlobalGroupsInCollection("collection1");

		verify(userServices).sync(legends);
		verify(userServices).sync(heroes);

	}

	@Test
	public void whenGetUserCredentialsThenOk()
			throws Exception {
		userServices.getActiveUserCredentials();

		verify(userCredentialsManager).getActiveUserCredentials();
	}

	//

	@Test
	public void givenLDAPAuthenticationAndSyncWhenCanModifyUserAndGroupThenFalse()
			throws Exception {

		when(configurationManager.isLDAPAuthentication()).thenReturn(true);
		when(configurationManager.idUsersSynchActivated()).thenReturn(true);

		assertThat(userServices.canAddOrModifyUserAndGroup()).isFalse();
	}

	@Test
	public void givenLDAPAuthenticationAndNotSyncWhenCanModifyUserAndGroupThenTrue()
			throws Exception {

		when(configurationManager.isLDAPAuthentication()).thenReturn(true);
		when(configurationManager.idUsersSynchActivated()).thenReturn(false);

		assertThat(userServices.canAddOrModifyUserAndGroup()).isTrue();
	}

	@Test
	public void givenNotLDAPAuthenticationAndSyncWhenCanModifyUserAndGroupThenTrue()
			throws Exception {

		when(configurationManager.isLDAPAuthentication()).thenReturn(false);
		when(configurationManager.idUsersSynchActivated()).thenReturn(true);

		assertThat(userServices.canAddOrModifyUserAndGroup()).isTrue();
	}

	@Test
	public void givenNotLDAPAuthenticationAndNotSyncWhenCanModifyUserAndGroupThenTrue()
			throws Exception {

		when(configurationManager.isLDAPAuthentication()).thenReturn(false);
		when(configurationManager.idUsersSynchActivated()).thenReturn(false);

		assertThat(userServices.canAddOrModifyUserAndGroup()).isTrue();
	}

	//

	@Test
	public void givenLDPADAuthAndCurrentUserAdminWhenCanModifyHimSelfPasswordThenTrue()
			throws Exception {

		when(configurationManager.isLDAPAuthentication()).thenReturn(true);

		assertThat(userServices.canModifyPassword(admin, admin)).isTrue();

	}

	@Test
	public void givenLDPADAuthAndCurrentUserAdminWhenCanModifyAlicesPasswordThenFalse()
			throws Exception {

		when(configurationManager.isLDAPAuthentication()).thenReturn(true);

		assertThat(userServices.canModifyPassword(admin, alice)).isFalse();

	}

	@Test
	public void givenLDPADAuthAndCurrentUserAliceWhenCanModifyPasswordThenFalse()
			throws Exception {

		when(configurationManager.isLDAPAuthentication()).thenReturn(true);

		assertThat(userServices.canModifyPassword(alice, alice)).isFalse();

	}

	@Test
	public void givenNotLDPADAuthAndCurrentUserAliceWhenCanModifyPasswordThenTrue()
			throws Exception {

		when(configurationManager.isLDAPAuthentication()).thenReturn(false);

		assertThat(userServices.canModifyPassword(alice, alice)).isTrue();

	}

	@Test
	public void whenIsLDAPAuthenticationThenOk()
			throws Exception {

		when(configurationManager.isLDAPAuthentication()).thenReturn(false).thenReturn(true);

		assertThat(userServices.isLDAPAuthentication()).isFalse();
		assertThat(userServices.isLDAPAuthentication()).isTrue();

	}
}
