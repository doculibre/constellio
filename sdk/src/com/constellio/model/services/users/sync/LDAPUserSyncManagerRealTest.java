package com.constellio.model.services.users.sync;

import com.constellio.data.utils.dev.Toggle;
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
import com.constellio.model.entities.security.global.GroupAddUpdateRequest;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.UserSyncMode;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserAddUpdateRequest;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.sync.model.LDAPUsersAndGroups;
import com.constellio.model.services.users.sync.model.LDAPUsersAndGroupsBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import java.util.List;
import java.util.stream.Collectors;

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

	private final String extraCollection = "extraCollection";

	@Before
	public void setup()
			throws Exception {
		//givenConstellioProperties(LDAPTestConfig.getConfigMap());
		prepareSystem(withZeCollection().withAllTestUsers(),
				withCollection(businessCollection).withAllTestUsers(),
				withCollection(extraCollection).withAllTestUsers());
		modelLayerFactory = getModelLayerFactory();

		saveValidLDAPConfigWithEntrepriseCollectionSelected();

		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);
	}


	@Test
	public void givenUserSyncConfiguredThenRunAndVerifyImportedUserSync()
			throws Exception {

		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes)
				.build());

		sync();

		UserCredential userCredentialImportedUser = user("nicolas");
		User importedUser = user("nicolas", businessCollection);
		assertThat(importedUser.getFirstName()).isEqualTo("Nicolas");
		assertThat(importedUser.getLastName()).isEqualTo("Belisle");
		assertThat(importedUser.getEmail()).isEqualTo("nicolas@doculibre.ca");
		assertThat(importedUser.getMsExchDelegateListBL()).isEmpty();
		assertThat(userCredentialImportedUser.getSyncMode()).isEqualTo(UserSyncMode.SYNCED);

		User importedUser2 = user("philippe", businessCollection);
		UserCredential userCredentialImportedUser2 = user("philippe");
		assertThat(importedUser2.getFirstName()).isEqualTo("Philippe");
		assertThat(importedUser2.getLastName()).isEqualTo("Houle");
		assertThat(importedUser2.getEmail()).isEqualTo("philippe@doculibre.ca");
		assertThat(importedUser2.getMsExchDelegateListBL()).isEmpty();
		assertThat(userCredentialImportedUser2.getSyncMode()).isEqualTo(UserSyncMode.SYNCED);

		User importedUser3 = user("dusty", businessCollection);
		UserCredential userCredentialImportedUser3 = user("dusty");
		assertThat(importedUser3.getFirstName()).isEqualTo("Dusty");
		assertThat(importedUser3.getLastName()).isEqualTo("Chien");
		assertThat(importedUser3.getEmail()).isEqualTo("dusty@doculibre.ca");
		assertThat(importedUser3.getMsExchDelegateListBL()).isEmpty();
		assertThat(userCredentialImportedUser3.getSyncMode()).isEqualTo(UserSyncMode.SYNCED);

	}

	/**
	 * Lorsqu’un utilisateur créé localement est rapporté, son mode change pour synchronisé
	 *
	 * @throws Exception
	 */
	@Test
	public void givenUserSyncConfiguredThenRunThenVerifyLocallyCreatedUsersAreSynced()
			throws Exception {

		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(chuck().addGroup(pilotes))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes)
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
		assertThat(userCredentialLocalUser.getSyncMode()).isEqualTo(UserSyncMode.SYNCED);
		assertThat(userCredentialLocalUser2.getSyncMode()).isEqualTo(UserSyncMode.LOCALLY_CREATED);
		assertThat(localUser.getEmail()).isEqualTo("chuckofldap@doculibre.ca");
	}

	/**
	 * Lorsqu’un utilisateur créé localement dans une collection synchronisée est rapporté, il est ajouté aux autres collections synchronisée
	 *
	 * @throws Exception
	 */
	@Test
	public void givenUserCreatedLocallyInOneCollectionAndSameUserFetchedFromLDAPSyncThenAllUserInChosenCollectionsSynced()
			throws Exception {
		this.ldapUserSyncConfiguration =
				LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(asList(businessCollection, zeCollection));
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);
		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes)
				.build());

		createLocalDusty(true, asList(businessCollection));
		User businessDusty = user("dusty", businessCollection);
		assertThat(businessDusty).isNotNull();

		sync();

		businessDusty = user("dusty", businessCollection);
		UserCredential userCredentialImportedUser2 = user("dusty");
		assertThat(businessDusty.getFirstName()).isEqualTo("Dusty");
		assertThat(businessDusty.getLastName()).isEqualTo("Chien");
		assertThat(businessDusty.getEmail()).isEqualTo("dusty@doculibre.ca");
		assertThat(userCredentialImportedUser2.getSyncMode()).isEqualTo(UserSyncMode.SYNCED);

		User zeDusty = user("dusty", zeCollection);
		UserCredential userCredentialZeDusty = user("dusty");
		assertThat(zeDusty.getFirstName()).isEqualTo("Dusty");
		assertThat(zeDusty.getLastName()).isEqualTo("Chien");
		assertThat(zeDusty.getEmail()).isEqualTo("dusty@doculibre.ca");
		assertThat(userCredentialZeDusty.getSyncMode()).isEqualTo(UserSyncMode.SYNCED);

	}


	@Test
	public void givenUserIsNotActiveAndIsSyncedChangeStatusToActive() {
		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes)
				.build());

		createLocalDusty(false, asList(businessCollection));

		User businessDusty = user("dusty", businessCollection);
		assertThat(businessDusty.getStatus()).isEqualTo(UserCredentialStatus.DISABLED);

		sync();

		businessDusty = user("dusty", businessCollection);
		assertThat(businessDusty.getStatus()).isEqualTo(UserCredentialStatus.ACTIVE);
	}

	/**
	 * Lorsqu’un utilisateur au statut inactif est rapporté, son statut change pour actif
	 *
	 * @throws Exception
	 */
	@Test
	public void givenUserSyncConfiguredThenModifyUserAndDesyncAndRunAgain()
			throws Exception {
		UserServices userServices = modelLayerFactory.newUserServices();

		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes)
				.build());

		sync();

		User importedUser2 = user("philippe", businessCollection);
		UserCredential userCredentialImportedUser2 = user("philippe");
		assertThat(importedUser2.getFirstName()).isEqualTo("Philippe");
		assertThat(importedUser2.getLastName()).isEqualTo("Houle");
		assertThat(importedUser2.getEmail()).isEqualTo("philippe@doculibre.ca");
		assertThat(importedUser2.getMsExchDelegateListBL()).isEmpty();
		assertThat(userCredentialImportedUser2.getSyncMode()).isEqualTo(UserSyncMode.SYNCED);

		//desync philippe and modify name and email
		UserAddUpdateRequest request = userServices.addUpdate("philippe")
				.setFirstName("Phillip")
				.setEmail("phillip@doculibre.ca")
				.setCollections(asList(businessCollection))
				.stopSyncingLDAP();
		userServices.execute(request);

		sync();

		//Phillip is not synced, therefore keeps his change
		importedUser2 = user("philippe", businessCollection);
		userCredentialImportedUser2 = user("philippe");
		assertThat(importedUser2.getFirstName()).isEqualTo("Phillip");
		assertThat(importedUser2.getEmail()).isEqualTo("phillip@doculibre.ca");
		assertThat(userCredentialImportedUser2.getSyncMode()).isEqualTo(UserSyncMode.NOT_SYNCED);

		//putting back on sync should resync user
		UserAddUpdateRequest request2 = userServices.addUpdate("philippe")
				.setCollections(asList(businessCollection))
				.resumeSyncingLDAP();
		userServices.execute(request2);

		sync();

		importedUser2 = user("philippe", businessCollection);
		userCredentialImportedUser2 = user("philippe");
		assertThat(importedUser2.getFirstName()).isEqualTo("Philippe");
		assertThat(importedUser2.getLastName()).isEqualTo("Houle");
		assertThat(importedUser2.getEmail()).isEqualTo("philippe@doculibre.ca");
		assertThat(userCredentialImportedUser2.getSyncMode()).isEqualTo(UserSyncMode.SYNCED);

	}

	/**
	 * Lorsqu’un utilisateur synchronisé est rapporté, son nom, prénom et email est ajusté
	 *
	 * @throws Exception
	 */
	@Test
	public void givenUserSyncConfiguredThenModifyUserAndRunAgain()
			throws Exception {

		UserServices userServices = modelLayerFactory.newUserServices();

		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes)
				.build());

		sync();

		User importedUser = user("nicolas", businessCollection);
		UserCredential userCredentialImportedUser = user("nicolas");
		assertThat(importedUser.getFirstName()).isEqualTo("Nicolas");
		assertThat(importedUser.getLastName()).isEqualTo("Belisle");
		assertThat(importedUser.getEmail()).isEqualTo("nicolas@doculibre.ca");
		assertThat(importedUser.getMsExchDelegateListBL()).isEmpty();
		assertThat(userCredentialImportedUser.getSyncMode()).isEqualTo(UserSyncMode.SYNCED);

		//Modify name and email for Nicolas

		UserAddUpdateRequest request = userServices.addUpdate("nicolas")
				.setFirstName("NicoNico")
				.setEmail("niiiiiiiii@doculibre.ca")
				.setCollections(asList(businessCollection))
				.ldapSyncRequest();

		userServices.execute(request);

		sync();

		//Nicolas is synchronized again
		importedUser = user("nicolas", businessCollection);
		userCredentialImportedUser = user("nicolas");
		assertThat(importedUser.getFirstName()).isEqualTo("Nicolas");
		assertThat(importedUser.getEmail()).isEqualTo("nicolas@doculibre.ca");
		assertThat(userCredentialImportedUser.getSyncMode()).isEqualTo(UserSyncMode.SYNCED);
	}

	/**
	 * Lorsqu’un utilisateur synchronisé est rapporté avec son nom, prénom et email différent, l'utilisateur est ajusté
	 *
	 * @throws Exception
	 */
	@Test
	public void givenUserSyncConfiguredAndNameAndEmailChangedThenModifyUser()
			throws Exception {

		UserServices userServices = modelLayerFactory.newUserServices();

		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes)
				.build());

		sync();

		User importedUser = user("nicolas", businessCollection);
		UserCredential userCredentialImportedUser = user("nicolas");
		assertThat(importedUser.getFirstName()).isEqualTo("Nicolas");
		assertThat(importedUser.getLastName()).isEqualTo("Belisle");
		assertThat(importedUser.getEmail()).isEqualTo("nicolas@doculibre.ca");
		assertThat(importedUser.getMsExchDelegateListBL()).isEmpty();
		assertThat(userCredentialImportedUser.getSyncMode()).isEqualTo(UserSyncMode.SYNCED);

		//Modify name and email for Nicolas in LDAP

		caballeros = caballeros();
		pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas2().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes)
				.build());

		sync();

		//Nicolas is synchronized again
		importedUser = user("nicolas", businessCollection);
		userCredentialImportedUser = user("nicolas");
		assertThat(importedUser.getFirstName()).isEqualTo("NicoNico");
		assertThat(importedUser.getEmail()).isEqualTo("niiiiiiiii@doculibre.ca");
		assertThat(userCredentialImportedUser.getSyncMode()).isEqualTo(UserSyncMode.SYNCED);
		assertThat(importedUser.getStatus()).isEqualTo(UserCredentialStatus.ACTIVE);
	}

	/**
	 * Lorsqu’un utilisateur synchronisé n’est pas rapporté, il est supprimé (lorsque possible) dans toutes les collections synchronisées et seulement celles-ci
	 */
	@Test
	public void givenAUserIsNotFetchedFromLDAPAndHisSyncedInConstellioThenDeleteInChosenCollections() {
		this.ldapUserSyncConfiguration =
				LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(asList(businessCollection, zeCollection, extraCollection));
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);
		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes)
				.build());

		sync();

		//remove Philippe
		this.ldapUserSyncConfiguration =
				LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(asList(businessCollection, zeCollection));
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes)
				.build());

		sync();

		User importedUser = user("philippe", businessCollection);
		UserCredential userCredentialImportedUser = user("philippe");
		assertThat(importedUser.getFirstName()).isEqualTo("Philippe");
		assertThat(importedUser.getEmail()).isEqualTo("philippe@doculibre.ca");
		assertThat(userCredentialImportedUser.getSyncMode()).isEqualTo(UserSyncMode.SYNCED);
	}

	/**
	 * Lorsqu’un utilisateur synchronisé est rapporté avec une nouvelle assignation, celle-ci est appliquée dans toutes les collections synchronisées et seulement celles-ci
	 */
	@Test
	public void WhenUserSyncedWithANewGroupAssignedApplyOnEveryChosenCollectionsOnSync() {
		this.ldapUserSyncConfiguration =
				LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(asList(businessCollection, zeCollection, extraCollection));
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);
		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes)
				.build());

		sync();

		this.ldapUserSyncConfiguration =
				LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(asList(businessCollection, zeCollection));
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroups(caballeros, pilotes))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes)
				.build());

		sync();


		User businessPhil = user("philippe", businessCollection);
		User zePhil = user("philippe", zeCollection);
		User extraPhil = user("philippe", extraCollection);
		Group businessCaballeros = group("caballeros", businessCollection);
		Group zeCaballeros = group("caballeros", zeCollection);
		Group extraCaballeros = group("caballeros", extraCollection);
		Group businessPilotes = group("pilotes", businessCollection);
		Group zePilotes = group("pilotes", zeCollection);

		assertThat(businessPhil.getUserGroups()).containsAll(asList(businessCaballeros.getId(), businessPilotes.getId()));
		assertThat(zePhil.getUserGroups()).containsAll(asList(zeCaballeros.getId(), zePilotes.getId()));
		assertThat(extraPhil.getUserGroups()).containsOnly(extraCaballeros.getId());
	}

	/**
	 * Lorsqu’un utilisateur synchronisé est rapporté sans une assignation à un groupe synchronisé, celle-ci est retirée dans toutes les collections synchronisées et seulement celles-ci
	 */
	@Test
	public void WhenUserSyncedWithoutAGroupAssignedPreviouslyApplyOnEveryChosenCollectionsOnSync() {
		this.ldapUserSyncConfiguration =
				LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(asList(businessCollection, zeCollection, extraCollection));
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);
		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes)
				.build());

		sync();

		this.ldapUserSyncConfiguration =
				LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(asList(businessCollection, zeCollection));
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(pilotes))
				.add(philippe().addGroups(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes)
				.build());

		sync();

		User businessNic = user("nicolas", businessCollection);
		User zeNic = user("nicolas", zeCollection);
		User extraNic = user("nicolas", extraCollection);

		Group businessPilotes = group("pilotes", businessCollection);
		Group zePilotes = group("pilotes", zeCollection);
		Group extraPilotes = group("pilotes", extraCollection);
		Group extraCaballeros = group("caballeros", extraCollection);

		assertThat(businessNic.getUserGroups()).containsOnly(businessPilotes.getId());
		assertThat(zeNic.getUserGroups()).containsOnly(zePilotes.getId());
		assertThat(extraNic.getUserGroups()).containsAll(asList(extraCaballeros.getId(), extraPilotes.getId()));
	}

	/**
	 * Lorsqu’un utilisateur synchronisé est rapporté, il conserve ses assignations aux groupes non-synchronisés dans les collections synchronisée
	 */
	@Test
	public void givenSyncUsersAreAssignedToLocallyCreatedGroupTheyKeepAssignationOnSync() {
		UserServices userServices = modelLayerFactory.newUserServices();
		this.ldapUserSyncConfiguration =
				LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(asList(businessCollection, zeCollection));
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);

		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes)
				.build());

		sync();

		UserAddUpdateRequest userAddUpdateRequestBusiness = userServices.addUpdate("nicolas");
		userAddUpdateRequestBusiness.addToGroupsInCollection(asList("heroes"), businessCollection);
		userAddUpdateRequestBusiness.addToGroupsInCollection(asList("legends"), zeCollection);
		userServices.execute(userAddUpdateRequestBusiness);

		User businessNic = user("nicolas", businessCollection);
		Group localHeroes = group("heroes", businessCollection);
		assertThat(businessNic.getUserGroups()).contains(localHeroes.getId());

		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes)
				.build());

		sync();

		businessNic = user("nicolas", businessCollection);
		User zeNic = user("nicolas", zeCollection);

		localHeroes = group("heroes", businessCollection);
		Group localLegends = group("legends", zeCollection);

		assertThat(businessNic.getUserGroups()).contains(localHeroes.getId());
		assertThat(zeNic.getUserGroups()).contains(localLegends.getId());
	}

	/**
	 * Lorsqu’un utilisateur synchronisé est rapporté, il conserve ses assignations à tous ses groupes (synchronisés ou non) dans les collections non-synchronisée
	 */
	@Test
	public void givenSyncUsersAreAssignedToLocallyCreatedGroupTheyKeepAssignationWhenCollectionIsNotSynchronized() {
		UserServices userServices = modelLayerFactory.newUserServices();
		this.ldapUserSyncConfiguration =
				LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(asList(businessCollection, zeCollection, extraCollection));
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);

		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes)
				.build());

		sync();

		UserAddUpdateRequest userAddUpdateRequestBusiness = new UserAddUpdateRequest("nicolas", asList(businessCollection), newArrayList());
		userAddUpdateRequestBusiness.addToGroupsInCollection(asList("heroes"), businessCollection);
		userServices.execute(userAddUpdateRequestBusiness);

		this.ldapUserSyncConfiguration =
				LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(asList(zeCollection, extraCollection));
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);

		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes)
				.build());

		sync();

		User businessNic = user("nicolas", businessCollection);

		Group localHeroes = group("heroes", businessCollection);
		Group localPilotes = group("pilotes", businessCollection);
		Group localCaballeros = group("caballeros", businessCollection);

		assertThat(businessNic.getUserGroups()).containsAll(asList(localHeroes.getId(), localPilotes.getId(), localCaballeros.getId()));
	}

	/**
	 * Lorsqu’un groupe créé localement est rapporté, il devient synchronisé et ses assignations aux utilisateurs synchronisées sont perdues
	 */
	@Test
	public void whenGroupLocallyCreatedIsSyncedLoseUserSyncsWhoAreNotAssignedFromLDAP() {
		UserServices userServices = modelLayerFactory.newUserServices();

		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(pilotes))
				.add(caballeros, pilotes)
				.build());

		sync();

		UserAddUpdateRequest userAddUpdateRequestBusiness = new UserAddUpdateRequest("nicolas", asList(businessCollection), newArrayList());
		userAddUpdateRequestBusiness.addToGroupsInCollection(asList("rumors"), businessCollection);
		userServices.execute(userAddUpdateRequestBusiness);

		LDAPGroup rumorsSync = rumors();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroups(caballeros, rumorsSync))
				.add(dusty().addGroups(pilotes, rumorsSync))
				.add(caballeros, pilotes, rumorsSync)
				.build());

		sync();

		User businessNic = user("nicolas", businessCollection);

		assertThat(businessNic.getUserGroups()).doesNotContain("rumors");
	}

	/**
	 * Lorsqu’un groupe créé localement est rapporté, il devient synchronisé et ses assignations aux
	 * utilisateurs créés localement sont conservées
	 */
	@Test
	public void whenGroupLocallyCreatedIsSyncedLocallyCreatedUsersKeepTheirAssignationToGroup() {

		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(pilotes))
				.add(caballeros, pilotes)
				.build());

		sync();

		LDAPGroup rumorsSync = rumors();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroups(caballeros, rumorsSync))
				.add(dusty().addGroups(pilotes, rumorsSync))
				.add(caballeros, pilotes, rumorsSync)
				.build());

		sync();

		User bigFoot = user(sasquatch, businessCollection);

		Group group = group(rumors, businessCollection);

		assertThat(bigFoot.getUserGroups()).contains(group.getId());
	}

	/**
	 * Lorsqu’un groupe créé localement est rapporté, il devient synchronisé et ses assignations aux
	 * utilisateurs non-synchronisées sont conservées
	 */

	@Test
	public void whenGroupLocallyCreatedIsSyncedUnsyncUsersKeepTheirAssignationToGroup() {
		UserServices userServices = modelLayerFactory.newUserServices();
		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(pilotes))
				.add(caballeros, pilotes)
				.build());

		sync();

		UserAddUpdateRequest userAddUpdateRequestBusiness = new UserAddUpdateRequest("nicolas", asList(businessCollection), newArrayList());
		userAddUpdateRequestBusiness.addToGroupsInCollection(asList("rumors"), businessCollection);
		userAddUpdateRequestBusiness.setSyncMode(UserSyncMode.NOT_SYNCED);
		userServices.execute(userAddUpdateRequestBusiness);

		LDAPGroup rumorsSync = rumors();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroups(caballeros, rumorsSync))
				.add(dusty().addGroups(pilotes, rumorsSync))
				.add(caballeros, pilotes, rumorsSync)
				.build());

		sync();

		User sasquatchLocal = user("sasquatch", businessCollection);

		Group rumorsLocal = group("rumors", businessCollection);

		assertThat(rumorsLocal.isLocallyCreated()).isFalse();

		assertThat(sasquatchLocal.getUserGroups()).contains(rumorsLocal.getId());
	}

	/**
	 * Lorsqu’un groupe synchronisé n’est pas rapporté, les utilisateurs synchronisés perdent leur assignation dans les collections synchronisées (et seulement celles-ci)
	 * Lorsqu’un groupe synchronisé n’est pas rapporté, le groupe est supprimé (lorsque possible) dans les collections synchronisées et seulement celles-ci
	 */
	@Test
	public void givenSyncGroupIsNotOnLDAPAnymoreSyncUsersLoseThatGroupAssignation() {
		this.ldapUserSyncConfiguration =
				LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(asList(businessCollection, zeCollection, extraCollection));
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);

		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes)
				.build());

		sync();

		this.ldapUserSyncConfiguration =
				LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(asList(businessCollection));
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);

		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros)
				.build());
		sync();

		User businessNic = user("nicolas", businessCollection);
		User zeNic = user("nicolas", zeCollection);

		Group zePilotes = group("pilotes", zeCollection);
		assertThat(zePilotes).isNotNull();
		assertThat(zePilotes.getLogicallyDeletedStatus()).isFalse();

		Group businessPilotes = null;
		try {
			businessPilotes = group("pilotes", businessCollection);
		} catch (Exception ex) {
		}
		assertThat(businessPilotes.isLogicallyDeletedStatus()).isTrue();

		assertThat(businessNic.getUserGroups()).doesNotContain(businessPilotes.getId());
		assertThat(zeNic.getUserGroups()).contains(zePilotes.getId());
	}

	/**
	 * Lorsqu’un groupe est rapporté avec un groupe parent, celui-ci est déplacé dans le groupe parent dans toutes les collections (mêmes celles non-synchronisées)
	 */
	@Test
	public void givenSyncGroupHasParentGroupAlsoSyncAllCollectionsHaveSubGroupInParent() {
		Toggle.ALLOW_LDAP_FETCH_SUB_GROUPS.enable();
		this.ldapUserSyncConfiguration =
				LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(asList(businessCollection, zeCollection, extraCollection));
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);

		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		LDAPGroup canadians = canadians();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes)
				.build());

		sync();

		this.ldapUserSyncConfiguration =
				LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(asList(businessCollection));
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);

		pilotes.addParent(canadians.getDistinguishedName());
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes, canadians)
				.build());

		sync();

		Group businessPilotes = group("pilotes", businessCollection);
		Group syncPilotes = group("pilotes", zeCollection);

		Group businessCanadians = group("canadians", businessCollection);
		Group syncCanadians = group("canadians", zeCollection);


		assertThat(businessPilotes.getParent()).contains(businessCanadians.getId());
		assertThat(syncPilotes.getParent()).contains(syncCanadians.getId());
	}

	/**
	 * Lorsqu’un sous-groupe est rapporté sans groupe parent, celui-ci devient un groupe racine dans toutes les collections (mêmes celles non-synchronisées)
	 */
	@Test
	public void givenSyncGroupHasNoParentAnymoreGroupBecomesRootInAllCollections() {
		Toggle.ALLOW_LDAP_FETCH_SUB_GROUPS.enable();
		this.ldapUserSyncConfiguration =
				LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(asList(businessCollection, zeCollection, extraCollection));
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);

		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		LDAPGroup canadians = canadians();
		pilotes.addParent(canadians.getDistinguishedName());
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes, canadians)
				.build());

		sync();
		Group syncCanadians = group("canadians", zeCollection);
		Group syncPilotes = group("pilotes", zeCollection);
		assertThat(syncPilotes.getParent()).contains(syncCanadians.getId());

		this.ldapUserSyncConfiguration =
				LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(asList(businessCollection));
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);

		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes)
				.build());

		sync();

		syncPilotes = group("pilotes", zeCollection);
		Group businessPilotes = group("pilotes", businessCollection);

		assertThat(businessPilotes.getParent()).isNull();
		//assertThat(syncPilotes.getParent()).isNull();
	}

	/**
	 * Lorsqu’un groupe synchronisé n’est pas rapporté, les sous-groupes synchronisés non-rapportés sont également supprimés
	 */
	@Test
	public void givenSyncGroupParentAndSubGroupAreNotFetchedRemoveParentAndSubGroup() {
		Toggle.ALLOW_LDAP_FETCH_SUB_GROUPS.enable();
		this.ldapUserSyncConfiguration =
				LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(asList(businessCollection, zeCollection, extraCollection));
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);

		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		LDAPGroup canadians = canadians();
		pilotes.addParent(canadians.getDistinguishedName());
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes, canadians)
				.build());

		sync();

		Group syncPilotes = group("pilotes", zeCollection);
		Group businessPilotes = group("pilotes", businessCollection);
		Group syncCanadians = group("canadians", zeCollection);
		Group businessCanadians = group("canadians", businessCollection);

		assertThat(asList(syncPilotes, businessPilotes, syncCanadians, businessCanadians)).doesNotContainNull();

		this.ldapUserSyncConfiguration =
				LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(asList(businessCollection));
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);

		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros)
				.build());

		sync();

		syncPilotes = group("pilotes", zeCollection);
		businessPilotes = group("pilotes", businessCollection);
		syncCanadians = group("canadians", zeCollection);
		businessCanadians = group("canadians", businessCollection);

		assertThat(syncPilotes.getLogicallyDeletedStatus()).isFalse();
		assertThat(businessPilotes.getLogicallyDeletedStatus()).isTrue();
	}

	/**
	 * Lorsqu’un groupe synchronisé n’est pas rapporté, les sous-groupes non-synchronisés sont supprimés (lorsque possible)
	 */
	@Test
	public void givenSyncGroupParentAndAreNotFetchedRemoveParentAndUnsyncedSubGroup() {
		Toggle.ALLOW_LDAP_FETCH_SUB_GROUPS.enable();
		UserServices userServices = modelLayerFactory.newUserServices();
		this.ldapUserSyncConfiguration =
				LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(asList(businessCollection, zeCollection, extraCollection));
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);

		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		LDAPGroup canadians = canadians();
		pilotes.addParent(canadians.getDistinguishedName());
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes, canadians)
				.build());

		sync();

		GroupAddUpdateRequest groupAddUpdateRequest = new GroupAddUpdateRequest("legends");
		groupAddUpdateRequest.setParent("canadians");
		userServices.execute(groupAddUpdateRequest);

		this.ldapUserSyncConfiguration =
				LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(asList(businessCollection));
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);

		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros)
				.build());

		sync();

		Group syncCanadians = group("canadians", zeCollection);
		Group legends = group("legends", zeCollection);
		Group businessLegends = null;
		try {
			businessLegends = group("legends", businessCollection);
		} catch (Exception e) {
		}
		Group businessCanadians = null;
		try {
			businessCanadians = group("canadians", businessCollection);
		} catch (Exception e) {
		}
		assertThat(legends).isNotNull();
		assertThat(syncCanadians).isNotNull();
		assertThat(businessCanadians.isLogicallyDeletedStatus()).isTrue();
		assertThat(businessLegends.isLogicallyDeletedStatus()).isTrue();
	}

	@Test
	public void givenSyncThenGroupsAreSyncedWithUsers()
			throws Exception {

		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes)
				.build());

		sync();

		Group importedGroup = group("caballeros", businessCollection);
		List<String> usersInCaballeros = users(importedGroup).stream().map(user -> user.getUsername()).collect(Collectors.toList());
		User importedUserPhil = user("philippe", businessCollection);
		User importedUserNicolas = user("nicolas", businessCollection);
		User importedUserDusty = user("dusty", businessCollection);
		assertThat(usersInCaballeros).containsAll(asList(importedUserNicolas.getUsername(),
				importedUserPhil.getUsername(), importedUserDusty.getUsername()));
		assertThat(importedGroup.getCode()).isEqualTo("caballeros");

	}

	@Test
	public void givenSyncThenGroupsAreSyncedThenExistingGroupWithLocallyCreatedUsersKeepsThem()
			throws Exception {

		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		LDAPGroup rumors = rumors();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroups(caballeros, rumors))
				.add(dusty().addGroup(caballeros))
				.add(caballeros, pilotes, rumors)
				.build());
		sync();

		Group importedGroup = group("rumors", businessCollection);
		List<String> usersInRumors = users(importedGroup).stream().map(user -> user.getUsername()).collect(Collectors.toList());
		User importedUserPhil = user("philippe", businessCollection);
		User importedUserBigFoot = user(sasquatch, businessCollection);
		assertThat(usersInRumors).containsAll(asList(importedUserBigFoot.getUsername(), importedUserPhil.getUsername()));

	}

	@Test
	public void givenMultipleCollectionSelectedWithOneNotSelectedSyncOnlySelected() throws RecordServicesException {
		this.ldapUserSyncConfiguration =
				LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(asList(businessCollection, extraCollection));
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);

		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(chuck().addGroup(pilotes))
				.add(caballeros, pilotes)
				.build());

		User userToUpdate = user(chuck, businessCollection);
		userToUpdate.setJobTitle("actor");

		modelLayerFactory.newRecordServices().update(userToUpdate);

		sync();

		//zecollection has none of the changes
		User importedUser = null;
		try {
			importedUser = user("nicolas", zeCollection);
		} catch (Exception e) {
		}
		assertThat(importedUser).isNull();

		User importedUser2 = null;
		try {
			importedUser2 = user("philippe", zeCollection);
		} catch (Exception e) {
		}
		assertThat(importedUser2).isNull();

		User importedUser3 = null;
		try {
			importedUser3 = user("dusty", zeCollection);
		} catch (Exception e) {
		}
		assertThat(importedUser3).isNull();

		//zecollection has chuck, the local one
		User localUser = user(chuckNorris, zeCollection);
		UserCredential userCredentialLocalUser = user(chuckNorris);
		//assertThat(localUser.getLastName()).isEqualTo("Norris");
		assertThat(localUser).isNotNull();
		assertThat(userCredentialLocalUser.getSyncMode()).isEqualTo(UserSyncMode.SYNCED); //credential is still replaced
		assertThat(userCredentialLocalUser.getEmail()).isEqualTo("chuckofldap@doculibre.ca"); //credential is still replaced


		//extracollection has the changes of the changes
		importedUser = user("nicolas", extraCollection);
		assertThat(importedUser).isNotNull();

		importedUser2 = user("philippe", extraCollection);
		assertThat(importedUser2).isNotNull();

		importedUser3 = user("dusty", extraCollection);
		assertThat(importedUser3).isNotNull();

		//extracollection has chuck Synced
		localUser = user(chuckNorris, extraCollection);
		userCredentialLocalUser = user(chuckNorris);
		assertThat(localUser.getLastName()).isEqualTo("Norris ldap");
		assertThat(localUser).isNotNull();
		assertThat(userCredentialLocalUser.getSyncMode()).isEqualTo(UserSyncMode.SYNCED);
		assertThat(localUser.getEmail()).isEqualTo("chuckofldap@doculibre.ca");

		//businesscollection has the changes of the changes
		importedUser = user("nicolas", businessCollection);
		assertThat(importedUser).isNotNull();

		importedUser2 = user("philippe", businessCollection);
		assertThat(importedUser2).isNotNull();

		importedUser3 = user("dusty", businessCollection);
		assertThat(importedUser3).isNotNull();


		localUser = user(chuckNorris, businessCollection);
		userCredentialLocalUser = user(chuckNorris);
		//assertThat(localUser.getLastName()).isEqualTo("Norris ldap");
		assertThat(localUser).isNotNull();
		assertThat(userCredentialLocalUser.getSyncMode()).isEqualTo(UserSyncMode.SYNCED);
		assertThat(localUser.getJobTitle()).isEqualTo("actor");

	}

	/**
	 * Lorsqu’un groupe créé localement dans une collection synchronisée est rapporté, il est ajouté aux autres collections synchronisée
	 * Lorsqu’un groupe synchronisé est rapporté, son nom est ajusté
	 */
	@Test
	public void whenExistingLocalGroupSyncedWithSameCodeGroupLDAPThenReplaceLocalGroupWithLDAP() {
		UserServices userServices = modelLayerFactory.newUserServices();
		GroupAddUpdateRequest pilotesLocal = new GroupAddUpdateRequest("pilotes");
		pilotesLocal.setName("Les pilotes x-wing");
		pilotesLocal.addCollection(businessCollection);
		userServices.execute(pilotesLocal);
		createLocalPhilippe(true, asList(businessCollection), asList("pilotes"));
		UserAddUpdateRequest chuckLocal = new UserAddUpdateRequest(chuck, asList(businessCollection), asList("pilotes", "rumors", "legends"));
		userServices.execute(chuckLocal);

		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(pilotes))
				.add(dusty().addGroup(caballeros))
				.add(chuck().addGroup(pilotes))
				.add(caballeros, pilotes)
				.build());

		sync();

		Group pilotesLocalSync = group("pilotes", businessCollection);
		assertThat(pilotesLocalSync.isLocallyCreated()).isFalse();
		assertThat(pilotesLocalSync.getCaption()).isEqualTo("Les pilotes du ciel");

		User philippeSync = user("philippe", businessCollection);
		User nicolasSync = user("nicolas", businessCollection);
		User chuckSync = user(chuck, businessCollection);

		assertThat(philippeSync.getUserGroups()).containsOnly(pilotesLocalSync.getId());
		assertThat(nicolasSync.getUserGroups()).contains(pilotesLocalSync.getId());
		assertThat(chuckSync.getUserGroups()).contains(pilotesLocalSync.getId());//chuck fais aussi parti des groupes locallyCreated
	}

	@Test
	public void whenUserIsSyncedIn2CollectionButNotInTheThirdAndSyncThenUserEmailAndNameAndLastNameAreReplaced() {

		this.ldapUserSyncConfiguration =
				LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(asList(businessCollection, extraCollection));
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);

		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(chuck().addGroup(pilotes))
				.add(caballeros, pilotes)
				.build());


		UserServices userServices = modelLayerFactory.newUserServices();

		UserAddUpdateRequest chuckToUpdate = userServices.addUpdate(chuck);
		chuckToUpdate.setJobTitle("actor");
		chuckToUpdate.setEmail("jabroni@doculibre.ca");
		chuckToUpdate.setName("Chucky", "Norrisu");
		userServices.execute(chuckToUpdate);

		sync();

		//userCredential is to ldap
		User localUserBusiness = user(chuck, businessCollection);
		User localUserZeCollection = user(chuck, zeCollection);
		User localUserExtra = user(chuck, extraCollection);
		UserCredential userCredentialLocalUser = user(chuck);
		assertThat(userCredentialLocalUser.getSyncMode()).isEqualTo(UserSyncMode.SYNCED);
		assertThat(localUserZeCollection.getEmail()).isEqualTo("chuckofldap@doculibre.ca");
		assertThat(localUserExtra.getEmail()).isEqualTo("chuckofldap@doculibre.ca");
		assertThat(localUserBusiness.getEmail()).isEqualTo("chuckofldap@doculibre.ca");
		assertThat(localUserBusiness.getFirstName()).isEqualTo("Chuck");
		assertThat(localUserExtra.getFirstName()).isEqualTo("Chuck");
		assertThat(localUserZeCollection.getFirstName()).isEqualTo("Chuck");
		assertThat(localUserBusiness.getLastName()).isEqualTo("Norris ldap");
		assertThat(localUserExtra.getLastName()).isEqualTo("Norris ldap");
		assertThat(localUserZeCollection.getLastName()).isEqualTo("Norris ldap");

	}

	@Test
	public void whenUserAzureHasDifferentUsernameButSameDNThenUpdateUserInsteadOfCreateActivateAndDoNotChangeUsername() {

		this.ldapUserSyncConfiguration =
				LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(asList(businessCollection));
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);
		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);

		LDAPGroup caballeros = caballeros();
		LDAPGroup pilotes = pilotes();
		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(nicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(chuck().addGroup(pilotes))
				.add(caballeros, pilotes)
				.build());
		sync();

		whenRetrievingInfosFromLDAP().thenReturn(new LDAPUsersAndGroupsBuilder()
				.add(falseNicolas().addGroups(caballeros, pilotes))
				.add(philippe().addGroup(caballeros))
				.add(dusty().addGroup(caballeros))
				.add(chuck().addGroup(pilotes))
				.add(caballeros, pilotes)
				.build());

		sync();

		//userCredential is to ldap
		User nicolasSync = user("nicolas", businessCollection);
		UserCredential userCredentialLocalUser = user("nicolas");

		assertThat(userCredentialLocalUser.getDn()).isEqualTo("2143e922-0361-45a2-bc9a-7fd426c5e5bd");
		assertThat(userCredentialLocalUser.getFirstName()).isEqualTo("Nicodas");
		assertThat(nicolasSync.getLastName()).isEqualTo("Bégin");
		assertThat(nicolasSync.getEmail()).isEqualTo("falsenicolas@doculibre.ca");

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
				.setName("Nicolas").setGivenName("Nicolas").setId("2143e922-0361-45a2-bc9a-7fd426c5e5bd");
	}

	private LDAPUser falseNicolas() {
		return new LDAPUser().setEmail("falsenicolas@doculibre.ca").setFamilyName("Bégin")
				.setName("falseNicolas").setGivenName("Nicodas").setId("2143e922-0361-45a2-bc9a-7fd426c5e5bd");
	}

	private LDAPUser nicolas2() {
		return new LDAPUser().setEmail("niiiiiiiii@doculibre.ca").setFamilyName("Bégin")
				.setName("Nicolas").setGivenName("NicoNico");
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
		return new LDAPUser().setEmail("chuckofldap@doculibre.ca").setFamilyName("Norris ldap")
				.setName("Chuck").setGivenName("Chuck");
	}

	private LDAPGroup caballeros() {
		return new LDAPGroup("The three caballeros", "caballeros");
	}

	private LDAPGroup pilotes() {
		return new LDAPGroup("Les pilotes du ciel", "pilotes");
	}

	private LDAPGroup canadians() {
		return new LDAPGroup("Des canadiens", "canadians");
	}

	private LDAPGroup rumors() {
		return new LDAPGroup("The rumors", "rumors");
	}

	private void createLocalDusty(boolean isActive, List<String> collections) {

		modelLayerFactory.newUserServices().createUser("dusty", (req) ->
				req.setNameEmail("Dusty", "le Chien", "dusty@constellio.com")
						.setStatusForAllCollections(isActive ? UserCredentialStatus.ACTIVE : UserCredentialStatus.DISABLED)
						.addToCollections(collections));
	}

	private void createLocalPhilippe(boolean isActive, List<String> collections, List<String> groups) {

		modelLayerFactory.newUserServices().createUser("philippe", (req) ->
				req.setNameEmail("Philippe", "Houle", "philippe@doculibre.ca")
						.setStatusForAllCollections(isActive ? UserCredentialStatus.ACTIVE : UserCredentialStatus.DISABLED)
						.addToCollections(collections).addToGroupsInEachCollection(groups));
	}

}
