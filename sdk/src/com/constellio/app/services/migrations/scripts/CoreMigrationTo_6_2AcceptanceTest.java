package com.constellio.app.services.migrations.scripts;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import org.junit.Test;

import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.SolrGlobalGroup;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import com.constellio.sdk.tests.annotations.SlowTest;

@SlowTest
public class CoreMigrationTo_6_2AcceptanceTest extends ConstellioTest {

	@Test
	public void whenMigratingFromASystemWithUserAndGroupsThenMigrated()
			throws OptimisticLockingConfiguration, NoSuchAlgorithmException, IOException, InvalidKeySpecException {
		givenSystemAtVersion5_1_2withTokens();
		UserServices userServices = getModelLayerFactory().newUserServices();

		SolrGlobalGroup heroes = (SolrGlobalGroup) userServices.getGroup("heroes");
		SolrGlobalGroup legends = (SolrGlobalGroup) userServices.getGroup("legends");
		SolrGlobalGroup sidekicks = (SolrGlobalGroup) userServices.getGroup("sidekicks");

		assertThat(heroes).isNotNull();
		assertThat(legends).isNotNull();
		assertThat(legends.getStatus()).isEqualTo(GlobalGroupStatus.ACTIVE);
		assertThat(legends.getName()).isEqualTo("The legends");

		assertThat(sidekicks.getParent()).isEqualTo(heroes.getId());

		assertThat(userServices.getUserCredential("admin")).isNotNull();
		assertThat(userServices.getUserCredential("bob")).isNotNull();
		assertThat(userServices.getUserCredential("charles")).isNotNull();
		assertThat(userServices.getUserCredential("dakota")).isNotNull();
		assertThat(userServices.getUserCredential("edouard")).isNotNull();
		assertThat(userServices.getUserCredential("gandalf")).isNotNull();

		UserCredential dakotaUser = userServices.getUserCredential("dakota");
		assertThat(dakotaUser.getFirstName()).isEqualTo("Dakota");
		assertThat(dakotaUser.getLastName()).isEqualTo("L'Indien");
		assertThat(dakotaUser.getUsername()).isEqualTo("dakota");
		assertThat(dakotaUser.getEmail()).isEqualTo("dakota@doculibre.com");
		assertThat(dakotaUser.getCollections()).containsOnly("zeCollection");
		assertThat(dakotaUser.getGlobalGroups()).containsOnly(heroes.getId());
		assertThat(dakotaUser.getStatus()).isEqualTo(UserCredentialStatus.ACTIVE);

		List<String> adminTokens = getModelLayerFactory().newUserServices().getUserCredential("admin").getTokenKeys();
		assertThat(adminTokens).containsOnly("6f9b7e63-a6c1-4783-9143-1e69edf34b4c");

	}

	private void givenSystemAtVersion5_1_2withTokens() {
		givenTransactionLogIsEnabled();
		File statesFolder = new File(new SDKFoldersLocator().getInitialStatesFolder(), "olds");
		File state = new File(statesFolder, "given_system_in_5.1.2.2_with_tasks,rm_modules__with_tokens.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

}
