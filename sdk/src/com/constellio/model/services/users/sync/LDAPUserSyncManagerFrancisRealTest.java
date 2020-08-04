package com.constellio.model.services.users.sync;

import com.constellio.model.conf.LDAPTestConfig;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.services.LDAPServicesFactory;
import com.constellio.model.conf.ldap.services.LDAPServicesImpl;
import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.conf.ldap.user.LDAPUser;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserSyncMode;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.sync.model.LDAPUsersAndGroups;
import com.constellio.model.services.users.sync.model.LDAPUsersAndGroupsBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class LDAPUserSyncManagerFrancisRealTest extends ConstellioTest {
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

	private LDAPGroup cabailleros() {
		return new LDAPGroup("cabailleros", "The three cabaielleros");
	}

	private LDAPGroup pilotes() {
		return new LDAPGroup("pilotes", "Les pilotes du ciel");
	}

}
