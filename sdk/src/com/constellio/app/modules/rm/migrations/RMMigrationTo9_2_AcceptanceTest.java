package com.constellio.app.modules.rm.migrations;

import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.model.entities.schemas.entries.DataEntryType.CALCULATED;
import static org.assertj.core.api.Assertions.assertThat;

public class RMMigrationTo9_2_AcceptanceTest extends ConstellioTest {

	@Test
	public void givenSystemIn9_0WithLDAPValidateMigrationOf9_0And9_2ThenOk() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "given_system_in_9.0.3_ldap.zip");

		getCurrentTestSession().getFactoriesTestFeatures()
				.givenSystemInState(state).withPasswordsReset()
				.withFakeEncryptionServices();

		ModelLayerFactory modelLayerFactory = getModelLayerFactory();

		UserServices userServices = modelLayerFactory.newUserServices();

		List<String> collections = getAppLayerFactory().getCollectionsManager().getCollectionCodes();

		//Verify users

		List<User> users = new ArrayList<>();
		for (String collection :
				collections) {
			users.addAll(userServices.getAllUsersInCollection(collection));
		}

		List<SystemWideUserInfos> credentials = userServices.getAllUserCredentials();

		//Les UserCredential sans collection sont supprimés?
		List<SystemWideUserInfos> userCredentialsWithCollection = credentials.stream()
				.filter(credential -> !credential.getCollections().isEmpty())
				.collect(Collectors.toList());

		//Check a certain user is not there? In LDAP case they are added to system collection?

		MetadataSchema credentialMetadataSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(zeCollection).getDefaultSchema(UserCredential.SCHEMA_TYPE);
		MetadataSchema userMetadataSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(zeCollection).getDefaultSchema(User.SCHEMA_TYPE);
		MetadataSchema groupMetadataSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(zeCollection).getDefaultSchema(Group.SCHEMA_TYPE);
		MetadataSchema globalGroupMetadataSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(zeCollection).getDefaultSchema(GlobalGroup.SCHEMA_TYPE);

		//SyncMode est dans les utilisateurs?
		assertThat(credentialMetadataSchema.getMetadata(UserCredential.SYNC_MODE).getDataEntry().getType()).isEqualTo(CALCULATED);
		for (SystemWideUserInfos user : credentials) {
			assertThat(user.getSyncMode()).isNotNull();
		}

		//Metadonnées retirées?
		assertThat(credentialMetadataSchema.hasMetadataWithCode("firstname")).isFalse();
		assertThat(credentialMetadataSchema.hasMetadataWithCode("lastname")).isFalse();
		assertThat(credentialMetadataSchema.hasMetadataWithCode("email")).isFalse();
		assertThat(credentialMetadataSchema.hasMetadataWithCode("personalEmails")).isFalse();
		assertThat(credentialMetadataSchema.hasMetadataWithCode("phone")).isFalse();
		assertThat(credentialMetadataSchema.hasMetadataWithCode("globalGroups")).isFalse();
		assertThat(credentialMetadataSchema.hasMetadataWithCode("fax")).isFalse();
		assertThat(credentialMetadataSchema.hasMetadataWithCode("jobTitle")).isFalse();
		assertThat(credentialMetadataSchema.hasMetadataWithCode("address")).isFalse();

		//Métadonnées déplacés dans users?
		assertThat(userMetadataSchema.hasMetadataWithCode(User.DOMAIN)).isTrue();
		assertThat(userMetadataSchema.hasMetadataWithCode(User.MS_EXCHANGE_DELEGATE_LIST)).isTrue();

		//Les utilisateurs qui n'étaient pas actifs sont supprimés logiquement?
		List<SystemWideUserInfos> credentialsDeleted = credentials.stream()
				.filter(x -> !x.getStatus().equals(UserCredentialStatus.ACTIVE))
				.collect(Collectors.toList());
		for (SystemWideUserInfos credential : credentialsDeleted) {
			assertThat(credential.getStatus()).isEqualTo(UserCredentialStatus.DISABLED);
		}

		//physically removed users if not used. Need to be encoded.

		//Verify Groups

		List<Group> groups = new ArrayList<>();
		for (String collection :
				collections) {
			groups.addAll(userServices.getAllGroupsInCollections(collection));
		}

		assertThat(globalGroupMetadataSchema.hasMetadataWithCode("usersAutomaticallyAddedToCollections")).isFalse();
		assertThat(globalGroupMetadataSchema.hasMetadataWithCode("status")).isFalse();
		assertThat(globalGroupMetadataSchema.hasMetadataWithCode("locallyCreated")).isFalse();
		assertThat(globalGroupMetadataSchema.hasMetadataWithCode("hierarchy")).isFalse();
		assertThat(groupMetadataSchema.hasMetadataWithCode("status")).isTrue();
		assertThat(groupMetadataSchema.hasMetadataWithCode("locallyCreated")).isTrue();
		assertThat(groupMetadataSchema.hasMetadataWithCode("hierarchy")).isTrue();

	}
}
