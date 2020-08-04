package com.constellio.model.services.users.sync;

import com.constellio.model.conf.LDAPTestConfig;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.services.LDAPServicesFactory;
import com.constellio.model.conf.ldap.services.LDAPServicesImpl;
import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.conf.ldap.user.LDAPUser;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserSyncMode;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.sync.model.LDAPUsersAndGroups;
import com.constellio.model.services.users.sync.model.LDAPUsersAndGroupsBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class LDAPUserSyncManagerRealTest extends ConstellioTest {
	ModelLayerFactory modelLayerFactory;

	@Mock
	private LDAPConfigurationManager ldapConfigurationManager;

	@Mock
	LDAPServicesFactory ldapServicesFactory;

	@Mock
	LDAPServicesImpl ldapServices;


	private LDAPServerConfiguration ldapServerConfiguration = LDAPTestConfig.getLDAPServerConfiguration();
	private LDAPUserSyncConfiguration ldapUserSyncConfiguration = LDAPTestConfig.getLDAPUserSyncConfiguration();

	@Before
	public void setup()
			throws Exception {
		//givenConstellioProperties(LDAPTestConfig.getConfigMap());
		prepareSystem(withZeCollection(), withCollection(businessCollection));
		modelLayerFactory = getModelLayerFactory();

		saveValidLDAPConfigWithEntrepriseCollectionSelected();

		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);
	}


	@Test
	public void givenUserSyncConfiguredThenRunAndVerifyImportedUserSync()
			throws Exception {

		LDAPGroup cabailleros = cabailleros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(cabailleros, pilotes))
				.add(philippe().addGroup(cabailleros))
				.add(dusty().addGroup(cabailleros))
				.add(cabailleros, pilotes)
				.build());

		sync();

		User importedUser = user("Nicolas", businessCollection);
		UserCredential userCredentialImportedUser = user("Nicolas");
		assertThat(importedUser.getFirstName()).isEqualTo("Nicolas");
		assertThat(importedUser.getLastName()).isEqualTo("Belisle");
		assertThat(importedUser.getEmail()).isEqualTo("nicolas@doculibre.ca");
		assertThat(importedUser.getMsExchDelegateListBL()).isEmpty();
		assertThat(userCredentialImportedUser.getSyncMode()).isEqualTo(UserSyncMode.SYNCED);

		User importedUser2 = user("Philippe", businessCollection);
		UserCredential userCredentialImportedUser2 = user("Philippe");
		assertThat(importedUser2.getFirstName()).isEqualTo("Philippe");
		assertThat(importedUser2.getLastName()).isEqualTo("Houle");
		assertThat(importedUser2.getEmail()).isEqualTo("philippe@doculibre.ca");
		assertThat(importedUser2.getMsExchDelegateListBL()).isEmpty();
		assertThat(userCredentialImportedUser2.getSyncMode()).isEqualTo(UserSyncMode.SYNCED);

		User importedUser3 = user("Dusty", businessCollection);
		UserCredential userCredentialImportedUser3 = user("Dusty");
		assertThat(importedUser3.getFirstName()).isEqualTo("Dusty");
		assertThat(importedUser3.getLastName()).isEqualTo("Chien");
		assertThat(importedUser3.getEmail()).isEqualTo("dusty@doculibre.ca");
		assertThat(importedUser3.getMsExchDelegateListBL()).isEmpty();
		assertThat(userCredentialImportedUser3.getSyncMode()).isEqualTo(UserSyncMode.SYNCED);

	}

	@Test
	public void givenUserSyncConfiguredThenRunThenVerifyLocallyCreatedUsersAreThere()
			throws Exception {

		LDAPGroup cabailleros = cabailleros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(cabailleros, pilotes))
				.add(philippe().addGroup(cabailleros))
				.add(chuck().addGroup(pilotes))
				.add(dusty().addGroup(cabailleros))
				.add(cabailleros, pilotes)
				.build());

		sync();

		User userAdmin = user(admin, businessCollection);
		UserCredential userCredentialAdmin = user(admin);
		User localUser = user(chuck, businessCollection);
		User localUser2 = user(gandalf, businessCollection);
		UserCredential userCredentialLocalUser = user(chuck);
		UserCredential userCredentialLocalUser2 = user(gandalf);

		assertThat(userAdmin).isNotNull();
		assertThat(userCredentialAdmin).isNotNull();
		assertThat(localUser).isNotNull();
		assertThat(userCredentialLocalUser).isNotNull();
		assertThat(localUser2).isNotNull();
		assertThat(userCredentialLocalUser2).isNotNull();

		assertThat(userCredentialAdmin.getSyncMode()).isEqualTo(UserSyncMode.LOCALLY_CREATED);
		assertThat(userCredentialLocalUser.getSyncMode()).isEqualTo(UserSyncMode.LOCALLY_CREATED);
		assertThat(userCredentialLocalUser2.getSyncMode()).isEqualTo(UserSyncMode.LOCALLY_CREATED);
		assertThat(localUser.getEmail()).isEqualTo("chuck@doculibre.com");
	}

	@Test
	public void givenUserSyncConfiguredThenModifyUserAndDesyncAndRunAgain()
			throws Exception {

		LDAPGroup cabailleros = cabailleros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(cabailleros, pilotes))
				.add(philippe().addGroup(cabailleros))
				.add(dusty().addGroup(cabailleros))
				.add(cabailleros, pilotes)
				.build());

		sync();

		User importedUser2 = user("Philippe", businessCollection);
		UserCredential userCredentialImportedUser2 = user("Philippe");
		assertThat(importedUser2.getFirstName()).isEqualTo("Philippe");
		assertThat(importedUser2.getLastName()).isEqualTo("Houle");
		assertThat(importedUser2.getEmail()).isEqualTo("philippe@doculibre.ca");
		assertThat(importedUser2.getMsExchDelegateListBL()).isEmpty();
		assertThat(userCredentialImportedUser2.getSyncMode()).isEqualTo(UserSyncMode.SYNCED);

		//desync philippe and modify name and email
		importedUser2 = importedUser2.setFirstName("Phillip").setEmail("phillip@doculibre.ca");
		userCredentialImportedUser2 = userCredentialImportedUser2.setSyncMode(UserSyncMode.NOT_SYNCED);

		saveChangedUser(importedUser2, userCredentialImportedUser2);

		sync();

		//Phillip is not synced, therefore keeps his change
		importedUser2 = user("Philippe", businessCollection);
		userCredentialImportedUser2 = user("Philippe");
		assertThat(importedUser2.getFirstName()).isEqualTo("Phillip");
		assertThat(importedUser2.getEmail()).isEqualTo("phillip@doculibre.ca");
		assertThat(userCredentialImportedUser2.getSyncMode()).isEqualTo(UserSyncMode.NOT_SYNCED);

		//putting back on sync should resync user
		userCredentialImportedUser2 = userCredentialImportedUser2.setSyncMode(UserSyncMode.SYNCED);
		saveChangedUser(importedUser2, userCredentialImportedUser2);
		sync();

		importedUser2 = user("Philippe", businessCollection);
		userCredentialImportedUser2 = user("Philippe");
		assertThat(importedUser2.getFirstName()).isEqualTo("Philippe");
		assertThat(importedUser2.getLastName()).isEqualTo("Houle");
		assertThat(importedUser2.getEmail()).isEqualTo("philippe@doculibre.ca");
		assertThat(userCredentialImportedUser2.getSyncMode()).isEqualTo(UserSyncMode.SYNCED);

	}

	@Test
	public void givenUserSyncConfiguredThenModifyUserAndRunAgain()
			throws Exception {

		LDAPGroup cabailleros = cabailleros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(cabailleros, pilotes))
				.add(philippe().addGroup(cabailleros))
				.add(dusty().addGroup(cabailleros))
				.add(cabailleros, pilotes)
				.build());

		sync();

		User importedUser = user("Nicolas", businessCollection);
		UserCredential userCredentialImportedUser = user("Nicolas");
		assertThat(importedUser.getFirstName()).isEqualTo("Nicolas");
		assertThat(importedUser.getLastName()).isEqualTo("Belisle");
		assertThat(importedUser.getEmail()).isEqualTo("nicolas@doculibre.ca");
		assertThat(importedUser.getMsExchDelegateListBL()).isEmpty();
		assertThat(userCredentialImportedUser.getSyncMode()).isEqualTo(UserSyncMode.SYNCED);

		//Modify name and email for Nicolas
		importedUser = importedUser.setFirstName("NicoNico").setEmail("niiiiiiiii@doculibre.ca");

		saveChangedUser(importedUser, userCredentialImportedUser);

		sync();

		//Nicolas is synchronized again
		importedUser = user("Nicolas", businessCollection);
		userCredentialImportedUser = user("Nicolas");
		assertThat(importedUser.getFirstName()).isEqualTo("Nicolas");
		assertThat(importedUser.getEmail()).isEqualTo("nicolas@doculibre.ca");
		assertThat(userCredentialImportedUser.getSyncMode()).isEqualTo(UserSyncMode.SYNCED);
	}

	@Test
	public void givenSyncThenGroupsAreSyncedWithUsers()
			throws Exception {

		LDAPGroup cabailleros = cabailleros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(cabailleros, pilotes))
				.add(philippe().addGroup(cabailleros))
				.add(dusty().addGroup(cabailleros))
				.add(cabailleros, pilotes)
				.build());

		sync();

		Group importedGroup = group("cabailleros", businessCollection);
		List<User> usersInCabailleros = users(importedGroup);
		User importedUserPhil = user("Philippe", businessCollection);
		User importedUserNicolas = user("Nicolas", businessCollection);
		User importedUserDusty = user("Dusty", businessCollection);
		assertThat(usersInCabailleros).containsAll(asList(importedUserNicolas, importedUserPhil, importedUserDusty));
		assertThat(importedGroup.getCode()).isEqualTo("cabailleros");

	}

	@Test
	public void givenSyncThenGroupsAreSyncedThenExistingGroupWithLocallyCreatedUsersKeepsThem()
			throws Exception {

		LDAPGroup cabailleros = cabailleros();
		LDAPGroup pilotes = pilotes();
		LDAPGroup rumors = rumors();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(cabailleros, pilotes))
				.add(philippe().addGroups(cabailleros, rumors))
				.add(dusty().addGroup(cabailleros))
				.add(cabailleros, pilotes, rumors)
				.build());
		sync();

		Group importedGroup = group("rumors", businessCollection);
		List<User> usersInRumors = users(importedGroup);
		User importedUserPhil = user("Philippe", businessCollection);
		User importedUserChuck = user(chuck, businessCollection);
		assertThat(usersInRumors).contains(importedUserChuck, importedUserPhil);

	}

	// ----- Utility methods

	private void saveValidLDAPConfigWithEntrepriseCollectionSelected() {
		this.ldapServerConfiguration = LDAPTestConfig.getLDAPServerConfiguration();
		this.ldapUserSyncConfiguration = LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(
				asList(businessCollection));

	}

	private OngoingStubbing<LDAPUsersAndGroups> whenRetrievingInfosFromLDAP() {
		return when(ldapServices.importUsersAndGroups(any(LDAPServerConfiguration.class), any(LDAPUserSyncConfiguration.class), anyString()));
	}

	private UserCredential user(String code) {
		return modelLayerFactory.newUserServices().getUserCredential(code);
	}

	private User user(String username, String collection) {
		return modelLayerFactory.newUserServices().getUserInCollection(username, collection);
	}

	private List<User> users(Group group) {
		return modelLayerFactory.newUserServices().getAllUsersInGroup(group, false, true);
	}

	private Group group(String username, String collection) {
		return modelLayerFactory.newUserServices().getGroupInCollection(username, collection);
	}

	private void sync() {
		LDAPUserSyncManager ldapUserSyncManager = new LDAPUserSyncManager(this.ldapConfigurationManager, modelLayerFactory.newRecordServices(),
				modelLayerFactory.getDataLayerFactory(), modelLayerFactory.newUserServices(),
				modelLayerFactory.getDataLayerFactory().getConstellioJobManager(), ldapServicesFactory);

		ldapUserSyncManager.synchronizeIfPossible();
	}


	private LDAPUser nicolas() {
		return new LDAPUser().setEmail("nicolas@doculibre.ca").setFamilyName("Belisle")
				.setName("Nicolas").setGivenName("Nicolas");
	}

	private LDAPUser dusty() {
		return new LDAPUser().setEmail("dusty@doculibre.ca").setFamilyName("Chien")
				.setName("Dusty").setGivenName("Dusty");
	}

	private LDAPUser philippe() {
		return new LDAPUser().setEmail("philippe@doculibre.ca").setFamilyName("Houle")
				.setName("Philippe").setGivenName("Philippe");
	}

	private LDAPUser chuck() {
		return new LDAPUser().setEmail("chuckofldap@doculibre.ca").setFamilyName("Norris")
				.setName("Chuck").setGivenName("Chuck");
	}

	private LDAPGroup cabailleros() {
		return new LDAPGroup("cabailleros", "The three cabaielleros");
	}

	private LDAPGroup pilotes() {
		return new LDAPGroup("pilotes", "Les pilotes du ciel");
	}

	private LDAPGroup rumors() {
		return new LDAPGroup("rumors", "The rumors");
	}

	private void saveChangedUser(User user, UserCredential userCredential) throws RecordServicesException {
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		recordServices.update(user);
		recordServices.update(userCredential);
	}

}
