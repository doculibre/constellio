package com.constellio.app.utils.scripts;

import com.constellio.app.extensions.api.scripts.ConsoleScriptActionLogger;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.users.GlobalGroupsManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class DeleteInactiveGroupsScriptAcceptanceTest extends ConstellioTest {

	private RMTestRecords records = new RMTestRecords(zeCollection);
	private Users users = new Users();

	private GlobalGroupsManager globalGroupsManager;
	private UserServices userServices;
	private RecordServices recordServices;
	private CollectionsManager collectionsManager;
	private AuthorizationsServices authorizationsServices;
	private MetadataSchemasManager metadataSchemasManager;

	@Before
	public void setup() {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
				.withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList());

		globalGroupsManager = getModelLayerFactory().getGlobalGroupsManager();
		recordServices = getModelLayerFactory().newRecordServices();
		userServices = getModelLayerFactory().newUserServices();
		collectionsManager = getAppLayerFactory().getCollectionsManager();
		authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
	}

	@Test
	public void givenLogicallyCreateGlobalGroupThenGlobalGroupNotDeleted() throws Exception {
		GlobalGroup globalGroup = globalGroupsManager.create("group1", "group1Name", singletonList("user1"),
				null, GlobalGroupStatus.ACTIVE, true);
		globalGroupsManager.addUpdate(globalGroup);

		new DeleteInactiveGroupsScript(getAppLayerFactory()).execute(new ConsoleScriptActionLogger(), null);

		assertThat(globalGroupsManager.getActiveGlobalGroupWithCode(globalGroup.getCode())).isNotNull();
	}

	@Test
	public void givenGlobalGroupWithoutAuthorizationThenGlobalGroupDeleted() throws Exception {
		GlobalGroup globalGroup = globalGroupsManager.create("group1", "group1Name", singletonList("user1"),
				null, GlobalGroupStatus.ACTIVE, false);
		globalGroupsManager.addUpdate(globalGroup);

		new DeleteInactiveGroupsScript(getAppLayerFactory()).execute(new ConsoleScriptActionLogger(), null);

		assertThat(globalGroupsManager.getGlobalGroupWithCode(globalGroup.getCode())).isNull();
	}

	@Test
	public void givenGlobalGroupWithAuthorizationThenGlobalGroupNotDeleted() throws Exception {
		GlobalGroup globalGroup = globalGroupsManager.create("group1", "group1Name", singletonList("user1"),
				null, GlobalGroupStatus.ACTIVE, false);
		globalGroupsManager.addUpdate(globalGroup);

		userServices.addGlobalGroupsInCollection(zeCollection);

		Group group = userServices.getGroupInCollection(globalGroup.getCode(), zeCollection);
		authorizationsServices.add(
				AuthorizationAddRequest.authorizationForGroups(group).on(records.folder_A01).givingReadWriteDeleteAccess());

		new DeleteInactiveGroupsScript(getAppLayerFactory()).execute(new ConsoleScriptActionLogger(), null);

		assertThat(globalGroupsManager.getGlobalGroupWithCode(globalGroup.getCode())).isNotNull();
	}

	@Test
	public void givenGlobalGroupWithAuthorizationInOneCollectionThenGlobalGroupNotDeleted() throws Exception {
		String collection = "aCollection";
		collectionsManager.createCollectionInCurrentVersion(collection, singletonList("fr"));

		GlobalGroup globalGroup = globalGroupsManager.create("group1", "group1Name", singletonList("user1"),
				null, GlobalGroupStatus.ACTIVE, false);
		globalGroupsManager.addUpdate(globalGroup);

		userServices.addGlobalGroupsInCollection(collection);
		userServices.addUserToCollection(users.alice(), collection);

		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
		Record record = recordServices.newRecordWithSchema(types.getDefaultSchema("task"));
		record.set(Schemas.TITLE, "record");
		recordServices.add(record);

		Group group = userServices.getGroupInCollection(globalGroup.getCode(), collection);
		recordServices.update(users.aliceIn(collection).addUserGroups(group.getId()));
		authorizationsServices.add(
				AuthorizationAddRequest.authorizationForGroups(group).on(record).givingReadWriteDeleteAccess());

		new DeleteInactiveGroupsScript(getAppLayerFactory()).execute(new ConsoleScriptActionLogger(), null);

		assertThat(globalGroupsManager.getGlobalGroupWithCode(globalGroup.getCode())).isNotNull();
	}

	@Test
	public void givenMultipleGlobalGroupsWithoutAuthorizationThenGlobalGroupsDeleted() throws Exception {
		GlobalGroup globalGroup = globalGroupsManager.create("group1", "group1Name", singletonList("user1"),
				null, GlobalGroupStatus.ACTIVE, false);
		globalGroupsManager.addUpdate(globalGroup);

		GlobalGroup globalGroup2 = globalGroupsManager.create("group2", "group2Name", singletonList("user2"),
				null, GlobalGroupStatus.ACTIVE, false);
		globalGroupsManager.addUpdate(globalGroup2);

		userServices.addGlobalGroupsInCollection(zeCollection);

		new DeleteInactiveGroupsScript(getAppLayerFactory()).execute(new ConsoleScriptActionLogger(), null);

		assertThat(globalGroupsManager.getGlobalGroupWithCode(globalGroup.getCode())).isNull();
		assertThat(globalGroupsManager.getGlobalGroupWithCode(globalGroup2.getCode())).isNull();
	}

	@Test
	public void givenGlobalGroupWithAuthorizationAndGlobalGroupWithoutAuthorizationThenOnlyOneGlobalGroupDeleted()
			throws Exception {
		GlobalGroup globalGroup = globalGroupsManager.create("group1", "group1Name", singletonList("user1"),
				null, GlobalGroupStatus.ACTIVE, false);
		globalGroupsManager.addUpdate(globalGroup);

		GlobalGroup globalGroup2 = globalGroupsManager.create("group2", "group2Name", singletonList("user2"),
				null, GlobalGroupStatus.ACTIVE, false);
		globalGroupsManager.addUpdate(globalGroup2);

		userServices.addGlobalGroupsInCollection(zeCollection);

		Group group = userServices.getGroupInCollection(globalGroup.getCode(), zeCollection);
		recordServices.update(users.aliceIn(zeCollection).addUserGroups(group.getId()));
		authorizationsServices.add(
				AuthorizationAddRequest.authorizationForGroups(group).on(records.folder_A01).givingReadWriteDeleteAccess());

		new DeleteInactiveGroupsScript(getAppLayerFactory()).execute(new ConsoleScriptActionLogger(), null);

		assertThat(globalGroupsManager.getGlobalGroupWithCode(globalGroup.getCode())).isNotNull();
		assertThat(globalGroupsManager.getGlobalGroupWithCode(globalGroup2.getCode())).isNull();
	}

	@Test
	public void givenGlobalGroupWithUserAndWithAuthorizationThenGroupeNotDeleted() throws Exception {
		GlobalGroup globalGroup = globalGroupsManager.create("group1", "group1Name", singletonList("user1"),
				null, GlobalGroupStatus.ACTIVE, false);
		globalGroupsManager.addUpdate(globalGroup);

		userServices.addGlobalGroupsInCollection(zeCollection);

		Group group = userServices.getGroupInCollection(globalGroup.getCode(), zeCollection);
		recordServices.update(users.aliceIn(zeCollection).addUserGroups(group.getId()));
		authorizationsServices.add(
				AuthorizationAddRequest.authorizationForGroups(group).on(records.folder_A01).givingReadWriteDeleteAccess());

		new DeleteInactiveGroupsScript(getAppLayerFactory()).execute(new ConsoleScriptActionLogger(), null);

		assertThat(globalGroupsManager.getGlobalGroupWithCode(globalGroup.getCode())).isNotNull();
	}

	@Test
	public void givenGlobalGroupWithUserAndWithoutAuthorizationThenUsersRemovedAndGlobalGroupDeleted()
			throws Exception {
		GlobalGroup globalGroup = globalGroupsManager.create("group1", "group1Name", singletonList("user1"),
				null, GlobalGroupStatus.ACTIVE, false);
		globalGroupsManager.addUpdate(globalGroup);

		userServices.addGlobalGroupsInCollection(zeCollection);

		Group group = userServices.getGroupInCollection(globalGroup.getCode(), zeCollection);
		recordServices.update(users.aliceIn(zeCollection).addUserGroups(group.getId()));

		new DeleteInactiveGroupsScript(getAppLayerFactory()).execute(new ConsoleScriptActionLogger(), null);

		assertThat(globalGroupsManager.getGlobalGroupWithCode(globalGroup.getCode())).isNull();

		User alice = userServices.getUserInCollection(users.alice().getUsername(), zeCollection);
		assertThat(alice.getUserGroups()).doesNotContain(group.getId());
	}

}
