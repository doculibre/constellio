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
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.sync.model.LDAPUsersAndGroups;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class LDAPUserSyncManagerRealTest extends ConstellioTest {
	ModelLayerFactory modelLayerFactory;

	UserServices userServices;

	@Mock
	private LDAPConfigurationManager ldapConfigurationManager;

	@Mock
	LDAPServicesFactory ldapServicesFactory;

	@Mock
	LDAPServicesImpl ldapServices;

	private LDAPUserSyncManager ldapUserSyncManager;

	private LDAPServerConfiguration ldapServerConfiguration = LDAPTestConfig.getLDAPServerConfiguration();
	private LDAPUserSyncConfiguration ldapUserSyncConfiguration = LDAPTestConfig.getLDAPUserSyncConfiguration();

	private LDAPUsersAndGroups testUpgrade;

	@Before
	public void setup()
			throws Exception {
		//givenConstellioProperties(LDAPTestConfig.getConfigMap());
		givenCollectionWithTitle(zeCollection, "Collection de test");
		givenCollectionWithTitle(businessCollection, "Collection de Rida");
		modelLayerFactory = getModelLayerFactory();
		userServices = spy(modelLayerFactory.newUserServices());

		LDAPUser nicolas = new LDAPUser();
		nicolas.setEmail("nicolas@doculibre.ca");
		nicolas.setFamilyName("Belisle");
		nicolas.setName("Nicolas");
		nicolas.setGivenName("Nicolas");
		LDAPUser dusty = new LDAPUser();
		dusty.setEmail("dusty@doculibre.ca");
		dusty.setFamilyName("Chien");
		dusty.setName("Dusty");
		dusty.setGivenName("Dusty");
		LDAPUser philippe = new LDAPUser();
		philippe.setEmail("philippe@doculibre.ca");
		philippe.setFamilyName("Houle");
		philippe.setGivenName("Philippe");
		philippe.setName("Philippe");

		LDAPGroup cabailleros = new LDAPGroup("cabailleros", "The three cabaielleros");
		LDAPGroup pilotes = new LDAPGroup("pilotes", "Les pilotes du ciel");

		Set<LDAPGroup> groups = new HashSet<>();
		Set<LDAPUser> users = new HashSet<>();


		nicolas.addGroup(cabailleros);
		nicolas.addGroup(pilotes);
		philippe.addGroup(cabailleros);
		dusty.addGroup(cabailleros);

		Set<LDAPUser> usernameTests = new HashSet<>();
		usernameTests.add(nicolas);
		usernameTests.add(dusty);
		usernameTests.add(philippe);

		Set<LDAPGroup> groupTests = new HashSet<>();
		groupTests.add(cabailleros);
		groupTests.add(pilotes);

		testUpgrade = new LDAPUsersAndGroups(usernameTests, groupTests);

		saveValidLDAPConfigWithEntrepriseCollectionSelected();
	}

	private void saveValidLDAPConfigWithEntrepriseCollectionSelected() {
		this.ldapServerConfiguration = LDAPTestConfig.getLDAPServerConfiguration();
		this.ldapUserSyncConfiguration = LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(
				asList(businessCollection));

	}

	@Test
	public void givenUserSyncConfiguredThenRunAndVerifyImportedUserSync()
			throws Exception {

		when(ldapConfigurationManager.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(this.ldapUserSyncConfiguration);
		when(ldapConfigurationManager.getLDAPServerConfiguration()).thenReturn(this.ldapServerConfiguration);
		when(ldapServices.importUsersAndGroups(any(LDAPServerConfiguration.class), any(LDAPUserSyncConfiguration.class), anyString()))
				.thenReturn(testUpgrade);
		when(ldapServicesFactory.newLDAPServices(any())).thenReturn(this.ldapServices);

		ldapUserSyncManager = new LDAPUserSyncManager(this.ldapConfigurationManager, modelLayerFactory.newRecordServices(),
				modelLayerFactory.getDataLayerFactory(), modelLayerFactory.newUserServices(),
				modelLayerFactory.getDataLayerFactory().getConstellioJobManager(), ldapServicesFactory);

		ldapUserSyncManager.synchronizeIfPossible();

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

	private UserCredential user(String code) {
		return userServices.getUserCredential(code);
	}

	private User user(String username, String collection) {
		return userServices.getUserInCollection(username, collection);
	}

}
